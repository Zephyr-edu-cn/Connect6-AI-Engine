# Design Notes

## 1. Problem Background

Connect6 is played on a 19x19 board. The first black move places one stone, and each later move places two stones.

This two-stone rule greatly increases the branching factor. A naive search would need to enumerate many pairs of empty positions, which is not practical under time constraints.

The engine focuses on three core problems:

1. how to evaluate a board efficiently;
2. how to reduce candidate moves;
3. how to search deeper under limited time.

## 2. Road-Based Board Representation

A road is any continuous six-cell segment on the board.

For a 19x19 board, there are 924 roads:

- 266 horizontal roads;
- 266 vertical roads;
- 196 diagonal-down roads;
- 196 diagonal-up roads.

The engine precomputes two static tables:

```text
ROADS[924][6]
POS_TO_ROADS[361][]
```

`ROADS` stores all six-cell roads.

`POS_TO_ROADS` maps each board position to the roads that contain it.

This design allows each make/unmake move to update only the affected roads instead of scanning the whole board.

## 3. Incremental Evaluation

Each road stores the number of black and white stones.

A road is considered dead if both players have stones on it. Dead roads are ignored during evaluation.

Otherwise, the score is accumulated according to the number of stones on that road.

The board evaluation follows:

```text
score = myScore - opponentScore * defenseFactor
```

This design avoids complex pattern matching such as live-three, dead-four, or open-four rules.

Instead, the engine uses road statistics as a compact approximation of tactical potential.

## 4. Threat-Space Candidate Generation

Connect6 moves consist of two positions, so enumerating all empty pairs is expensive.

The V3 engine generates candidates from:

- roads with potential threats;
- empty cells on those roads;
- local neighborhoods around existing stones;
- fallback neighborhood expansion when the threat space is too small.

This reduces the candidate space and allows deeper Alpha-Beta search.

## 5. Alpha-Beta Search

The engine uses Alpha-Beta pruning over two-stone moves.

The main search flow is:

```text
emergency attack/defense check
    -> threat-space candidate generation
    -> heuristic candidate ordering
    -> Alpha-Beta search
    -> fallback move if no candidate is available
```

In the G33 engine, the default search depth is 4 and the search width is 14.

## 6. Emergency Attack and Defense

Before running the regular Alpha-Beta search, the engine checks whether there are urgent tactical moves:

- immediate winning opportunities;
- opponent threats that must be blocked;
- high-value threat roads.

This helps the engine avoid missing decisive local tactics.

## 7. GA-Based Weight Tuning

The evaluation weights can be tuned by `GATuner`.

The tuner evolves weight arrays through repeated match evaluation.

The goal is not to prove global optimality, but to replace manual weight guessing with an automated evaluation loop.

A representative evolved chromosome is:

```text
[0, 14, 65, 157, 630, 1104, 10000000]
```

## 8. Summary

The project combines three levels of optimization:

1. board representation: road-based incremental evaluation;
2. search pruning: threat-space candidate generation and Alpha-Beta pruning;
3. parameter tuning: offline GA-based evaluation-weight search.

The key idea is to convert a large two-stone branching problem into a structured search over threat-related candidate positions.
