public class Search
{
    private static final int checkmate = 100000;
    public static int negamax(Board board,int depth,int individualMovementCount,int alpha,int beta,boolean nullAllowed)
    {
        positionSeen++;
        peekAtClock();
        if(timesUp)
        {
            return 0;
        }
        if(board.isRepetition())
        {
            return 0;
        }
        boolean inCheck = MoveGenerator.isKingAttacked(board,board.whiteToMove);
        if(inCheck&&individualMovementCount<100)
        {
            depth++;
        }
        if(depth==0)
        {
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
        int staticEval=0;
        if(!inCheck)
        {
            staticEval=Evaluator.evaluate(board);
        }
        boolean pvNode=beta-alpha>1;
        if(!pvNode&&!inCheck&&depth<=6&&beta<checkmate-1000&&beta>-checkmate+1000&&staticEval-120*depth>=beta)
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
        boolean mover = board.whiteToMove;
        int legalMoveCount=0;
        for(int move:moves)
        {
            board.makeMove(move);
            if(MoveGenerator.isKingAttacked(board,mover))
            {
                board.unmakeMove();
                continue;
            }
            legalMoveCount++;
            int score;
            boolean reduce = false;
            if(moveNumber>=3&&depth>3&&!inCheck&&!Move.unbitpackcapturePosition(move)&&Move.unbitpackpromotionPosition(move)==0&&!MoveGenerator.isKingAttacked(board,board.whiteToMove))
            {
                reduce =true;
            }
            if(moveNumber==0)
            {
                score=-negamax(board,depth-1,individualMovementCount+1,-beta,-alpha,true);
            }
            else
            {
                int reduction=0;
                if(reduce)
                {
                    reduction=lateMoveReduction[Math.min(depth,63)][Math.min(moveNumber,63)];
                    if(depth-1-reduction<1)
                    {
                        reduction=depth-2;
                    }
                }
                score=-negamax(board,depth-1-reduction,individualMovementCount+1,-alpha-1,-alpha,true);
                if(score>alpha&&reduction>0)
                {
                    score=-negamax(board,depth-1,individualMovementCount+1,-alpha-1,-alpha,true);
                }
                if(score>alpha&&score<beta)
                {
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
    private static int scoreMove(Board board,int move,int individualMovementCount)
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
            score+=Math.min(history[sideIndex][from][to],6000);
        }
        return score;
    }
    private static final int [] orderScores =new int[256];
    private static int[] orderMoves(Board board,int[] moves,int individualMovementCount)
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
    private static long doomsdayClock=Long.MAX_VALUE;
    private static boolean timesUp = false;
    public static volatile boolean stopRequested=false;
    private static long positionSeen = 0;
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
    private static final int [][] killers=new int[128][2];
    private static final int[][][] history = new int[2][64][64];
    private static void peekAtClock()
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
    public static int search(Board board,int maxDepth, long timeMs) {
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
                    history[side][from][to]=0;
                }
            }
        }
        long startTime = System.nanoTime();
        long budgetNanos=timeMs*1000000L;
        int[] rootMoves = MoveGenerator.generateLegalMoves(board);
        if (rootMoves.length == 0) {
            return 0;
        }
        rootMoves = orderMoves(board, rootMoves,0);
        int bestMove = rootMoves[0];
        int previousScore=0;
        for (int depth = 1; depth <= maxDepth; depth++)
        {
            if(!fixedMoveTime&&depth>1&&System.nanoTime()-startTime>budgetNanos/2)
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
            previousScore=alpha;
            System.out.println("info depth "+depth+" score "+scoreToUci(alpha)+" nodes "+positionSeen+" time "+(System.nanoTime()-startTime)/1000000+" pv "+principalVariation(board,bestMove,depth));
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
    private static int quiescence(Board board,int alpha,int beta,int individualMovementCount)
    {
        positionSeen++;
        peekAtClock();
        if(timesUp)
        {
            return 0;
        }
        boolean inCheck = MoveGenerator.isKingAttacked(board,board.whiteToMove);
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
            }
            board.makeMove(move);
            if(MoveGenerator.isKingAttacked(board,mover))
            {
                board.unmakeMove();
                continue;
            }
            legalMoveCount++;
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
