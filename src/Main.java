public class Main
{
    public static void main(String[] args)
    {
        String[] fens = {
                Board.STARTING_POSITION,
                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
                "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
                "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"
        };
        int[] depths = {5, 5, 5, 4};
        for(int i=0;i<fens.length;i++)
        {
            Board board = Board.fromFen(fens[i]);
            long startTime = System.nanoTime();
            long nodes = Perft.perft(board,depths[i]);
            System.out.println("position "+(i+1)+" depth "+depths[i]+" nodes "+nodes+" time "+(System.nanoTime()-startTime)/1000000+" ms");
        }
    }
}