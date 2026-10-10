public class Perft
{
    public static long perft(Board board,int depth)
    {
        if(depth==0)
        {
            return 1;
        }
        int[] moves = MoveGenerator.generateLegalMoves(board);
        if(depth==1)
        {
            return moves.length;
        }
        long totalPositions =0;
        for(int i:moves)
        {
            board.makeMove(i);
            totalPositions+=perft(board,depth-1);
            board.unmakeMove();
        }
        return totalPositions;

    }
}
