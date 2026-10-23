import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class Nnue
{
    public static int hiddenSize=256;
    public static final int accumulatorStackSize = 256;
    private static final int QA=255;
    private static final int QB=64;
    private static final int evalScale=400;
    public static boolean ready=false;
    private static int[] featureWeights=new int[0];
    private static int[] featureBias=new int[0];
    private static int[] ownOutputWeights=new int[0];
    private static int[] enemyOutputWeights =new int[0];
    private static int[] outputBias=new int[1];
    public static int outputBuckets=1;
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
            for(int candidateBuckets=8;candidateBuckets>=1;candidateBuckets-=7)
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
            int[] newFeatureWeights=new int[768*size];
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
            }
            int[] newOutputBias=new int[buckets];
            for(int bucket=0;bucket<buckets;bucket++)
            {
                newOutputBias[bucket]=buffer.getShort();
            }
            outputBias=newOutputBias;
            outputBuckets=buckets;
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
    public static int featureIndex(int perspective,int piece,int square)
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
        return colorOffset+(pieceType-1)*64+viewSquare;
    }
    public static void refresh(Board board,int[] whiteAccumulator,int[] blackAccumulator)
    {
        System.arraycopy(featureBias,0,whiteAccumulator,0,hiddenSize);
        System.arraycopy(featureBias,0,blackAccumulator,0,hiddenSize);
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square];
            if(piece!=Board.EMPTY)
            {
                addFeature(whiteAccumulator,featureIndex(0,piece,square));
                addFeature(blackAccumulator,featureIndex(1,piece,square));
            }
        }
    }
    private static void addFeature(int[] accumulator,int feature)
    {
        int offset=feature*hiddenSize;
        for(int i=0;i<hiddenSize;i++)
        {
            accumulator[i]+=featureWeights[offset+i];
        }
    }
    public static void moveFeature(int[] before,int[] after,int removedFeature,int addedFeature)
    {
        int remvoedOffset=removedFeature*hiddenSize;
        int addedOffset=addedFeature*hiddenSize;
        for(int i=0;i<hiddenSize;i++)
        {
            after[i]=(before[i]-featureWeights[remvoedOffset+i]+featureWeights[addedOffset+i]);
        }
    }
    public static void captureFeature(int[] before,int[] after,int removedFeature,int capturedFeature,int addedFeature)
    {
        int removedOffset=removedFeature*hiddenSize;
        int capturedOffset=capturedFeature*hiddenSize;
        int addedOffset=addedFeature*hiddenSize;
        for(int i=0;i<hiddenSize;i++)
        {
            after[i]=(before[i]-featureWeights[removedOffset+i]-featureWeights[capturedOffset+i]+featureWeights[addedOffset+i]);
        }
    }
    public static void castleFeature(int[] accumulator,int removedFeature,int addedFeature)
    {
        int removedOffset=removedFeature*hiddenSize;
        int addedOffset=addedFeature*hiddenSize;
        for(int i=0;i<hiddenSize;i++)
        {
            accumulator[i]=(accumulator[i]-featureWeights[removedOffset+i]+featureWeights[addedOffset+i]);
        }
    }
    public static void update(int[] whiteBefore,int[] blackBefore,int[] whiteAfter,int[] blackAfter,int movedPiece,int from,int placedPiece,int to,int captured,int capturedSquare,int castleRookFrom,int castleRookTo)
    {
        if(captured==Board.EMPTY)
        {
            moveFeature(whiteBefore,whiteAfter,featureIndex(0,movedPiece,from),featureIndex(0,placedPiece,to));
            moveFeature(blackBefore,blackAfter,featureIndex(1,movedPiece,from),featureIndex(1,placedPiece,to));
        }
        else
        {
            captureFeature(whiteBefore,whiteAfter,featureIndex(0,movedPiece,from),featureIndex(0,captured,capturedSquare),featureIndex(0,placedPiece,to));
            captureFeature(blackBefore,blackAfter,featureIndex(1,movedPiece,from),featureIndex(1,captured,capturedSquare),featureIndex(1,placedPiece,to));
        }
        if(castleRookFrom>=0)
        {
            int rook=movedPiece>0?Board.Rook:-Board.Rook;
            castleFeature(whiteAfter,featureIndex(0,rook,castleRookFrom),featureIndex(0,rook,castleRookTo));
            castleFeature(blackAfter,featureIndex(1,rook,castleRookFrom),featureIndex(1,rook,castleRookTo));
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
        int output=0;
        for(int i=0;i<hiddenSize;i++)
        {
            int own=Math.min(Math.max(ownAccumulator[i],0),QA);
            int enemy=Math.min(Math.max(enemyAccumulator[i], 0),QA);
            output+=own*own*ownOutputWeights[weightOffset+i]+enemy*enemy*enemyOutputWeights[weightOffset+i];
        }
        output/=QA;
        output+=outputBias[bucket];
        output*=evalScale;
        output/=QA*QB;
        return output;
    }
}
