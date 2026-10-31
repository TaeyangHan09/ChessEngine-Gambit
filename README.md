
# ChessEngine_Gambit 1.0

A UCI chess engine written in Java by Taeyang Han.

## Why I built it

I started the Gambit project in September 2026. Last September, I started a chess club at my school, and our coach is rated 2200. I seemed to lose to him every time, so I thought the only way to beat him was through what I am good at: programming. At first, Gambit lost to my coach every time. I found that the biggest problem was that it wasn't prioritizing king safety: it got into risky positions with an exposed king, and then one small mistake led to a defeat. Learning from that, I fixed the hand-written evaluation and then added an NNUE evaluation. Since then, it hasn't lost to a human, and it is currently rated around 2600 in Lichess blitz.

## Download

- **Windows (no Java needed):** `ChessEngine_Gambit-1.0-windows.zip`. Unzip it and point your GUI at
  `ChessEngine_Gambit-1.0\ChessEngine_Gambit-1.0.exe`.
- **Any OS with Java 25+:** `gambit.jar`. Run it with
  `java --add-modules jdk.incubator.vector -Xmx2g -jar gambit.jar`
  (the `--add-modules` flag is required).

The neural network (`gambit.nnue`) is built into both downloads. If the engine ever prints
`info string WARNING: gambit.nnue not found`, it is running without its network and is much weaker.

## UCI options

| Option  | Default | Range   | Notes |
|---------|---------|---------|-------|
| Hash    | 16      | 1-1024  | MB. If the size does not fit in memory, the engine halves it instead of crashing. |
| Threads | 1       | 1-64    | Lazy SMP. |
| Ponder  | false   |         | Supports `go ponder` / `ponderhit`. |

No own opening book or endgame tablebases.

## How it works

- Search: alpha-beta (PVS) with iterative deepening, aspiration windows, transposition table,
  null-move pruning, late move reductions, internal iterative reduction, killer / history /
  countermove move ordering, quiescence search, Lazy SMP.
- Evaluation: NNUE (768 -> 384x2 -> 1, SCReLU), trained on about 132 million positions from
  Gambit's own self-play, with inference vectorized using the Java Vector API.

## Lichess

Plays as [ChessEngine_Gambit](https://lichess.org/@/ChessEngine_Gambit) on Lichess.

## Acknowledgements

I wrote the engine myself: board representation, move generation, search, time management
and the UCI interface.

I used Claude (an AI assistant) as a testing and research partner:
- running engine-vs-engine matches to test which changes gained Elo
- suggesting search and evaluation ideas, which I reviewed and applied to the code myself
- building the NNUE training pipeline and training the networks on self-play data
  generated on my own PC
- helping debug problems (for example time losses and a Hash crash)
- drafting the technical sections of this README

Every change in this repository was applied and committed by me.

## License

MIT (see `LICENSE`).
