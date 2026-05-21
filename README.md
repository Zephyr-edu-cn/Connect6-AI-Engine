# Connect6 AI Engine

A Java-based Connect6 AI engine built on a course Connect6 framework. The main implementation focus is the `stud.g33` AI: road-based board evaluation, Alpha-Beta search, TBS-inspired candidate generation, GA-based weight tuning, and benchmark review against internal baselines.

## Project Scope

- This project originated from an Artificial Intelligence course Connect6 platform.
- The shared `src/core/` game framework and public interfaces come from the course platform.
- The core AI strategy work in this repository focuses on RoadBoard-based evaluation, Alpha-Beta search, TBS-inspired candidate generation, GA-based weight tuning, and evaluation logs.
- Reported win rates are internal-baseline results under this framework. They should not be read as proof of absolute playing strength against professional Connect6 engines.

## Highlights

- Road-based board representation for 19x19 Connect6.
- 924 precomputed six-cell roads and position-to-road reverse index.
- Incremental road-state update for fast evaluation and search backtracking.
- Alpha-Beta search with TBS-inspired candidate generation.
- Emergency attack/defense detection for immediate winning or blocking moves.
- Offline genetic algorithm for evaluation-weight tuning.
- V3 achieved 80% win rate over V2 in 100 evaluation games.
- GA-tuned G33 achieved 694 wins / 306 losses against the G22 baseline in 1000 games.

## Architecture

- `src/core/`: board, game, match, timer, UI and player framework.
- `src/stud/g22/`: G22 baseline with RoadBoard and Alpha-Beta search.
- `src/stud/g33/RoadBoard.java`: road-based board evaluator.
- `src/stud/g33/Connect6Engine.java`: Alpha-Beta search engine with TBS-inspired candidate generation.
- `src/stud/g33/GATuner.java`: offline genetic algorithm tuner.
- `src/stud/g33/ThreatDetector.java`: threat detection utilities.
- `src/stud/AITester.java`: match evaluation entry.

## Key Idea

Connect6 has a very large branching factor because each move places two stones.

Instead of enumerating all empty pairs, this engine represents the board using precomputed six-cell roads and generates candidate moves from threat-related roads and local neighborhoods.

The optimization strategy is:

```text
Road-based board representation
    -> incremental evaluation
    -> TBS-inspired candidate generation
    -> Alpha-Beta pruning
    -> GA-tuned evaluation weights
```

## Experimental Results

| Experiment | Games | Result | Purpose |
|---|---:|---:|---|
| V3 vs V2 | 100 | 80 wins / 20 losses | Validate TBS-inspired candidate generation |
| GA-G33 vs G22 baseline | 1000 | 694 wins / 306 losses | Validate GA-tuned weights against baseline |
| GA-G33 vs Manual-G33 | 1000 | 547 wins / 453 losses | Compare GA-tuned weights with hand-tuned weights |

Detailed reports:

- [Design Notes](docs/design.md)
- [Benchmark Report](docs/benchmark.md)
- [Reproduce](docs/reproduce.md)

Raw logs:

- [GA Tuning Log](results/ga_tuning_log.txt)
- [G33 vs G22 1000-game Match Log](results/match_g33_vs_g22_1000.txt)
