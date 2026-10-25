import java.io.BufferedReader;
import java.io.InputStreamReader;
public class Uci
{
    private static Board board=Board.fromFen(Board.STARTING_POSITION);
    private static String[] lastPosition={"position","startpos"};
    public static String moveToString(int move)
    {
        String viscabarca=Board.squareNotation(Move.unbitpackcurrentPosition(move))+Board.squareNotation(Move.unbitpackchangedPosition(move));
        int promotionLetter = Move.unbitpackpromotionPosition(move);
        if(promotionLetter!=0)
        {
            viscabarca+=Character.toLowerCase(Board.pieceToCHar(promotionLetter));
        }
        return viscabarca;
    }
    private static int StringToMove(String text)
    {
        for(int move:MoveGenerator.generateLegalMoves(board))
        {
            if(moveToString(move).equals(text))
            {
                return move;
            }
        }
        return 0;
    }
    private static void handlePosition(String[] t)
    {
        int i=1;
        if(t[i].equals("startpos"))
        {
            board = Board.fromFen(Board.STARTING_POSITION);
            i++;
        }
        else if(t[i].equals("fen"))
        {
            StringBuilder fen =new StringBuilder();
            i++;
            while(i<t.length&&!t[i].equals("moves"))
            {
                fen.append(t[i]).append(" ");
                i++;
            }
            board=Board.fromFen(fen.toString());
        }
        if(i<t.length&&t[i].equals("moves"))
        {
            for(i++;i<t.length;i++)
            {
                int m=StringToMove(t[i]);
                if(m==0)
                {
                    System.out.println("info string illegal move "+t[i]);
                    break;
                }
                board.makeMove(m);
            }
        }

    }
    private static void handleGo(String[] t)
    {
        long timeMs=1000;
        Search.fixedMoveTime=false;
        Search.softTimeMs=0;
        long incrementMs = 0;
        int depth=64;
        long myTimeMs=-1;
        int moveToGo=0;
        for(int i=1;i<t.length-1;i++)
        {
            if(t[i].equals("movetime"))
            {
                timeMs= Long.parseLong(t[i+1]);
                Search.fixedMoveTime=true;
            }
            if(t[i].equals("depth"))
            {
                depth = Integer.parseInt(t[i+1]);
                timeMs = 100000;
            }
            if(board.whiteToMove&&t[i].equals("wtime"))
            {
                myTimeMs = Long.parseLong(t[i+1]);
            }
            if(!board.whiteToMove&&t[i].equals("btime"))
            {
                myTimeMs=Long.parseLong(t[i+1]);
            }
            if(t[i].equals("movestogo"))
            {
                moveToGo=Integer.parseInt(t[i+1]);
            }
            if(board.whiteToMove&&t[i].equals("winc"))
            {
                incrementMs = Long.parseLong(t[i+1]);
            }
            if(!board.whiteToMove&&t[i].equals("binc"))
            {
                incrementMs=Long.parseLong(t[i+1]);
            }

        }
        if(myTimeMs>=0)
        {
            int divisor=14;
            if(moveToGo>0)
            {
                divisor=Math.min(moveToGo+1,14);
            }
            timeMs=myTimeMs/divisor;
            int gamePhase=0;
            for(int square=0;square<64;square++)
            {
                int absoluteValue=Math.abs(board.squares[square]);
                if(absoluteValue==Board.Knight||absoluteValue==Board.Bishop)
                {
                    gamePhase+=1;
                }
                if(absoluteValue==Board.Rook)
                {
                    gamePhase+=2;
                }
                if(absoluteValue==Board.Queen)
                {
                    gamePhase+=4;
                }
            }
            if(gamePhase<=12)
            {
                timeMs=timeMs*4/3;
            }
        }
        timeMs = timeMs+incrementMs*3/4;
        if(myTimeMs>=0&&timeMs>myTimeMs/2)
        {
            timeMs=myTimeMs/2;
        }
        timeMs=timeMs-100;
        if(timeMs<20)
        {
            timeMs=20;
        }
        if(myTimeMs>=0&&!Search.fixedMoveTime)
        {
            Search.softTimeMs=timeMs;
            long hardTimeMs=Math.min(timeMs*3,myTimeMs/4-100);
            if(hardTimeMs>timeMs)
            {
                timeMs=hardTimeMs;
            }
        }
        for(String word:t)
        {
            if(word.equals("infinite"))
            {
                depth=64;
                timeMs=24L*60*60*1000;
                Search.fixedMoveTime=true;
            }
        }
        Search.stopRequested=false;
        Board mainBoard=board;
        Board[] helperBoards=new Board[SearchThread.helperSearches.length];
        for(int i=0;i< helperBoards.length;i++)
        {
            handlePosition(lastPosition);
            helperBoards[i]=board;
        }
        board=mainBoard;
        searchThread=new SearchThread(board,helperBoards,depth,timeMs);
        searchThread.start();
    }
    private static Thread searchThread=null;
    private static void waitForSearch()
    {
        if(searchThread!=null)
        {
            try
            {
                searchThread.join();
            }
            catch(InterruptedException e)
            {
            }
            searchThread=null;
        }
    }
    public static void main(String[] args) throws Exception
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = in.readLine();
        while(line!=null)
        {
            String[] t = line.trim().split("\\s+");
            String command=t[0];
            if(command.equals("uci"))
            {
                System.out.println("id name Gambit");
                System.out.println("id author Shawn the Goat");
                System.out.println("option name Hash type spin default 16 min 1 max 1024");
                System.out.println("option name Threads type spin default 1 min 1 max 64");
                System.out.println("uciok");
            }
            else if(command.equals("setoption"))
            {
                waitForSearch();
                if(t.length>=5&&t[2].equals("Hash"))
                {
                    TranspositionTable.resize(Integer.parseInt(t[4]));
                }
                if(t.length>=5&&t[2].equals("Threads"))
                {
                    SearchThread.setThread(Integer.parseInt(t[4]));
                }
            }
            else if(command.equals("isready"))
            {
                System.out.println("readyok");
            }
            else if(command.equals("ucinewgame"))
            {
                waitForSearch();
                board = Board.fromFen(Board.STARTING_POSITION);
                TranspositionTable.clear();
            }
            else if(command.equals("position"))
            {
                waitForSearch();
                lastPosition=t;
                handlePosition(t);
            }
            else if(command.equals("go"))
            {
                waitForSearch();
                handleGo(t);
            }
            else if(command.equals("stop"))
            {
                Search.stopRequested=true;
                waitForSearch();
            }
            else if(command.equals("quit"))
            {
                Search.stopRequested=true;
                waitForSearch();
                return;
            }
            line =in.readLine();
        }
        waitForSearch();
    }
}


