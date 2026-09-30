public class Move
{
    private int move;
    private static final int currentPosition = 0;
    private static final int changedPosition = 6;
    private static final int promotionPosition = 12;
    private static final int capturePosition = 15;
    private static final int enPassant = 16;
    private static final int doubleJump = 17;
    private static final int castlingPosition = 18;
    public Move(int current_Position, int changed_Position,int promotion_Position, boolean capture_Position,boolean en_Passant,boolean double_Jump,boolean castling_Position)
    {
        move = current_Position <<currentPosition;
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
    public int unbitpackcurrentPosition()
    {
        return move&63;
    }
    public int unbitpackchangedPosition()
    {
        return(move>>changedPosition)&63;
    }
    public int unbitpackpromotionPosition()
    {
        return (move>>promotionPosition)&7;
    }
    public boolean unbitpackcapturePosition()
    {
        return(move&(1<<capturePosition))!=0;
    }
    public boolean unbitpackenPassant()
    {
        return(move&(1<<enPassant))!=0;
    }
    public boolean unbitpackdoubleJump()
    {
        return(move&(1<<doubleJump))!=0;
    }
    public boolean unbitpackcastlingPosition()
    {
        return(move&(1<<castlingPosition))!=0;
    }





}
