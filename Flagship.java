public class Flagship extends AbstractShip {
    public Flagship () {
        super(1, 5, "Flagship");
    }

    public Flagship (int numDice, int combatValue) {
        super(numDice, combatValue, "Flagship");
    }
}