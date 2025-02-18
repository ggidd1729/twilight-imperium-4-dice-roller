public interface Ship {
    CombatResult rollDice();
    int getCombatValue();
    int getNumDice();
    String getShipType();
}