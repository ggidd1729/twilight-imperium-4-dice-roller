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

    public CombatResult rollDice(List<RollModifier> modifiers, List<Ship> fleet) {
        List<Integer> preModifierRolls = new ArrayList<>();
        List<Integer> postModifierRolls = new ArrayList<>();
        int hits = 0;
        int extraDice = 0;
        boolean wasModified = false;

        // Check for NRA ability (Visz el Vir affecting Z Grav Eidolons)
        if (this instanceof Mech && isNRAFlagshipPresent(fleet)) {
            extraDice = 1; // Add one extra die for Z Grav Eidolons
        }

        int totalDice = numDice + extraDice;
        
        for (int i = 0; i < totalDice; i++) {
            int roll = random.nextInt(10) + 1; // Roll d10 (1-10)
            preModifierRolls.add(roll);

            // Check for Jol-Nar ability before modifiers
            int naturalHits = 0;
            if (this instanceof Flagship && isJolNarFlagshipPresent(fleet) && (roll == 9 || roll == 10)) {
                naturalHits = 2; // Add 2 hits for natural 9 or 10
            }
            
            int modifiedRoll = roll;
            // Apply Sardakk modifier if present (but not to itself)
            if (isSardakkFlagshipPresent(fleet) && !(this instanceof Flagship)) {
                modifiedRoll += 1;
                wasModified = true;
            }
            
            // Apply regular modifiers
            int finalRoll = applyModifiers(modifiedRoll, modifiers);
            if (finalRoll != modifiedRoll) {
                wasModified = true;
            }
            
            // Ensure roll stays within 1-10 range
            finalRoll = Math.max(1, Math.min(10, finalRoll));
            
            postModifierRolls.add(finalRoll);
            if (finalRoll >= combatValue) {
                hits++;
            }
            
            // Add Jol-Nar natural hits
            hits += naturalHits;
        }
        
        return new CombatResult(preModifierRolls, postModifierRolls, hits, wasModified);
    }

    private boolean isJolNarFlagshipPresent(List<Ship> fleet) {
        return fleet.stream().anyMatch(ship -> 
            ship instanceof Flagship && ((Flagship)ship).isJolNar());
    }

    private boolean isSardakkFlagshipPresent(List<Ship> fleet) {
        return fleet.stream().anyMatch(ship -> 
            ship instanceof Flagship && ((Flagship)ship).isSardakk());
    }

    private boolean isNRAFlagshipPresent(List<Ship> fleet) {
        return fleet.stream().anyMatch(ship -> 
            ship instanceof Flagship && ((Flagship)ship).isNRA());
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
                        modifiedRoll += 2;
                    }
                case PLUS_TWO_MECH:
                    if (this instanceof Mech) {
                        modifiedRoll += 2;
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