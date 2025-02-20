import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


public class CombatSimulator extends JFrame {
    private JPanel shipSelectionPanel;
    private JComboBox<String> factionComboBox;
    private JTextField searchField;
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
        BASE_SHIP_TYPES.put("War Sun", new String[]{"War Sun"});
        // Infantry and Mech only for Nekro by default
        BASE_SHIP_TYPES.put("Infantry", new String[]{"Infantry I", "Infantry II/Spec Ops I"});
        BASE_SHIP_TYPES.put("Mech", new String[]{"Mech"});
    }
    
    // Faction-specific unit overrides
    private static final Map<String, Map<String, String[]>> FACTION_SPECIFIC_UNITS = new HashMap<>();
    static {
        // Sardakk N'orr - Exotriremes
        Map<String, String[]> sardakkUnits = new HashMap<>();
        sardakkUnits.put("Dreadnought", new String[]{"Exotrireme I", "Exotrireme II"});
        FACTION_SPECIFIC_UNITS.put("The Sardakk N'orr", sardakkUnits);

        // Federation of Sol - Advanced Carriers
        Map<String, String[]> solUnits = new HashMap<>();
        solUnits.put("Carrier", new String[]{"Advanced Carrier I", "Advanced Carrier II"});
        FACTION_SPECIFIC_UNITS.put("The Federation of Sol", solUnits);

        // Embers of Muaat - Prototype War Suns
        Map<String, String[]> muaatUnits = new HashMap<>();
        muaatUnits.put("War Sun", new String[]{"Prototype War Sun I", "Prototype War Sun II"});
        FACTION_SPECIFIC_UNITS.put("The Embers of Muaat", muaatUnits);

        // Titans of Ul - Saturn Engines
        Map<String, String[]> ulUnits = new HashMap<>();
        ulUnits.put("Cruiser", new String[]{"Saturn Engine I", "Saturn Engine II"});
        FACTION_SPECIFIC_UNITS.put("The Titans of Ul", ulUnits);

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
        "<html>Morale Boost<br>(+1 to all combat rolls)</html>", 
        "<html>Prophecy of Ixth<br>(+1 to fighter rolls)</html>", 
        "<html>Fighter Prototype<br>(+2 to fighter rolls)</html>"
    };

    private static final Map<String, String> FACTION_SPECIFIC_MODIFIERS = new HashMap<>(); 
    static {
        FACTION_SPECIFIC_MODIFIERS.put("The Mahact Gene-Sorcerers", "<html>Arvicon Rex's Ability<br>(+2 to it's combat rolls)</html>");
        FACTION_SPECIFIC_MODIFIERS.put("The Nekro Virus", "<html>Nekro Mech's Ability<br>(+2 to their combat rolls)</html>");
    };

    Set<String> modifierFactions = Set.of("The Mahact Gene-Sorcerers", "The Nekro Virus");
    
    // Define faction abilities as RollModifiers
    private static final Map<String, List<RollModifier>> FACTION_ABILITIES = new HashMap<>();
    static {
        // Sardakk N'orr +1 to all rolls
        FACTION_ABILITIES.put("The Sardakk N'orr", List.of(RollModifier.PLUS_ONE_ALL));
        // Jol-Nar -1 to all rolls
        FACTION_ABILITIES.put("The Universities of Jol-Nar", List.of(RollModifier.MINUS_ONE_ALL));
    }
    
    // Flag to track if Jol-Nar flagship is present (negates faction penalty)
    private boolean hasJolNarFlagship = false;
    
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
        UNIT_NAME_TO_CODE.put("War Sun", "ws");
        
        // Faction-specific units
        UNIT_NAME_TO_CODE.put("Exotrireme", "dr");
        UNIT_NAME_TO_CODE.put("Exotrireme II", "dr");
        UNIT_NAME_TO_CODE.put("Advanced Carrier I", "c");
        UNIT_NAME_TO_CODE.put("Advanced Carrier II", "c");
        UNIT_NAME_TO_CODE.put("Prototype War Sun I", "ws");
        UNIT_NAME_TO_CODE.put("Prototype War Sun II", "ws");
        UNIT_NAME_TO_CODE.put("Saturn Engine I", "cr");
        UNIT_NAME_TO_CODE.put("Saturn Engine II", "cr2");
        UNIT_NAME_TO_CODE.put("Super Dreadnought I", "dr2");
        UNIT_NAME_TO_CODE.put("Super Dreadnought II", "dr3");
        UNIT_NAME_TO_CODE.put("Hybrid Crystal Fighter I", "f2");
        UNIT_NAME_TO_CODE.put("Hybrid Crystal Fighter II", "f3");
        UNIT_NAME_TO_CODE.put("Strike Wing Alpha I", "d2");
        UNIT_NAME_TO_CODE.put("Strike Wing Alpha II", "d3");
        UNIT_NAME_TO_CODE.put("Z-Grav Eidolon", "z_grav_eidolon");
    }
    
    // Custom combo box model to implement search functionality
    private class SearchableComboBoxModel extends DefaultComboBoxModel<String> {
        private final String[] allItems;
        private String filterText = "";
        
        public SearchableComboBoxModel(String[] items) {
            super(items);
            this.allItems = items.clone();
        }
        
        public void setFilter(String filter) {
            this.filterText = filter.toLowerCase();
            updateFilteredItems();
        }
        
        private void updateFilteredItems() {
            removeAllElements();
            
            // If filter is empty, add all items
            if (filterText.isEmpty()) {
                for (String item : allItems) {
                    addElement(item);
                }
                return;
            }
            
            // Add only items that match the filter
            for (String item : allItems) {
                if (item.toLowerCase().contains(filterText)) {
                    addElement(item);
                }
            }
        }
    }
    
    public CombatSimulator() {
        setTitle("Twilight Imperium 4 Combat Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Try to load a background image
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/background.png"));
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
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // North Panel - Search and Faction Selection
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search Faction:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(15);
        searchField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        searchPanel.add(searchField);
        
        // Faction Selection Panel
        JPanel factionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        factionPanel.setOpaque(false);
        
        JLabel factionLabel = new JLabel("Select Faction:");
        factionLabel.setForeground(Color.WHITE);
        factionLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        factionPanel.add(factionLabel);
        
        // Initialize the combo box with the searchable model
        SearchableComboBoxModel comboBoxModel = new SearchableComboBoxModel(FACTIONS);
        factionComboBox = new JComboBox<>(comboBoxModel);
        factionComboBox.setFont(new Font("Monospaced", Font.PLAIN, 16));
        factionComboBox.setMaximumRowCount(10); // Show more items in the dropdown
        factionComboBox.setPreferredSize(new Dimension(300, 30));
        
        // Add listener to update based on faction selection
        factionComboBox.addActionListener(e -> {
            updateShipSelectionForFaction();
            updateModifiersForFaction();
            checkForJolNarFlagship();
        });
        factionPanel.add(factionComboBox);
        
        // Add search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearch();
            }
            
            private void updateSearch() {
                String searchText = searchField.getText();
                SearchableComboBoxModel model = (SearchableComboBoxModel) factionComboBox.getModel();
                model.setFilter(searchText);
                
                // If there's only one item left after filtering, select it automatically
                if (model.getSize() == 1 && !searchText.isEmpty()) {
                    factionComboBox.setSelectedIndex(0);
                }
                
                // Make sure the popup is visible when typing
                if (model.getSize() > 0 && !searchText.isEmpty()) {
                    factionComboBox.setPopupVisible(true);
                }
            }
        });

        // Add key listeners to the search field for keyboard navigation
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                
                // Only process if the dropdown is visible
                if (factionComboBox.isPopupVisible()) {
                    switch (keyCode) {
                        case KeyEvent.VK_DOWN:
                            // Select next item
                            int currentIndex = factionComboBox.getSelectedIndex();
                            int nextIndex = (currentIndex + 1) % factionComboBox.getModel().getSize();
                            factionComboBox.setSelectedIndex(nextIndex);
                            break;
                            
                        case KeyEvent.VK_UP:
                            // Select previous item
                            currentIndex = factionComboBox.getSelectedIndex();
                            if (currentIndex > 0) {
                                factionComboBox.setSelectedIndex(currentIndex - 1);
                            } else {
                                // Wrap around to bottom
                                factionComboBox.setSelectedIndex(factionComboBox.getModel().getSize() - 1);
                            }
                            break;
                            
                        case KeyEvent.VK_ENTER:
                            // Select current item and close dropdown
                            // This will automatically trigger the actionListener
                            factionComboBox.setPopupVisible(false);
                            // Transfer focus back to main panel to avoid further input to search field
                            mainPanel.requestFocusInWindow();
                            break;
                            
                        case KeyEvent.VK_ESCAPE:
                            // Just close the dropdown
                            factionComboBox.setPopupVisible(false);
                            break;
                    }
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    // If dropdown isn't visible, show it when pressing down arrow
                    factionComboBox.setPopupVisible(true);
                }
            }
        });

        // Add focus listener to improve search field behavior
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // When search field gets focus, show dropdown if there are filtered results
                SearchableComboBoxModel model = (SearchableComboBoxModel) factionComboBox.getModel();
                if (model.getSize() > 0 && !searchField.getText().isEmpty()) {
                    factionComboBox.setPopupVisible(true);
                }
            }
        });
        
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(factionPanel, BorderLayout.CENTER);
        
        contentPanel.add(northPanel, BorderLayout.NORTH);
        
        // Center Panel - Ship Selection and Modifiers
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Ship Selection Panel with styled title
        shipSelectionPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        shipSelectionPanel.setOpaque(false);
        
        // Create a styled titled border with larger white text on black background
        TitledBorder shipBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Ship Selection");
        shipBorder.setTitleFont(new Font("Monospaced", Font.BOLD, 16));
        shipBorder.setTitleColor(Color.WHITE);
        shipSelectionPanel.setBorder(shipBorder);
        
        // Add a component listener to paint the black background for the title
        shipSelectionPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                try {
                    // Get the border title position and paint a black background
                    TitledBorder border = (TitledBorder) shipSelectionPanel.getBorder();
                    int titleWidth = border.getTitleFont().getStringBounds(border.getTitle(), 
                            ((Graphics2D)shipSelectionPanel.getGraphics()).getFontRenderContext()).getBounds().width;
                    int titleX = shipSelectionPanel.getInsets().left + 5;
                    int titleY = 0;
                    
                    // Create a black panel behind the title
                    JPanel titleBackPanel = new JPanel();
                    titleBackPanel.setBounds(titleX - 5, titleY, titleWidth + 10, 22);
                    titleBackPanel.setBackground(Color.BLACK);
                    shipSelectionPanel.add(titleBackPanel);
                    shipSelectionPanel.setComponentZOrder(titleBackPanel, 0);
                } catch (Exception ex) {
                    // Handle potential null pointer exception if graphics context isn't ready
                }
            }
        });
        
        // Initial ship selection panel will be updated when faction is selected
        initializeShipSelectionPanel();
        
        JScrollPane shipScrollPane = new JScrollPane(shipSelectionPanel);
        shipScrollPane.setOpaque(false);
        shipScrollPane.getViewport().setOpaque(false);
        
        centerPanel.add(shipScrollPane, BorderLayout.CENTER);
        
        // Modifiers Panel with styled title
        modifiersPanel = new JPanel(new GridLayout(0, 3, 10, 5));
        modifiersPanel.setOpaque(false);
        
        TitledBorder modifiersBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Combat Modifiers");
        modifiersBorder.setTitleFont(new Font("Monospaced", Font.BOLD, 16));
        modifiersBorder.setTitleColor(Color.WHITE);
        modifiersPanel.setBorder(modifiersBorder);
        
        // Add similar component listener for modifiers panel
        modifiersPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                try {
                    TitledBorder border = (TitledBorder) modifiersPanel.getBorder();
                    int titleWidth = border.getTitleFont().getStringBounds(border.getTitle(), 
                            ((Graphics2D)modifiersPanel.getGraphics()).getFontRenderContext()).getBounds().width;
                    int titleX = modifiersPanel.getInsets().left + 5;
                    int titleY = 0;
                    
                    JPanel titleBackPanel = new JPanel();
                    titleBackPanel.setBounds(titleX - 5, titleY, titleWidth + 10, 22);
                    titleBackPanel.setBackground(Color.BLACK);
                    modifiersPanel.add(titleBackPanel);
                    modifiersPanel.setComponentZOrder(titleBackPanel, 0);
                } catch (Exception ex) {
                    // Handle potential null pointer exception
                }
            }
        });
        
        initializeModifiersPanel();
        
        centerPanel.add(modifiersPanel, BorderLayout.SOUTH);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // South Panel - Results and Buttons
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        
        resultsArea = new JTextArea(10, 40);
        resultsArea.setEditable(false);
        resultsArea.setBackground(new Color(240, 240, 240));
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        
        southPanel.add(resultsScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        JButton rollButton = new JButton("Roll for Combat");
        rollButton.setFont(new Font("Monospaced", Font.PLAIN, 13));
        rollButton.addActionListener(e -> performCombatRoll());
        
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Monospaced", Font.PLAIN, 13));
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
            flagshipLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
            
            SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1, 1);
            JSpinner quantitySpinner = new JSpinner(model);
            quantitySpinner.setFont(new Font("Monospaced", Font.PLAIN, 13));
            quantitySpinner.setMaximumSize(new Dimension(80, 30));
            quantitySpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Add a change listener to check for Jol-Nar flagship
            quantitySpinner.addChangeListener(e -> checkForJolNarFlagship());
            
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
    
    private void checkForJolNarFlagship() {
        String selectedFaction = (String) factionComboBox.getSelectedItem();
        if (!"The Universities of Jol-Nar".equals(selectedFaction)) {
            hasJolNarFlagship = false;
            return;
        }
        
        // Check if Jol-Nar flagship is present
        JSpinner flagshipSpinner = shipQuantities.get("Flagship:J.N.S. Hylarim");
        hasJolNarFlagship = flagshipSpinner != null && (Integer)flagshipSpinner.getValue() > 0;
    }
    
    private void updateModifiersForFaction() {
        String selectedFaction = (String) factionComboBox.getSelectedItem();
    
        // Clear all existing modifiers
        modifiersPanel.removeAll();
        modifierCheckboxes.clear();
        
        // Re-add the standard modifiers
        for (String modifier : MODIFIERS) {
            JCheckBox checkBox = new JCheckBox(modifier);
            checkBox.setForeground(Color.WHITE);
            checkBox.setFont(new Font("Monospaced", Font.PLAIN, 13));
            checkBox.setOpaque(false);
            
            modifiersPanel.add(checkBox);
            modifierCheckboxes.add(checkBox);
        }
        
        // Add faction-specific modifiers if needed
        if (selectedFaction != null && !selectedFaction.equals("Select Faction")) {
            if (modifierFactions.contains(selectedFaction)) {
                String modifier = FACTION_SPECIFIC_MODIFIERS.get(selectedFaction);
    
                JCheckBox checkBox = new JCheckBox(modifier);
                checkBox.setForeground(Color.WHITE);
                checkBox.setFont(new Font("Monospaced", Font.PLAIN, 13));
                checkBox.setOpaque(false);
                
                modifiersPanel.add(checkBox);
                modifierCheckboxes.add(checkBox);
            }
        }
        
        modifiersPanel.revalidate();
        modifiersPanel.repaint();
    }
    
    private void addShipToPanel(String shipName) {
        JPanel shipPanel = new JPanel();
        shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.Y_AXIS));
        shipPanel.setOpaque(false);
        
        JLabel shipLabel = new JLabel(shipName);
        shipLabel.setForeground(Color.WHITE);
        shipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        shipLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 20, 1);
        JSpinner quantitySpinner = new JSpinner(model);
        quantitySpinner.setFont(new Font("Monospaced", Font.PLAIN, 13));
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
        addShipToPanel("War Sun");
        
        shipSelectionPanel.revalidate();
        shipSelectionPanel.repaint();
    }
    
    private void initializeModifiersPanel() {
        modifiersPanel.removeAll();
        modifierCheckboxes.clear();
        
        for (String modifier : MODIFIERS) {
            JCheckBox checkBox = new JCheckBox(modifier);
            checkBox.setForeground(Color.WHITE);
            checkBox.setFont(new Font("Monospaced", Font.PLAIN, 13));
            checkBox.setOpaque(false);
            
            modifiersPanel.add(checkBox);
            modifierCheckboxes.add(checkBox);
        }
        
        modifiersPanel.revalidate();
        modifiersPanel.repaint();
    }
    
    private void performCombatRoll() {
        String selectedFaction = (String) factionComboBox.getSelectedItem();
        if (selectedFaction == null || selectedFaction.equals("Select Faction")) {
            resultsArea.setText("Please select a faction before rolling for combat.");
            return;
        }
        
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
        
        // Gather modifiers from user selection
        List<RollModifier> userModifiers = new ArrayList<>();
        for (JCheckBox checkBox : modifierCheckboxes) {
            if (checkBox.isSelected()) {
                RollModifier modifier = convertToRollModifier(checkBox.getText());
                if (modifier != null) {
                    userModifiers.add(modifier);
                }
            }
        }
        
        // Add faction abilities (unless canceled by flagship)
        List<RollModifier> factionModifiers = new ArrayList<>();
        if (FACTION_ABILITIES.containsKey(selectedFaction)) {
            List<RollModifier> abilities = FACTION_ABILITIES.get(selectedFaction);
            
            // For Jol-Nar, check if flagship cancels the -1 penalty
            if (selectedFaction.equals("The Universities of Jol-Nar") && hasJolNarFlagship) {
                // Skip adding the penalty
            } else {
                factionModifiers.addAll(abilities);
            }
        }
        
        // Combine all modifiers
        List<RollModifier> allModifiers = new ArrayList<>();
        allModifiers.addAll(userModifiers);
        allModifiers.addAll(factionModifiers);
        
        // Perform combat simulation
        resultsArea.setText(""); // Clear previous results
        
        // Print faction ability information
        if (selectedFaction.equals("The Sardakk N'orr")) {
            resultsArea.append("Sardakk N'orr Faction Ability: +1 to all combat rolls\n");
        } else if (selectedFaction.equals("The Universities of Jol-Nar")) {
            if (hasJolNarFlagship) {
                resultsArea.append("J.N.S. Hylarim negates Jol-Nar's -1 combat penalty\n");
            } else {
                resultsArea.append("Universities of Jol-Nar Faction Penalty: -1 to all combat rolls\n");
            }
        }
        
        // Print applied user modifiers
        if (!userModifiers.isEmpty()) {
            resultsArea.append("Applied modifiers: " + 
                userModifiers.stream()
                    .map(RollModifier::getFlag)
                    .collect(Collectors.joining(", ")) + "\n");
        }
        
        resultsArea.append("Rolling for combat...\n");
        
        Map<String, List<Ship>> groupedShips = fleet.stream()
            .collect(Collectors.groupingBy(Ship::getShipType));
        
        AtomicInteger grandTotalHits = new AtomicInteger(0);
        
        groupedShips.forEach((type, ships) -> {
            int totalHits = 0;
            List<Integer> allPreModifierRolls = new ArrayList<>();
            List<Integer> allPostModifierRolls = new ArrayList<>();
            boolean anyModified = false;
            
            for (Ship ship : ships) {
                CombatResult result = ship.rollDice(allModifiers, fleet);
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
        
        resultsArea.append(String.format("Total hits: %d\n", grandTotalHits.get()));
    }
    
    private void resetSimulator() {
        // Reset ship quantities
        for (JSpinner spinner : shipQuantities.values()) {
            spinner.setValue(0);
        }
        
        // Reset modifiers
        for (JCheckBox checkBox : modifierCheckboxes) {
            checkBox.setSelected(false);
            checkBox.setEnabled(true);
        }
        
        // Reset faction selection
        factionComboBox.setSelectedIndex(0);

        // Clear the search field
        searchField.setText("");
        
        // Clear results
        resultsArea.setText("");
        
        // Reset Jol-Nar flagship flag
        hasJolNarFlagship = false;
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
        if (text.contains("Morale Boost")) return RollModifier.PLUS_ONE_ALL;
        if (text.contains("Prophecy of Ixth")) return RollModifier.PLUS_ONE_FIGHTER;
        if (text.contains("Fighter Prototype")) return RollModifier.PLUS_TWO_FIGHTER;
        if (text.contains("Arvicon Rex's Ability")) return RollModifier.PLUS_TWO_FLAGSHIP;
        if (text.contains("Nekro Mech's Ability")) return RollModifier.PLUS_TWO_MECH;
        return null;
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
            case "ws" -> new WarSun();
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