import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CombatSimulator {
    private static Ship createShip(String type) {
        return switch (type.toLowerCase()) {
            case "c" -> new Carrier();
            case "c2" -> new Carrier();
            case "cr" -> new Cruiser();
            case "cr2" -> new Cruiser(2);
            case "d" -> new Destroyer();
            case "d2" -> new Destroyer(2);
            case "d3" -> new Destroyer(3);
            case "dr" -> new Dreadnought();
            case "dr2" -> new Dreadnought();
            case "dr3" -> new Dreadnought(3);
            case "f" -> new Fighter();
            case "f2" -> new Fighter(2);
            case "f3" -> new Fighter(3);
            default -> throw new IllegalArgumentException("Unknown ship type: " + type);
        };
    }

    public static void main(String[] args) {
        if (args.length < 2 || args.length % 2 != 0) {
            System.out.println("Usage: java CombatSimulator <ship-type> <quantity> [<ship-type> <quantity> ...]");
            System.exit(1);
        }

        List<Ship> fleet = new ArrayList<>();

        for (int i = 0; i < args.length; i += 2) {
            try {
                String shipType = args[i];
                int numShips = Integer.parseInt(args[i + 1]);

                for (int j = 0; j < numShips; j++) {
                    fleet.add(createShip(shipType));
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for " + args[i] + ". Skipping...");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        if (fleet.isEmpty()) {
            System.out.println("No valid ships entered. Exiting.");
            System.exit(1);
        }

        System.out.println("\nRolling for combat...");

        Map<String, List<Ship>> groupedShips = fleet.stream()
            .collect(Collectors.groupingBy(Ship::getShipType));

        AtomicInteger grandTotalHits = new AtomicInteger(0);  // Thread-safe counter

        groupedShips.forEach((type, ships) -> {
            int totalHits = 0;
            List<Integer> allRolls = new ArrayList<>();

            for (Ship ship : ships) {
                CombatResult result = ship.rollDice();
                totalHits += result.getHits();
                allRolls.addAll(result.getRolls());
            }

            System.out.printf("%d %s rolled %s with %d hits%n",
                ships.size(),
                ships.size() == 1 ? type : type + "s",
                allRolls,
                totalHits);
            
            grandTotalHits.addAndGet(totalHits);  // Thread-safe addition
        });

        // Print the grand total hits
        System.out.printf("\nTotal hits: %d%n", grandTotalHits.get());
    }
}