public class Move
{

    private static final int currentPosition = 0;
    private static final int changedPosition = 6;
    private static final int promotionPosition = 12;
    private static final int capturePosition = 15;
    private static final int enPassant = 16;
    private static final int doubleJump = 17;
    private static final int castlingPosition = 18;
    public static int bitpack(int current_Position, int changed_Position,int promotion_Position, boolean capture_Position,boolean en_Passant,boolean double_Jump,boolean castling_Position)
    {
        int move = current_Position <<currentPosition;
        move |= changed_Position <<changedPosition;
        move |= promotion_Position << promotionPosition;
        if(capture_Position)
        {
            move |= 1<< capturePosition;
        }
        if (en_Passant)
        {
            move |= 1<<enPassant;
        }
        if (double_Jump)
        {
            move|= 1<<doubleJump;
        }
        if (castling_Position)
        {
            move|= 1<<castlingPosition;
        }

    }
    public static int unbitpackcurrentPosition(int move)
    {
        return move&63;
    }
    public static int unbitpackchangedPosition(int move)
    {
        return(move>>changedPosition)&63;
    }
    public static int unbitpackpromotionPosition(int move)
    {
        return (move>>promotionPosition)&7;
    }
    public static boolean unbitpackcapturePosition(int move)
    {
        return(move&(1<<capturePosition))!=0;
    }
    public static boolean unbitpackenPassant(int move)
    {
        return(move&(1<<enPassant))!=0;
    }
    public static boolean unbitpackdoubleJump(int move)
    {
        return(move&(1<<doubleJump))!=0;
    }
    public static boolean unbitpackcastlingPosition(int move)
    {
        return(move&(1<<castlingPosition))!=0;
    }







}
