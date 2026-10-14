public class TranspositionTable
{
    public static final int exact = 0;
    public static final int lower = 1;
    public static final int upper = 2;
    private static final int size = 1<<20;
    private static final int mask=size-1;
    private static final long[] keys=new long[size];
    private static final int[] depths=new int[size];
    private static final int[] scores=new int[size];
    private static final int[] flags=new int[size];
    private static final int[] moves=new int[size];
    public static void store(long hash,int depth,int score,int flag,int move)
    {
        int index = (int)(hash&mask);
        if(keys[index]==hash&&move==0)
        {
            move = moves[index];
        }
        if(keys[index]!=hash||depth>=depths[index]-2)
        {
            keys[index] = hash;
            depths[index] = depth;
            scores[index] = score;
            flags[index] = flag;
            moves[index]=move;
        }
    }
    public static int find(long hash)
    {
        int index=(int)(hash&mask);
        if(keys[index]==hash)
        {
            return index;
        }
        return -1;
    }
    public static void clear()
    {
        for(int i=0;i<size;i++)
        {
            keys[i] = 0;
            depths[i]=0;
            scores[i]=0;
            flags[i]=0;
            moves[i]=0;
        }
    }
    public static int depthAt(int index)
    {
        return depths[index];
    }
    public static int scoreAt(int index)
    {
        return scores[index];
    }
    public static int flagAt(int index)
    {
        return flags[index];
    }
    public static int moveAt(int index)
    {
        return moves[index];
    }
}
