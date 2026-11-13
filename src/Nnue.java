import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
public class Nnue
{
    public static int hiddenSize=256;
    public static final int accumulatorStackSize = 256;
    private static final int QA=255;
    private static final int QB=64;
    private static final int evalScale=400;
    public static boolean ready=false;
    private static final VectorSpecies<Integer> lanes=IntVector.SPECIES_PREFERRED;
    private static int[] featureWeights=new int[0];
    private static int[] featureBias=new int[0];
    private static int[] ownOutputWeights=new int[0];
    private static int[] enemyOutputWeights =new int[0];
    private static int[] outputBias=new int[1];
    public static int outputBuckets=1;
    public static boolean mirrored=false;
    public static int kingBuckets=1;
    private static int[] kingBucketMap=new int[32];
    static
    {
        load();
    }
    private static void load()
    {
        String path=System.getProperty("gambit.nnue");
        try
        {
            InputStream stream =null;
            if(path!=null)
            {
                stream=new FileInputStream(path);
            }
            if(stream==null)
            {
                stream=Nnue.class.getResourceAsStream("/gambit.nnue");
            }
            if(stream==null)
            {
                File besideClasses=new File(new File(Nnue.class.getProtectionDomain().getCodeSource().getLocation().toURI()),"gambit.nnue");
                if(besideClasses.exists())
                {
                    stream=new FileInputStream(besideClasses);
                }
            }
            if(stream==null)
            {
                return;
            }
            byte[] bytes=stream.readAllBytes();
            stream.close();
            int shorts=bytes.length/2;
            int size=0;
            int buckets=0;
            int headerBytes=0;
            int newKingBuckets=1;
            boolean newMirrored=false;
            int[] newKingBucketMap=new int[32];
            if(bytes.length>=64&&bytes[0]=='G'&&bytes[1]=='B'&&bytes[2]=='N'&&bytes[3]=='2')
            {
                ByteBuffer header=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                size=header.getShort(4);
                newKingBuckets=header.getShort(6);
                buckets=header.getShort(8);
                newMirrored=header.getShort(10)!=0;
                for(int i=0;i<32&&newKingBuckets>1;i++)
                {
                    newKingBucketMap[i]=bytes[12+i];
                }
                headerBytes=64;

            }
            for(int candidateBuckets=8;candidateBuckets>=1&&headerBytes==0;candidateBuckets-=7)
            {
                int candidateSize=(shorts-candidateBuckets)/(769+2*candidateBuckets);
                int unpadded=candidateSize*(769+2*candidateBuckets)+candidateBuckets;
                if(candidateSize>=8&&shorts-unpadded<32)
                {
                    size=candidateSize;
                    buckets=candidateBuckets;
                    break;
                }
            }
            if(size==0)
            {
                return;
            }
            ByteBuffer buffer=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(headerBytes);
            int[] newFeatureWeights=new int[newKingBuckets*768*size];
            for(int i=0;i<newFeatureWeights.length;i++)
            {
                newFeatureWeights[i]=buffer.getShort();
            }
            int[] newFeatureBias=new int[size];
            for(int i=0; i<size;i++)
            {
                newFeatureBias[i]= buffer.getShort();
            }
            int[] newOwnOutputWeights=new int[buckets*size];
            int[] newEnemyOutputWeights=new int[buckets*size];
            for(int bucket=0;bucket<buckets;bucket++)
            {
                for(int i=0;i<size;i++)
                {
                    newOwnOutputWeights[bucket*size+i]=buffer.getShort();
                }
                for(int i=0;i<size;i++)
                {
                    newEnemyOutputWeights[bucket*size+i]=buffer.getShort();
                }
            }
            int[] newOutputBias=new int[buckets];
            for(int bucket=0;bucket<buckets;bucket++)
            {
                newOutputBias[bucket]=buffer.getShort();
            }
            outputBias=newOutputBias;
            outputBuckets=buckets;
            mirrored=newMirrored;
            kingBuckets=newKingBuckets;
            kingBucketMap=newKingBucketMap;
            hiddenSize=size;
            featureWeights=newFeatureWeights;
            featureBias=newFeatureBias;
            ownOutputWeights=newOwnOutputWeights;
            enemyOutputWeights=newEnemyOutputWeights;
            ready=true;
        }
        catch (Exception e)
        {
            ready=false;
        }
    }
    public static int kingView(int perspective,int ownKingSquare)
    {
        if(!mirrored)
        {
            return 0;
        }
        int viewSquare=ownKingSquare;
        if(perspective==0)
        {
            viewSquare=ownKingSquare^56;
        }
        int flip=0;
        if((viewSquare&7)>3)
        {
            flip=1;
            viewSquare=viewSquare^7;
        }
        return kingBucketMap[(viewSquare>>3)*4+(viewSquare&7)]*2+flip;
    }
    public static int featureIndex(int perspective,int piece,int square,int kingView)
    {
        int pieceType=Math.abs(piece);
        boolean whitePiece=piece>0;
        int colorOffset;
        int viewSquare;
        if(perspective==0)
        {
            colorOffset=whitePiece?0:384;
            viewSquare=square^56;
        }
        else
        {
            colorOffset=whitePiece?384:0;
            viewSquare=square;
        }
        return (kingView>>1)*768+colorOffset+(pieceType-1)*64+(viewSquare^((kingView&1)*7));
    }
    public static void refresh(Board board,int perspective,int[] accumulator)
    {
        int kingView=kingView(perspective,board.kingSquare[perspective]);
        System.arraycopy(featureBias,0,accumulator,0,hiddenSize);
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square];
            if(piece!=Board.EMPTY)
            {
                addFeature(accumulator,featureIndex(perspective,piece,square,kingView));
            }
        }
    }
    private static void addFeature(int[] accumulator,int feature)
    {
        int offset=feature*hiddenSize;
        for(int i=0;i<hiddenSize;i+=lanes.length())
        {
            IntVector.fromArray(lanes,accumulator,i).add(IntVector.fromArray(lanes,featureWeights,offset+i)).intoArray(accumulator,i);
        }
    }
    public static void moveFeature(int[] before,int[] after,int removedFeature,int addedFeature)
    {
        int remvoedOffset=removedFeature*hiddenSize;
        int addedOffset=addedFeature*hiddenSize;
        for(int i=0;i<hiddenSize;i+= lanes.length())
        {
            IntVector.fromArray(lanes,before,i).sub(IntVector.fromArray(lanes,featureWeights,remvoedOffset+i)).add(IntVector.fromArray(lanes,featureWeights,addedOffset+i)).intoArray(after,i);
        }
    }
    public static void captureFeature(int[] before,int[] after,int removedFeature,int capturedFeature,int addedFeature)
    {
        int removedOffset=removedFeature*hiddenSize;
        int capturedOffset=capturedFeature*hiddenSize;
        int addedOffset=addedFeature*hiddenSize;
        for(int i=0;i<hiddenSize;i+=lanes.length())
        {
            IntVector.fromArray(lanes,before,i).sub(IntVector.fromArray(lanes,featureWeights,removedOffset+i)).sub(IntVector.fromArray(lanes,featureWeights,capturedOffset+i)).add(IntVector.fromArray(lanes,featureWeights,addedOffset+i)).intoArray(after,i);
        }
    }
    public static void castleFeature(int[] accumulator,int removedFeature,int addedFeature)
    {
        int removedOffset=removedFeature*hiddenSize;
        int addedOffset=addedFeature*hiddenSize;
        for(int i=0;i<hiddenSize;i+=lanes.length())
        {
            IntVector.fromArray(lanes,accumulator,i).sub(IntVector.fromArray(lanes,featureWeights,removedOffset+i)).add(IntVector.fromArray(lanes,featureWeights,addedOffset+i)).intoArray(accumulator,i);
        }
    }
    public static void update(int perspective,int kingView,int[] before ,int[] after,int movedPiece,int from,int placedPiece,int to,int captured,int capturedSquare,int castleRookFrom,int castleRookTo)
    {
        if(captured==Board.EMPTY)
        {
            moveFeature(before,after,featureIndex(perspective,movedPiece,from,kingView),featureIndex(perspective,placedPiece,to,kingView));
        }
        else
        {
            captureFeature(before,after,featureIndex(perspective,movedPiece,from,kingView),featureIndex(perspective,captured,capturedSquare,kingView),featureIndex(perspective,placedPiece,to,kingView));
        }
        if(castleRookFrom>=0)
        {
            int rook=movedPiece>0?Board.Rook:-Board.Rook;
            castleFeature(after,featureIndex(perspective,rook,castleRookFrom,kingView),featureIndex(perspective,rook,castleRookTo,kingView));
        }
    }
    public static int evaluate(int[] ownAccumulator,int[] enemyAccumulator,int pieceCount)
    {
        int bucket=0;
        if(outputBuckets==8)
        {
            bucket=(pieceCount-2)/4;
        }
        int weightOffset=bucket*hiddenSize;
        IntVector sum =IntVector.zero(lanes);
        for(int i=0;i<hiddenSize;i+= lanes.length())
        {
            IntVector own=IntVector.fromArray(lanes,ownAccumulator,i).max(0).min(QA);
            IntVector enemy=IntVector.fromArray(lanes,enemyAccumulator,i).max(0).min(QA);
            sum=sum.add(own.mul(own).mul(IntVector.fromArray(lanes,ownOutputWeights,weightOffset+i))).add(enemy.mul(enemy).mul(IntVector.fromArray(lanes,enemyOutputWeights,weightOffset+i)));
        }
        int output=sum.reduceLanes(VectorOperators.ADD);
        output/=QA;
        output+=outputBias[bucket];
        output*=evalScale;
        output/=QA*QB;
        return output;
    }
}
