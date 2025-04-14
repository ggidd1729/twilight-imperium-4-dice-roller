public class Flagship extends AbstractShip {
    private final String name;
    private final boolean isJolNar;
    private final boolean isSardakk;
    private final boolean isNRA;
    private final boolean isWinnu;
    private final boolean isSaar;
    private final boolean isNomad;

    public Flagship () {
        super(1, 5, "Flagship");
        this.name = "";
        this.isJolNar = false;
        this.isSardakk = false;
        this.isNRA = false;
        this.isWinnu = false;
        this.isSaar = false;
        this.isNomad = false;
    }

    public Flagship (int numDice, int combatValue) {
        super(numDice, combatValue, "Flagship");
        this.name = "";
        this.isJolNar = false;
        this.isSardakk = false;
        this.isNRA = false;
        this.isWinnu = false;
        this.isSaar = false;
        this.isNomad = false;
    }

    public Flagship (int numDice, int combatValue, String name){
        super(numDice, combatValue, "Flagship");
        this.name = name;
        this.isJolNar = "J.N.S. Hylarim".equals(name);
        this.isSardakk = "C'Morran N'orr".equals(name);
        this.isNRA = "Visz el Vir".equals(name);
        this.isWinnu = "Salai Sai Corian".equals(name);
        this.isSaar = "Son of Ragh".equals(name);
        this.isNomad = "Memoria".equals(name) || "Memoria II".equals(name);
    }

    public boolean isJolNar() {
        return isJolNar;
    }

    public boolean isSardakk() {
        return isSardakk;
    }

    public boolean isNRA() {
        return isNRA;
    }

    public boolean isWinnu() {
        return isWinnu;
    }
    
    public boolean isSaar() {
        return isSaar;
    }
    
    public boolean isNomad() {
        return isNomad;
    }

    public String getShipName() {
        return name;
    }
}