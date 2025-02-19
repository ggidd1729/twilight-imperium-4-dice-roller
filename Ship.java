import java.util.List;

public interface Ship {
    CombatResult rollDice(List<RollModifier> modifiers);
    int getCombatValue();
    int getNumDice();
    String getShipType();
}