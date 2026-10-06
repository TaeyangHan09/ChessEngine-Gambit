public class Test {
    public static void main(String[] args)
    {
        Board c = Board.fromFen("4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1");
        String cStart = c.toFen();
        for (int m : MoveGenerator.gernerateMove(c)) {
            if (Move.unbitpackenPassant(m)) {
                c.makeMove(m);
                System.out.println(c.toFen());
                c.unmakeMove();
                System.out.println("restored=" + c.toFen().equals(cStart));
            }
        }
    }
}
