public class Evaluator
{
    private static final int[] pawnValueTable =
    {
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 20,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    private static final int[] knightValueTable =
    {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] bishopValueTable =
    {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] rookValueTable =
    {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] queenValueTable =
    {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,  0,  5,  5,  5,  5,  0, -5,
        0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] kingValueTable =
    {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20, 20,  0,  0,  0,  0, 20, 20,
        20, 30, 10,  0,  0, 10, 30, 20
    };
    private static final int[] endgameKingValueTable =
    {
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,  0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };
    private static int [] tableAll(int pieceType)
    {
        if(pieceType==Board.Pawn)
        {
            return pawnValueTable;
        }
        if(pieceType==Board.Knight)
        {
            return knightValueTable;
        }
        if(pieceType==Board.Bishop)
        {
            return bishopValueTable;
        }
        if(pieceType==Board.Rook)
        {
            return rookValueTable;
        }
        if(pieceType==Board.Queen)
        {
            return queenValueTable;
        }
        return kingValueTable;

    }
    private static boolean isEndgame(Board board)
    {
        int nonPawnMaterial=0;
        for(int square = 0;square<64;square++)
        {
            int pieceType = Math.abs((board.squares[square]));
            if(pieceType==Board.Knight||pieceType==Board.Bishop||pieceType==Board.Rook||pieceType==Board.Queen)
            {
                nonPawnMaterial = nonPawnMaterial+value[pieceType];
            }
        }
        if(nonPawnMaterial<=2600)
        {
            return true;
        }
        return false;
    }
    private static final int[] value = {0,100,320,330,500,900,0};
    private static final int doubledPawnPenalty = 15;
    private static final int isolatedPawnPenalty = 15;
    private static final int[] passedPawnBonus={0,5,10,20,40,70,0,0};
    private static boolean isPassedPawn(int square,boolean white,int[]whiteFrontPawnRow,int[]blackFrontPawnRow)
    {
        int row=square/8;
        int column=square%8;
        for(int checkColumn=column-1;checkColumn<=column+1;checkColumn++)
        {
            if(checkColumn<0||checkColumn>7)
            {
                continue;
            }
            if(white&&blackFrontPawnRow[checkColumn]<row)
            {
                return false;
            }
            if(!white&&whiteFrontPawnRow[checkColumn]>row)
            {
                return false;
            }
        }
        return true;
    }
    private static int pawnStructure(Board board,int[] whitePawny,int[] blackPawny,int[] whiteFrontPawnRow,int[] blackFrontPawnRow)
    {
        int score=0;
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square];
            if(piece==Board.Pawn)
            {
            if(isPassedPawn(square,true,whiteFrontPawnRow,blackFrontPawnRow))
                {
                    score = score+passedPawnBonus[6-square/8];
                }
            }
            if(piece==-Board.Pawn)
            {
            if(isPassedPawn(square,false,whiteFrontPawnRow,blackFrontPawnRow))
                {
                    score=score-passedPawnBonus[square/8-1];
                }
            }
        }
        for(int column=0;column<8;column++)
        {
            if(whitePawny[column]>1)
            {
                score=score-doubledPawnPenalty*(whitePawny[column]-1);
            }
            if(blackPawny[column]>1)
            {
                score=score+doubledPawnPenalty*(blackPawny[column]-1);
            }
            boolean whiteHasBuddy = false;
            boolean blackHasBuddy = false;
            if(column>0)
            {
                if(whitePawny[column-1]>0)
                {
                    whiteHasBuddy = true;
                }
                if(blackPawny[column-1]>0)
                {
                    blackHasBuddy = true;
                }
            }
            if(column<7)
            {
                if(whitePawny[column+1]>0)
                {
                    whiteHasBuddy = true;
                }
                if(blackPawny[column+1]>0)
                {
                    blackHasBuddy = true;
                }
            }
            if(whitePawny[column]>0&&!whiteHasBuddy)
            {
                score = score-isolatedPawnPenalty*whitePawny[column];
            }
            if(blackPawny[column]>0&&!blackHasBuddy)
            {
                score = score+isolatedPawnPenalty*blackPawny[column];
            }
        }
        return score;
    }
    private static final int knightMobilityWeight=4;
    private static final int bishopMobilityWeight=3;
    private static final int rookMobilityWeight=2;
    private static final int queenMobilityWeight=1;
    private static final int[] mobilityKnightRow={-2,-2,-1,-1,1,1,2,2};
    private static final int[] mobilityKnightColumn={-1,1,-2,2,-2,2,-1,1};
    private static final int[] mobilityRookRow={-1,1,0,0};
    private static final int[] mobilityRookColumn={-0,0,-1,1};
    private static final int[] mobilityBishopRow={-1,-1,1,1};
    private static final int[] mobilityBishopColumn={-1,1,-1,1};
    private static int countKnightSquare(Board board, int square)
    {
        int piece=board.squares[square];
        int row =square/8;
        int column=square%8;
        int count=0;
        for(int horseyjump=0;horseyjump<8;horseyjump++)
        {
            int changedRow=row+mobilityKnightRow[horseyjump];
            int changedColumn = column+mobilityKnightColumn[horseyjump];
            if(changedRow<0||changedRow>7||changedColumn<0||changedColumn>7)
            {
                continue;
            }
            int changedPiece=board.squares[changedRow*8+changedColumn];
            if(changedPiece==Board.EMPTY||changedPiece*piece<0)
            {
                count++;
            }
        }
        return count;
    }
    private static int countSliderSquares(Board board,int square,int[] rowChanges,int[] columnChanges)
    {
        int piece =board.squares[square];
        int row=square/8;
        int column=square%8;
        int count=0;
        for(int direction=0;direction<rowChanges.length;direction++)
        {
            int changedRow=row+rowChanges[direction];
            int changedColumn = column+columnChanges[direction];
            while(changedRow>=0&&changedRow<=7&&changedColumn>=0&&changedColumn<=7)
            {
                int changedPiece =board.squares[changedRow*8+changedColumn];
                if(changedPiece==Board.EMPTY)
                {
                    count++;
                }
                else
                {
                    if(changedPiece*piece<0)
                    {
                        count++;
                    }
                    break;
                }
                changedRow=changedRow+rowChanges[direction];
                changedColumn=changedColumn+columnChanges[direction];
            }
        }
        return count;
    }
    private static int mobility(Board board)
    {
        int score = 0;
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square];
            if(piece==Board.EMPTY)
            {
                continue;
            }
            int pieceType=Math.abs(piece);
            int pieceMobility=0;
            int weight=0;
            if(pieceType==Board.Knight)
            {
                pieceMobility= countKnightSquare(board,square);
                weight=knightMobilityWeight;
            }
            else if(pieceType==Board.Bishop)
            {
                pieceMobility=countSliderSquares(board,square,mobilityBishopRow,mobilityBishopColumn);
                weight=bishopMobilityWeight;
            }
            else if(pieceType==Board.Rook)
            {
                pieceMobility=countSliderSquares(board,square,mobilityRookRow,mobilityRookColumn);
                weight=rookMobilityWeight;
            }
            else if(pieceType==Board.Queen)
            {
                pieceMobility = countSliderSquares(board,square,mobilityRookRow,mobilityRookColumn)+countSliderSquares(board,square,mobilityBishopRow,mobilityBishopColumn);
                weight=queenMobilityWeight;
            }
            if(weight==0)
            {
                continue;
            }
            if(piece>0)
            {
                score=score+pieceMobility*weight;
            }
            else
            {
                score=score-pieceMobility*weight;
            }
        }
        return score;
    }
    private static final int missingShieldPenalty = 10;
    private static final int openFilePnenalty = 15;
    private static int kingDangerPenalty(Board board,boolean white,int[] ownPawny)
    {
        int kingSquare =board.kingSquare[white?0:1];
        if(kingSquare==-1)
        {
            return 0;
        }
        int row=kingSquare/8;
        int column=kingSquare%8;
        int direction;
        int ownPawn;
        if(white)
        {
            direction=-1;
            ownPawn=Board.Pawn;
        }
        else
        {
            direction=1;
            ownPawn=-Board.Pawn;
        }
        int penalty=0;
        for(int checkColumn=column-1;checkColumn<=column+1;checkColumn++)
        {
            if(checkColumn<0||checkColumn>7)
            {
                continue;
            }
            boolean shieldFound=false;
            for(int step=1;step<=2;step++)
            {
                int checkRow=row+direction*step;
                if(checkRow<0||checkRow>7)
                {
                    continue;
                }
                if(board.squares[checkRow*8+checkColumn]==ownPawn)
                {
                    shieldFound=true;
                }
            }
            if(!shieldFound)
            {
                penalty=penalty+missingShieldPenalty;
            }
            if(ownPawny[checkColumn]==0)
            {
                penalty=penalty+openFilePnenalty;
            }
        }
        return penalty;
    }
    public static int evaluate(Board board)
    {
        int score = 0;
        boolean endgame = isEndgame(board);
        int[] whitePawny=new int[8];
        int[] blackPawny=new int[8];
        int[] whiteFrontPawnRow={-1,-1,-1,-1,-1,-1,-1,-1};
        int[] blackFrontPawnRow={8,8,8,8,8,8,8,8};
        int mobilityScore=0;
        for(int square=0;square<64;square++)
        {
            int piece =board.squares[square];
            if(piece==Board.EMPTY)
            {
                continue;
            }
            int absolutevalue = Math.abs(piece);
            if(absolutevalue==Board.Pawn)
            {
                int column=square%8;
                int row=square/8;
                if(piece>0)
                {
                    whitePawny[column]++;
                    if(row>whiteFrontPawnRow[column])
                    {
                        whiteFrontPawnRow[column]=row;
                    }
                }
                else
                {
                    blackPawny[column]++;
                    if(row<blackFrontPawnRow[column])
                    {
                        blackFrontPawnRow[column]=row;
                    }
                }
            }
            int pieceMobility =0;
            if(absolutevalue==Board.Knight)
            {
                pieceMobility= countKnightSquare(board,square)*knightMobilityWeight;
            }
            if(absolutevalue==Board.Bishop)
            {
                pieceMobility= countSliderSquares(board,square,mobilityBishopRow,mobilityBishopColumn)*bishopMobilityWeight;
            }
            if(absolutevalue==Board.Rook)
            {
                pieceMobility= countSliderSquares(board,square,mobilityRookRow,mobilityRookColumn)*rookMobilityWeight;
            }
            if(absolutevalue==Board.Queen)
            {
                pieceMobility= (countSliderSquares(board,square,mobilityRookRow,mobilityRookColumn)+countSliderSquares(board,square,mobilityBishopRow,mobilityBishopColumn))*queenMobilityWeight;
            }
            if(piece>0)
            {
                mobilityScore+=pieceMobility;
            }
            else
            {
                mobilityScore-=pieceMobility;
            }
            int[] specificTable;
            if(absolutevalue==board.King&&endgame)
            {
                specificTable=endgameKingValueTable;
            }
            else
            {
                specificTable=tableAll(absolutevalue);
            }
            if(piece>0)
            {
                score+=value[absolutevalue]+specificTable[square];
            }
            else
            {
                score-=value[absolutevalue]+specificTable[square^56];
            }
        }
        score=score+pawnStructure(board,whitePawny,blackPawny,whiteFrontPawnRow,blackFrontPawnRow);
        score=score+mobilityScore;
        if(!endgame)
        {
            score=score-kingDangerPenalty(board,true,whitePawny)+kingDangerPenalty(board,false,blackPawny);
        }
        if(board.whiteToMove)
        {
            return score;
        }
        else
        {
            return -score;
        }
    }
}
