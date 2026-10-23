import java.time.ZonedDateTime;

public class Board {
    public static final int EMPTY = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5, King = 6;
    public final int[] squares = new int[64];
    public boolean whiteToMove = true;
    public int halfmoveClock = 0;
    public int fullmoveNumber = 1;
    public boolean whiteKingside = false;
    public boolean whiteQueenside = false;
    public boolean blackKingside = false;
    public boolean blackQueenside = false;
    public int enPassantSquare = -1;
    public long hash;
    public final int[] kingSquare={-1,-1};
    public static final boolean debugMode=false;
    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private final int[] moveHistory = new int[4096];
    private final int[] movedpieceHistory = new int[4096];
    private final int[] capturedPieceHistory = new int[4096];
    private final int[] enPassantHistory = new int[4096];
    private final int[] castlingHistory = new int[4096];
    private final int[] halfmoveHistory = new int[4096];
    private final long[] hashHistory = new long[4096];
    private int individualmoveCount = 0;
    public final int[][] whiteAccumulator=new int[Nnue.accumulatorStackSize][Nnue.hiddenSize];
    public final int[][] blackAccumulator=new int[Nnue.accumulatorStackSize][Nnue.hiddenSize];
    private final boolean[] accumulatorReady=new boolean[Nnue.accumulatorStackSize];
    private final boolean[] pendingNull=new boolean[Nnue.accumulatorStackSize];
    private final int[] pendingMovedPiece=new int[Nnue.accumulatorStackSize];
    private final int[] pendingFrom=new int[Nnue.accumulatorStackSize];
    private final int[] pendingPlacedPiece=new int[Nnue.accumulatorStackSize];
    private final int[] pendingTo=new int[Nnue.accumulatorStackSize];
    private final int[] pendingCaptured=new int[Nnue.accumulatorStackSize];
    private final int[] pendingCapturedSquare=new int[Nnue.accumulatorStackSize];
    private final int[] pendingRookFrom=new int[Nnue.accumulatorStackSize];
    private final int[] pendingRookTo=new int[Nnue.accumulatorStackSize];
    private void updateAccumulators()
    {
        int stackSize=Nnue.accumulatorStackSize;
        int readyIndex=individualmoveCount;
        while(!accumulatorReady[readyIndex%stackSize])
        {
            readyIndex--;
            if(readyIndex<0||individualmoveCount-readyIndex>=stackSize-1)
            {
                int slot=individualmoveCount% stackSize;
                Nnue.refresh(this,whiteAccumulator[slot],blackAccumulator[slot]);
                accumulatorReady[slot]=true;
                return;
            }
        }
        for(int index=readyIndex+1;index<=individualmoveCount;index++)
        {
            int before=(index-1)%stackSize;
            int slot=index%stackSize;
            if(pendingNull[slot])
            {
                System.arraycopy(whiteAccumulator[before],0,whiteAccumulator[slot],0,Nnue.hiddenSize);
                System.arraycopy(blackAccumulator[before],0,blackAccumulator[slot],0,Nnue.hiddenSize);
            }
            else
            {
                Nnue.update(whiteAccumulator[before],blackAccumulator[before],whiteAccumulator[slot],blackAccumulator[slot],pendingMovedPiece[slot],pendingFrom[slot],pendingPlacedPiece[slot],pendingTo[slot],pendingCaptured[slot],pendingCaptured[slot],pendingCapturedSquare[slot],pendingRookFrom[slot],pendingRookTo[slot]);
            }
            accumulatorReady[slot]=true;
        }
    }
    private int castlingTracker()
    {
        int castleTracker = 0;
        if(whiteKingside) castleTracker = castleTracker+1;
        if(whiteQueenside) castleTracker = castleTracker+2;
        if(blackKingside) castleTracker = castleTracker+4;
        if(blackQueenside) castleTracker = castleTracker+8;
        return castleTracker;
    }
    public long computeHash()
    {
        long hash = 0;
        for(int square=0;square<64;square++)
        {
            int piece= squares[square];
            if(piece!=EMPTY)
            {
                hash^=ZobristHash.pieceKeys[piece+6][square];
            }
        }
        if(!whiteToMove)
        {
            hash^=ZobristHash.sideKey;
        }
        hash^=ZobristHash.castlingkeys[castlingTracker()];
        if(enPassantSquare==-1)
        {
            hash^=ZobristHash.enPassantKeys[64];
        }
        else
        {
            hash^=ZobristHash.enPassantKeys[enPassantSquare];
        }
        return hash;
    }
    public int nnueEvaluate()
    {
        updateAccumulators();
        int pieceCount=0;
        if(Nnue.outputBuckets>1)
        {
            for(int square=0;square<64;square++)
            {
                if(squares[square]!=EMPTY)
                {
                    pieceCount++;
                }
            }
        }
        if(whiteToMove)
        {
            return Nnue.evaluate(whiteAccumulator[individualmoveCount%Nnue.accumulatorStackSize],blackAccumulator[individualmoveCount%Nnue.accumulatorStackSize],pieceCount);
        }
        return Nnue.evaluate(blackAccumulator[individualmoveCount%Nnue.accumulatorStackSize],whiteAccumulator[individualmoveCount%Nnue.accumulatorStackSize],pieceCount);
    }
    public int lastMove()
    {
        if(individualmoveCount==0)
        {
            return 0;
        }
        return moveHistory[individualmoveCount-1];
    }
    public boolean isRepetition()
    {
        int limit=Math.max(0,individualmoveCount-halfmoveClock);
        for(int i=individualmoveCount-2;i>=limit;i-=2)
        {
            if(hashHistory[i]==hash)
            {
                return true;
            }
        }
        return false;
    }
    private void unpackcastlingTracker(int castleTracker)
    {
        whiteKingside = (castleTracker&1)!=0;
        whiteQueenside = (castleTracker&2)!=0;
        blackKingside = (castleTracker&4)!=0;
        blackQueenside = (castleTracker&8)!=0;
    }
    private int findCapturedSquare(int from,int to, boolean isEnPassant)
    {
        if(isEnPassant)
        {
            int startRow = from/8;
            int changedColumn = to%8;
            return startRow*8+changedColumn;
        }
        else
        {
            return to;
        }
    }
    private boolean enemyPawnBeside(int to,int piece)
    {
        int column=to%8;
        int enemyPawn=-piece;
        if(column>0&&squares[to-1]==enemyPawn)
        {
            return true;
        }
        if(column<7&&squares[to+1]==enemyPawn)
        {
            return true;
        }
        return false;
    }
    public void makeMove(int move)
    {
        int from = Move.unbitpackcurrentPosition(move);
        int to = Move.unbitpackchangedPosition(move);
        boolean isEnPassant = Move.unbitpackenPassant(move);
        boolean isDoublePush = Move.unbitpackdoubleJump(move);
        boolean isCastling = Move.unbitpackcastlingPosition(move);
        int piece = squares[from];
        int capturedSquare=findCapturedSquare(from,to,isEnPassant);
        int captured = squares[capturedSquare];
        moveHistory[individualmoveCount] = move;
        movedpieceHistory[individualmoveCount] = piece;
        capturedPieceHistory[individualmoveCount] = captured;
        enPassantHistory[individualmoveCount] = enPassantSquare;
        castlingHistory[individualmoveCount] = castlingTracker();
        halfmoveHistory[individualmoveCount]=halfmoveClock;
        hashHistory[individualmoveCount] = hash;
        individualmoveCount++;
        squares[capturedSquare] = EMPTY;
        squares[from] = EMPTY;
        int promo = Move.unbitpackpromotionPosition(move);
        if(promo!=0)
        {
            if(piece>0) squares[to] = promo;
            else squares[to] = -promo;
        }
        else
        {
            squares[to] = piece;
        }
        if(Math.abs(piece)==King)
        {
            kingSquare[piece>0 ? 0:1]=to;
        }
        if(isCastling)
        {
            if(to==62)
            {
                squares[63] = EMPTY;
                squares[61] = Board.Rook;
            }
            else if(to==58)
            {
                squares[56]=EMPTY;
                squares[59] = Board.Rook;
            }
            else if(to ==6)
            {
                squares[7] = EMPTY;
                squares[5] = -Board.Rook;
            }
            else if(to ==2)
            {
                squares[0] = EMPTY;
                squares[3] = -Board.Rook;
            }
        }
        if(Nnue.ready)
        {
            int castleRookFrom=-1;
            int castleRookTo=-1;
            if(isCastling)
            {
                if(to==62)
                {
                    castleRookFrom=63;
                    castleRookTo=61;
                }
                else if(to==58)
                {
                    castleRookFrom=56;
                    castleRookTo=59;
                }
                else if(to==6)
                {
                    castleRookFrom=7;
                    castleRookTo=5;
                }
                else if(to==2)
                {
                    castleRookFrom=0;
                    castleRookTo=3;
                }
            }
            int slot= individualmoveCount%Nnue.accumulatorStackSize;
            accumulatorReady[slot]=false;
            pendingNull[slot]=false;
            pendingMovedPiece[slot]=piece;
            pendingFrom[slot]=from;
            pendingPlacedPiece[slot]=squares[to];
            pendingTo[slot]=to;
            pendingCaptured[slot]=captured;
            pendingCapturedSquare[slot]=capturedSquare;
            pendingRookFrom[slot]=castleRookFrom;
            pendingRookTo[slot]=castleRookTo;
        }
        if(isDoublePush&&enemyPawnBeside(to,piece))
        {
           enPassantSquare = (from+to)/2;
        }
        else
        {
            enPassantSquare = -1;
        }
        if(from==60)
        {
            whiteKingside = false;whiteQueenside = false;
        }
        if(from == 4)
        {
            blackKingside = false;blackQueenside = false;
        }
        if(from == 63||to==63)
        {
            whiteKingside = false;
        }
        if(from == 56||to ==56)
        {
            whiteQueenside=false;
        }
        if(from == 7||to==7)
        {
            blackKingside = false;
        }
        if(from == 0||to ==0)
        {
            blackQueenside=false;
        }
        if(Math.abs(piece)==Pawn||captured!=EMPTY)
        {
            halfmoveClock=0;
        }
        else
        {
            halfmoveClock++;
        }
        if(!whiteToMove)
        {
            fullmoveNumber++;
        }
        whiteToMove =!whiteToMove;
        hash^=ZobristHash.pieceKeys[piece+6][from];
        if(captured!=EMPTY)
        {
            hash^= ZobristHash.pieceKeys[captured+6][capturedSquare];
        }
        hash^=ZobristHash.pieceKeys[squares[to]+6][to];
        if(isCastling)
        {
            if(to==62)
            {
                hash^=ZobristHash.pieceKeys[Board.Rook+6][63];
                hash^=ZobristHash.pieceKeys[Board.Rook+6][61];
            }
            else if(to==58)
            {
                hash^=ZobristHash.pieceKeys[Board.Rook+6][56];
                hash^=ZobristHash.pieceKeys[Board.Rook+6][59];
            }
            else if(to==6)
            {
                hash^=ZobristHash.pieceKeys[-Board.Rook+6][7];
                hash^=ZobristHash.pieceKeys[-Board.Rook+6][5];
            }
            else if(to==2)
            {
                hash^=ZobristHash.pieceKeys[-Board.Rook+6][0];
                hash^=ZobristHash.pieceKeys[-Board.Rook+6][3];
            }
        }
        hash^=ZobristHash.sideKey;
        hash^=ZobristHash.castlingkeys[castlingHistory[individualmoveCount-1]];
        hash^=ZobristHash.castlingkeys[castlingTracker()];
        int oldEnPassantIndex=enPassantHistory[individualmoveCount-1];
        if(oldEnPassantIndex==-1)
        {
            oldEnPassantIndex=64;
        }
        int newEnPassantIndex=enPassantSquare;
        if(newEnPassantIndex==-1)
        {
            newEnPassantIndex=64;
        }
        hash^=ZobristHash.enPassantKeys[oldEnPassantIndex];
        hash^=ZobristHash.enPassantKeys[newEnPassantIndex];
        if(debugMode&&hash!=computeHash())
        {
            throw new IllegalArgumentException("hash mismatch after move "+move);
        }

    }
    public void unmakeMove()
    {
        individualmoveCount--;
        int move = moveHistory[individualmoveCount];
        int from = Move.unbitpackcurrentPosition(move);
        int to = Move.unbitpackchangedPosition(move);
        boolean isEnPassant = Move.unbitpackenPassant(move);
        boolean isCastling = Move.unbitpackcastlingPosition(move);
        int capturedSquare = findCapturedSquare(from,to,isEnPassant);
        whiteToMove =!whiteToMove;
        if(!whiteToMove)
        {
            fullmoveNumber--;
        }
        squares[from] = movedpieceHistory[individualmoveCount];
        int movedPiece = movedpieceHistory[individualmoveCount];
        if(Math.abs(movedPiece)==King)
        {
            kingSquare[movedPiece>0?0:1]=from;
        }
        squares[to] = EMPTY;
        if(isCastling)
        {
            if(to==62)
            {
                squares[63] = Board.Rook;
                squares[61] = EMPTY;
            }
            else if(to ==58)
            {
                squares[56] = Board.Rook;
                squares[59] = EMPTY;
            }
            else if(to ==6)
            {
                squares[7] = -Board.Rook;
                squares[5] = EMPTY;
            }
            else if(to ==2)
            {
                squares[0] = -Board.Rook;
                squares[3] = EMPTY;
            }

        }
        squares[capturedSquare] = capturedPieceHistory[individualmoveCount];
        enPassantSquare = enPassantHistory[individualmoveCount];
        halfmoveClock = halfmoveHistory[individualmoveCount];
        hash=hashHistory[individualmoveCount];
        unpackcastlingTracker(castlingHistory[individualmoveCount]);


    }

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
                return sign * Pawn;
            case 'n':
                return sign * Knight;
            case 'b':
                return sign * Bishop;
            case 'r':
                return sign * Rook;
            case 'q':
                return sign * Queen;
            case 'k':
                return sign * King;
            default:
                throw new IllegalArgumentException("Unknown piece: "+c);
        }
    }
    public void makeNullMove()
    {
        moveHistory[individualmoveCount] = 0;
        enPassantHistory[individualmoveCount] = enPassantSquare;
        castlingHistory[individualmoveCount] = castlingTracker();
        halfmoveHistory[individualmoveCount]=halfmoveClock;
        hashHistory[individualmoveCount] = hash;
        individualmoveCount++;
        if(Nnue.ready)
        {
            accumulatorReady[individualmoveCount%Nnue.accumulatorStackSize]=false;
            pendingNull[individualmoveCount%Nnue.accumulatorStackSize]=true;
        }
        int oldEnPassantIndex=enPassantSquare;
        if(oldEnPassantIndex==-1)
        {
            oldEnPassantIndex=64;
        }
        hash^=ZobristHash.enPassantKeys[oldEnPassantIndex];
        hash^=ZobristHash.enPassantKeys[64];
        hash^=ZobristHash.sideKey;
        enPassantSquare=-1;
        halfmoveClock=0;
        whiteToMove=!whiteToMove;
        if(debugMode&&hash!=computeHash())
        {
            throw new IllegalArgumentException(("hash dont match after null"));
        }
    }
    public void unmakeNullMove()
    {
        individualmoveCount--;
        whiteToMove=!whiteToMove;
        enPassantSquare=enPassantHistory[individualmoveCount];
        halfmoveClock = halfmoveHistory[individualmoveCount];
        hash = hashHistory[individualmoveCount];
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
        char rank = (char)('8'-square/8);
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
            board.halfmoveClock = Integer.parseInt(parts[4]);
        }
        if (parts.length>5)
        {
            board.fullmoveNumber = Integer.parseInt(parts[5]);
        }
        board.hash=board.computeHash();
        if(Nnue.ready)
        {
            Nnue.refresh(board,board.whiteAccumulator[0],board.blackAccumulator[0]);
            board.accumulatorReady[0]=true;
        }
        return board;
    }
    private void readPieces(String boardText)
    {
        String[] rows = boardText.split("/");
        for (int row = 0; row<8; row++)
        {
            String rowText = rows[row];
            int column = 0;
            for (int i =0;i<rowText.length(); i++)
            {
                char c = rowText.charAt(i);
                if (Character.isDigit(c))
                {
                    column = column + Character.getNumericValue(c);
                }
                else
                {
                    int placed = pieceFromChar(c);
                    squares[row*8+column] = placed;
                    if(Math.abs(placed)==King)
                    {
                        kingSquare[placed>0?0:1] = row*8+column;
                    }
                    column=column+1;
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
        whiteKingside = castlingText.contains("K");
        whiteQueenside = castlingText.contains("Q");
        blackKingside = castlingText.contains("k");
        blackQueenside = castlingText.contains("q");
    }
    private void readEnPassant(String enPassantText)
    {
        if(enPassantText.equals("-"))
        {
            enPassantSquare = -1;
        }
        else
        {
            int column = "abcdefgh".indexOf(enPassantText.charAt(0));
            int rankNumber = Character.getNumericValue(enPassantText.charAt(1));
            int row = 8 -rankNumber;
            enPassantSquare = row*8+column;
        }
    }
    public String toString()
    {
        String boardDisplay = "";
        for (int row = 0; row<8;row++)
        {
            boardDisplay = boardDisplay + (8-row)+" ";
            for(int column = 0;column<8;column++)
            {
                int piece = squares[row*8+column];
                if(piece==EMPTY)
                {
                    boardDisplay = boardDisplay + ". ";
                }
                else
                {
                    boardDisplay = boardDisplay + pieceToCHar(piece) +" ";
                }
            }
            boardDisplay = boardDisplay +"\n";
        }
        boardDisplay = boardDisplay + "\n a b c d e f g h";
        return boardDisplay;
    }
    public String toFen()
    {
        return piecesToFen()+" "+turnToFen()+" "+castlingToFen()+" "+enPassantToFen()+" "+halfmoveClock + " "+fullmoveNumber;
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
                        emptyCount =0;
                    }
                    FEN = FEN +pieceToCHar(piece);
                }
            }
            if(emptyCount>0)
            {
                FEN = FEN+emptyCount;
            }
            if (row < 7)
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
    private String castlingToFen()
    {
        String castlingText = "";
        if(whiteKingside)
        {
            castlingText = castlingText+"K";
        }
        if(whiteQueenside)
        {
            castlingText = castlingText+"Q";
        }
        if(blackKingside)
        {
            castlingText = castlingText+"k";
        }
        if(blackQueenside)
        {
            castlingText = castlingText+"q";
        }
        if(castlingText.equals(""))
        {
            castlingText = "-";
        }
        return castlingText;
    }
    private String enPassantToFen()
    {
        if (enPassantSquare ==-1)
        {
            return"-";
        }
        else
        {
            return squareNotation(enPassantSquare);
        }
    }
}
