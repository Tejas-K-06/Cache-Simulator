# Developer Guide

Welcome to the internal workings of the Cache Simulator. This document provides a deeper technical dive into our architecture, mapping logic, and the patterns used to make the simulator highly pluggable and extensible.

## 1. Architecture Overview
The system processes memory accesses via a 3-layer architecture:

1. **Config Loader & Validation**: 
   Reads `config/cache_config.json`, extracts variables ensuring data constraints (e.g. power-of-2 dimensions, 32-bit addresses), and dynamically instantiates objects through `CacheFactory`.

2. **Cache Hierarchy Engine**: 
   A Chain of Responsibility approach (`CacheHierarchy.java`). An access trickles down L1 → L2 → ... → Main Memory. If a miss happens at L1, L2 is queried. If L2 misses, Main Memory is accessed. The requested block is then brought back up through the levels using the respective replacement algorithms.

3. **Cache Controllers & Policies (`Cache.java`)**: 
   Handles bit manipulation, tag comparison, hit/miss routing, eviction strategies (`ReplacementPolicy`), and write allocations (`WritePolicy`).

## 2. Abstraction Framework & Mapping Logic
At the heart of the simulation is the abstract base class `Cache.java`. The simulator supports Direct Mapped, Set Associative, and Fully Associative mapped caches without duplicating code, thanks to polymorphism.

### Core Equation
`32-bit Memory Address = Tag | Index | Block Offset`

The base `Cache` class automatically handles address bit-decomposition using the following sizing:
- **Block Offset Bits = log2(BlockSize)**
- **Index Bits = log2(NumberOfBlocks / Associativity)**
- **Tag Bits = 32 - Index - Offset**

### Inheritance
- `DirectMappedCache`: Defines `associativity = 1`. One frame per index.
- `SetAssociativeCache`: Defines `associativity = N`. An array of blocks inside a mapped index constraint.
- `FullyAssociativeCache`: Overrides `computeIndexBits()` to return 0. The entire cache acts as one massive set, allowing a block to go into any frame. Address decomposition changes to just `Tag | Offset`.

## 3. Plug-and-Play Policies (Strategy Pattern)
The cache replacement and write behaviors are fully decoupled from the core mapping logic using interface abstractions.

### Replacement Policies (`ReplacementPolicy.java`)
- **LRU (Least Recently Used)**: Tracks block access history natively. Maintains a "Last Accessed" timestamp to remove the oldest block.
- **FIFO (First In First Out)**: A basic queue logic tracking block insertion time.

### Write Policies (`WritePolicy.java`)
- **Write-Through**: On memory store, write the data identically into the Cache and immediately trickle the write to the lower level. Great for preventing data loss natively.
- **Write-Back**: The lower level isn't touched directly. Instead, the block natively asserts a `dirty` flag. When the `ReplacementPolicy` evicts this block, only *then* does the controller push the data memory-down. Improves latency extensively at the cost of coherence logic overhead.

## 4. Multi-Threading & Concurrency
The synthetic trace generator (`TraceGenerator.java`) takes advantage of modern multicore architectures to generate multiple stress-test files concurrently using native Java `ExecutorService` thread pools.

We utilize `java.util.concurrent.atomic.AtomicInteger` as a thread-safe synchronized counting mechanism allowing child generation tasks to instantly increment the global error log without causing race conditions or corrupted output metrics. 

## 5. Extensibility Guide
**To create a new policy (e.g., LFU - Least Frequently Used):**
1. Create `LFUPolicy.java` implementing `ReplacementPolicy`.
2. Add necessary frequency parameters.
3. Hook the string mapping inside `CacheFactory.createReplacementPolicy()`.
4. Update `cache_config.json` directly. No simulator source code edits are necessary.
