public class Test
{
    public static void main(String[] args)
    {
        String[] fens = {
                "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
                "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1"
        };
        for (String fen : fens) {
            Board b = Board.fromFen(fen);
            String start = b.toFen();
            for (int m : MoveGenerator.generateMove(b)) {
                if (Move.unbitpackcastlingPosition(m)) {
                    b.makeMove(m);
                    System.out.println(b.toFen());
                    b.unmakeMove();
                    System.out.println("restored=" + b.toFen().equals(start));
                }
            }
        }
    }
}