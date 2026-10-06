public class Test
{
    public static void main(String[] args)
    {
        testWhiteKingside();
        testWhiteQueenside();
        testBlackKingside();
        testBlackQueenside();
    }

    public static void testWhiteKingside()
    {
        System.out.println("\n===== WHITE KINGSIDE =====");

        Board board = Board.fromFen(
                "4k2r/8/8/8/8/8/8/4K2R w Kk - 0 1"
        );

        testCastle(board, 60, 62, 63, 61, "White Kingside");
    }

    public static void testWhiteQueenside()
    {
        System.out.println("\n===== WHITE QUEENSIDE =====");

        Board board = Board.fromFen(
                "4k2r/8/8/8/8/8/8/R3K3 w Qk - 0 1"
        );

        testCastle(board, 60, 58, 56, 59, "White Queenside");
    }

    public static void testBlackKingside()
    {
        System.out.println("\n===== BLACK KINGSIDE =====");

        Board board = Board.fromFen(
                "4k2r/8/8/8/8/8/8/4K2R b Kk - 0 1"
        );

        testCastle(board, 4, 6, 7, 5, "Black Kingside");
    }

    public static void testBlackQueenside()
    {
        System.out.println("\n===== BLACK QUEENSIDE =====");

        Board board = Board.fromFen(
                "r3k3/8/8/8/8/8/8/4K2R b Kq - 0 1"
        );

        testCastle(board, 4, 2, 0, 3, "Black Queenside");
    }

    public static void testCastle(
            Board board,
            int kingFrom,
            int kingTo,
            int rookFrom,
            int rookTo,
            String name)
    {
        System.out.println("START:");
        System.out.println(board);

        int castleMove = -1;

        int[] moves = MoveGenerator.generateMove(board);

        for (int move : moves)
        {
            if (Move.unbitpackcastlingPosition(move))
            {
                castleMove = move;
                break;
            }
        }

        if (castleMove == -1)
        {
            System.out.println("❌ " + name + ": Castling move NOT generated!");
            return;
        }

        board.makeMove(castleMove);

        System.out.println("AFTER CASTLING:");
        System.out.println(board);

        int expectedKing = kingFrom == 60 ? Board.King : -Board.King;
        int expectedRook = rookFrom == 63 || rookFrom == 56
                ? Board.Rook
                : -Board.Rook;

        if (board.squares[kingTo] == expectedKing
                && board.squares[rookTo] == expectedRook
                && board.squares[kingFrom] == Board.EMPTY
                && board.squares[rookFrom] == Board.EMPTY)
        {
            System.out.println("✅ " + name + " makeMove PASSED!");
        }
        else
        {
            System.out.println("❌ " + name + " makeMove FAILED!");
        }

        board.unmakeMove();

        System.out.println("AFTER UNMAKE:");
        System.out.println(board);

        if (board.squares[kingFrom] == expectedKing
                && board.squares[rookFrom] == expectedRook
                && board.squares[kingTo] == Board.EMPTY
                && board.squares[rookTo] == Board.EMPTY)
        {
            System.out.println("✅ " + name + " unmakeMove PASSED!");
        }
        else
        {
            System.out.println("❌ " + name + " unmakeMove FAILED!");
        }
    }
}