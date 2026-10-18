public class TranspositionTable
{
    public static final int exact = 0;
    public static final int lower = 1;
    public static final int upper = 2;
    private static int size = 1<<20;
    private static int mask=size-1;
    private static long[] keys=new long[size];
    private static long[] packedEntry=new long[size];
    public static void resize(int megabytes)
    {
        long bytes=(long)megabytes*1024*1024;
        int newSize=1;
        while((long)newSize*2*16<=bytes)
        {
            newSize=newSize*2;
        }
        size=newSize;
        mask=size-1;
        keys=new long[size];
        packedEntry=new long[size];
    }
    private static long pack(int depth,int score,int flag,int move)
    {
        long packed=(long)(depth&0xFF);
        packed|=((long)(flag&3))<<8;
        packed|=((long)(move&0xFFFFF))<<10;
        packed|=((long)score)<<32;
        return packed;
    }
    public static void store(long hash,int depth,int score,int flag,int move)
    {
        int index = (int)(hash&mask);
        if(keys[index]==hash&&move==0)
        {
            move = moveAt(index);
        }
        if(keys[index]!=hash||depth>=depthAt(index)-2)
        {
            keys[index] = hash;
            packedEntry[index]=pack(depth, score, flag, move);
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
            packedEntry[i]=0;
        }
    }
    public static int depthAt(int index)
    {
        return (int)(packedEntry[index]&0xFF);
    }
    public static int scoreAt(int index)
    {
        return (int)(packedEntry[index]>>32);
    }
    public static int flagAt(int index)
    {
        return ((int)(packedEntry[index]>>8)&3);
    }
    public static int moveAt(int index)
    {
        return (int)((packedEntry[index]>>10)&0xFFFFF);
    }
}
