public class Board {
    public static final int EMPTY = 0, PAWN = 1, KNIGHT = 2, BISHOP = 3, ROOK = 4, QUEEN = 5, KING = 6;
    public final int[] squares = new int[64];
    public static int pieceFromChar(char c)
    {
        int sign;
        if (Character.isUpperCase(c)) {
            sign = 1;
        } else {
            sign = -1;
        }
        switch (Character.toLowerCase(c)) {
            case 'p':
                return sign * PAWN;
            case 'n':
                return sign * KNIGHT;
            case 'b':
                return sign * BISHOP;
            case 'r':
                return sign * ROOK;
            case 'q':
                return sign * QUEEN;
            case 'k':
                return sign * KING;
        }
    }
    public static char pieceToCHar(int piece)
    {
        int pieceType = Math.abs(piece);
        char c;
        if (pieceType ==1)
        {
            c = 'P';
        }
        if (pieceType ==2)
        {
            c = 'N';
        }
        if (pieceType ==3)
        {
            c = 'B';
        }
        if (pieceType ==4)
        {
            c = 'R';
        }
        if (pieceType ==5)
        {
            c = 'Q';
        }
        if (pieceType ==6)
        {
            c = 'K';
        }
        if (piece >0)
        {
            return c;
        }
        else
        {
            return Character.toLowerCase(c);
        }
    }
    public static String squareNotation(int square)
    {
        char file = (char)('a' +square%8);
        char rank = (char)(8-square/8);
        return "" + file + rank;
    }
    public static fromFen(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid FEN (too few fields): " + fen);
        }
        Board board = new Board();
        board.readPieces(parts[0]);
        board.readTurn(parts[1]);
        board.readCastling(parts[2]);
        board.readEnPassant(parts[3]);

        if (parts.length > 4)
        {
            board.halfmoveclock = Integer.parseInt(parts[4]);
        }
        if (parts.length>5)
        {
            board.fullmoveNumber = Integer.parseInt(parts[5]);
        }
    }
}
