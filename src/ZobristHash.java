import java.util.Random;
public class ZobristHash
{
    public static final long[][]pieceKeys = new long[13][64];
    public static final long sideKey;
    public static final long[] castlingkeys = new long[16];
    public static final long[] enPassantKeys = new long[65];
    static
    {
        Random random = new Random(12345);
        for(int piece=0;piece<13;piece++)
        {
            for(int square=0;square<64;square++)
            {
                pieceKeys[piece][square]=random.nextLong();
            }
        }
        sideKey = random.nextLong();
        for(int i=0;i<16;i++)
        {
            castlingkeys[i]=random.nextLong();
        }
        for(int i=0;i<65;i++)
        {
            enPassantKeys[i] = random.nextLong();
        }
    }
}
