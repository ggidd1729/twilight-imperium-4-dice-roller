public class Fighter extends AbstractShip {
    public Fighter() {
        super(1, 9, "Fighter");
    }

    public Fighter(int mode) {
        super(1, validateCombatValue(mode), "Fighter");
    }

    private static int validateCombatValue(int mode) {
        return switch (mode) {
            case 1 -> 9; // Standard Fighter
            case 2 -> 8; // Upgraded Fighter or Hybrid Crystal Fighter 1
            case 3 -> 7; // Hybrid Crystal Fighter 2
            default -> throw new IllegalArgumentException("Invalid mode: " + mode + ". Use 1 for standard, 2 for upgraded or HCF1 or 3 for HCF2.");
        };
    }
}