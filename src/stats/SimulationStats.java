package stats;

public class SimulationStats {
    private int hits = 0;
    private int misses = 0;

    public void recordHit() {
        hits++;
    }

    public void recordMiss() {
        misses++;
    }

    public int getHits() { return hits; }
    public int getMisses() { return misses; }
}
