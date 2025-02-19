public class Infantry extends AbstractShip {
    public Infantry() {
        super(1, 8, "Infantry");
    }

    public Infantry(int mode) {
        super(1, validateCombatValue(mode), "Infantry");
    }

    private static int validateCombatValue(int mode) {
        return switch (mode) {
            case 1 -> 8; // Standard Infantry
            case 2 -> 7; // Upgraded Infantry or Spec Ops 1
            case 3 -> 6; // Spec Ops 2
            default -> throw new IllegalArgumentException("Invalid mode: " + mode + ". Use 1 for standard, 2 for upgraded or SO1 or 3 for SO2.");
        };
    }
}
