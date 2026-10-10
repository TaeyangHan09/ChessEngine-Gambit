public class Search
{
    private static final int checkmate = 999999;
    public static int negamax(Board board, int depth, int individualMoveCount)
    {
        int[] moves = MoveGenerator.generateLegalMoves(board);
        if(moves.length==0)
        {
            if(MoveGenerator.isKingAttacked(board, board.whiteToMove))
            {
                return -checkmate+individualMoveCount;
            }
            return 0;
        }
        if(depth==0)
        {
            return Evaluator.evaluate(board);
        }
        int best = -checkmate-1;
        for(int move:moves)
        {
            board.makeMove(move);
            int score = -negamax(board,depth-1,individualMoveCount+1);
            board.unmakeMove();
            if(score>best)
            {
                best=score;
            }
        }
        return best;
    }
    public static int findBestMove(Board board,int depth)
    {
        int[] moves = MoveGenerator.generateLegalMoves(board);
        int bestMove = 0;
        int best = -checkmate-1;
        for(int move:moves)
        {
            int score = -negamax(board,depth -1,1);
            board.unmakeMove();
            if (score>best)
            {
                best =score;
                bestMove = move;
            }
        }
        return bestMove;
    }
}
