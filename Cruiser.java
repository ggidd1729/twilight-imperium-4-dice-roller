public class Cruiser extends AbstractShip {
    public Cruiser() {
        super(1, 7, "Cruiser");
    }

    public Cruiser(int mode) {
        super(1, validateCombatValue(mode), "Cruiser");
    }

    private static int validateCombatValue(int mode) {
        return switch (mode) {
            case 1 -> 7; // Standard Cruiser
            case 2 -> 6; // Upgraded Cruiser
            default -> throw new IllegalArgumentException("Invalid mode: " + mode + ". Use 1 for standard or 2 for upgraded.");
        };
    }
}