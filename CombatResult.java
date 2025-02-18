import java.util.List;

public class CombatResult {
    private final List<Integer> rolls;
    private final int hits;
    
    public CombatResult(List<Integer> rolls, int hits) {
        this.rolls = rolls;
        this.hits = hits;
    }
    
    public List<Integer> getRolls() {
        return rolls;
    }
    
    public int getHits() {
        return hits;
    }
}