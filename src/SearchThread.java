public class SearchThread extends Thread
{
    private final Board board;
    private final int depth;
    private final long timeMs;
    public SearchThread(Board board,int depth,long timeMs)
    {
        this.board=board;
        this.depth=depth;
        this.timeMs=timeMs;
    }
    public void run()
    {
        int best=Search.search(board,depth,timeMs);
        if(best==0)
        {
            System.out.println("bestmove 0000");
        }
        else
        {
            System.out.println("bestmove "+Uci.moveToString(best));
        }
    }
}
