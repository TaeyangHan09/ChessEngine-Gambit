public class Evaluator
{
    private static final int[] pawnValueTable =
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    48,  48,  49,  47,  51,  52,  49,  54,
                    14,  13,  20,  12,  20,  19,   8,  14,
                    14,   9,   1,  22,   8,   4,   2,   4,
                    4,   0,   7,  17,  18,  12,   2,  -2,
                    3,  -7,  -5,  -1,   7,   6,  13,   2,
                    -5,   2,   8, -20, -18,   6,  15,  -6,
                    0,   0,   0,   0,   0,   0,   0,   0
            };
    private static final int[] knightValueTable =
            {
                    -54, -39, -31, -29, -29, -31, -41, -50,
                    -38, -17,   0,   2,  -1,   7, -19, -38,
                    -29,   6,   9,  23,  14,  14,   1, -27,
                    -24,   2,  16,  26,  20,  18,   8, -28,
                    -20,  -2,  16,  12,  13,  13,   1, -24,
                    -24,  -4,   0,  15,   7,   4,   0, -33,
                    -39, -20,   6,  -6,  -2,  -5, -23, -37,
                    -51, -33, -27, -29, -27, -30, -46, -50
            };

    private static final int[] bishopValueTable =
            {
                    -22,  -8, -12, -11, -10,  -9, -10, -20,
                    -13,  -1,   0,  -5,  -3,   3,   1, -11,
                    -9,   1,   3,  10,  11,  12,   1,  -6,
                    -7,   5,   5,  13,  14,   2,   5, -13,
                    -6,   1,   8,   6,  10,   2,  -3,  -7,
                    -1,   7,  17,   5,   4,   2,   6,  -5,
                    -9,   2,   3,  -1,  -9,  -1,  11, -10,
                    -19,  -6,  -8, -15,  -8, -16,  -8, -23
            };

    private static final int[] rookValueTable =
            {
                    2,   2,  -1,   2,   2,   2,   1,   4,
                    7,  12,  15,  12,  13,  12,   9,  11,
                    4,   9,   3,  -1,   1,   1,   3,  -3,
                    -2,  -2,   3,   2,  -2,   0,  -3,  -3,
                    -3,   2,  -1,   7,  -2,   2,  -3,   1,
                    -6,  -3,   1,  -7,  -3,  -3,  -1,  -5,
                    -13,  -2,  -4,  -4,  -5,  -9,  -3,  -8,
                    -7,  -9,   2,   6,   2,   5, -13, -13
            };

    private static final int[] queenValueTable =
            {
                    -19,  -9, -11,  -5,  -4,  -9, -10, -16,
                    -14,   2,   0,   1,   3,   5,   4,  -5,
                    -12,   0,   3,   6,   8,   4,   5,  -5,
                    -7,  -6,   1,   4,   6,   5,   0,  12,
                    -9,  -2,  -5,   6,   3,   2,  -2,  -8,
                    -11,  -1,  10,  -5,   7,  -2,   2, -10,
                    -12,   4,   4,   6,   1,   1,  -2,  -8,
                    -19,  -6,  -9,   5,   2,  -8, -11, -20
            };

    private static final int[] kingValueTable =
            {
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -38, -39, -30,
                    -31, -40, -40, -50, -49, -38, -39, -30,
                    -29, -39, -39, -51, -50, -40, -39, -29,
                    -19, -29, -32, -45, -42, -32, -29, -21,
                    -11, -21, -23, -26, -22, -19, -20, -10,
                    22,  20,  -5,  -3,  -6,   3,  23,  13,
                    19,  19,  24,  -4,  19,   5,  34,  22
            };
    private static final int[] endgameKingValueTable =
            {
                    -51, -41, -31, -19, -19, -28, -38, -50,
                    -30, -18,  -7,   4,   4,  -4, -14, -29,
                    -30,  -1,  23,  34,  34,  28,   0, -26,
                    -26,  -3,  34,  35,  43,  29,  -1, -23,
                    -28,  -6,  21,  32,  30,  20,  -6, -25,
                    -30, -15,  12,  17,  15,  14, -10, -32,
                    -29, -29, -11,  -9,  -2,  -4, -29, -33,
                    -46, -36, -21, -30, -28, -31, -35, -55
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
