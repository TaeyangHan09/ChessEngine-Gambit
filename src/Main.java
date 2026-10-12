public class Main
{
    public static void main(String[] args)
    {
        Board board = Board.fromFen("4k3/8/8/8/8/8/8/Q3K3 w - - 0 1");

        System.out.println("evaluate: " + Evaluator.evaluate(board));
        System.out.println("repetition at start: " + board.isRepetition());
        System.out.println("depth 0: " + Search.negamax(board, 0, 0, -100000, 100000));
        System.out.println("depth 1: " + Search.negamax(board, 1, 0, -100000, 100000));
        System.out.println("depth 2: " + Search.negamax(board, 2, 0, -100000, 100000));

        board.makeMove(Move.bitpack(56,48,0,false,false,false,false));
        board.makeMove(Move.bitpack(4,3,0,false,false,false,false));
        board.makeMove(Move.bitpack(48,56,0,false,false,false,false));
        board.makeMove(Move.bitpack(3,4,0,false,false,false,false));

        System.out.println("is repetition: " + board.isRepetition());
        System.out.println("after shuffling: " + Search.negamax(board, 2, 0, -100000, 100000));
    }
}