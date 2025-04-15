public class Flagship extends AbstractShip {
    private final String name;

    public Flagship () {
        super(1, 5, "Flagship");
        this.name = "";
    }

    public Flagship (int numDice, int combatValue) {
        super(numDice, combatValue, "Flagship");
        this.name = "";
    }

    public Flagship (int numDice, int combatValue, String name){
        super(numDice, combatValue, "Flagship");
        this.name = name;
    }

    public boolean isJolNar() {
        return "J.N.S. Hylarim".equals(name);
    }

    public boolean isSardakk() {
        return "C'Morran N'orr".equals(name);
    }

    public boolean isNRA() {
        return "Visz el Vir".equals(name);
    }

    public boolean isWinnu() {
        return "Salai Sai Corian".equals(name);
    }
    
    public boolean isSaar() {
        return "Son of Ragh".equals(name);
    }
    
    public boolean isNomad() {
        return "Memoria".equals(name) || "Memoria II".equals(name);
    }

    public String getShipName() {
        return name;
    }
}