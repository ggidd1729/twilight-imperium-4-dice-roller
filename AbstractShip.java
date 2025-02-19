import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractShip implements Ship {
    private final int numDice;
    private final int combatValue;
    private final String shipType;
    private final Random random;

    protected AbstractShip(int numDice, int combatValue, String shipType) {
        this.numDice = numDice;
        this.combatValue = combatValue;
        this.shipType = shipType;
        this.random = new Random();
    }

    public CombatResult rollDice(List<RollModifier> modifiers) {
        List<Integer> rolls = new ArrayList<>();
        int hits = 0;
        
        for (int i = 0; i < numDice; i++) {
            int roll = random.nextInt(10) + 1; // Roll d10 (1-10)
            
            // Apply modifiers
            roll = applyModifiers(roll, modifiers);
            
            // Ensure roll stays within 1-10 range
            roll = Math.max(1, Math.min(10, roll));
            
            rolls.add(roll);
            if (roll >= combatValue) {
                hits++;
            }
        }
        
        return new CombatResult(rolls, hits);
    }

    private int applyModifiers(int roll, List<RollModifier> modifiers) {
        int modifiedRoll = roll;
        
        for (RollModifier modifier : modifiers) {
            switch (modifier) {
                case MINUS_ONE_ALL:
                    modifiedRoll -= 1;
                    break;
                case PLUS_ONE_ALL:
                    modifiedRoll += 1;
                    break;
                case PLUS_TWO_ALL:
                    modifiedRoll += 2;
                    break;
                case PLUS_ONE_FIGHTER:
                    if (this instanceof Fighter) {
                        modifiedRoll += 1;
                    }
                    break;
                case PLUS_TWO_FIGHTER:
                    if (this instanceof Fighter) {
                        modifiedRoll += 2;
                    }
                    break;
                case PLUS_TWO_FLAGSHIP:
                    if (this instanceof Flagship) {
                        modifiedRoll +=2;
                    }
            }
        }
        
        return modifiedRoll;
    }

    @Override
    public int getCombatValue() {
        return combatValue;
    }

    @Override
    public int getNumDice() {
        return numDice;
    }

    @Override
    public String getShipType() {
        return shipType;
    }
}