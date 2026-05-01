# Benchmark Report

## 1. V3 vs V2 Evaluation

The original project report compares V3 with V2 under a 100-game setting.

The goal of this experiment is to evaluate whether threat-space search improves search depth and playing strength.

| Configuration | Search Depth | Search Width | Candidate Generation |
|---|---:|---:|---|
| V2 | 2 | 12 | Local neighborhood |
| V3 | 4 | 14 | Threat-space search |

Reported performance changes:

| Metric | V2 | V3 | Change |
|---|---:|---:|---:|
| Average search depth | 2.0 | 4.0 | +100% |
| Average candidate points | ~66 | ~40 | -39% |
| Effective node ratio | ~60% | ~85% | +42% |

Match result:

| Matchup | Games | Wins | Losses | Draws | Win Rate |
|---|---:|---:|---:|---:|---:|
| V3 vs V2 | 100 | 80 | 20 | 0 | 80.0% |

This experiment validates the effect of threat-space search.

## 2. GA-Tuned G33 vs G22-V1

The GA-tuned G33 engine was evaluated against the G22-V1 baseline over 1000 games.

| Player | Wins | Losses | Draws | Win Rate |
|---|---:|---:|---:|---:|
| G33-AlphaBot | 694 | 306 | 0 | 69.4% |
| G22-V1 | 306 | 694 | 0 | 30.6% |

Detailed raw log:

```text
results/match_g33_vs_g22_1000.txt
```

## 3. First-Player / Second-Player Breakdown

The 1000-game match log also records first-player and second-player results.

For G33-AlphaBot:

| Role | Wins | Losses | Draws | Notes |
|---|---:|---:|---:|---|
| First player | 194 | 306 | 0 | weaker opening-side result |
| Second player | 500 | 0 | 0 | stable defensive performance |

This result suggests that the tuned weights produced a particularly stable response strategy when playing second.

## 4. GA Tuning Log

The GA tuner runs offline evolution over evaluation-weight arrays.

A representative best chromosome appeared in generation 3:

```text
[0, 14, 65, 157, 630, 1104, 10000000]
```

The generation-3 chromosome achieved the highest reported fitness in the tuning log.

Detailed raw log:

```text
results/ga_tuning_log.txt
```

## 5. Interpretation

The two experiments answer different questions.

The V3 vs V2 experiment shows the benefit of threat-space search:

```text
local-neighborhood Alpha-Beta
    -> threat-space Alpha-Beta
    -> deeper search and higher win rate
```

The GA-G33 vs G22 experiment shows that the tuned engine remains stronger than the earlier baseline under a larger match setting:

```text
manual/heuristic weights
    -> GA-based weight search
    -> 1000-game baseline evaluation
```

The two reported win rates should not be mixed:

- 80.0% win rate comes from the 100-game V3 vs V2 report.
- 69.4% win rate comes from the 1000-game GA-G33 vs G22-V1 evaluation.

## 6. Limitations

The current evaluation is based on internal baselines rather than professional Connect6 engines.

Future evaluation can include:

- Manual-G33 vs GA-G33 direct comparison;
- different random seeds;
- stricter first-player/second-player balance;
- per-move time statistics;
- node-count and pruning-rate statistics.

## 7. Summary

The project demonstrates a staged improvement path:

1. V2 introduces road-based evaluation and Alpha-Beta search.
2. V3 adds threat-space candidate generation and reaches 80.0% win rate over V2 in 100 games.
3. GA-tuned G33 introduces offline weight tuning and obtains 694 wins / 306 losses against G22-V1 in 1000 games.
