public class Main
{
    public static void main(String[] args)
    {
        Board board = Board.fromFen(Board.STARTING_POSITION);
        String before = board.toFen();
        long hashBefore = board.hash;
        board.makeNullMove();
        board.unmakeNullMove();
        System.out.println(before.equals(board.toFen()) && hashBefore==board.hash);
    }
}