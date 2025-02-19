import java.util.List;

public interface Ship {
    CombatResult rollDice(List<RollModifier> modifiers, List<Ship> fleet);
    int getCombatValue();
    int getNumDice();
    String getShipType();
}