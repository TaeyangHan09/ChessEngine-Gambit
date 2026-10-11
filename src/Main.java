public class Main
{
    public static void main(String[] args)
    {
        Board board = Board.fromFen(Board.STARTING_POSITION);

        board.makeMove(Move.bitpack(52,36,0,false,false,true,false));
        System.out.println(board.toFen());

        board.makeMove(Move.bitpack(6,21,0,false,false,false,false));
        System.out.println(board.toFen());

        board.makeMove(Move.bitpack(62,45,0,false,false,false,false));
        System.out.println(board.toFen());

        board.unmakeMove();
        board.unmakeMove();
        board.unmakeMove();
        System.out.println(board.toFen());
    }
}