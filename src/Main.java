public class Main
{
    static int failures = 0;

    static void checkBoard(Board bd, String where)
    {
        int wk = -1;
        int bk = -1;
        for(int s=0;s<64;s++)
        {
            if(bd.squares[s]==Board.King) wk = s;
            if(bd.squares[s]==-Board.King) bk = s;
        }
        if(bd.kingSquare[0]!=wk || bd.kingSquare[1]!=bk)
        {
            throw new IllegalStateException("king cache wrong " + where + " fen: " + bd.toFen());
        }
        if(bd.hash!=bd.computeHash())
        {
            throw new IllegalStateException("hash wrong " + where + " fen: " + bd.toFen());
        }
    }

    static long walk(Board bd, int depth)
    {
        if(depth==0)
        {
            return 1;
        }
        long nodes = 0;
        for(int move : MoveGenerator.generateLegalMoves(bd))
        {
            String name = Board.squareNotation(Move.unbitpackcurrentPosition(move))
                    + Board.squareNotation(Move.unbitpackchangedPosition(move));
            String before = bd.toFen();
            bd.makeMove(move);
            checkBoard(bd, "after make " + name);
            nodes += walk(bd, depth-1);
            bd.unmakeMove();
            checkBoard(bd, "after unmake " + name);
            if(!bd.toFen().equals(before))
            {
                throw new IllegalStateException("unmake did not restore position after " + name
                        + " expected " + before + " got " + bd.toFen());
            }
        }
        return nodes;
    }

    static void test(String name, String fen, int depth, long expected)
    {
        Board bd = Board.fromFen(fen);
        long start = System.nanoTime();
        long nodes = walk(bd, depth);
        long ms = (System.nanoTime() - start) / 1000000;
        String result = (nodes == expected) ? "PASS" : "FAIL";
        if(nodes != expected)
        {
            failures++;
        }
        System.out.println(result + "  " + name + " depth " + depth + ": " + nodes + " (expected " + expected + ")  " + ms + "ms");
    }

    static void speed(String name, String fen, int depth, long expected)
    {
        Board bd = Board.fromFen(fen);
        long start = System.nanoTime();
        long nodes = Perft.perft(bd, depth);
        long ms = (System.nanoTime() - start) / 1000000;
        String result = (nodes == expected) ? "PASS" : "FAIL";
        if(nodes != expected)
        {
            failures++;
        }
        System.out.println(result + "  " + name + " depth " + depth + ": " + nodes + "  " + ms + "ms  (speed test)");
    }

    public static void main(String[] args)
    {
        System.out.println("=== Correctness: perft + king cache + hash + unmake checks ===");
        try
        {
            test("start position", Board.STARTING_POSITION, 4, 197281);
            test("kiwipete", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97862);
            test("endgame/en passant", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 4, 43238);
            test("promotions", "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3, 9467);
            test("position 5", "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379);
        }
        catch(IllegalStateException e)
        {
            failures++;
            System.out.println("FAIL  " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Speed (plain perft, no extra checks) ===");
        speed("start position", Board.STARTING_POSITION, 5, 4865609);
        speed("kiwipete", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 4, 4085603);

        System.out.println();
        System.out.println("=== FEN round trip ===");
        String[] fens = {
                Board.STARTING_POSITION,
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"
        };
        for(String fen : fens)
        {
            String out = Board.fromFen(fen).toFen();
            if(out.equals(fen))
            {
                System.out.println("PASS  " + fen);
            }
            else
            {
                failures++;
                System.out.println("FAIL  in:  " + fen);
                System.out.println("      out: " + out);
            }
        }

        System.out.println();
        System.out.println("=== Captures-only generator ===");
        String[] capFens = {
                Board.STARTING_POSITION,
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
                "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
                "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"
        };
        for(String fen : capFens)
        {
            Board cb = Board.fromFen(fen);
            java.util.Set<Integer> expected = new java.util.HashSet<>();
            for(int m : MoveGenerator.generateLegalMoves(cb))
            {
                if(Move.unbitpackcapturePosition(m) || Move.unbitpackpromotionPosition(m)!=0)
                {
                    expected.add(m);
                }
            }
            java.util.Set<Integer> got = new java.util.HashSet<>();
            for(int m : MoveGenerator.generateLegalCaptures(cb))
            {
                got.add(m);
            }
            if(expected.equals(got))
            {
                System.out.println("PASS  " + got.size() + " captures/promotions  " + fen);
            }
            else
            {
                failures++;
                System.out.println("FAIL  expected " + expected.size() + " got " + got.size() + "  " + fen);
            }
        }

        System.out.println();
        System.out.println("=== Evaluation sanity (expected values in brackets) ===");
        String[] evalFens = {
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1",
                "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
                "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"
        };
        String[] evalHints = {
                "[near 0: start position]",
                "[about -900: White has no queen]",
                "[about +900: Black has no queen]",
                "[near 0: start position, Black to move]",
                "[about +100 or more: White has an extra pawn]"
        };
        for(int i = 0; i < evalFens.length; i++)
        {
            System.out.println(Evaluator.evaluate(Board.fromFen(evalFens[i])) + "   " + evalHints[i] + "   " + evalFens[i]);
        }

        System.out.println();
        System.out.println("=== Search: finds mate in 1 and does not crash ===");
        TranspositionTable.clear();
        Board mate = Board.fromFen("6k1/5ppp/8/8/8/8/5PPP/R5K1 w - - 0 1");
        int best = Search.search(mate, 4, 1000);
        String bestName = Board.squareNotation(Move.unbitpackcurrentPosition(best))
                + Board.squareNotation(Move.unbitpackchangedPosition(best));
        if(bestName.equals("a1a8"))
        {
            System.out.println("PASS  found a1a8");
        }
        else
        {
            failures++;
            System.out.println("FAIL  expected a1a8, got " + bestName);
        }

        System.out.println();
        System.out.println();
        System.out.println("=== Search diagnostic: same position via moves vs via FEN ===");
        Board viaMoves = Board.fromFen(Board.STARTING_POSITION);
        for(int m : MoveGenerator.generateLegalMoves(viaMoves))
        {
            String name = Board.squareNotation(Move.unbitpackcurrentPosition(m))
                    + Board.squareNotation(Move.unbitpackchangedPosition(m));
            if(name.equals("e2e4"))
            {
                viaMoves.makeMove(m);
                break;
            }
        }
        Board viaFen = Board.fromFen(viaMoves.toFen());

        int repChildren = 0;
        int totalChildren = 0;
        for(int m : MoveGenerator.generateLegalMoves(viaMoves))
        {
            viaMoves.makeMove(m);
            totalChildren++;
            if(viaMoves.isRepetition())
            {
                repChildren++;
            }
            viaMoves.unmakeMove();
        }
        System.out.println("children flagged as repetition (via moves): " + repChildren + " of " + totalChildren
                + "   halfmoveClock=" + viaMoves.halfmoveClock);

        System.out.println("--- search via moves ---");
        TranspositionTable.clear();
        Search.search(viaMoves, 5, 3000);
        System.out.println("--- search via FEN ---");
        TranspositionTable.clear();
        Search.search(viaFen, 5, 3000);
        if(failures == 0)
        {
            System.out.println("ALL TESTS PASSED");
        }
        else
        {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}