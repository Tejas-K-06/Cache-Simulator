package policy;

import cache.CacheBlock;

/**
 * Strategy interface for cache block eviction.
 *
 * <p>Implementations decide <em>which</em> block to evict when a cache miss
 * occurs and all candidate slots are occupied. The two built-in strategies are:
 * <ul>
 *   <li>{@link LRUPolicy} — evicts the block with the smallest
 *       {@code lastUsed} timestamp (Least-Recently-Used).</li>
 *   <li>{@link FIFOPolicy} — evicts the block with the smallest
 *       {@code insertOrder} timestamp (First-In, First-Out).</li>
 * </ul>
 *
 * <h2>Contract for implementors</h2>
 * <ol>
 *   <li><b>Prefer invalid blocks.</b> If any block in {@code candidates} is
 *       not valid ({@code !block.isValid()}), return it immediately — no real
 *       eviction is needed.</li>
 *   <li><b>Never return {@code null}.</b> Always return one of the elements
 *       from the supplied {@code candidates} array.</li>
 *   <li><b>Do not mutate the block.</b> The policy only selects the victim;
 *       loading new data ({@code block.load(...)}) is the cache's responsibility.</li>
 *   <li><b>Be stateless.</b> All tracking data ({@code lastUsed},
 *       {@code insertOrder}) lives on {@link CacheBlock}. Policy objects carry
 *       no per-block state and are therefore safe to share across cache levels.</li>
 * </ol>
 *
 * <h2>How it fits in the design</h2>
 * <pre>
 *   Cache (SetAssociative / FullyAssociative)
 *       │  on MISS
 *       └──▶ ReplacementPolicy.evict(candidates)
 *                 │
 *                 ├── LRUPolicy  → picks min(lastUsed)
 *                 └── FIFOPolicy → picks min(insertOrder)
 * </pre>
 *
 * This follows the <b>Strategy</b> design pattern: the cache holds a reference
 * of type {@code ReplacementPolicy} and delegates the eviction decision without
 * knowing the concrete algorithm in use.
 */
public interface ReplacementPolicy {

    /**
     * Select a block to evict from the given array of candidate blocks.
     *
     * <p>Caller context:
     * <ul>
     *   <li><b>Direct-Mapped cache</b> — never calls this; there is only one
     *       candidate block per index slot.</li>
     *   <li><b>Set-Associative cache</b> — passes the way array for the matched
     *       set (length = associativity).</li>
     *   <li><b>Fully-Associative cache</b> — passes the entire block array
     *       (length = numberOfBlocks).</li>
     * </ul>
     *
     * @param candidates Non-null, non-empty array of {@link CacheBlock}s from
     *                   which one victim must be chosen.
     * @return The {@link CacheBlock} that should be evicted and overwritten
     *         with the incoming address. Never {@code null}.
     * @throws IllegalArgumentException if {@code candidates} is {@code null}
     *                                  or has length 0.
     */
    CacheBlock evict(CacheBlock[] candidates);
}
