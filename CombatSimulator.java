import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CombatSimulator {
    private static Ship createShip(String type) {
        return switch (type.toLowerCase()) {
            case "c", "c2" -> new Carrier();
            case "cr" -> new Cruiser();
            case "cr2" -> new Cruiser(2);
            case "d" -> new Destroyer();
            case "d2" -> new Destroyer(2);
            case "d3" -> new Destroyer(3);
            case "dr", "dr2" -> new Dreadnought();
            case "dr3" -> new Dreadnought(3);
            case "f" -> new Fighter();
            case "f2" -> new Fighter(2);
            case "f3" -> new Fighter(3);
            case "hil_colish" -> new Flagship(1, 5);
            case "arc_secundus", "son_of_ragh", "inferno", "dynamo", "genesis", "001", 
                 "arvicon_rex", "memoria2", "terror_between", "ysia_yssrila" -> new Flagship(2, 5);
            case "cmorran_norr", "jns_hylarim" -> new Flagship(2, 6);
            case "salai_sai_corian" -> new Flagship(1, 7);
            case "duha_menaimon", "quetzecoatl", "artemiris", "wrath_of_kenara", 
                 "fourth_moon", "memoria", "ouranos", "loncarra_ssodu" -> new Flagship(2, 7);
            case "matriarch", "visz_el_vir", "alastor", "van_hauge" -> new Flagship(2, 9);
            case "z_grav_eidolon" -> new Mech(2, 8);
            default -> throw new IllegalArgumentException("Unknown ship type: " + type);
        };
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java CombatSimulator [<modifier>] <ship-type> <quantity> [<ship-type> <quantity> ...]");
            System.out.println("Modifiers: -1all, +1all, +2all, +1fighter, +2fighter, +2flagship, +2mech");
            System.exit(1);
        }

        List<Ship> fleet = new ArrayList<>();
        List<RollModifier> modifiers = new ArrayList<>();
        
        // Check for modifiers at the beginning of arguments
        int startIndex = 0;
        while (startIndex < args.length) {
            RollModifier modifier = RollModifier.fromFlag(args[startIndex]);
            if (modifier != null) {
                modifiers.add(modifier);
                startIndex++;
            } else {
                break;
            }
        }

        // Ensure we have ship arguments after modifiers
        if (startIndex >= args.length || (args.length - startIndex) % 2 != 0) {
            System.out.println("Invalid ship specifications after modifiers.");
            System.exit(1);
        }

        // Parse ships
        for (int i = startIndex; i < args.length; i += 2) {
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

        // Print applied modifiers
        if (!modifiers.isEmpty()) {
            System.out.println("Applied modifiers: " + 
                modifiers.stream()
                    .map(RollModifier::getFlag)
                    .collect(Collectors.joining(", ")));
        }

        System.out.println("\nRolling for combat...");

        Map<String, List<Ship>> groupedShips = fleet.stream()
            .collect(Collectors.groupingBy(Ship::getShipType));

        AtomicInteger grandTotalHits = new AtomicInteger(0);

        groupedShips.forEach((type, ships) -> {
            int totalHits = 0;
            List<Integer> allRolls = new ArrayList<>();

            for (Ship ship : ships) {
                CombatResult result = ship.rollDice(modifiers);
                totalHits += result.getHits();
                allRolls.addAll(result.getRolls());
            }

            System.out.printf("%d %s rolled %s with %d hits%n",
                ships.size(),
                ships.size() == 1 ? type : type + "s",
                allRolls,
                totalHits);
            
            grandTotalHits.addAndGet(totalHits);
        });

        System.out.printf("\nTotal hits: %d%n", grandTotalHits.get());
    }
}