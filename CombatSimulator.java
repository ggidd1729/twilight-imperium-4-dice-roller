import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CombatSimulator extends JFrame {
    private JPanel shipSelectionPanel;
    private JComboBox<String> factionComboBox;
    private JTextArea resultsArea;
    private JPanel modifiersPanel;
    private Map<String, JSpinner> shipQuantities = new HashMap<>();
    private List<JCheckBox> modifierCheckboxes = new ArrayList<>();
    private ImageIcon backgroundImage;
    
    private static final String[] FACTIONS = {
        "Select Faction", "The Arborec", "The Barony of Letnev", "The Clan of Saar", "The Embers of Muaat",
        "The Emirates of Hacan", "The Federation of Sol", "The Ghosts of Creuss", "The L1Z1X Mindnet",
        "The Mentak Coalition", "The Naalu Collective", "The Nekro Virus", "The Sardakk N'orr",
        "The Universities of Jol-Nar", "The Winnu", "The Xxcha Kingdom", "The Yin Brotherhood",
        "The Yssaril Tribes", "The Argent Flight", "The Empyrean", "The Mahact Gene-Sorcerers",
        "The Naaz-Rokha Alliance", "The Nomad", "The Titans of Ul", "The Vuil'raith Cabal"
    };
    
    // Base ship types available to most factions
    private static final Map<String, String[]> BASE_SHIP_TYPES = new HashMap<>();
    static {
        BASE_SHIP_TYPES.put("Carrier", new String[]{"Carrier I", "Carrier II"});
        BASE_SHIP_TYPES.put("Cruiser", new String[]{"Cruiser I", "Cruiser II"});
        BASE_SHIP_TYPES.put("Destroyer", new String[]{"Destroyer I", "Destroyer II"});
        BASE_SHIP_TYPES.put("Dreadnought", new String[]{"Dreadnought I", "Dreadnought II"});
        BASE_SHIP_TYPES.put("Fighter", new String[]{"Fighter I", "Fighter II"});
        // Infantry and Mech only for Nekro by default
        BASE_SHIP_TYPES.put("Infantry", new String[]{"Infantry I", "Infantry II/Spec Ops I"});
        BASE_SHIP_TYPES.put("Mech", new String[]{"Mech"});
    }
    
    // Faction-specific unit overrides
    private static final Map<String, Map<String, String[]>> FACTION_SPECIFIC_UNITS = new HashMap<>();
    static {
        // L1Z1X Mindnet - Super Dreadnoughts
        Map<String, String[]> l1z1xUnits = new HashMap<>();
        l1z1xUnits.put("Dreadnought", new String[]{"Super Dreadnought I", "Super Dreadnought II"});
        FACTION_SPECIFIC_UNITS.put("The L1Z1X Mindnet", l1z1xUnits);
        
        // Naalu Collective - Hybrid Crystal Fighters
        Map<String, String[]> naaluUnits = new HashMap<>();
        naaluUnits.put("Fighter", new String[]{"Hybrid Crystal Fighter I", "Hybrid Crystal Fighter II"});
        FACTION_SPECIFIC_UNITS.put("The Naalu Collective", naaluUnits);
        
        // Argent Flight - Strike Wing Alpha
        Map<String, String[]> argentUnits = new HashMap<>();
        argentUnits.put("Destroyer", new String[]{"Strike Wing Alpha I", "Strike Wing Alpha II"});
        FACTION_SPECIFIC_UNITS.put("The Argent Flight", argentUnits);
        
        // Naaz-Rokha Alliance - Z-Grav Eidolon Mech
        Map<String, String[]> nraUnits = new HashMap<>();
        nraUnits.put("Mech", new String[]{"Z-Grav Eidolon"});
        FACTION_SPECIFIC_UNITS.put("The Naaz-Rokha Alliance", nraUnits);
        
        // Enable Infantry and Mech for Nekro
        Map<String, String[]> nekroUnits = new HashMap<>();
        nekroUnits.put("Infantry", new String[]{"Infantry I", "Infantry II/Spec Ops I", "Spec Ops II"});
        nekroUnits.put("Mech", new String[]{"Mech"});
        FACTION_SPECIFIC_UNITS.put("The Nekro Virus", nekroUnits);
    }
    
    private static final String[] MODIFIERS = {
        "-1 All Rolls", "+1 All Rolls","Prophecy of Ixth (+1 to fighter rolls)", 
        "Fighter Prototype (+2 fighter rolls)", "+2 Flagship Rolls", "+2 Mech Rolls"
    };
    
    private static final Map<String, String[]> FACTION_FLAGSHIPS = new HashMap<>();
    static {
        FACTION_FLAGSHIPS.put("The Arborec", new String[]{"Duha Menaimon"});
        FACTION_FLAGSHIPS.put("The Barony of Letnev", new String[]{"Arc Secundus"});
        FACTION_FLAGSHIPS.put("The Clan of Saar", new String[]{"Son of Ragh"});
        FACTION_FLAGSHIPS.put("The Embers of Muaat", new String[]{"Inferno"});
        FACTION_FLAGSHIPS.put("The Emirates of Hacan", new String[]{"Wrath of Kenara"});
        FACTION_FLAGSHIPS.put("The Federation of Sol", new String[]{"Genesis"});
        FACTION_FLAGSHIPS.put("The Ghosts of Creuss", new String[]{"Hil Colish"});
        FACTION_FLAGSHIPS.put("The L1Z1X Mindnet", new String[]{"0.0.1"});
        FACTION_FLAGSHIPS.put("The Mentak Coalition", new String[]{"Fourth Moon"});
        FACTION_FLAGSHIPS.put("The Naalu Collective", new String[]{"Matriarch"});
        FACTION_FLAGSHIPS.put("The Nekro Virus", new String[]{"Alastor"});
        FACTION_FLAGSHIPS.put("The Sardakk N'orr", new String[]{"C'Morran N'orr"});
        FACTION_FLAGSHIPS.put("The Universities of Jol-Nar", new String[]{"J.N.S. Hylarim"});
        FACTION_FLAGSHIPS.put("The Winnu", new String[]{"Salai Sai Corian"});
        FACTION_FLAGSHIPS.put("The Xxcha Kingdom", new String[]{"Loncarra Ssodu"});
        FACTION_FLAGSHIPS.put("The Yin Brotherhood", new String[]{"Van Hauge"});
        FACTION_FLAGSHIPS.put("The Yssaril Tribes", new String[]{"Y'sia Y'ssrila"});
        FACTION_FLAGSHIPS.put("The Argent Flight", new String[]{"Quetzcoatl"});
        FACTION_FLAGSHIPS.put("The Empyrean", new String[]{"Dynamo"});
        FACTION_FLAGSHIPS.put("The Mahact Gene-Sorcerers", new String[]{"Arvicon Rex"});
        FACTION_FLAGSHIPS.put("The Naaz-Rokha Alliance", new String[]{"Visz el Vir"});
        FACTION_FLAGSHIPS.put("The Nomad", new String[]{"Memoria"});
        FACTION_FLAGSHIPS.put("The Titans of Ul", new String[]{"Ouranos"});
        FACTION_FLAGSHIPS.put("The Vuil'raith Cabal", new String[]{"Terror Between"});
    }
    
    // Map unit names to their code in the combat simulator
    private static final Map<String, String> UNIT_NAME_TO_CODE = new HashMap<>();
    static {
        // Standard units
        UNIT_NAME_TO_CODE.put("Carrier I", "c");
        UNIT_NAME_TO_CODE.put("Carrier II", "c2");
        UNIT_NAME_TO_CODE.put("Cruiser I", "cr");
        UNIT_NAME_TO_CODE.put("Cruiser II", "cr2");
        UNIT_NAME_TO_CODE.put("Destroyer I", "d");
        UNIT_NAME_TO_CODE.put("Destroyer II", "d2");
        UNIT_NAME_TO_CODE.put("Destroyer III", "d3");
        UNIT_NAME_TO_CODE.put("Dreadnought I", "dr");
        UNIT_NAME_TO_CODE.put("Dreadnought II", "dr2");
        UNIT_NAME_TO_CODE.put("Dreadnought III", "dr3");
        UNIT_NAME_TO_CODE.put("Fighter I", "f");
        UNIT_NAME_TO_CODE.put("Fighter II", "f2");
        UNIT_NAME_TO_CODE.put("Fighter III", "f3");
        UNIT_NAME_TO_CODE.put("Infantry I", "i");
        UNIT_NAME_TO_CODE.put("Infantry II/Spec Ops I", "i2");
        UNIT_NAME_TO_CODE.put("Spec Ops II", "i3");
        UNIT_NAME_TO_CODE.put("Mech", "m");
        
        // Faction-specific units
        UNIT_NAME_TO_CODE.put("Super Dreadnought I", "dr2");
        UNIT_NAME_TO_CODE.put("Super Dreadnought II", "dr3");
        UNIT_NAME_TO_CODE.put("Hybrid Crystal Fighter I", "f2");
        UNIT_NAME_TO_CODE.put("Hybrid Crystal Fighter II", "f3");
        UNIT_NAME_TO_CODE.put("Strike Wing Alpha I", "d2");
        UNIT_NAME_TO_CODE.put("Strike Wing Alpha II", "d3");
        UNIT_NAME_TO_CODE.put("Z-Grav Eidolon", "z_grav_eidolon");
    }
    
    public CombatSimulator() {
        setTitle("Twilight Imperium 4 Combat Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Try to load a background image
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/ti4_background.jpg"));
            // If you don't have the image yet, comment out the line above and use a color instead
        } catch (Exception e) {
            System.out.println("Background image not found, using default color.");
        }
        
        initComponents();
    }
    
    private void initComponents() {
        // Main panel with background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(20, 20, 40));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // North Panel - Faction Selection
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        northPanel.setOpaque(false);
        
        JLabel factionLabel = new JLabel("Select Faction:");
        factionLabel.setForeground(Color.WHITE);
        northPanel.add(factionLabel);
        
        factionComboBox = new JComboBox<>(FACTIONS);
        factionComboBox.addActionListener(e -> updateShipSelectionForFaction());
        northPanel.add(factionComboBox);
        
        contentPanel.add(northPanel, BorderLayout.NORTH);
        
        // Center Panel - Ship Selection and Modifiers
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Ship Selection Panel
        shipSelectionPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        shipSelectionPanel.setOpaque(false);
        shipSelectionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Ship Selection"));
        
        // Initial ship selection panel will be updated when faction is selected
        initializeShipSelectionPanel();
        
        JScrollPane shipScrollPane = new JScrollPane(shipSelectionPanel);
        shipScrollPane.setOpaque(false);
        shipScrollPane.getViewport().setOpaque(false);
        
        centerPanel.add(shipScrollPane, BorderLayout.CENTER);
        
        // Modifiers Panel
        modifiersPanel = new JPanel(new GridLayout(0, 3, 10, 5));
        modifiersPanel.setOpaque(false);
        modifiersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Combat Modifiers"));
        
        initializeModifiersPanel();
        
        centerPanel.add(modifiersPanel, BorderLayout.SOUTH);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // South Panel - Results and Buttons
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        
        resultsArea = new JTextArea(10, 40);
        resultsArea.setEditable(false);
        resultsArea.setBackground(new Color(240, 240, 240));
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        
        southPanel.add(resultsScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        JButton rollButton = new JButton("Roll for Combat");
        rollButton.addActionListener(e -> performCombatRoll());
        
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetSimulator());
        
        buttonPanel.add(rollButton);
        buttonPanel.add(resetButton);
        
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        contentPanel.add(southPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }
    
    private void updateShipSelectionForFaction() {
        String selectedFaction = (String) factionComboBox.getSelectedItem();
        
        // Clear existing panel and quantities
        shipSelectionPanel.removeAll();
        shipQuantities.clear();
        
        if (selectedFaction == null || selectedFaction.equals("Select Faction")) {
            // Display default ships if no faction selected
            initializeShipSelectionPanel();
            return;
        }
        
        // Get faction-specific unit overrides
        Map<String, String[]> factionUnits = FACTION_SPECIFIC_UNITS.get(selectedFaction);
        
        // Process standard ship types first
        for (Map.Entry<String, String[]> entry : BASE_SHIP_TYPES.entrySet()) {
            String shipCategory = entry.getKey();
            String[] variants = entry.getValue();
            
            // Skip Infantry and Mech for non-Nekro factions unless explicitly allowed
            if ((shipCategory.equals("Infantry") || shipCategory.equals("Mech")) && 
                !selectedFaction.equals("The Nekro Virus")) {
                continue;
            }
            
            // Use faction-specific variants if available
            if (factionUnits != null && factionUnits.containsKey(shipCategory)) {
                variants = factionUnits.get(shipCategory);
            }
            
            // Skip Mech for NRA (will add Z-Grav Eidolon specially)
            if (selectedFaction.equals("The Naaz-Rokha Alliance") && shipCategory.equals("Mech")) {
                continue;
            }
            
            // Add each ship variant
            for (String variant : variants) {
                addShipToPanel(variant);
            }
        }
        
        // Handle special cases
        if (selectedFaction.equals("The Naaz-Rokha Alliance")) {
            // Add Z-Grav Eidolon for NRA
            addShipToPanel("Z-Grav Eidolon");
        }
        
        // Add the faction's flagship
        String[] flagships = FACTION_FLAGSHIPS.get(selectedFaction);
        if (flagships != null && flagships.length > 0) {
            JPanel flagshipPanel = new JPanel();
            flagshipPanel.setLayout(new BoxLayout(flagshipPanel, BoxLayout.Y_AXIS));
            flagshipPanel.setOpaque(false);
            
            JLabel flagshipLabel = new JLabel("Flagship: " + flagships[0]);
            flagshipLabel.setForeground(Color.WHITE);
            flagshipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1, 1);
            JSpinner quantitySpinner = new JSpinner(model);
            quantitySpinner.setMaximumSize(new Dimension(80, 30));
            quantitySpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            flagshipPanel.add(flagshipLabel);
            flagshipPanel.add(Box.createVerticalStrut(5));
            flagshipPanel.add(quantitySpinner);
            
            shipSelectionPanel.add(flagshipPanel);
            
            // Store the flagship spinner
            shipQuantities.put("Flagship:" + flagships[0], quantitySpinner);
        }
        
        shipSelectionPanel.revalidate();
        shipSelectionPanel.repaint();
    }
    
    private void addShipToPanel(String shipName) {
        JPanel shipPanel = new JPanel();
        shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.Y_AXIS));
        shipPanel.setOpaque(false);
        
        JLabel shipLabel = new JLabel(shipName);
        shipLabel.setForeground(Color.WHITE);
        shipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 20, 1);
        JSpinner quantitySpinner = new JSpinner(model);
        quantitySpinner.setMaximumSize(new Dimension(80, 30));
        quantitySpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        shipPanel.add(shipLabel);
        shipPanel.add(Box.createVerticalStrut(5));
        shipPanel.add(quantitySpinner);
        
        shipSelectionPanel.add(shipPanel);
        
        // Store the spinner for later access
        shipQuantities.put(shipName, quantitySpinner);
    }
    
    private void initializeShipSelectionPanel() {
        shipSelectionPanel.removeAll();
        shipQuantities.clear();
        
        // Add default ship options (no faction selected)
        addShipToPanel("Carrier I");
        addShipToPanel("Carrier II");
        addShipToPanel("Cruiser I");
        addShipToPanel("Cruiser II");
        addShipToPanel("Destroyer I");
        addShipToPanel("Destroyer II");
        addShipToPanel("Dreadnought I");
        addShipToPanel("Dreadnought II");
        addShipToPanel("Fighter I");
        addShipToPanel("Fighter II");
        
        shipSelectionPanel.revalidate();
        shipSelectionPanel.repaint();
    }
    
    private void initializeModifiersPanel() {
        modifiersPanel.removeAll();
        modifierCheckboxes.clear();
        
        for (String modifier : MODIFIERS) {
            JCheckBox checkBox = new JCheckBox(modifier);
            checkBox.setForeground(Color.WHITE);
            checkBox.setOpaque(false);
            modifiersPanel.add(checkBox);
            modifierCheckboxes.add(checkBox);
        }
        
        modifiersPanel.revalidate();
        modifiersPanel.repaint();
    }
    
    private void performCombatRoll() {
        // Gather selected ships
        List<Ship> fleet = new ArrayList<>();
        
        // Process regular ships
        for (Map.Entry<String, JSpinner> entry : shipQuantities.entrySet()) {
            String shipKey = entry.getKey();
            int quantity = (Integer) entry.getValue().getValue();
            
            if (quantity > 0) {
                String shipType;
                if (shipKey.startsWith("Flagship:")) {
                    // Handle flagship
                    String flagshipName = shipKey.substring(9);
                    shipType = convertFlagshipNameToCode(flagshipName);
                } else {
                    // Handle regular ships - convert ship name to code
                    shipType = UNIT_NAME_TO_CODE.getOrDefault(shipKey, "");
                    if (shipType.isEmpty()) {
                        resultsArea.append("Warning: Unknown ship type: " + shipKey + "\n");
                        continue;
                    }
                }
                
                // Create ships and add to fleet
                for (int i = 0; i < quantity; i++) {
                    try {
                        Ship ship = createShip(shipType);
                        fleet.add(ship);
                    } catch (IllegalArgumentException e) {
                        resultsArea.append("Error creating ship: " + e.getMessage() + "\n");
                    }
                }
            }
        }
        
        if (fleet.isEmpty()) {
            resultsArea.setText("No ships selected. Please select at least one ship.");
            return;
        }
        
        // Gather selected modifiers
        List<RollModifier> modifiers = new ArrayList<>();
        for (int i = 0; i < modifierCheckboxes.size(); i++) {
            JCheckBox checkBox = modifierCheckboxes.get(i);
            if (checkBox.isSelected()) {
                RollModifier modifier = convertToRollModifier(checkBox.getText());
                if (modifier != null) {
                    modifiers.add(modifier);
                }
            }
        }
        
        // Perform combat simulation
        resultsArea.setText(""); // Clear previous results
        
        // Print applied modifiers
        if (!modifiers.isEmpty()) {
            resultsArea.append("Applied modifiers: " + 
                modifiers.stream()
                    .map(RollModifier::getFlag)
                    .collect(Collectors.joining(", ")) + "\n");
        }
        
        resultsArea.append("\nRolling for combat...\n");
        
        Map<String, List<Ship>> groupedShips = fleet.stream()
            .collect(Collectors.groupingBy(Ship::getShipType));
        
        AtomicInteger grandTotalHits = new AtomicInteger(0);
        
        groupedShips.forEach((type, ships) -> {
            int totalHits = 0;
            List<Integer> allPreModifierRolls = new ArrayList<>();
            List<Integer> allPostModifierRolls = new ArrayList<>();
            boolean anyModified = false;
            
            for (Ship ship : ships) {
                CombatResult result = ship.rollDice(modifiers, fleet);
                totalHits += result.getHits();
                allPreModifierRolls.addAll(result.getPreModifierRolls());
                allPostModifierRolls.addAll(result.getPostModifierRolls());
                if (result.wasModified()) {
                    anyModified = true;
                }
            }
            
            // Print results
            resultsArea.append(String.format("%d %s rolled %s",
                ships.size(),
                ships.size() == 1 ? type : type + "s",
                allPreModifierRolls));
            
            // Only print post-modifier rolls if modifications were applied
            if (anyModified) {
                resultsArea.append(String.format(" (modified to %s)", allPostModifierRolls));
            }
            
            resultsArea.append(String.format(" with %d hits\n", totalHits));
            
            grandTotalHits.addAndGet(totalHits);
        });
        
        resultsArea.append(String.format("\nTotal hits: %d\n", grandTotalHits.get()));
    }
    
    private void resetSimulator() {
        // Reset ship quantities
        for (JSpinner spinner : shipQuantities.values()) {
            spinner.setValue(0);
        }
        
        // Reset modifiers
        for (JCheckBox checkBox : modifierCheckboxes) {
            checkBox.setSelected(false);
        }
        
        // Reset faction selection
        factionComboBox.setSelectedIndex(0);
        
        // Clear results
        resultsArea.setText("");
    }
    
    private String convertFlagshipNameToCode(String flagshipName) {
        // Map flagship names to their codes as in the original CombatSimulator
        switch (flagshipName) {
            case "Duha Menaimon": return "duha_menaimon";
            case "Arc Secundus": return "arc_secundus";
            case "Son of Ragh": return "son_of_ragh";
            case "Inferno": return "inferno";
            case "Wrath of Kenara": return "wrath_of_kenara";
            case "Genesis": return "genesis";
            case "Hil Colish": return "hil_colish";
            case "0.0.1": return "001";
            case "Fourth Moon": return "fourth_moon";
            case "Matriarch": return "matriarch";
            case "Alastor": return "alastor";
            case "C'Morran N'orr": return "cmorran_norr";
            case "J.N.S. Hylarim": return "jns_hylarim";
            case "Salai Sai Corian": return "salai_sai_corian";
            case "Loncarra Ssodu": return "loncarra_ssodu";
            case "Van Hauge": return "van_hauge";
            case "Y'sia Y'ssrila": return "ysia_yssrila";
            case "Quetzcoatl": return "quetzecoatl";
            case "Dynamo": return "dynamo";
            case "Arvicon Rex": return "arvicon_rex";
            case "Visz el Vir": return "visz_el_vir";
            case "Memoria": return "memoria";
            case "Ouranos": return "ouranos";
            case "Terror Between": return "terror_between";
            default: return flagshipName.toLowerCase().replace(' ', '_');
        }
    }
    
    private RollModifier convertToRollModifier(String text) {
        switch (text) {
            case "-1 All Rolls": return RollModifier.MINUS_ONE_ALL;
            case "+1 All Rolls": return RollModifier.PLUS_ONE_ALL;
            case "Prophecy of Ixth (+1 to fighter rolls)": return RollModifier.PLUS_ONE_FIGHTER;
            case "Fighter Prototype (+2 to fighter rolls)": return RollModifier.PLUS_TWO_FIGHTER;
            case "+2 Flagship Rolls": return RollModifier.PLUS_TWO_FLAGSHIP;
            case "+2 Mech Rolls": return RollModifier.PLUS_TWO_MECH;
            default: return null;
        }
    }
    
    // This method maps to the original createShip method
    private Ship createShip(String type) {
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
            case "i" -> new Infantry();
            case "i2" -> new Infantry(2);
            case "i3" -> new Infantry(3);
            case "m" -> new Mech();
            case "z_grav_eidolon" -> new Mech(2, 8);
            case "hil_colish" -> new Flagship(1, 5);
            case "arc_secundus", "son_of_ragh", "inferno", "dynamo", "genesis", "001", 
                 "arvicon_rex", "memoria2", "terror_between", "ysia_yssrila" -> new Flagship(2, 5);
            case "salai_sai_corian" -> new Flagship(1, 7);
            case "duha_menaimon", "quetzecoatl", "artemiris", "wrath_of_kenara", 
                 "fourth_moon", "memoria", "ouranos", "loncarra_ssodu" -> new Flagship(2, 7);
            case "matriarch", "alastor", "van_hauge" -> new Flagship(2, 9);
            case "cmorran_norr" -> new Flagship(2, 6, "C'Morran N'orr");
            case "jns_hylarim" -> new Flagship(2, 6, "J.N.S. Hylarim");
            case "visz_el_vir" -> new Flagship(2, 9, "Visz el Vir");
            default -> throw new IllegalArgumentException("Unknown ship type: " + type);
        };
    }
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run the application
        SwingUtilities.invokeLater(() -> {
            new CombatSimulator().setVisible(true);
        });
    }
}