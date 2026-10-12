public class Main
{
    public static void main(String[] args)
    {
        TranspositionTable.store(12345L, 6, 150, TranspositionTable.exact, 777);

        int found = TranspositionTable.find(12345L);
        System.out.println("found: " + (found != -1));
        System.out.println("depth: " + TranspositionTable.depthAt(found));
        System.out.println("score: " + TranspositionTable.scoreAt(found));
        System.out.println("flag: " + TranspositionTable.flagAt(found));
        System.out.println("move: " + TranspositionTable.moveAt(found));
        System.out.println("unknown hash: " + TranspositionTable.find(99999L));
    }
}