import java.util.ArrayList;
import java.util.List;

public class CombatResult {
    private final List<Integer> preModifierRolls;
    private final List<Integer> postModifierRolls;
    private final int hits;
    private final boolean wasModified;
    
    public CombatResult(List<Integer> preModifierRolls, List<Integer> postModifierRolls, int hits, boolean wasModified) {
        this.preModifierRolls = new ArrayList<>(preModifierRolls);
        this.postModifierRolls = new ArrayList<>(postModifierRolls);
        this.hits = hits;
        this.wasModified = wasModified;
    }
    
    public List<Integer> getPreModifierRolls() {
        return preModifierRolls;
    }

    public List<Integer> getPostModifierRolls() {
        return postModifierRolls;
    }
    
    public int getHits() {
        return hits;
    }

    public boolean wasModified() {
        return wasModified;
    }
}