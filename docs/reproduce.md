# Reproduce

This page records the minimal commands needed to build and run the repository.

## Environment

- JDK 11 or later.
- Run commands from the repository root.
- No Maven or Gradle setup is required.

## Compile

PowerShell:

```powershell
$sources = Get-ChildItem -Recurse -File src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp "lib\jagoclient.jar" -d target\classes $sources
```

## Smoke Demo

The smoke demo validates the core RoadBoard path without running a long match:

- initialize RoadBoard;
- verify 924 precomputed roads;
- place stones;
- generate neighbor candidates;
- evaluate one candidate point.

```powershell
java -cp "target\classes;lib\jagoclient.jar" stud.SmokeDemo
```

Expected output:

```text
roads=924
neighborCandidates=28
pointScore=1288
smoke=PASS
```

## Optional Benchmark

Small benchmark runs can be launched through `BenchmarkRunner`:

```powershell
java -cp "target\classes;lib\jagoclient.jar" stud.BenchmarkRunner --match g33-vs-g22 --games 10
java -cp "target\classes;lib\jagoclient.jar" stud.BenchmarkRunner --match ga-vs-manual --games 10
```

The logged 100-game and 1000-game results in `docs/benchmark.md` are internal-baseline evaluations. They should not be interpreted as absolute playing strength against professional Connect6 engines.
