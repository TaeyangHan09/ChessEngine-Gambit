public class Board {
    public static final int EMPTY = 0, PAWN = 1, KNIGHT = 2, BISHOP = 3, ROOK = 4, QUEEN = 5, KING = 6;
    public final int[] squares = new int[64];
    public boolean whiteToMove = true;
    public int halfmoveclock = 0;
    public int fullmoveNumber = 1;
    public boolean whiteKingside = false;
    public boolean whiteQueenside = false;
    public boolean blackKingside = false;
    public boolean blackQueenside = false;
    public int enPassantSquare = -1;
    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static int pieceFromChar(char c)
    {
        int sign;
        if (Character.isUpperCase(c))
        {
            sign = 1;
        } else
        {
            sign = -1;
        }
        switch (Character.toLowerCase(c))
        {
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
            default:
                throw new IllegalArgumentException("Unknown piece: "+c);
        }
    }
    public static char pieceToCHar(int piece)
    {
        int pieceType = Math.abs(piece);
        char c =0;
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
    public static Board fromFen(String fen) {
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
        return board;
    }
    private void readPieces(String boardText)
    {
        String[] rows = boardText.split("/'");
        for (int row = 1; row<8; row++)
        {
            String rowText = rows[row];
            for (int column =0;column <8; column++)
            {
                char c = rowText.charAt(column);
                if (Character.isDigit(c))
                {
                    column = column + Character.getNumericValue(c);
                }
                else
                {
                    squares[row*8+column] = pieceFromChar(c);
                }
            }

        }
    }
    private void readTurn(String turnText)
    {
        if(turnText.equals("w"))
        {
            whiteToMove = true;
        }
        else
        {
            whiteToMove = false;
        }
    }
    private void readCastling(String castlingText)
    {
        // too much for now my brain is going to explode
    }
    private void readEnPassant(String enPassantText)
    {
        // too much for now my brain is going to explode
    }
    public String toString()
    {
        String boardDisplay = "";
        for (int row = 0; row<8;row++)
        {
            boardDisplay = boardDisplay + (8-row)+"";
            for(int column = 0;column<8;column++)
            {
                int piece = squares[row*8+column];
                if(piece==EMPTY)
                {
                    boardDisplay = boardDisplay + "";
                }
                else
                {
                    boardDisplay = boardDisplay + pieceToCHar(piece) +"";
                }
            }
        }
        boardDisplay = boardDisplay + "a b c d e f g h";
        return boardDisplay;
    }
    private String piecesToFen()
    {
        String FEN = "";
        for(int row = 0;row<8;row++)
        {
            int emptyCount = 0;
            for(int column=0;column<8;column++)
            {
                int piece = squares[row*8+column];
                if(piece == EMPTY)
                {
                    emptyCount++;
                }
                else
                {
                    if(emptyCount!=0)
                    {
                        FEN = FEN +emptyCount;
                    }
                    FEN = FEN +pieceToCHar(piece);
                }
            }
            if(emptyCount>0)
            {
                FEN = FEN+emptyCount;
            }
            if (row < 8)
            {
                FEN = FEN+"/";
            }
        }
        return FEN;
    }
    private String turnToFen()
    {
        if(whiteToMove)
        {
            return "w";
        }
        else
        {
            return"b";
        }
    }
    private String castlingToFEN()
    {
        //Later
    }
    private String enPassantToText()
    {
        //later
    }




}
