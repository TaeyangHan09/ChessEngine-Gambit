public class Search
{
    private static final int checkmate = 100000;
    private final boolean helper;
    public Search(boolean helper)
    {
        this.helper=helper;
    }
    public int negamax(Board board,int depth,int individualMovementCount,int alpha,int beta,boolean nullAllowed)
    {
        positionSeen++;
        boolean inCheck=takeKnownInCheck(board,individualMovementCount);
        peekAtClock();
        if(timesUp)
        {
            return 0;
        }
        if(board.isRepetition())
        {
            return 0;
        }
        if(inCheck&&individualMovementCount<100)
        {
            depth++;
        }
        if(depth==0)
        {
            setKnownInCheck(individualMovementCount,inCheck?1:0);
            return quiescence(board, alpha, beta, individualMovementCount);
        }
        if(board.halfmoveClock>=100&&!inCheck)
        {
            return 0;
        }
        int originalAlpha = alpha;
        int transpositionTableIndex = TranspositionTable.find(board.hash);
        if(transpositionTableIndex!=-1&&TranspositionTable.depthAt(transpositionTableIndex)>=depth)
        {
            int transpositionTableScore = scorefromTable(TranspositionTable.scoreAt(transpositionTableIndex),individualMovementCount);
            int transpositionTableFlag = TranspositionTable.flagAt(transpositionTableIndex);
            if(transpositionTableFlag ==TranspositionTable.exact)
            {
                return transpositionTableScore;
            }
            if(transpositionTableFlag==TranspositionTable.lower&&transpositionTableScore>=beta)
            {
                return transpositionTableScore;
            }
            if(transpositionTableFlag==TranspositionTable.upper&&transpositionTableScore<=alpha)
            {
                return transpositionTableScore;
            }
        }
        if(depth>=4&&(transpositionTableIndex==-1||TranspositionTable.moveAt(transpositionTableIndex)==0))
        {
            depth--;
        }
        int staticEval=0;
        if(!inCheck)
        {
            staticEval=Evaluator.evaluate(board);
        }
        int rawEval=staticEval;
        if(!inCheck&&transpositionTableIndex!=-1)
        {
            int tableScore=scorefromTable(TranspositionTable.scoreAt(transpositionTableIndex),individualMovementCount);
            int tableFlag=TranspositionTable.flagAt(transpositionTableIndex);
            if(Math.abs(tableScore)<checkmate-1000&&(tableFlag==TranspositionTable.exact||(tableFlag==TranspositionTable.lower&&tableScore>staticEval)||(tableFlag==TranspositionTable.upper&&tableScore<staticEval)))
            {
                staticEval=tableScore;
            }
        }
        if(individualMovementCount<128)
        {
            if(inCheck)
            {
                staticEvalAt[individualMovementCount]=noEval;
            }
            else
            {
                staticEvalAt[individualMovementCount]=rawEval;
            }
        }
        boolean improving=false;
        if(!inCheck&&individualMovementCount>=2&&individualMovementCount<128&&staticEvalAt[individualMovementCount-2]!=noEval&&rawEval>staticEvalAt[individualMovementCount-2])
        {
            improving=true;
        }
        int improvingStep=0;
        if(improving)
        {
            improvingStep=1;
        }
        boolean pvNode=beta-alpha>1;
        if(!pvNode&&!inCheck&&depth<=6&&beta<checkmate-1000&&beta>-checkmate+1000&&staticEval-120*(depth-improvingStep)>=beta)
        {
            return staticEval;
        }
        if(nullAllowed&&!inCheck&&depth>=3&&beta<checkmate-1000&&hasPieces(board)&&staticEval>=beta)
        {
            board.makeNullMove();
            int nullMoveReduction=2+depth/4;
            int nullDepth=depth-1-nullMoveReduction;
            if(nullDepth<0)
            {
                nullDepth=0;
            }
            int nullMoveScore = -negamax(board,nullDepth,individualMovementCount+1,-beta,-beta+1,false);
            board.unmakeNullMove();
            if(timesUp)
            {
                return 0;
            }
            if(nullMoveScore>=beta)
            {
                if(nullMoveScore>=checkmate-1000)
                {
                    return beta;
                }
                return nullMoveScore;
            }

        }
        int[] moves=MoveGenerator.generateMove(board);
        int previousMove=board.lastMove();
        currentCounterMove=0;
        if(previousMove!=0)
        {
            currentCounterMove=counterMoves[Move.unbitpackcurrentPosition(previousMove)][Move.unbitpackchangedPosition(previousMove)];
        }
        moves=orderMoves(board,moves,individualMovementCount);
        if(transpositionTableIndex!=-1)
        {
            int tableMove= TranspositionTable.moveAt((transpositionTableIndex));
            for(int i=0;i<moves.length;i++)
            {
                if(moves[i]==tableMove)
                {
                    for(int j=i;j>0;j--)
                    {
                        moves[j]=moves[j-1];
                    }
                    moves[0]=tableMove;
                    break;
                }
            }
        }
        int bestMove = 0;
        int bestScore=-checkmate-1;
        int moveNumber = 0;
        int triedQuietCount=0;
        int[] triedQuietMoves =triedQuietMovesAt[Math.min(individualMovementCount,127)];
        boolean mover = board.whiteToMove;
        int legalMoveCount=0;
        for(int move:moves)
        {
            if(!pvNode&&!inCheck&&depth<=3&&moveNumber>0&&Move.unbitpackcapturePosition(move)&&Move.unbitpackpromotionPosition(move)==0&&staticExchange(board,move)<-100*depth)
            {
                continue;
            }
            int ownKingSquare=board.kingSquare[mover?0:1];
            int enemyKingSquare=board.kingSquare[mover?1:0];
            board.makeMove(move);
            if((inCheck||mightExposeKing(move,ownKingSquare))&&MoveGenerator.isKingAttacked(board,mover))
            {
                board.unmakeMove();
                continue;
            }
            legalMoveCount++;
            boolean checksOpponent=mightGiveCheck(move,enemyKingSquare)&&MoveGenerator.isKingAttacked(board,board.whiteToMove);
            boolean quietMove=!Move.unbitpackcapturePosition(move)&&Move.unbitpackpromotionPosition(move)==0;
            if(!pvNode&&!inCheck&&quietMove&&!checksOpponent&&moveNumber>0&&bestScore>-checkmate+1000)
            {
                int lateMoveCount=3+depth*depth;
                if(!improving)
                {
                    lateMoveCount=lateMoveCount/2+1;
                }
                if(depth<=4&&moveNumber>=lateMoveCount)
                {
                    board.unmakeMove();
                    moveNumber++;
                    continue;
                }
                if(depth<=3&&staticEval+futilityMargin[depth]<=alpha)
                {
                    board.unmakeMove();
                    moveNumber++;
                    continue;
                }
            }
            int score;
            boolean reduce = false;
            if(moveNumber>=3&&depth>3&&!inCheck&&quietMove&&!checksOpponent)
            {
                reduce =true;
            }
            if(moveNumber==0)
            {
                setKnownInCheck(individualMovementCount+1,checksOpponent?1:0);
                score=-negamax(board,depth-1,individualMovementCount+1,-beta,-alpha,true);
            }
            else
            {
                int reduction=0;
                if(reduce)
                {
                    reduction=lateMoveReduction[Math.min(depth,63)][Math.min(moveNumber,63)];
                    int moverIndex;
                    if(mover)
                    {
                        moverIndex=0;
                    }
                    else
                    {
                        moverIndex=1;
                    }
                    int moveHistoryScore=history[moverIndex][Move.unbitpackcurrentPosition(move)][Move.unbitpackchangedPosition(move)];
                    reduction=reduction-Math.max(-2,Math.min(2,moveHistoryScore/2000));
                    if(reduction<0)
                    {
                        reduction=0;
                    }
                    if(depth-1-reduction<1)
                    {
                        reduction=depth-2;
                    }
                }
                setKnownInCheck(individualMovementCount+1,checksOpponent?1:0);
                score=-negamax(board,depth-1-reduction,individualMovementCount+1,-alpha-1,-alpha,true);
                if(score>alpha&&reduction>0)
                {
                    setKnownInCheck(individualMovementCount+1,checksOpponent?1:0);
                    score=-negamax(board,depth-1,individualMovementCount+1,-alpha-1,-alpha,true);
                }
                if(score>alpha&&score<beta)
                {
                    setKnownInCheck(individualMovementCount+1,checksOpponent?1:0);
                    score=-negamax(board,depth-1,individualMovementCount+1,-beta,-alpha,true);
                }
            }
            board.unmakeMove();
            if(timesUp)
            {
                return 0;
            }
            if(score>=beta) {
                if (!Move.unbitpackcapturePosition(move))
                {
                    int sideIndex;
                    if (board.whiteToMove)
                    {
                        sideIndex = 0;
                    }
                    else
                    {
                        sideIndex = 1;
                    }
                    history[sideIndex][Move.unbitpackcurrentPosition(move)][Move.unbitpackchangedPosition(move)] += depth * depth;
                    for(int i=0;i<triedQuietCount;i++)
                    {
                        int triedMove= triedQuietMoves[i];
                        int triedFrom=Move.unbitpackcurrentPosition(triedMove);
                        int triedTo=Move.unbitpackchangedPosition(triedMove);
                        history[sideIndex][triedFrom][triedTo]=Math.max(history[sideIndex][triedFrom][triedTo]-depth*depth,-6000);
                    }
                    if(previousMove!=0)
                    {
                        counterMoves[Move.unbitpackcurrentPosition(previousMove)][Move.unbitpackchangedPosition(previousMove)]=move;
                    }
                    if (individualMovementCount < 128 && killers[individualMovementCount][0] != move)
                    {
                        killers[individualMovementCount][1] = killers[individualMovementCount][0];
                        killers[individualMovementCount][0] = move;
                    }
                }
                TranspositionTable.store(board.hash, depth,scoreToTable(score,individualMovementCount), TranspositionTable.lower, move);
                return score;
            }
            if(score>bestScore)
            {
                bestScore=score;
            }
            if(score>alpha)
            {
                alpha=score;
                bestMove=move;
            }
            if(!Move.unbitpackcapturePosition(move)&&Move.unbitpackpromotionPosition(move)==0&&triedQuietCount<256)
            {
                triedQuietMoves[triedQuietCount++]=move;
            }
            moveNumber++;
        }
        if(legalMoveCount==0)
        {
            if(inCheck)
            {
                return -checkmate+individualMovementCount;
            }
            return 0;
        }
        if(board.halfmoveClock>=100)
        {
            return 0;
        }
        int flag;
        if(bestScore>originalAlpha)
        {
            flag=TranspositionTable.exact;
        }
        else
        {
            flag=TranspositionTable.upper;
        }
        TranspositionTable.store(board.hash,depth,scoreToTable(bestScore,individualMovementCount),flag,bestMove);
        return bestScore;
    }
    private static final int[] value = {0,100,320,330,500,900,20000};
    private static final int[] futilityMargin={0,150,300,450};
    private static final int[] exchangeValue={0,100,320,330,500,900,20000};
    private static final int[] seeKnightRow = {-2,-2,-1,-1,1,1,2,2};
    private static final int[] seeKnightColumn={-1,1,-2,2,-2,2,-1,1};
    private static final int[] seeDiagonalRow = {-1,-1,1,1};
    private static final int[] seeDiagonalColumn={-1,1,-1,1};
    private static final int[] seeStraightRow = {-1,1,0,0};
    private static final int[] seeStraightColumn={0,0,-1,1};
    private static int[] wValue=new int[40];
    private static int cheapestAttacker(Board board,int target,boolean white,long removedSquare)
    {
        int row=target/8;
        int column=target%8;
        int sign;
        if(white)
        {
            sign=1;
        }
        else
        {
            sign=-1;
        }
        int pawnRow;
        if(white)
        {
            pawnRow=row+1;
        }
        else
        {
            pawnRow=row-1;
        }
        if(pawnRow>=0&&pawnRow<=7)
        {
            for(int columnChange=-1;columnChange<=1;columnChange+=2)
            {
                int pawnColumn=column+columnChange;
                if(pawnColumn<0||pawnColumn>7)
                {
                    continue;
                }
                int square=pawnRow*8+pawnColumn;
                if((removedSquare&(1L<<square))==0&&board.squares[square]==sign*Board.Pawn)
                {
                    return square;
                }
            }
        }
        for(int jump=0;jump<8;jump++)
        {
            int changedRow=row+seeKnightRow[jump];
            int changedColumn=column+seeKnightColumn[jump];
            if(changedRow<0||changedRow>7||changedColumn<0||changedColumn>7)
            {
                continue;
            }
            int square=changedRow*8+changedColumn;
            if((removedSquare&(1L<<square))==0&&board.squares[square]==sign*Board.Knight)
            {
                return square;
            }
        }
        int bestSquare=-1;
        int bestValue=Integer.MAX_VALUE;
        for(int direction=0;direction<8;direction++)
        {
            int rowChange;
            int columnChange;
            boolean diagonal=direction<4;
            if(diagonal)
            {
                rowChange=seeDiagonalRow[direction];
                columnChange=seeDiagonalColumn[direction];
            }
            else
            {
                rowChange=seeStraightRow[direction-4];
                columnChange=seeStraightColumn[direction-4];
            }
            int changedRow=row+rowChange;
            int changedColumn=column+columnChange;
            boolean firstStep=true;
            while(changedRow>=0&&changedRow<=7&&changedColumn>=0&&changedColumn<=7)
            {
                int square=changedRow*8+changedColumn;
                if((removedSquare&(1L<<square))==0&&board.squares[square]!=Board.EMPTY)
                {
                    int piece=board.squares[square];
                    if(piece*sign>0)
                    {
                        int pieceType=Math.abs(piece);
                        boolean canAttack=pieceType==Board.Queen||(diagonal&&pieceType==Board.Bishop)||(!diagonal&&pieceType==Board.Rook)||(firstStep&&pieceType==Board.King);
                        if(canAttack&&exchangeValue[pieceType]<bestValue)
                        {
                            bestValue=exchangeValue[pieceType];
                            bestSquare=square;
                        }
                    }
                    break;
                }
                firstStep=false;
                changedRow+=rowChange;
                changedColumn+=columnChange;
            }
        }
        return bestSquare;
    }
    private int staticExchange(Board board,int move)
    {
        int from=Move.unbitpackcurrentPosition(move);
        int to=Move.unbitpackchangedPosition(move);
        int victim=Math.abs(board.squares[to]);
        if(Move.unbitpackenPassant(move))
        {
            victim=Board.Pawn;
        }
        int attackerType=Math.abs(board.squares[from]);
        long removedSquares=1L<<from;
        boolean white=board.squares[from]<0;
        int exchangeDepth=0;
        wValue[0]=exchangeValue[victim];
        while(exchangeDepth<38)
        {
            exchangeDepth++;
            wValue[exchangeDepth]=exchangeValue[attackerType]-wValue[exchangeDepth-1];
            if(Math.max(-wValue[exchangeDepth-1],wValue[exchangeDepth])<0)
            {
                break;
            }
            int nextSquare=cheapestAttacker(board,to,white,removedSquares);
            if(nextSquare<0)
            {
                break;
            }
            attackerType=Math.abs(board.squares[nextSquare]);
            removedSquares|=1L<<nextSquare;
            white=!white;
        }
        while(--exchangeDepth>0)
        {
            wValue[exchangeDepth-1]=-Math.max(-wValue[exchangeDepth-1], wValue[exchangeDepth]);
        }
        return wValue[0];
    }
    private boolean loseMaterial(Board board,int move)
    {
        int from=Move.unbitpackcurrentPosition(move);
        int to=Move.unbitpackchangedPosition(move);
        int victim=Math.abs(board.squares[to]);
        if(Move.unbitpackenPassant(move))
        {
            victim=Board.Pawn;
        }
        int attackerType=Math.abs(board.squares[from]);
        if(exchangeValue[attackerType]<=exchangeValue[victim])
        {
            return false;
        }
        return staticExchange(board,move)<0;
    }

    private static boolean hasPieces(Board board)
    {
        for(int square=0;square<64;square++)
        {
            int piece = board.squares[square];
            if(piece==Board.EMPTY)
            {
                continue;
            }
            boolean pieceIsWhite =piece>0;
            if(pieceIsWhite!=board.whiteToMove)
            {
                continue;
            }
            int pieceType = Math.abs(piece);
            if(pieceType!=Board.Pawn&&pieceType!=Board.King)
            {
                return true;
            }
        }
        return false;
    }
    private int scoreMove(Board board,int move,int individualMovementCount)
    {
        int score=0;
        int from=Move.unbitpackcurrentPosition(move);
        int to=Move.unbitpackchangedPosition(move);
        if(Move.unbitpackcapturePosition(move))
        {
            int victim =Math.abs(board.squares[to]);
            if(Move.unbitpackenPassant(move))
            {
                victim = Board.Pawn;
            }
            int attacker = Math.abs(board.squares[from]);
            score+=10000+value[victim]*10-value[attacker]/10;
            if(loseMaterial(board,move))
            {
                score-=20000;
            }
        }
        int promo = Move.unbitpackpromotionPosition(move);
        if(promo!=0)
        {
            score+=9000+value[promo];
        }
        boolean isKiller = false;

        if(individualMovementCount<128)
        {
            if(move==killers[individualMovementCount][0])
            {
                score+=8000;
                isKiller=true;
            }
            else if(move==killers[individualMovementCount][1])
            {
                score+=7000;
                isKiller=true;
            }
        }
        if(!isKiller&&!Move.unbitpackcapturePosition(move)&&promo==0)
        {
            int sideIndex;
            if(board.whiteToMove)
            {
                sideIndex=0;
            }
            else
            {
                sideIndex=1;
            }
            if(move==currentCounterMove)
            {
                score+=6500;
            }
            else
            {
                score+=Math.min(history[sideIndex][from][to],6000);
            }
        }
        return score;
    }
    private final int [] orderScores =new int[256];
    private int[] orderMoves(Board board,int[] moves,int individualMovementCount)
    {
        int n=moves.length;
        int[] scores=orderScores;
        for(int i=0; i<n;i++)
        {
            scores[i] = scoreMove(board,moves[i],individualMovementCount);
        }
        for(int i=1;i<n;i++)
        {
            int m = moves[i];
            int s =scores[i];
            int j=i-1;
            while(j>=0&&scores[j]<s)
            {
                moves[j+1]=moves[j];
                scores[j+1]=scores[j];
                j--;
            }
            moves[j+1]=m;
            scores[j+1]=s;
        }
        return moves;
    }
    public static boolean fixedMoveTime=false;
    public static long softTimeMs=0;
    private long doomsdayClock=Long.MAX_VALUE;
    private boolean timesUp = false;
    public static volatile int safeMove=0;
    public static volatile boolean stopRequested=false;
    private long positionSeen = 0;
    private static final int[][] lateMoveReduction=new int[64][64];
    static
    {
        for(int depth=1;depth<64;depth++)
        {
            for(int moveNumber=1;moveNumber<64;moveNumber++)
            {
                lateMoveReduction[depth][moveNumber]=(int)(0.75+Math.log(depth)*Math.log(moveNumber)/2.25);
            }
        }
    }
    private final int [][] killers=new int[128][2];
    private final int[][][] history = new int[2][64][64];
    private static final int noEval=Integer.MIN_VALUE;
    private final int[] staticEvalAt=new int[130];
    private static boolean onSameLine(int firstSquare,int secondSquare)
    {
        int firstRow=firstSquare/8;
        int firstColumn=firstSquare%8;
        int secondRow=secondSquare/8;
        int secondColumn=secondSquare%8;
        return firstRow==secondRow||firstColumn==secondColumn||Math.abs(firstRow-secondRow)==Math.abs(firstColumn-secondColumn);
    }
    private static boolean mightExposeKing(int move,int ownKingSquare)
    {
        int from=Move.unbitpackcurrentPosition(move);
        return from==ownKingSquare||Move.unbitpackenPassant(move)||onSameLine(from,ownKingSquare);
    }
    private static boolean mightGiveCheck(int move,int enemyKingSquare)
    {
        if(enemyKingSquare==-1)
        {
            return false;
        }
        if(Move.unbitpackenPassant(move)||Move.unbitpackcastlingPosition(move))
        {
            return true;
        }
        int from=Move.unbitpackcurrentPosition(move);
        int to=Move.unbitpackchangedPosition(move);
        if(onSameLine(from,enemyKingSquare)||onSameLine(to,enemyKingSquare))
        {
            return true;
        }
        int rowGap=Math.abs(to/8-enemyKingSquare/8);
        int columnGap=Math.abs(to%8-enemyKingSquare%8);
        return rowGap*columnGap==2;
    }
    private final int[] knownInCheckAt=new int[140];
    {
        java.util.Arrays.fill(knownInCheckAt,-1);
    }
    private boolean takeKnownInCheck(Board board,int individualMovementCount)
    {
        if(individualMovementCount<140)
        {
            int known=knownInCheckAt[individualMovementCount];
            knownInCheckAt[individualMovementCount]=-1;
            if(known!=-1)
            {
                return known==1;
            }
        }
        return MoveGenerator.isKingAttacked(board,board.whiteToMove);
    }
    private void setKnownInCheck(int individualMovementCount,int known)
    {
        if(individualMovementCount<140)
        {
            knownInCheckAt[individualMovementCount]=known;
        }
    }
    private final int[][] triedQuietMovesAt=new int[128][256];
    private final int[][] counterMoves=new int[64][64];
    private int currentCounterMove=0;
    private void peekAtClock()
    {
        if(stopRequested)
        {
            timesUp=true;
            return;
        }
        if((positionSeen&2047)==0&&System.nanoTime()>=doomsdayClock)
        {
            timesUp=true;
        }
    }
    public int search(Board board,int maxDepth, long timeMs) {
        doomsdayClock = System.nanoTime() + timeMs*1000000L;
        timesUp = false;
        positionSeen = 0;
        for(int i=0;i<128;i++)
        {
            killers[i][0]=0;
            killers[i][1]=0;
        }
        for(int side=0;side<2;side++)
        {
            for(int from=0;from<64;from++)
            {
                for(int to=0;to<64;to++)
                {
                    history[side][from][to]=history[side][from][to]/2;
                }
            }
        }
        long startTime = System.nanoTime();
        long budgetNanos=timeMs*1000000L;
        if(softTimeMs>0&&softTimeMs<timeMs)
        {
            budgetNanos=softTimeMs*1000000L;
        }
        staticEvalAt[0]=noEval;
        staticEvalAt[1]=noEval;
        int[] rootMoves = MoveGenerator.generateLegalMoves(board);
        if (rootMoves.length == 0) {
            return 0;
        }
        for(int from=0;from<64;from++)
        {
            for(int to=0;to<64;to++)
            {
                counterMoves[from][to]=0;
            }
        }
        currentCounterMove=0;
        rootMoves = orderMoves(board, rootMoves,0);
        int bestMove = rootMoves[0];
        if(!helper)
        {
            safeMove=bestMove;
        }
        int previousScore=0;
        int stableCount=0;
        int previousBestMove=0;
        boolean scoreFell=false;
        for (int depth = 1; depth <= maxDepth; depth++)
        {
            long softPercent=50;
            if(depth>6&&stableCount>=3)
            {
                softPercent=30;
            }
            if(depth>6&&stableCount==0)
            {
                softPercent=70;
            }
            if(depth>6&&scoreFell)
            {
                softPercent=100;
            }
            if(!fixedMoveTime&&depth>1&&System.nanoTime()-startTime>budgetNanos*softPercent/100)
            {
                break;
            }
            int window=50;
            int alpha;
            int beta;
            if(depth>=5)
            {
                alpha=previousScore-window;
                beta=previousScore+window;
            }
            else
            {
                alpha = -checkmate - 1;
                beta = checkmate + 1;
            }
            int sofarBest=0;
            while(true)
            {
                sofarBest = 0;
                boolean failHigh = false;
                boolean firstRootMove = true;
                for (int move : rootMoves)
                {
                    board.makeMove(move);
                    int score;
                    if (firstRootMove) {
                        score = -negamax(board, depth - 1, 1, -beta, -alpha, true);
                        firstRootMove = false;
                    } else {
                        score = -negamax(board, depth - 1, 1, -alpha - 1, -alpha, true);
                        if (score > alpha && !timesUp) {
                            score = -negamax(board, depth - 1, 1, -beta, -alpha, true);
                        }
                    }
                    board.unmakeMove();
                    if (timesUp) {
                        if (sofarBest != 0) {
                            bestMove = sofarBest;
                        }
                        break;
                    }
                    if (score > alpha) {
                        alpha = score;
                        sofarBest = move;
                    }
                    if (score >= beta) {
                        failHigh = true;
                        break;
                    }
                }
                if (timesUp) {
                    break;
                }
                if (failHigh || sofarBest == 0) {
                    window = window * 4;
                    if (window >= 1000) {
                        alpha = -checkmate - 1;
                        beta = checkmate + 1;
                    } else {
                        alpha = previousScore - window;
                        beta = previousScore + window;
                    }
                    continue;
                }
                break;
            }
            if(timesUp)
            {
                break;
            }
            bestMove = sofarBest;
            if(!helper)
            {
                safeMove=bestMove;
            }
            scoreFell=depth>1&&alpha<previousScore-20;
            previousScore=alpha;
            if(bestMove==previousBestMove)
            {
                stableCount++;
            }
            else
            {
                stableCount=0;
            }
            previousBestMove=bestMove;
            if(!helper)
            {
                System.out.println("info depth " + depth + " score " + scoreToUci(alpha) + " nodes " + positionSeen + " time " + (System.nanoTime() - startTime) / 1000000 + " pv " + principalVariation(board, bestMove, depth));
            }
            for (int i = 0; i < rootMoves.length; i++) {
                if (rootMoves[i] == bestMove) {
                    for (int j = i; j > 0; j--) {
                        rootMoves[j] = rootMoves[j - 1];
                    }
                    rootMoves[0] = bestMove;
                    break;
                }
            }
        }
        return bestMove;
    }
    private static String principalVariation(Board board,int firstMove,int depth)
    {
        String line=Uci.moveToString(firstMove);
        board.makeMove(firstMove);
        int individualMovementCount=1;
        while(individualMovementCount<depth)
        {
            int transpositionTableIndex=TranspositionTable.find(board.hash);
            if(transpositionTableIndex==-1)
            {
                break;
            }
            int tableMove=TranspositionTable.moveAt(transpositionTableIndex);
            boolean isLegal=false;
            for(int move:MoveGenerator.generateLegalMoves(board))
            {
                if(move==tableMove)
                {
                    isLegal=true;
                    break;
                }
            }
            if(!isLegal)
            {
                break;
            }
            line=line+" "+Uci.moveToString(tableMove);
            board.makeMove(tableMove);
            individualMovementCount++;
        }
        for(int i=0;i<individualMovementCount;i++)
        {
            board.unmakeMove();
        }
        return line;
    }
    private static String scoreToUci(int score)
    {
        if(score>checkmate-1000)
        {
            int individualMovementCount=checkmate-score;
            return"mate "+(individualMovementCount+1)/2;
        }
        if(score<-checkmate+1000)
        {
            int individualMovementCount=checkmate+score;
            return"mate -"+(individualMovementCount+1)/2;
        }
        return "cp "+score;
    }
    private int quiescence(Board board,int alpha,int beta,int individualMovementCount)
    {
        positionSeen++;
        boolean inCheck=takeKnownInCheck(board,individualMovementCount);
        peekAtClock();
        if(timesUp)
        {
            return 0;
        }
        int bestScore=-checkmate-1;
        int standPat=0;
        if(!inCheck)
        {
            int currentEval = Evaluator.evaluate(board);
            standPat=currentEval;
            if(currentEval>=beta)
            {
                return currentEval;
            }
            bestScore=currentEval;
            if(currentEval>alpha)
            {
                alpha = currentEval;
            }
        }
        int[] moves = MoveGenerator.generateMove(board,!inCheck);
        currentCounterMove=0;
        moves=orderMoves(board,moves,128);
        boolean mover = board.whiteToMove;
        int legalMoveCount = 0;
        for(int move:moves)
        {
            if(!inCheck&&Move.unbitpackpromotionPosition(move)==0)
            {
                int victim=Math.abs(board.squares[Move.unbitpackchangedPosition(move)]);
                if(Move.unbitpackenPassant(move))
                {
                    victim=Board.Pawn;
                }
                if(standPat+value[victim]+200<=alpha)
                {
                    continue;
                }
                if(loseMaterial(board,move))
                {
                    continue;
                }
            }
            int ownKingSquare=board.kingSquare[mover?0:1];
            int enemyKingSquare=board.kingSquare[mover?1:0];
            board.makeMove(move);
            if((inCheck||mightExposeKing(move,ownKingSquare))&&MoveGenerator.isKingAttacked(board,mover))
            {
                board.unmakeMove();
                continue;
            }
            legalMoveCount++;
            if(!mightGiveCheck(move,enemyKingSquare))
            {
                setKnownInCheck(individualMovementCount+1,0);
            }
            int score = -quiescence(board,-beta,-alpha,individualMovementCount+1);
            board.unmakeMove();
            if(timesUp)
            {
                return 0;
            }
            if(score>=beta)
            {
                return score;
            }
            if(score>bestScore)
            {
                bestScore=score;
            }
            if(score>alpha)
            {
                alpha=score;
            }
        }
        if(inCheck&&legalMoveCount==0)
        {
            return -checkmate+individualMovementCount;
        }
        return bestScore;
    }
    private static int scoreToTable(int score,int individualmovementcount)
    {
        if(score>checkmate-1000)
        {
            return score+individualmovementcount;
        }
        if(score<-checkmate+1000)
        {
            return score-individualmovementcount;
        }
        return score;
    }
    private static int scorefromTable(int score,int individualmovementcount)
    {
        if(score>checkmate-1000)
        {
            return  score-individualmovementcount;
        }
        if(score<-checkmate+1000)
        {
            return score+individualmovementcount;
        }
        return score;
    }
}
