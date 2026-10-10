public class Search
{
    private static final int checkmate = 100000;
    public static int negamaxWithoutPruning(Board board, int depth, int individualMoveCount)
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
            int score = -negamaxWithoutPruning(board,depth-1,individualMoveCount+1);
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
        int alpha =-checkmate-1;
        int beta = checkmate+1;
        for(int move:moves)
        {
            board.makeMove(move);
            int score = -negamaxWithoutPruning(board,depth -1,1);
            board.unmakeMove();
            if (score>alpha)
            {
                alpha =score;
                bestMove = move;
            }
        }
        return bestMove;
    }
    public static int negamax(Board board,int depth,int individualMovementCount,int alpha,int beta)
    {
        int[] moves = MoveGenerator.generateLegalMoves(board);
        if(moves.length==0)
        {
            if(MoveGenerator.isKingAttacked(board, board.whiteToMove))
            {
                return -checkmate+individualMovementCount;
            }
            return 0;
        }
        if(depth==0)
        {
            return Evaluator.evaluate(board);
        }
        for(int move:moves)
        {
            board.makeMove(move);
            int score = -negamax(board,depth-1,individualMovementCount+1,-beta,-alpha);
            board.unmakeMove();
            if(score>=beta)
            {
                return beta;
            }
        }
        return alpha;
    }
    private static final int[] value = {0,100,320,330,500,900,20000};
    private static int scoreMove(Board board,int move)
    {
        int score=0;
        int from=Move.unbitpackcurrentPosition(move);
        int to=Move.unbitpackchangedPosition(move);
        if(Move.unbitpackcapturePosition(move))
        {
            int victim =Math.abs(board.squares[to]);
            if(Move.unbitpackenPassant(move))
            {
                victim = Board.Pawn;
            }
            int attacker = Math.abs(board.squares[from]);
            score+=10000+value[victim]*10-value[attacker]/10;
        }
        int promo = Move.unbitpackpromotionPosition(move);
        if(promo!=0)
        {
            score+=9000+value[promo];
        }
        return score;
    }
    private static int[] orderMoves(Board board,int[] moves)
    {
        int n=moves.length;
        int[] scores=new int[n];
        for(int i=0; i<n;i++)
        {
            scores[i] = scoreMove(board,moves[i]);
        }
        for(int i=1;i<n;i++)
        {
            int m = moves[i];
            int s =scores[i];
            int j=i-1;
            while(j>=0&&scores[j]<s)
            {
                moves[j+1]=moves[j];
                scores[j+1]=scores[j];
                j--;
            }
            moves[j+1]=m;
            scores[j+1]=s;
        }
        return moves;
    }
}
