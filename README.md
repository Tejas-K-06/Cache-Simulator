# Cache Simulator
> A command-line CPU cache simulator written in Java.
> Simulates how a processor cache works — feed it memory address traces, get back hit/miss statistics.

---

> ⚠️ **Status: Work in Progress**
> This project is actively being developed as part of an OOPD Mini Project.
> Features marked 🚧 are planned but not yet implemented.

---

## What it Simulates

Modern CPUs don't read directly from RAM on every access — they use a small, fast cache memory to store recently used data. This simulator replicates that behavior:

- You provide a **memory address trace** (a sequence of reads and writes)
- The simulator processes each access through a configurable cache
- It reports how many **hits** (data found in cache) and **misses** (had to fetch from memory) occurred

---

## Feature Status

| Feature | Status |
|---|---|
| `Cache.java` abstract base class | ✅ Done |
| `CacheBlock.java` | ✅ Done |
| Direct Mapped Cache | ✅ Done |
| Set Associative Cache |✅ Done |
| Fully Associative Cache | ✅ Done |
| LRU Replacement Policy | ✅ Done |
| FIFO Replacement Policy | ✅ Done |
| Write-Back Policy | ✅ Done |
| Write-Through Policy | ✅ Done |
| Trace Loader | 🚧 In Progress |
| Simulation Stats | 🚧 In Progress |
| Exception Handling | 🚧 In Progress |
| L1 → L2 → L3 Hierarchy | ✅ Done |
| JSON Config Loader | ✅ Done |
| UML Diagrams | 🚧 In Progress |

---

## Project Architecture

```
                        Main.java
                            │
                    CacheFactory.java
                            │
               ┌────────────┼────────────┐
               │            │            │
     DirectMapped     SetAssociative   FullyAssociative
        Cache            Cache            Cache
               └────────────┼────────────┘
                            │
                       Cache.java
                      (abstract base)
                            │
              ┌─────────────┴──────────────┐
              │                            │
    ReplacementPolicy               WritePolicy
    (LRU / FIFO)                (WriteBack / WriteThrough)
              │                            │
              └─────────────┬──────────────┘
                            │
                    SimulationStats
                            │
                      CacheHierarchy
                     (L1 → L2 → L3)
```

---

## OOP Concepts Covered

| Concept | Where |
|---|---|
| Abstract Class | `Cache.java` |
| Inheritance | `DirectMappedCache`, `SetAssociativeCache`, `FullyAssociativeCache` extend `Cache` |
| Polymorphism | `access()`, `evict()`, `onHit()`, `onMiss()` per subclass |
| Encapsulation | Private fields with controlled access throughout |
| Interfaces | `ReplacementPolicy`, `WritePolicy` |
| Generics | `CacheSet<T extends CacheBlock>` |
| Exception Handling | `InvalidAddressException`, `CacheConfigException` |
| Multithreading | Background thread for trace processing |
| Design Patterns | Strategy, Factory, Singleton, Observer, Chain of Responsibility |

---

## Folder Structure

```
cache-simulator/
├── src/
│   ├── cache/
│   │   ├── Cache.java
│   │   ├── CacheBlock.java
│   │   ├── DirectMappedCache.java
│   │   ├── SetAssociativeCache.java
│   │   └── FullyAssociativeCache.java
│   ├── config/
│   │   ├── CacheConfig.java
│   │   ├── CacheFactory.java
│   │   └── ConfigLoader.java
│   ├── policy/
│   │   ├── ReplacementPolicy.java
│   │   ├── LRUPolicy.java
│   │   └── FIFOPolicy.java
│   ├── write/
│   │   ├── WritePolicy.java
│   │   ├── WriteBack.java
│   │   └── WriteThrough.java
│   ├── memory/
│   │   └── MainMemory.java
│   ├── multilevel/
│   │   └── CacheHierarchy.java
│   ├── trace/
│   │   ├── TraceLoader.java
│   │   └── MemoryAccess.java
│   ├── stats/
│   │   └── SimulationStats.java
│   ├── exception/
│   │   ├── InvalidAddressException.java
│   │   └── CacheConfigException.java
│   └── main/
│       └── Main.java
├── config/
│   └── cache_config.json
├── traces/
│   ├── sample_trace.txt
│   └── stress_trace.txt
├── CONTRIBUTING.md
└── README.md
```

---

## Trace File Format

A trace file is a plain text file where each line is one memory access:

```
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

---

## Configuration Setup (JSON)

The cache simulator loads its architecture dynamically using `config/cache_config.json`. You can build your hierarchy out of lightweight, cascading arrays.

Example configuration for an `L1` (Direct Mapped) and `L2` (Set Associative) cache:

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

---

## Setup

🚧 Full setup instructions will be added once the project compiles end-to-end.

**Requirements:** JDK 17+. No external dependencies.

```bash
# Clone the repository
git clone https://github.com/<your-org>/cache-simulator.git
cd cache-simulator
```

---

*OOPD Mini Project — actively being developed*
