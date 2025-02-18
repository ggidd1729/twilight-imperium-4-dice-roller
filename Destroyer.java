public class Destroyer extends AbstractShip {
    public Destroyer() {
        super(1, 9, "Destroyer");
    }

    public Destroyer(int mode) {
        super(1, validateCombatValue(mode), "Destroyer");
    }

    private static int validateCombatValue(int mode) {
        return switch (mode) {
            case 1 -> 9; // Standard Destroyer
            case 2 -> 8; // Upgraded Destroyers or Strike Wing Alpha 1
            case 3 -> 7; // Strike Wing Alpha 2
            default -> throw new IllegalArgumentException("Invalid mode: " + mode + ". Use 1 for standard, 2 for upgraded or SWA1 or 3 for SWA2.");
        };
    }
}