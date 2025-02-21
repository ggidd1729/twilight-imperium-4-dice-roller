public class Flagship extends AbstractShip {
    private final String name;
    private final boolean isJolNar;
    private final boolean isSardakk;
    private final boolean isNRA;
    private final boolean isWinnu;

    public Flagship () {
        super(1, 5, "Flagship");
        this.name = "";
        this.isJolNar = false;
        this.isSardakk = false;
        this.isNRA = false;
        this.isWinnu = false;
    }

    public Flagship (int numDice, int combatValue) {
        super(numDice, combatValue, "Flagship");
        this.name = "";
        this.isJolNar = false;
        this.isSardakk = false;
        this.isNRA = false;
        this.isWinnu = false;
    }

    public Flagship (int numDice, int combatValue, String name){
        super(numDice, combatValue, "Flagship");
        this.name = name;
        this.isJolNar = "J.N.S. Hylarim".equals(name);
        this.isSardakk = "C'Morran N'orr".equals(name);
        this.isNRA = "Visz el Vir".equals(name);
        this.isWinnu = "Salai Sai Corian".equals(name);
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

    public String getShipName() {
        return name;
    }
}