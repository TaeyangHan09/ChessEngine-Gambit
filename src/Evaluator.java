public class Evaluator
{
    private static final int[] value = {0,100,320,330,500,900,0};
    public static int evaluate(Board board)
    {
        int score = 0;
        for(int square=0;square<64;square++)
        {
            int piece =board.squares[square];
            if(piece==Board.EMPTY)
            {
                continue;
            }
            int absolutevalue = value[Math.abs(piece)];
            if(piece>0)
            {
                score+=absolutevalue;
            }
            else
            {
                score-=absolutevalue;
            }
        }
        if(board.whiteToMove)
        {
            return score;
        }
        else
        {
            return -score;
        }
    }
}
