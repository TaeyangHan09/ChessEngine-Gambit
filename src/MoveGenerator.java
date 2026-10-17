import java.util.Arrays;
public class MoveGenerator
{
    private static final int[] rookRow = {-1,1,0,0};
    private static final int[] rookColumn = {0,0,-1,1};
    private static final int[] bishopRow = {-1,-1,1,1};
    private static final int[] bishopColumn = {-1,1,-1,1};
    private static final int[] kingRow = {-1,-1,-1,0,0,1,1,1};
    private static final int[] kingColumn = {-1,0,1,-1,1,-1,0,1};
    private static final int[] pawnCaptureColumn = {-1,1};
    private static final int[] knightRow = {-2,-2,-1,-1,1,1,2,2};
    private static final int[] knightColumn = {-1,1,-2,2,-2,2,-1,1};
    private static final int[] promotionPieces={Board.Knight,Board.Bishop,Board.Rook,Board.Queen};
    public static int[] generateLegalMoves(Board board)
    {
        return generateLegalMoves(board,false);
    }
    public static int[] generateLegalCaptures(Board board)
    {
        return generateLegalMoves(board,true);
    }
    public static int[] generateLegalMoves(Board board,boolean captruesOnly)
    {
        int[] pseudo = generateMove(board,captruesOnly);
        int[] legal = new int[pseudo.length];
        int n =0;
        boolean mover = board.whiteToMove;
        for(int move:pseudo)
        {
            board.makeMove(move);
            if(!isKingAttacked(board,mover))
            {
                legal[n++] = move;
            }
            board.unmakeMove();
        }
        return Arrays.copyOf(legal,n);
    }
    public static int[] generateMove(Board board)
    {
        return generateMove(board,false);
    }
    private static final int[] Messi=new int[256];
    public static int[] generateMove(Board board,boolean capturesOnly)
    {
        int[] moves = Messi;
        int count = 0;
        for (int square = 0; square < 64; square++) {
            int piece = board.squares[square];
            if (piece == Board.EMPTY) {
                continue;
            }
            boolean pieceIsWhite = piece > 0;
            if (pieceIsWhite != board.whiteToMove) {
                continue;
            }
            int pieceType = Math.abs(piece);
            if (pieceType == Board.Knight) {
                int row = square / 8;
                int column = square % 8;
                for (int horseyjump = 0; horseyjump < 8; horseyjump++)
                {
                    int changedRow = row + knightRow[horseyjump];
                    int changedColumn = column + knightColumn[horseyjump];
                    if (changedRow < 0 || changedRow > 7 || changedColumn < 0 || changedColumn > 7) {
                        continue;
                    }
                    int changedSquare = changedRow * 8 + changedColumn;
                    int changedPiece = board.squares[changedSquare];
                    if (changedPiece == Board.EMPTY)
                    {
                        if(!capturesOnly)
                        {
                            moves[count] = Move.bitpack(square, changedSquare, 0, false, false, false, false);
                            count++;
                        }
                    }
                    else if (changedPiece * piece < 0) {
                        moves[count] = Move.bitpack(square, changedSquare, 0, true, false, false, false);
                        count++;
                    }

                }

            }
            if (pieceType == Board.King) {
                int row = square / 8;
                int column = square % 8;
                for (int mightykingwalk = 0; mightykingwalk < 8; mightykingwalk++)
                {
                    int changedRow = row + kingRow[mightykingwalk];
                    int changedColumn = column + kingColumn[mightykingwalk];
                    if (changedRow < 0 || changedRow > 7 || changedColumn < 0 || changedColumn > 7) {
                        continue;
                    }
                    int changedSquare = changedRow * 8 + changedColumn;
                    int changedPiece = board.squares[changedSquare];
                    if (changedPiece == Board.EMPTY)
                    {
                        if(!capturesOnly)
                        {
                            moves[count] = Move.bitpack(square, changedSquare, 0, false, false, false, false);
                            count++;
                        }
                    } else if (changedPiece * piece < 0) {
                        moves[count] = Move.bitpack(square, changedSquare, 0, true, false, false, false);
                        count++;
                    }
                }
                if(pieceIsWhite&&square==60&&board.whiteKingside&&!capturesOnly)
                {

                    if(board.squares[61] == Board.EMPTY && board.squares[62] == Board.EMPTY &&board.squares[63] == Board.Rook&&!isSquareAttacked(board,60,false)&&!isSquareAttacked(board,61,false))
                    {
                        moves[count] = Move.bitpack(60,62,0,false,false,false,true);
                        count++;
                    }
                }
                if(pieceIsWhite&&square==60&&board.whiteQueenside&&!capturesOnly)
                {

                    if(board.squares[59] == Board.EMPTY &&board.squares[58]==Board.EMPTY&& board.squares[57] == Board.EMPTY &&board.squares[56] == Board.Rook&&!isSquareAttacked(board,60,false)&&!isSquareAttacked(board,59,false))
                    {
                        moves[count] = Move.bitpack(60,58,0,false,false,false,true);
                        count++;
                    }
                }
                if(!pieceIsWhite&&square==4&&board.blackKingside&&!capturesOnly)
                {

                    if(board.squares[5] == Board.EMPTY && board.squares[6] == Board.EMPTY &&board.squares[7] == -Board.Rook&&!isSquareAttacked(board,4,true)&&!isSquareAttacked(board,5,true))
                    {
                        moves[count] = Move.bitpack(4,6,0,false,false,false,true);
                        count++;
                    }
                }
                if(!pieceIsWhite&&square==4&&board.blackQueenside&&!capturesOnly)
                {

                    if(board.squares[3] == Board.EMPTY &&board.squares[2]==Board.EMPTY&& board.squares[1] == Board.EMPTY &&board.squares[0] == -Board.Rook&&!isSquareAttacked(board,4,true)&&!isSquareAttacked(board,3,true))
                    {
                        moves[count] = Move.bitpack(4,2,0,false,false,false,true);
                        count++;
                    }
                }
            }
            if (pieceType == Board.Rook) {
                count = sliders(board, square, rookRow, rookColumn, moves, count,capturesOnly);
            }
            if (pieceType == Board.Bishop) {
                count = sliders(board, square, bishopRow, bishopColumn, moves, count,capturesOnly);
            }
            if (pieceType == Board.Queen) {
                count = sliders(board, square, rookRow, rookColumn, moves, count,capturesOnly);
                count = sliders(board, square, bishopRow, bishopColumn, moves, count,capturesOnly);
            }
            if (pieceType == Board.Pawn)
            {

                int row = square / 8;
                int column = square % 8;
                int direction;
                int startingRow;
                int promotionRow;
                if (pieceIsWhite) {
                    direction = -1;
                    startingRow = 6;
                    promotionRow=0;
                } else {
                    direction = 1;
                    startingRow = 1;
                    promotionRow = 7;
                }
                int oneStep = row + direction;
                if (oneStep >= 0 && oneStep <= 7)
                {
                    int changedoneStep = oneStep * 8 + column;
                    if (board.squares[changedoneStep] == Board.EMPTY)
                    {
                        if(oneStep==promotionRow)
                        {
                            count = addPromotions(moves,count,square,changedoneStep,false,capturesOnly);
                        }
                        else if(!capturesOnly)
                        {
                            moves[count] = Move.bitpack(square,changedoneStep,0,false,false,false,false);
                            count++;
                        }
                        if (row == startingRow&&!   capturesOnly)
                        {
                            int twoSteps = (row + 2 * direction) * 8 + column;
                            if (board.squares[twoSteps] == Board.EMPTY) {
                                moves[count] = Move.bitpack(square, twoSteps, 0, false, false, true, false);
                                count++;
                            }
                        }
                    }
                    for (int k = 0; k < 2; k++) {
                        int captureRow = row + direction;
                        int captureColumn = column + pawnCaptureColumn[k];
                        if (captureRow < 0 || captureRow > 7 || captureColumn < 0 || captureColumn > 7) {
                            continue;
                        }
                        int captureSquare = captureRow * 8 + captureColumn;
                        int capturedPiece = board.squares[captureSquare];
                        if (capturedPiece * piece < 0)
                        {
                            if(oneStep ==promotionRow)
                            {
                                count = addPromotions(moves,count,square,captureSquare,true,capturesOnly);
                            }
                            else
                            {
                                moves[count] = Move.bitpack(square,captureSquare,0,true,false,false,false);
                                count ++;
                            }
                        }
                        else if (captureSquare == board.enPassantSquare)
                        {
                            moves[count] = Move.bitpack(square,captureSquare,0,true,true,false,false);
                            count++;
                        }
                    }

                }
            }
            }
            return Arrays.copyOf(moves, count);
        }

            private static int sliders (Board board,int square, int[] rowChanges, int[] columnChanges, int[] moves, int count,boolean captureOnly)
            {
                int piece = board.squares[square];
                int row = square / 8;
                int column = square % 8;
                for (int direction = 0; direction < rowChanges.length; direction++) {
                    int changedRow = row + rowChanges[direction];
                    int changedColumn = column + columnChanges[direction];
                    while (changedRow >= 0 && changedRow <= 7 && changedColumn >= 0 && changedColumn <= 7) {
                        int changedSquare = changedRow * 8 + changedColumn;
                        int changedPiece = board.squares[changedSquare];
                        if (changedPiece == Board.EMPTY)
                        {
                            if(!captureOnly)
                            {
                                moves[count] = Move.bitpack(square, changedSquare, 0, false, false, false, false);
                                count++;
                            }
                        }
                        else
                        {
                            if (changedPiece * piece < 0) {
                                moves[count] = Move.bitpack(square, changedSquare, 0, true, false, false, false);
                                count++;
                            }
                            break;
                        }
                        changedRow = changedRow + rowChanges[direction];
                        changedColumn = changedColumn + columnChanges[direction];
                    }

                }
                return count;
            }
            private static int addPromotions(int[] moves,int count, int from, int to, boolean capture,boolean queenOnly)
            {
                int firstPromotion=0;
                if(queenOnly)
                {
                    firstPromotion=3;
                }
                for(int i=firstPromotion;i<4;i++)
                {
                    moves[count] = Move.bitpack(from,to,promotionPieces[i],capture,false,false,false);
                    count++;
                }
                return count;
            }
            public static int findKing(Board board, boolean white)
            {
                int king;
                if(white)
                {
                    king=Board.King;
                }
                else
                {
                    king=-Board.King;
                }
                for(int square = 0;square<64;square++)
                {
                    if(board.squares[square]==king)
                    {
                        return square;
                    }
                }
                return -1;
            }
            public static boolean isSquareAttacked(Board board,int square,boolean byWhite)
            {
                int row = square/8;
                int column = square%8;
                int attackerRow;
                int attackingPawn;
                if(byWhite)
                {
                    attackerRow = row+1;
                    attackingPawn = Board.Pawn;
                }
                else
                {
                    attackerRow = row-1;
                    attackingPawn = -Board.Pawn;
                }
                for(int i=0;i<2;i++)
                {
                    int attackColumn = column +pawnCaptureColumn[i];
                    if(attackColumn<0||attackColumn>7||attackerRow<0||attackerRow>7)
                    {
                        continue;
                    }
                    if(board.squares[attackerRow*8+attackColumn]==attackingPawn)
                    {
                        return true;
                    }
                }
                int attackingKnight;
                if(byWhite)
                {
                    attackingKnight = Board.Knight;
                }
                else
                {
                    attackingKnight = -Board.Knight;
                }
                for(int i=0;i<8;i++)
                {
                    int jumpRow = row+knightRow[i];
                    int jumpColumn = column+knightColumn[i];
                    if(jumpRow<0||jumpRow>7||jumpColumn<0||jumpColumn>7)
                    {
                        continue;
                    }
                    if(board.squares[jumpRow*8+jumpColumn]==attackingKnight)
                    {
                        return true;
                    }
                }
                int attackingKing;
                if(byWhite)
                {
                    attackingKing = Board.King;
                }
                else
                {
                    attackingKing = -Board.King;
                }
                for(int i = 0;i<8;i++)
                {
                    int stepRow = row+kingRow[i];
                    int stepColumn = column +kingColumn[i];
                    if(stepRow<0||stepRow>7||stepColumn<0||stepColumn>7)
                    {
                        continue;
                    }
                    if(board.squares[stepRow*8+stepColumn] ==attackingKing)
                    {
                        return true;
                    }
                }
                int attackingRook;
                int attackingQueen;
                if(byWhite)
                {
                    attackingRook = Board.Rook;
                    attackingQueen = Board.Queen;
                }
                else
                {
                    attackingRook =-Board.Rook;
                    attackingQueen =-Board.Queen;
                }
                for(int direction=0;direction<4;direction++)
                {
                    int slideRow = row+rookRow[direction];
                    int slideColumn=column+rookColumn[direction];
                    while(slideRow>=0&&slideRow<=7&&slideColumn>=0&&slideColumn<=7)
                    {
                        int changedPiece = board.squares[slideRow*8+slideColumn];
                        if(changedPiece!=Board.EMPTY)
                        {
                            if(changedPiece ==attackingRook||changedPiece==attackingQueen)
                            {
                                return true;
                            }
                            break;
                        }
                        slideRow = slideRow+rookRow[direction];
                        slideColumn = slideColumn+rookColumn[direction];
                    }
                }
                int attackingbishopPiece;
                if(byWhite)
                {
                    attackingbishopPiece = Board.Bishop;
                }
                else
                {
                    attackingbishopPiece = -Board.Bishop;
                }
                for(int sniperDirection = 0;sniperDirection<4;sniperDirection++)
                {
                    int sniperRow = row+bishopRow[sniperDirection];
                    int sniperColumn = column+bishopColumn[sniperDirection];
                    while(sniperRow>=0&&sniperRow<=7&&sniperColumn>=0&&sniperColumn<=7)
                    {
                        int changedPiece = board.squares[sniperRow*8+sniperColumn];
                        if(changedPiece!=Board.EMPTY)
                        {
                            if(changedPiece==attackingbishopPiece||changedPiece==attackingQueen)
                            {
                                return true;
                            }
                            break;
                        }
                        sniperRow = sniperRow +bishopRow[sniperDirection];
                        sniperColumn = sniperColumn+bishopColumn[sniperDirection];
                    }
                }
                return false;
            }
            public static boolean isKingAttacked(Board board,boolean white)
            {
                int mightykingSquare=board.kingSquare[white?0:1];
                if(mightykingSquare==-1)
                {
                    return false;
                }
                return isSquareAttacked(board,mightykingSquare,!white);
            }

}