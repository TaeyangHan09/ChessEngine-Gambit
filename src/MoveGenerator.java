import java.util.Arrays;
public class MoveGenerator
{
    private static final int[] rookRow = {-1,1,0,0};
    private static final int[] rookColumn = {0,0,-1,1};
    private static final int[] bishopRow = {-1,-1,1,1};
    private static final int[] bishopColumn = {-1,1,-1,1};
    public static int[] gernerateMove(Board board) {
        int[] moves = new int[256];
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
                int[] rowChanges = {-2, -2, -1, -1, 1, 1, 2, 2};
                int[] columnChanges = {-1, 1, -2, 2, -2, 2, -1, 1};
                for (int horseyjump = 0; horseyjump < 8; horseyjump++) {
                    int changedRow = row + rowChanges[horseyjump];
                    int changedColumn = column + columnChanges[horseyjump];
                    if (changedRow < 0 || changedRow > 7 || changedColumn < 0 || changedColumn > 7) {
                        continue;
                    }
                    int changedSquare = changedRow * 8 + changedColumn;
                    int changedPiece = board.squares[changedSquare];
                    if (changedPiece == Board.EMPTY) {
                        moves[count] = Move.bitpack(square, changedSquare, 0, false, false, false, false);
                        count++;
                    } else if (changedPiece * piece < 0) {
                        moves[count] = Move.bitpack(square, changedSquare, 0, true, false, false, false);
                        count++;
                    }

                }

            }
            if (pieceType == Board.King) {
                int row = square / 8;
                int column = square % 8;
                int[] rowChanges = {-1, -1, -1, 0, 0, 1, 1, 1};
                int[] columnChanges = {-1, 0, 1, -1, 1, -1, 0, 1};
                for (int mightykingwalk = 0; mightykingwalk < 8; mightykingwalk++) {
                    int changedRow = row + rowChanges[mightykingwalk];
                    int changedColumn = column + columnChanges[mightykingwalk];
                    if (changedRow < 0 || changedRow > 7 || changedColumn < 0 || changedColumn > 7) {
                        continue;
                    }
                    int changedSquare = changedRow * 8 + changedColumn;
                    int changedPiece = board.squares[changedSquare];
                    if (changedPiece == Board.EMPTY) {
                        moves[count] = Move.bitpack(square, changedSquare, 0, false, false, false, false);
                        count++;
                    } else if (changedPiece * piece < 0) {
                        moves[count] = Move.bitpack(square, changedSquare, 0, true, false, false, false);
                        count++;
                    }
                }
            }
            if (pieceType == Board.Rook) {
                count = sliders(board, square, rookRow, rookColumn, moves, count);
            }
            if (pieceType == Board.Bishop) {
                count = sliders(board, square, bishopRow, bishopColumn, moves, count);
            }
            if (pieceType == Board.Queen) {
                count = sliders(board, square, rookRow, rookColumn, moves, count);
                count = sliders(board, square, bishopRow, bishopColumn, moves, count);
            }
            if (pieceType == Board.Pawn)
            {

                int row = square / 8;
                int column = square % 8;
                int direction;
                int startingRow;
                if (pieceIsWhite) {
                    direction = -1;
                    startingRow = 6;
                } else {
                    direction = 1;
                    startingRow = 1;
                }
                int oneStep = row + direction;
                if (oneStep >= 0 && oneStep <= 7)
                {
                    int changedoneStep = oneStep * 8 + column;
                    if (board.squares[changedoneStep] == Board.EMPTY) {
                        moves[count] = Move.bitpack(square, changedoneStep, 0, false, false, false, false);
                        count++;
                        if (row == startingRow) {
                            int twoSteps = (row + 2 * direction) * 8 + column;
                            if (board.squares[twoSteps] == Board.EMPTY) {
                                moves[count] = Move.bitpack(square, twoSteps, 0, false, false, true, false);
                                count++;
                            }
                        }
                    }
                    int[] capturecolumnChange = {-1, 1};
                    for (int k = 0; k < 2; k++) {
                        int captureRow = row + direction;
                        int captureColumn = column + capturecolumnChange[k];
                        if (captureRow < 0 || captureRow > 7 || captureColumn < 0 || captureColumn > 7) {
                            continue;
                        }
                        int captureSquare = captureRow * 8 + captureColumn;
                        int capturedPiece = board.squares[captureSquare];
                        if (capturedPiece * piece < 0) {
                            moves[count] = Move.bitpack(square, captureSquare, 0, true, false, false, false);
                            count++;
                        } else if (captureSquare == board.enPassantSquare)
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

            private static int sliders (Board board,int square, int[] rowChanges, int[] columnChanges, int[] moves,
            int count)
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
                        if (changedPiece == Board.EMPTY) {
                            moves[count] = Move.bitpack(square, changedSquare, 0, false, false, false, false);
                            count++;
                        } else {
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

}