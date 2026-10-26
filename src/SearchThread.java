public class SearchThread extends Thread
{
    private final Board board;
    private final Board[] helperBoards;
    public static final Search mainSearch=new Search(false);
    public static Search[] helperSearches=new Search[0];
    static
    {
        setThread(1);
    }
    public static void setThread(int threads)
    {
        helperSearches=new Search[Math.max(0,threads-1)];
        for(int i=0;i<helperSearches.length;i++)
        {
            helperSearches[i]=new Search(true);
        }
    }
    private final int depth;
    private final long timeMs;
    public SearchThread(Board board,Board[] helperBoards,int depth,long timeMs)
    {
        this.board=board;
        this.helperBoards=helperBoards;
        this.depth=depth;
        this.timeMs=timeMs;
    }
    public void run()
    {
        Thread[] helpers=new Thread[helperBoards.length];
        for(int i=0;i< helpers.length;i++)
        {
            final int helperIndex=i;
            helpers[i]=new Thread(()->
            {
                try
                {
                    helperSearches[helperIndex].search(helperBoards[helperIndex],depth,timeMs);
                }
                catch (Exception e)
                {
                }
            });
            helpers[i].start();
        }
        int best;
        try
        {
            best=mainSearch.search(board,depth,timeMs);
        }
        catch(Exception e)
        {
            System.out.println("info string search error "+e);
            best=Search.safeMove;
        }
        while(Search.pondering&&!Search.stopRequested)
        {
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException e){}
        }
        Search.stopRequested=true;
        for(Thread helper:helpers)
        {
            try
            {
                helper.join();
            }
            catch (InterruptedException e){}
        }
        if(best==0)
        {
            System.out.println("bestmove 0000");
        }
        else
        {
            int ponderMove=Search.ponderMove(board,best);
            if(ponderMove==0)
            {
                System.out.println("bestmove "+Uci.moveToString(best));
            }
            else
            {
                System.out.println("bestmove "+Uci.moveToString(best)+" ponder "+Uci.moveToString(ponderMove));
            }
        }
    }
}
