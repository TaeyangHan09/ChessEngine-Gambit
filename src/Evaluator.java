public class Evaluator
{
    private static final int[] pawnValueTable =
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    47,  48,  48,  46,  51,  52,  49,  54,
                    13,  11,  19,  9,  19,  19,   8,  12,
                    15,   8,   3,  21,   13,   5,   5,   2,
                    -2,   0,   8,  17,  17,  11,   0,  -10,
                    0,  -7,  5,  -2,   9,   4,  15,   -1,
                    -6,  -1,   0, -17,  -8,  11,  21,  -6,
                    0,   0,   0,   0,   0,   0,   0,   0
            };
    private static final int[] knightValueTable =
            {
                    -57, -39, -31, -29, -28, -32, -41, -50,
                    -39, -17,   0,   3,  -1,   8, -19, -38,
                    -30,   6,  10,  23,  15,  16,   1, -26,
                    -23,   1,  16,  31,  17,  20,   4, -26,
                    -18,  -2,  15,  10,  15,  13,   1, -24,
                    -25,  -8,   2,  15,   7,   4,  -1, -34,
                    -38, -21,   3,  -4,  -3,  -6, -22, -34,
                    -51, -32, -27, -29, -27, -28, -42, -50
            };

    private static final int[] bishopValueTable =
            {
                    -22,  -8, -13, -11, -10, -10, -10, -20,
                    -14,  -1,  -1,  -5,  -3,   4,   1, -12,
                    -8,   1,   4,   9,  11,  13,   1,  -2,
                    -7,   2,   5,  15,  15,   1,   3, -13,
                    -5,   2,   3,   7,  11,  -3,  -4,  -9,
                    3,   6,  15,   2,   2,   1,   4,  -4,
                    -8,   7,   3,  -2,  -3,  -1,  20,  -9,
                    -18,  -7,  -7, -14,  -7, -18,  -8, -23
            };

    private static final int[] rookValueTable =
            {
                    3,   2,  -2,   2,   2,   3,   2,   4,
                    7,   9,  16,  13,  12,  12,  10,  11,
                    5,   9,   4,   0,   2,   2,   4,  -2,
                    -2,  -2,   4,   2,  -3,   1,  -5,  -2,
                    -4,   1,  -1,   7,  -3,   1,  -3,   1,
                    -8,  -6,   0,  -9,  -4,  -4,   0,  -6,
                    -15,  -4,  -4,  -4,  -5, -10,  -4, -11,
                    -8, -10,   4,   5,   4,   3, -17, -11
            };

    private static final int[] queenValueTable =
            {
                    -19,  -9, -11,  -5,  -4,  -9, -10, -16,
                    -13,  -4,  -1,   1,   2,   5,   5,  -4,
                    -12,   0,   1,   6,   8,   4,   6,  -2,
                    -8,  -8,   1,   1,   4,   5,   0,  15,
                    -10,  -3,  -7,   4,   3,   1,  -1,  -8,
                    -11,  -1,   8,  -5,   7,   1,   4,  -9,
                    -12,   3,   4,   7,   5,   1,  -2,  -9,
                    -15,  -5,  -5,   8,   3,  -8, -11, -20
            };

    private static final int[] kingValueTable =
            {
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -38, -39, -30,
                    -31, -39, -40, -50, -49, -38, -39, -30,
                    -29, -39, -39, -51, -51, -40, -39, -29,
                    -19, -29, -33, -46, -43, -32, -28, -21,
                    -12, -22, -23, -28, -22, -18, -20, -10,
                    23,  19,  -5,  -6,  -7,   2,  25,  11,
                    20,  22,  27,  -5,  20,   3,  34,  23
            };
    private static final int[] endgameKingValueTable =
            {
                    -51, -41, -30, -18, -19, -28, -38, -49,
                    -31, -16,  -6,   5,   5,  -2, -12, -29,
                    -30,   1,  22,  33,  34,  29,   4, -25,
                    -25,  -1,  32,  32,  40,  29,   2, -20,
                    -28,  -5,  16,  28,  25,  18,  -3, -21,
                    -32, -15,   9,  14,  12,  12,  -7, -30,
                    -29, -29, -11,  -9,  -2,  -5, -24, -33,
                    -45, -36, -22, -31, -29, -29, -39, -58
            };
    private static final int[] endgamePawnValueTable =
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    45,  47,  49,  40,  53,  55,  51,  55,
                    21,  20,  23,   7,  16,  15,   7,  10,
                    11,  11,   6,   8,   3,   6,   1,   3,
                    8,  -1,   3,   5,   9,   4,   3,   3,
                    7,  -3,  -2,   6,   9,   2,   5,   4,
                    8,   8,   8, -10, -10,  12,   3,   3,
                    0,   0,   0,   0,   0,   0,   0,   0
            };
    private static final int[] endgameKnightValueTable =
            {
                    -55, -37, -31, -27, -30, -32, -42, -49,
                    -39, -18,  -3,   4,  -1,   5, -20, -38,
                    -27,   4,   9,  22,  11,  12,   1, -25,
                    -22,   6,  16,  20,  23,  16,   6, -24,
                    -24,   0,  17,  13,   9,  12,   1, -23,
                    -26,  -5,  -1,   8,   7,   5,   2, -30,
                    -40, -19,   3,  -5,  -3,  -5, -22, -37,
                    -50, -38, -28, -29, -29, -29, -44, -50
            };
    private static final int[] endgameBishopValueTable =
            {
                    -21,  -8, -14, -12, -10,  -9, -10, -18,
                    -11,  -3,   0,  -6,  -3,   1,   3, -12,
                    -10,   2,   1,   8,   8,  13,   0,  -6,
                    -7,   5,   5,   8,  11,  -3,   4, -13,
                    -8,  -1,   8,   5,   9,  11,  -2,  -3,
                    -3,   9,  10,   7,  10,   3,   3,  -6,
                    -11,   0,   0,   2,  -5,   1,   2,  -8,
                    -19,  -6,  -9, -14,  -9,  -4,  -9, -23
            };
    private static final int[] endgameRookValueTable =
            {
                    4,   1,  -3,   3,   3,   4,   2,   7,
                    7,  11,  12,  12,  13,  11,   9,   8,
                    6,   9,   3,   1,   2,   3,   5,  -3,
                    -1,   2,   5,   1,  -3,   2,  -2,   1,
                    -2,   4,  -1,   3,  -4,   0,  -2,  -2,
                    -4,   0,   3,  -6,  -1,  -2,  -2,  -5,
                    -10,  -2,  -5,  -7,  -7,  -8,  -5,  -9,
                    -5,  -3,  -5,   2,  -5,   0,  -6, -11
            };
    private static final int[] endgameQueenValueTable =
            {
                    -19,  -9, -12,  -5,  -4,  -9, -11, -16,
                    -15,   1,   0,   2,   4,   5,   4,  -4,
                    -11,   1,   4,   5,   8,   4,   4,  -4,
                    -7,  -3,   3,   5,   8,   5,   0,   5,
                    -9,  -1,  -4,   8,   5,   0,  -1,  -5,
                    -12,   1,  12,  -3,   8,   0,   1,  -8,
                    -11,   2,   2,   4,  -1,   2,  -1,  -8,
                    -18,  -7, -10,  -1,   0, -10, -11, -19
            };
    private static final int[] endgameValue = {0,84,310,311,510,931,0};
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
    private static final int[] value = {0,77,339,321,478,932,0};
    private static final int doubledPawnPenalty = 7;
    private static final int isolatedPawnPenalty = 15;
    private static final int[] passedPawnBonus={6,5,8,19,37,47,0,0};
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
    private static int squareDistance(int firstSquare,int secondSquare)
    {
        return Math.max(Math.abs(firstSquare/8-secondSquare/8),Math.abs(firstSquare%8-secondSquare%8));
    }
    private static int passedKingDistance(Board board,int square,boolean white,int rankIndex)
    {
        int weight=rankIndex-1;
        if(weight<=0)
        {
            return 0;
        }
        int frontSquare = white?square-8:square+8;
        int ownKing=board.kingSquare[white?0:1];
        int enemyKing=board.kingSquare[white?1:0];
        if(ownKing==-1||enemyKing==-1)
        {
            return 0;
        }
        return weight*(5*squareDistance(enemyKing,frontSquare)-2*squareDistance(ownKing,frontSquare));
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
                    score=score+passedKingDistance(board,square,true,6-square/8)*(24-gamePhase)/24;
                }
            }
            if(piece==-Board.Pawn)
            {
            if(isPassedPawn(square,false,whiteFrontPawnRow,blackFrontPawnRow))
                {
                    score=score-passedPawnBonus[square/8-1]*(48-gamePhase)/24;
                    score=score-passedKingDistance(board,square,false,square/8-1)*(24-gamePhase)/24;
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
    private static final int knightMobilityWeight=1;
    private static final int bishopMobilityWeight=3;
    private static final int rookMobilityWeight=3;
    private static final int queenMobilityWeight=2;
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
    private static final int missingShieldPenalty = 12;
    private static final int openFilePnenalty = 20;
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
    public static final int bishopPairBonus=39;
    private static final int pawnThreatPenalty=38;
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
    private static final int rookSeventhRankBonus=5;
    private static final int tempoBonus=17;
    private static final int rookOpenFileBonus=15;
    private static final int rookSemiOpenFileBonus=12;
    public static int evaluate(Board board)
    {
        if(Nnue.ready)
        {
            return  board.nnueEvaluate();
        }
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
