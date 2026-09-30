import java.util.Arrays;
public class MoveGenerator
{
    public static int[] gernerateMove(Board board)
    {
        int[] moves = new int[256];
        int count = 0;
        for(int square = 0;square<64;square++)
        {
            int piece = board.squares[square];
            if(piece ==Board.EMPTY)
            {
                continue;
            }
            boolean pieceIsWhite = piece>0;
            if(pieceIsWhite!=board.whiteToMove)
            {
                continue;
            }
            int pieceType = Math.abs(piece);
            if(pieceType ==Board.Knight)
            {
                int row = square/8;
                int column = square%8;
                int[] rowChanges = {-2,-2,-1,-1,1,1,2,2};
                int[] columnChanges = {-1,1,-2,2,-2,2,-1,1};
                for(int horseyjump = 0;horseyjump<8;horseyjump++)
                {
                    int changedRow = row+rowChanges[horseyjump];
                    int changedColumn = column +columnChanges[horseyjump];
                    if(changedRow<0||changedRow>7||changedColumn<0||changedColumn>7)
                    {
                        continue;
                    }
                    int changedSquare = changedRow *8+changedColumn;
                    int changedPiece = board.squares[changedSquare];
                    if(changedPiece ==Board.EMPTY)
                    {
                        moves[count] = Move.bitpack(square,changedSquare,0,false,false,false,false);
                        count++;
                    }
                    else if(changedPiece*piece<0)
                    {
                        moves[count] = Move.bitpack(square,changedSquare,0,true,false,false,false);
                        count++;
                    }

                }

            }
            if (pieceType==Board.King)
            {
                int row = square/8;
                int column = square%8;
                int[] rowChanges = {-1,-1,-1,0,0,0,1,1,1};
                int[] columnChanges = {-1,0,1,-1,0,1,-1,0,1};
                for (int mightykingwalk = 0;mightykingwalk<8;mightykingwalk++)
                {
                    int changedRow = row+ rowChanges[mightykingwalk];
                    int changedColumn = column+ columnChanges[mightykingwalk];
                    if(changedRow<0||changedRow>7||changedColumn<0||changedColumn>7)
                    {
                        continue;
                    }
                    int changedSquare = changedRow *8+changedColumn;
                    int changedPiece = board.squares[changedSquare];
                    if(changedPiece ==Board.EMPTY)
                    {
                        moves[count] = Move.bitpack(square,changedSquare,0,false,false,false,false);
                        count++;
                    }
                    else if(changedPiece*piece<0)
                    {
                        moves[count] = Move.bitpack(square,changedSquare,0,true,false,false,false);
                        count++;
                    }
                }
            }
        }
        return Arrays.copyOf(moves,count);
    }


}
