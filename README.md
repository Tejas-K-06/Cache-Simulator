# Cache Simulator
> A command-line CPU cache simulator written in Java.
> Simulates how a processor cache works — feed it memory address traces, get back hit/miss statistics.

---

## What it Simulates

Modern CPUs don't read directly from RAM on every access — they use a small, fast cache memory to store recently used data. This simulator replicates that behavior:

- You provide a **memory address trace** (a sequence of reads and writes)
- The simulator processes each access through a configurable **multi-level cache hierarchy** (L1 → L2 → Main Memory)
- It reports how many **hits** (data found in cache) and **misses** (had to fetch from a lower level) occurred per cache level

---

## Feature Status

| Feature | Status |
|---|---|
| `Cache.java` abstract base class | ✅ Done |
| `CacheBlock.java` | ✅ Done |
| Direct Mapped Cache | ✅ Done |
| Set Associative Cache | ✅ Done |
| Fully Associative Cache | ✅ Done |
| LRU Replacement Policy | ✅ Done |
| FIFO Replacement Policy | ✅ Done |
| Write-Back Policy | ✅ Done |
| Write-Through Policy | ✅ Done |
| Trace Loader | ✅ Done |
| Trace Generator (with Multithreading) | ✅ Done |
| Simulation Stats | ✅ Done |
| Exception Handling | ✅ Done |
| L1 → L2 → L3 Hierarchy | ✅ Done |
| JSON Config Loader | ✅ Done |
| UML Diagrams | ✅ Done |

---

## Quick Start

**Requirements:** JDK 17+. No external dependencies.

### 1. Clone the Repository
```bash
git clone https://github.com/Tejas-K-06/Cache-Simulator.git
cd Cache-Simulator
```

### 2. Compile the Code
We recommend creating an `out` directory to keep the compiled `.class` files separate from your source code.

**For macOS / Linux:**
```bash
mkdir -p out
# Compile by letting javac find dependencies automatically from the root
javac -d out -sourcepath src src/main/Main.java

# OR compile by manually finding all java files (foolproof for zsh/bash):
javac -d out $(find src -name "*.java")
```

**For Windows (Command Prompt / PowerShell):**
```cmd
mkdir out
javac -d out -sourcepath src src/main/Main.java
```

### 3. Run the Simulator
Make sure you run the `Main` class from the root folder, and tell Java to look in the `out` directory you just created:

```bash
java -cp out main.Main
```

### Expected Output

The simulator will:
1. Load cache configuration from `config/cache_config.json`
2. Build the L1 → L2 → Main Memory hierarchy
3. Load and parse `traces/sample_trace.txt`
4. Simulate every memory access through the hierarchy (showing HIT/MISS per level)
5. Print per-level statistics (hit rate, miss rate, total accesses)
6. Generate synthetic trace files concurrently using multithreading

### Running with Different Traces

To simulate with a different trace file, edit `Main.java` line 86:

```java
String traceFile = "traces/stress_trace.txt";        // 200-access stress test
String traceFile = "traces/generated_random.txt";     // randomly generated
String traceFile = "traces/generated_sequential.txt"; // sequential stride pattern
String traceFile = "traces/generated_looping.txt";    // looping pattern (temporal locality)
```

### Changing Cache Configuration

Edit `config/cache_config.json` to experiment with different architectures:

```json
{
  "caches": [
    {
      "level": "L1",
      "type": "DirectMapped",
      "size": 1024,
      "blockSize": 64,
      "replacementPolicy": "LRU",
      "writePolicy": "WriteThrough"
    },
    {
      "level": "L2",
      "type": "SetAssociative",
      "size": 4096,
      "blockSize": 64,
      "associativity": 4,
      "replacementPolicy": "LRU",
      "writePolicy": "WriteBack"
    }
  ],
  "mainMemorySize": 1048576,
  "mainMemoryLatency": 100
}
```

**Configurable parameters per level:**

| Parameter | Values | Description |
|---|---|---|
| `type` | `DirectMapped`, `SetAssociative`, `FullyAssociative` | Cache mapping technique |
| `size` | Power of 2 (bytes) | Total cache size |
| `blockSize` | Power of 2 (bytes) | Size of each cache block/line |
| `associativity` | 2, 4, 8... | Ways per set (Set Associative only) |
| `replacementPolicy` | `LRU`, `FIFO` | Block eviction strategy |
| `writePolicy` | `WriteBack`, `WriteThrough` | Write handling strategy |

---

## Project Architecture

```
                        Main.java
                            │
          ┌─────────────────┼──────────────────┐
          │                 │                  │
    ConfigLoader      TraceLoader        TraceGenerator
    CacheFactory      MemoryAccess     (Multithreaded)
          │                 │                  │
          └────────┬────────┘                  │
                   │                           │
            CacheHierarchy ◄───────────────────┘
           (L1 → L2 → RAM)
                   │
     ┌─────────────┼─────────────┐
     │             │             │
DirectMapped  SetAssociative  FullyAssociative
   Cache         Cache           Cache
     └─────────────┼─────────────┘
                   │
              Cache.java
            (abstract base)
                   │
     ┌─────────────┴──────────────┐
     │                            │
ReplacementPolicy            WritePolicy
  (LRU / FIFO)          (WriteBack / WriteThrough)
     │                            │
     └─────────────┬──────────────┘
                   │
           SimulationStats
```

---

## OOP Concepts Covered

| Concept | Where |
|---|---|
| Abstract Class | `Cache.java` — defines access(), computeIndexBits() |
| Inheritance | `DirectMappedCache`, `SetAssociativeCache`, `FullyAssociativeCache` extend `Cache` |
| Polymorphism | `access()`, `evict()`, `onHit()`, `onMiss()` — each subclass has different behavior |
| Encapsulation | Private fields with controlled access throughout all classes |
| Interfaces | `ReplacementPolicy`, `WritePolicy` — decoupled, pluggable strategies |
| Method Overloading | `SimulationStats.recordHit()` and `recordHit(boolean isWrite)` |
| Exception Handling | `InvalidAddressException` (checked), `CacheConfigException` (unchecked) |
| Multithreading | `TraceGenerator` — concurrent trace file generation using `ExecutorService` thread pool, `AtomicInteger` for thread-safe error counting |
| Design Patterns | Strategy (policies), Factory (CacheFactory), Chain of Responsibility (CacheHierarchy) |

---

## Trace File Format

A trace file is a plain text file where each line is one memory access:

```
# Comments start with #
R 0x0001A4F0
W 0x0003B220
R 0x0001A4F0
R 0x00FF1100
W 0x0003B220
```

| Column | Values | Meaning |
|---|---|---|
| 1 | `R` or `W` | Read or Write |
| 2 | `0x...` | 32-bit hex memory address |

Blank lines and lines starting with `#` are ignored.

### Trace Generator

The `TraceGenerator` utility can create synthetic trace files programmatically:

```java
// Random trace — 1000 accesses, 20-bit address space, 30% writes
TraceGenerator.generate("traces/my_trace.txt", 1000, 20, 0.3);

// Sequential — 500 reads starting at 0x1000, stride 64 bytes
TraceGenerator.generateSequential("traces/seq.txt", 0x1000, 500, 64, false);

// Looping — 8 blocks repeated 50 times (tests LRU vs FIFO)
TraceGenerator.generateLooping("traces/loop.txt", 0x5000, 8, 50, false);
```

**Multithreaded batch generation** — generate multiple traces concurrently:

```java
AtomicInteger errorCount = new AtomicInteger(0);

List<TraceGenerator.TraceTask> tasks = new ArrayList<>();
tasks.add(TraceGenerator.TraceTask.random("trace1.txt", 1000, 20, 0.3, errorCount));
tasks.add(TraceGenerator.TraceTask.sequential("trace2.txt", 0x1000, 500, 64, false, errorCount));
tasks.add(TraceGenerator.TraceTask.looping("trace3.txt", 0x5000, 8, 50, false, errorCount));

// All 3 files generated concurrently on separate threads
TraceGenerator.generateAllParallel(tasks, 3);
```

---

## Folder Structure

```
Cache-Simulator/
├── src/
│   ├── cache/
│   │   ├── Cache.java                  # Abstract base class for all cache types
│   │   ├── CacheBlock.java             # Single cache line (valid, dirty, tag)
│   │   ├── DirectMappedCache.java      # 1-way associative cache
│   │   ├── SetAssociativeCache.java    # N-way set associative cache
│   │   └── FullyAssociativeCache.java  # Fully associative cache
│   ├── config/
│   │   ├── CacheConfig.java            # POJO for parsed cache config
│   │   ├── CacheFactory.java           # Instantiates caches from config
│   │   └── ConfigLoader.java           # JSON config file parser
│   ├── policy/
│   │   ├── ReplacementPolicy.java      # Interface for eviction strategies
│   │   ├── LRUPolicy.java              # Least Recently Used eviction
│   │   └── FIFOPolicy.java             # First In First Out eviction
│   ├── write/
│   │   ├── WritePolicy.java            # Interface for write strategies
│   │   ├── WriteBack.java              # Write to cache, flush on eviction
│   │   └── WriteThrough.java           # Write to cache and memory
│   ├── memory/
│   │   └── MainMemory.java             # Main memory (RAM) access counter
│   ├── multilevel/
│   │   └── CacheHierarchy.java         # L1 → L2 → L3 → RAM chain
│   ├── trace/
│   │   ├── TraceLoader.java            # Parses trace files into MemoryAccess list
│   │   ├── TraceGenerator.java         # Generates synthetic traces (multithreaded)
│   │   └── MemoryAccess.java           # Single memory access (address + R/W)
│   ├── stats/
│   │   └── SimulationStats.java        # Hit/miss/eviction statistics tracker
│   ├── exception/
│   │   ├── InvalidAddressException.java  # Bad address in trace file (checked)
│   │   └── CacheConfigException.java     # Invalid config parameter (unchecked)
│   └── main/
│       └── Main.java                   # Entry point — end-to-end simulation
├── config/
│   └── cache_config.json               # Cache hierarchy configuration
├── traces/
│   ├── sample_trace.txt                # 20-access hand-crafted sample
│   ├── stress_trace.txt                # 200-access stress test (5 sections)
│   ├── generated_random.txt            # Auto-generated random pattern
│   ├── generated_sequential.txt        # Auto-generated sequential stride
│   └── generated_looping.txt           # Auto-generated looping pattern
└── README.md
```


## Compile and Run
```
The project follows a standard Java package structure with all source files inside the `src/` directory. The program entry point is `main.Main`.

### Fish Shell

Compile:

```fish
javac -g -d out (find src -type f -name "*.java")
```

Run:

```fish
java -cp out main.Main
```

### Bash

Compile:

```bash
javac -g -d out $(find src -type f -name "*.java")
```

Run:

```bash
java -cp out main.Main
```

### Zsh

Compile:

```zsh
javac -g -d out $(find src -type f -name "*.java")
```

Run:

```zsh
java -cp out main.Main
```

### Notes

* `-g` embeds debugging information in the compiled `.class` files.
* `-d out` places compiled classes into the `out/` directory while preserving the package structure.
* `main.Main` is the fully qualified name of the program’s entry class.



## Team

| Role | Member | Contribution |
|---|---|---|
| Cache Core | Tejas | `CacheBlock`, `Cache`, `DirectMappedCache`, `SetAssociativeCache`, `FullyAssociativeCache` |
| Hierarchy & Config | Jibran | `CacheHierarchy`, `CacheFactory`, `ConfigLoader`, `CacheConfig`, JSON config |
| Policy Manager | Kanak | `ReplacementPolicy`, `LRUPolicy`, `FIFOPolicy`, `WritePolicy`, `WriteBack`, `WriteThrough` |
| Simulation Engine & Stats | Varad | `TraceLoader`, `TraceGenerator` (multithreaded), `SimulationStats`, `MemoryAccess`, `InvalidAddressException`, `CacheConfigException`, `Main`, trace files |

---

*OOPD Mini Project — SY Sem 4, 2026*
