public class Test
{
    public static void main(String[] args)
    {
        Board start = Board.fromFen(Board.STARTING_POSITION);
        long t = System.nanoTime();
        System.out.println("start d5: " + Perft.perft(start, 5) + " (expect 4865609) " + (System.nanoTime() - t) / 1000000 + "ms");

        Board kiwi = Board.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        t = System.nanoTime();
        System.out.println("kiwipete d4: " + Perft.perft(kiwi, 4) + " (expect 4085603) " + (System.nanoTime() - t) / 1000000 + "ms");
    }
}