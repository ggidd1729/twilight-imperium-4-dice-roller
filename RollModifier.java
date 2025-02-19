enum RollModifier {
    MINUS_ONE_ALL("-1all"),
    PLUS_ONE_ALL("+1all"),
    PLUS_ONE_FIGHTER("+1fighter"),
    PLUS_TWO_FIGHTER("+2fighter"),
    PLUS_TWO_FLAGSHIP("+2flagship"),
    PLUS_TWO_MECH("+2mech");

    private final String flag;

    RollModifier(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

    public static RollModifier fromFlag(String flag) {
        for (RollModifier modifier : values()) {
            if (modifier.flag.equals(flag)) {
                return modifier;
            }
        }
        return null;
    }
}