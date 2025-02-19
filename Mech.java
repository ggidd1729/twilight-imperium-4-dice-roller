public class Mech extends AbstractShip {
    public Mech () {
        super(1, 6, "Mech");
    }

    public Mech (int numDice, int combatValue) {
        super(numDice, combatValue, "Mech");
    }
} 