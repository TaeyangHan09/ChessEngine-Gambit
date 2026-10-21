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
    private static final int[] endgamePawnValueTable =
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    47,  47,  49,  43,  52,  54,  50,  55,
                    19,  18,  23,  10,  18,  16,   8,  11,
                    11,  10,   4,  14,   7,   8,   0,   2,
                    6,  -3,   5,   8,  14,   8,   4,   1,
                    8,  -7,  -5,   3,   9,   0,  10,   6,
                    6,   8,   9, -14, -16,  12,   7,   5,
                    0,   0,   0,   0,   0,   0,   0,   0
            };
    private static final int[] endgameKnightValueTable =
            {
                    -54, -38, -31, -28, -30, -31, -42, -49,
                    -39, -18,  -2,   4,  -1,   6, -20, -38,
                    -27,   4,   8,  23,  12,  12,   2, -26,
                    -24,   6,  15,  20,  23,  16,   7, -27,
                    -26,  -1,  18,  14,   9,  12,   1, -25,
                    -26,  -3,   0,  10,   8,   8,   4, -31,
                    -41, -19,   5,  -4,  -2,  -4, -22, -37,
                    -50, -39, -27, -29, -28, -30, -45, -50
            };
    private static final int[] endgameBishopValueTable =
            {
                    -21,  -8, -14, -12, -10,  -9, -10, -19,
                    -11,  -2,   0,  -5,  -3,   2,   2, -11,
                    -10,   1,   2,  10,  10,  13,   0,  -7,
                    -7,   5,   5,   8,  12,  -1,   5, -13,
                    -8,   0,   9,   6,  10,  10,  -1,  -5,
                    -4,  10,  13,   8,   9,   4,   5,  -6,
                    -11,   2,   1,   2,  -8,   0,   4,  -9,
                    -20,  -6,  -8, -15,  -9,  -8,  -9, -23
            };
    private static final int[] endgameRookValueTable =
            {
                    2,   1,  -2,   2,   3,   2,   0,   5,
                    7,  12,  13,  11,  15,  12,   9,   9,
                    5,  10,   3,   0,   1,   2,   3,  -3,
                    -2,   1,   4,   2,  -3,   1,  -2,  -1,
                    -3,   4,  -2,   4,  -3,   1,  -2,  -1,
                    -4,   0,   3,  -6,  -1,  -2,  -2,  -5,
                    -10,  -1,  -5,  -6,  -6,  -7,  -4,  -8,
                    -7,  -4,  -3,   5,  -4,   2,  -6, -10
            };
    private static final int[] endgameQueenValueTable =
            {
                    -19,  -9, -12,  -5,  -4,  -9, -11, -17,
                    -15,   2,   1,   2,   4,   4,   3,  -5,
                    -12,   1,   4,   5,   7,   4,   4,  -5,
                    -7,  -3,   3,   5,   8,   5,   0,   5,
                    -9,  -1,  -3,   8,   5,   0,  -2,  -6,
                    -12,   3,  13,  -3,   7,  -1,   0,  -9,
                    -12,   3,   3,   4,  -1,   2,  -1,  -8,
                    -20,  -7, -11,   2,   0,  -9, -11, -19
            };
    private static final int[] endgameValue = {0,93,317,321,509,925,0};
    private static int [] endgameTableAll(int pieceType)
    {
        if(pieceType==Board.Pawn)
        {
            return endgamePawnValueTable;
        }
        if(pieceType==Board.Knight)
        {
            return endgameKnightValueTable;
        }
        if(pieceType==Board.Bishop)
        {
            return endgameBishopValueTable;
        }
        if(pieceType==Board.Rook)
        {
            return endgameRookValueTable;
        }
        return endgameQueenValueTable;
    }
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
    private static int pawnStructure(Board board,int[] whitePawny,int[] blackPawny,int[] whiteFrontPawnRow,int[] blackFrontPawnRow,int gamePhase)
    {
        int score=0;
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square];
            if(piece==Board.Pawn)
            {
            if(isPassedPawn(square,true,whiteFrontPawnRow,blackFrontPawnRow))
                {
                    score = score+passedPawnBonus[6-square/8]*(48-gamePhase)/24;
                }
            }
            if(piece==-Board.Pawn)
            {
            if(isPassedPawn(square,false,whiteFrontPawnRow,blackFrontPawnRow))
                {
                    score=score-passedPawnBonus[square/8-1]*(48-gamePhase)/24;
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
    public static final int bishopPairBonus=30;
    private static final int pawnThreatPenalty=40;
    private static boolean attackedByEnemyPawn(Board board,int square,boolean white)
    {
        int row=square/8;
        int column=square%8;
        int pawnRow;
        int enemyPawn;
        if(white)
        {
            pawnRow=row-1;
            enemyPawn=-Board.Pawn;
        }
        else
        {
            pawnRow=row+1;
            enemyPawn=Board.Pawn;
        }
        if(pawnRow<0||pawnRow>7)
        {
            return false;
        }
        if(column>0&&board.squares[pawnRow*8+column-1]==enemyPawn)
        {
            return true;
        }
        if(column<7&&board.squares[pawnRow*8+column+1]==enemyPawn)
        {
            return true;
        }
        return false;

    }
    private static final int rookSeventhRankBonus=20;
    private static final int tempoBonus=15;
    private static final int rookOpenFileBonus=20;
    private static final int rookSemiOpenFileBonus=10;
    public static int evaluate(Board board)
    {
        int score = 0;
        int gamePhase=0;
        int middlegamePieces=0;
        int endgamePieces=0;
        int[] whitePawny=new int[8];
        int[] blackPawny=new int[8];
        int[] whiteFrontPawnRow={-1,-1,-1,-1,-1,-1,-1,-1};
        int[] blackFrontPawnRow={8,8,8,8,8,8,8,8};
        int mobilityScore=0;
        int whiteMinors=0;
        int blackMinors=0;
        int whiteBishops=0;
        int blackBishops=0;
        int whiteKnights=0;
        int blackKnights=0;
        int whiteMajors=0;
        int blackMajors=0;
        int whitePieceMaterial=0;
        int blackPieceMaterial=0;
        for(int square=0;square<64;square++)
        {
            int piece =board.squares[square];
            if(piece==Board.EMPTY)
            {
                continue;
            }
            int absolutevalue = Math.abs(piece);
            if(absolutevalue==Board.Knight||absolutevalue==Board.Bishop)
            {
                if(piece>0)
                {
                    whiteMinors++;
                }
                else
                {
                    blackMinors++;
                }
            }
            if(absolutevalue==Board.Bishop)
            {
                if(piece>0)
                {
                    whiteBishops++;
                }
                else
                {
                    blackBishops++;
                }
            }
            if(absolutevalue==Board.Knight)
            {
                if(piece>0)
                {
                    whiteKnights++;
                }
                else
                {
                    blackKnights++;
                }
            }
            if(absolutevalue==Board.Rook||absolutevalue==Board.Queen)
            {
                if(piece>0)
                {
                    whiteMajors++;
                }
                else
                {
                    blackMajors++;
                }
            }
            if(absolutevalue==Board.Knight||absolutevalue==Board.Bishop)
            {
                gamePhase+=1;
            }
            if(absolutevalue==Board.Rook)
            {
                gamePhase+=2;
            }
            if(absolutevalue==Board.Queen)
            {
                gamePhase+=4;
            }
            if(absolutevalue!=Board.Pawn&&absolutevalue!=Board.King)
            {
                if(piece>0)
                {
                    whitePieceMaterial+=value[absolutevalue];
                }
                else
                {
                    blackPieceMaterial+=value[absolutevalue];
                }
            }
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
            if(absolutevalue>=Board.Knight&&absolutevalue<=Board.Queen&&attackedByEnemyPawn(board,square,piece>0))
            {
                if(piece>0)
                {
                    score=score-pawnThreatPenalty;
                }
                else
                {
                    score=score+pawnThreatPenalty;
                }
            }
            if(absolutevalue==board.King)
            {
                continue;
            }
            int [] specificTable=tableAll(absolutevalue);
            int [] endgameTable=endgameTableAll(absolutevalue);
            if(piece>0)
            {
                middlegamePieces+=value[absolutevalue]+specificTable[square];
                endgamePieces+=endgameValue[absolutevalue]+endgameTable[square];
            }
            else
            {
                middlegamePieces-=value[absolutevalue]+specificTable[square^56];
                endgamePieces-=endgameValue[absolutevalue]+endgameTable[square^56];
            }
        }
        score=score+pawnStructure(board,whitePawny,blackPawny,whiteFrontPawnRow,blackFrontPawnRow,Math.min(gamePhase,24));
        score=score+mobilityScore;
        if(whiteBishops>=2)
        {
            score=score+bishopPairBonus;
        }
        if(blackBishops>=2)
        {
            score=score-bishopPairBonus;
        }
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square];
            if(piece==Board.Rook&&square/8==1)
            {
                score=score+rookSeventhRankBonus;
            }
            if(piece==-Board.Rook&&square/8==6)
            {
                score=score-rookSeventhRankBonus;
            }
            if(piece==Board.Rook)
            {
                int column=square%8;
                if(whitePawny[column]==0&&blackPawny[column]==0)
                {
                    score=score+rookOpenFileBonus;
                }
                else if(whitePawny[column]==0)
                {
                    score=score+rookSemiOpenFileBonus;
                }
            }
            if(piece==-Board.Rook)
            {
                int column=square%8;
                if(whitePawny[column]==0&&blackPawny[column]==0)
                {
                    score=score-rookOpenFileBonus;
                }
                else if(blackPawny[column]==0)
                {
                    score=score-rookSemiOpenFileBonus;
                }
            }
        }
        if(gamePhase>24)
        {
            gamePhase=24;
        }
        int middlegameKing=0;
        int endgameKing=0;
        if(board.kingSquare[0]!=-1)
        {
            middlegameKing+=kingValueTable[board.kingSquare[0]];
            endgameKing+=endgameKingValueTable[board.kingSquare[0]];
        }
        if(board.kingSquare[1]!=-1)
        {
            middlegameKing-=kingValueTable[board.kingSquare[1]^56];
            endgameKing-=endgameKingValueTable[board.kingSquare[1]^56];
        }
        middlegameKing=middlegameKing-kingDangerPenalty(board,true,whitePawny)+kingDangerPenalty(board,false,blackPawny);
        score=score+(middlegameKing*gamePhase+endgameKing*(24-gamePhase))/24;
        score=score+(middlegamePieces*gamePhase+endgamePieces*(24-gamePhase))/24;
        boolean whiteHasPawns=false;
        boolean blackHasPawns=false;
        for(int column=0;column<8;column++)
        {
            if(whitePawny[column]>0)
            {
                whiteHasPawns=true;
            }
            if(blackPawny[column]>0)
            {
                blackHasPawns=true;
            }
        }
        if(!whiteHasPawns&&!blackHasPawns&&whiteMajors==0&&blackMajors==0)
        {
            if(whiteMinors<=1&&blackMinors<=1)
            {
                return 0;
            }
            if(whiteMinors==2&&whiteKnights==2&&blackMinors==0)
            {
                return 0;
            }
            if(blackMinors==2&&blackKnights==2&&whiteMinors==0)
            {
                return 0;
            }
        }
        if(score>0&&!whiteHasPawns&&whitePieceMaterial-blackPieceMaterial<400)
        {
            score=score/8;
        }
        if(score<0&&!blackHasPawns&&blackPieceMaterial-whitePieceMaterial<400)
        {
            score=score/8;
        }
        if(board.whiteToMove)
        {
            return score+tempoBonus;
        }
        else
        {
            return -score+tempoBonus;
        }
    }
}
