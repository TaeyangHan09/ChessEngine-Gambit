public class Test {
    public static void main(String[] args)
    {
        Board p = Board.fromFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        for (int m : MoveGenerator.generateMove(p)) {
            System.out.println(Board.squareNotation(Move.unbitpackcurrentPosition(m))
                    + Board.squareNotation(Move.unbitpackchangedPosition(m))
                    + " promo=" + Move.unbitpackpromotionPosition(m));
        }
    }
}
