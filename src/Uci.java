import java.io.BufferedReader;
import java.io.InputStreamReader;
public class Uci
{
    private static Board board=Board.fromFen(Board.STARTING_POSITION);
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
                timeMs = Long.parseLong(t[i+1]);
            }
            if(!board.whiteToMove&&t[i].equals("btime"))
            {
                timeMs=Long.parseLong(t[i+1]);
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
            int divisor=30;
            if(moveToGo>0)
            {
                divisor=Math.min(moveToGo+1,30);
            }
            timeMs=myTimeMs/divisor;
        }
        timeMs = timeMs+incrementMs/2;
        timeMs=timeMs-30;
        if(timeMs<20)
        {
            timeMs=20;
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
        searchThread=new SearchThread(board,depth,timeMs);
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
                System.out.println("uciok");
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


