public class Test
{
    public static void main(String[] args)
    {
        String[] restoreFens = {
                Board.STARTING_POSITION,
                "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
                "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1",
                "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1",
                "1n2k3/P7/8/8/8/8/8/4K3 w - - 0 1"
        };
        for (String restoreFen : restoreFens)
        {
            Board restoreBoard = Board.fromFen(restoreFen);
            String restoreStart = restoreBoard.toFen();
            boolean allRestored = true;
            for (int restoreMove : MoveGenerator.generateMove(restoreBoard))
            {
                restoreBoard.makeMove(restoreMove);
                restoreBoard.unmakeMove();
                if (!restoreBoard.toFen().equals(restoreStart))
                {
                    allRestored = false;
                    System.out.println("failed: " + Board.squareNotation(Move.unbitpackcurrentPosition(restoreMove))
                            + Board.squareNotation(Move.unbitpackchangedPosition(restoreMove)));
                }
            }
            System.out.println(restoreFen + "  allRestored=" + allRestored);
        }
    }
}