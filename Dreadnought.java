public class Dreadnought extends AbstractShip {
    public Dreadnought() {
        super(1, 5, "Dreadnought");
    }

    public Dreadnought(int mode) {
        super(1, validateCombatValue(mode), "Destroyer");
    }

    private static int validateCombatValue(int mode) {
        return switch (mode) {
            case 1 -> 5; // Standard Dreadnought
            case 2 -> 5; // Upgraded Dreadnought
            case 3 -> 4; // L1Z1X's Super Dreadnought 2
            default -> throw new IllegalArgumentException("Invalid mode: " + mode + ". Use 1 for standard, 2 for upgraded or SWA1 or 3 for SWA2.");
        };
    }
}