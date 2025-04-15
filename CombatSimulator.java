import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
        BASE_SHIP_TYPES.put("Infantry", new String[]{"Infantry I", "Infantry II"});
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
        nekroUnits.put("Infantry", new String[]{"Infantry I", "Infantry II"});
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
        FACTION_SPECIFIC_MODIFIERS.put("The Mahact Gene-Sorcerers", 
                                     "<html>Arvicon Rex's Ability<br>(+2 to it's combat rolls)</html>");
        FACTION_SPECIFIC_MODIFIERS.put("The Nekro Virus", 
                                     "<html>Nekro Mech's Ability<br>(+2 to their combat rolls)</html>");
        FACTION_SPECIFIC_MODIFIERS.put("The Naaz-Rokha Alliance", 
                                     "<html>Supercharge Technology<br>(+1 to all combat rolls)</html>");
        FACTION_SPECIFIC_MODIFIERS.put("The Winnu", 
                                     "<html>Commander Rickar Rickani<br>(+2 to all combat rolls)</html>");
    };

    Set<String> modifierFactions = Set.of("The Mahact Gene-Sorcerers", "The Nekro Virus",
                                          "The Naaz-Rokha Alliance", "The Winnu");
    
    // Define faction abilities as RollModifiers
    private static final Map<String, List<RollModifier>> FACTION_ABILITIES = new HashMap<>();
    static {
        // Sardakk N'orr +1 to all rolls
        FACTION_ABILITIES.put("The Sardakk N'orr", List.of(RollModifier.PLUS_ONE_ALL));
        // Jol-Nar -1 to all rolls
        FACTION_ABILITIES.put("The Universities of Jol-Nar", List.of(RollModifier.MINUS_ONE_ALL));
    }
    
    // Flag to track if Jol-Nar flagship is present (negates faction penalty)
    // private boolean hasJolNarFlagship = false;  // This line is deleted
    
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
        FACTION_FLAGSHIPS.put("The Nomad", new String[]{"Memoria", "Memoria II"});
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
        UNIT_NAME_TO_CODE.put("Infantry II", "i2");
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
        
        // Disable automatic selection when using keyboard navigation
        factionComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        // Modify the factionComboBox actionListener
        factionComboBox.addActionListener(e -> {
            // Only update ships and modifiers if the event was triggered by user selection
            // and not just by highlighting with arrow keys
            if (e.getActionCommand().equals("comboBoxEdited") || e.getModifiers() != 0) {
                updateShipSelectionForFaction();
                updateModifiersForFaction();
            }
        });
        factionPanel.add(factionComboBox);

        // Also update the JComboBox itself to handle mouse selection properly
        factionComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // When mouse clicked, confirm the selection and update UI
                updateShipSelectionForFaction();
                updateModifiersForFaction();
            }
        });
        
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
                
                if (factionComboBox.isPopupVisible()) {
                    switch (keyCode) {
                        case KeyEvent.VK_DOWN:
                            // Move to next item without looping
                            int nextIndex = factionComboBox.getSelectedIndex() + 1;
                            if (nextIndex < factionComboBox.getModel().getSize()) {
                                factionComboBox.setSelectedIndex(nextIndex);
                            }
                            // Prevent default behavior
                            e.consume();
                            break;
                            
                        case KeyEvent.VK_UP:
                            // Move to previous item without looping
                            int prevIndex = factionComboBox.getSelectedIndex() - 1;
                            if (prevIndex >= 0) {
                                factionComboBox.setSelectedIndex(prevIndex);
                            }
                            // Prevent default behavior
                            e.consume();
                            break;
                            
                        case KeyEvent.VK_ENTER:
                            // Explicitly commit selection
                            Object selectedItem = factionComboBox.getSelectedItem();
                            SearchableComboBoxModel model = (SearchableComboBoxModel) factionComboBox.getModel();
                            model.setSelectedItem(selectedItem);
                            
                            // Update UI based on selection
                            updateShipSelectionForFaction();
                            updateModifiersForFaction();
                            
                            // Close dropdown and shift focus
                            factionComboBox.setPopupVisible(false);
                            mainPanel.requestFocusInWindow();
                            break;
                            
                        case KeyEvent.VK_ESCAPE:
                            // Close dropdown without changing selection
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
            for (String flagship : flagships) {
                JPanel flagshipPanel = new JPanel();
                flagshipPanel.setLayout(new BoxLayout(flagshipPanel, BoxLayout.Y_AXIS));
                flagshipPanel.setOpaque(false);
                
                JLabel flagshipLabel = new JLabel("Flagship: " + flagship);
                flagshipLabel.setForeground(Color.WHITE);
                flagshipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                flagshipLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
                
                SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1, 1);
                JSpinner quantitySpinner = new JSpinner(model);
                quantitySpinner.setFont(new Font("Monospaced", Font.PLAIN, 13));
                quantitySpinner.setMaximumSize(new Dimension(80, 30));
                quantitySpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                flagshipPanel.add(flagshipLabel);
                flagshipPanel.add(Box.createVerticalStrut(5));
                flagshipPanel.add(quantitySpinner);
                
                shipSelectionPanel.add(flagshipPanel);
                
                // Store the flagship spinner
                shipQuantities.put("Flagship:" + flagship, quantitySpinner);
            }
        }
        
        shipSelectionPanel.revalidate();
        shipSelectionPanel.repaint();
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
            
            // Special Winnu flagship ability with additional dice spinner
            if (selectedFaction.equals("The Winnu")) {
                // Use a horizontal panel instead of vertical
                JPanel winnuPanel = new JPanel(new BorderLayout(5, 0));
                winnuPanel.setOpaque(false);
                
                JCheckBox flagshipCheckBox = new JCheckBox("<html>Salai Sai Corian's Ability<br>(roll additional dice)</html>");
                flagshipCheckBox.setForeground(Color.WHITE);
                flagshipCheckBox.setFont(new Font("Monospaced", Font.PLAIN, 13));
                flagshipCheckBox.setOpaque(false);
                
                // Create a panel for the spinner
                JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.Y_AXIS));
                spinnerPanel.setOpaque(false);

                // Add vertical glue before spinner
                spinnerPanel.add(Box.createVerticalGlue());

                // Create the spinner in its own panel
                JPanel spinnerControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                spinnerControlPanel.setOpaque(false);
                
                SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 5, 1);
                JSpinner diceSpinner = new JSpinner(model);
                diceSpinner.setPreferredSize(new Dimension(60, 25));
                diceSpinner.setMaximumSize(new Dimension(80, 50000));
                diceSpinner.setFont(new Font("Monospaced", Font.PLAIN, 13));
                
                spinnerPanel.add(diceSpinner);
                
                // Add checkbox to LEFT and spinner panel to RIGHT
                winnuPanel.add(flagshipCheckBox, BorderLayout.WEST);
                winnuPanel.add(spinnerPanel, BorderLayout.CENTER);
                
                modifiersPanel.add(winnuPanel);
                modifierCheckboxes.add(flagshipCheckBox);
                
                // Store the spinner for later access
                shipQuantities.put("WinnuFlagshipExtraDice", diceSpinner);
                
                // Enable/disable the spinner based on checkbox selection
                flagshipCheckBox.addActionListener(e -> {
                    diceSpinner.setEnabled(flagshipCheckBox.isSelected());
                });
                diceSpinner.setEnabled(false);
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
            
            factionModifiers.addAll(abilities);
        }
        
        // Check for Winnu flagship special ability
        AtomicBoolean hasWinnuFlagshipAbility = new AtomicBoolean(false);
        AtomicInteger extraWinnuDice = new AtomicInteger(0);
        
        if (selectedFaction.equals("The Winnu")) {
            for (JCheckBox checkBox : modifierCheckboxes) {
                if (checkBox.isSelected() && checkBox.getText().contains("Salai Sai Corian's Ability")) {
                    hasWinnuFlagshipAbility.set(true);
                    JSpinner extraDiceSpinner = shipQuantities.get("WinnuFlagshipExtraDice");
                    if (extraDiceSpinner != null) {
                        extraWinnuDice.set((Integer) extraDiceSpinner.getValue());
                    }
                    break;
                }
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
            resultsArea.append("Universities of Jol-Nar Faction Penalty: -1 to all combat rolls\n");
        }
        
        // Print applied user modifiers
        if (!userModifiers.isEmpty()) {
            resultsArea.append("Applied modifiers: " + 
                userModifiers.stream()
                    .map(RollModifier::getFlag)
                    .collect(Collectors.joining(", ")) + "\n");
        }

        // Execute Mentak Coalition Ambush ability if applicable
        if (selectedFaction.equals("The Mentak Coalition")) {
            performMentakAmbush(fleet);
        }
        
        // Check for destroyers and offer Anti-Fighter Barrage
        boolean hasDestroyers = fleet.stream().anyMatch(ship -> ship instanceof Destroyer);
        // Check for flagships with AFB abilities
        boolean hasFlagshipWithAFB = fleet.stream()
            .anyMatch(ship -> ship instanceof Flagship && 
                     (((Flagship)ship).isSaar() || ((Flagship)ship).isNomad()));
                      
        if (hasDestroyers || hasFlagshipWithAFB) {
            performAntiFighterBarrage(fleet);
        }
        
        resultsArea.append("== ROLLING FOR COMBAT ==\n");
        
        Map<String, List<Ship>> groupedShips = fleet.stream()
            .collect(Collectors.groupingBy(Ship::getShipType));
        
        AtomicInteger grandTotalHits = new AtomicInteger(0);
        
        groupedShips.forEach((type, ships) -> {
            int totalHits = 0;
            List<Integer> allPreModifierRolls = new ArrayList<>();
            List<Integer> allPostModifierRolls = new ArrayList<>();
            boolean anyModified = false;
            
            for (Ship ship : ships) {
                // Handle Winnu flagship extra dice
                if (hasWinnuFlagshipAbility.get() && 
                    ship instanceof Flagship && 
                    ((Flagship)ship).isWinnu()) {
                    
                    // Roll extra dice for Winnu flagship
                    for (int i = 0; i < extraWinnuDice.get(); i++) {
                        CombatResult result = ship.rollDice(allModifiers, fleet);
                        totalHits += result.getHits();
                        allPreModifierRolls.addAll(result.getPreModifierRolls());
                        allPostModifierRolls.addAll(result.getPostModifierRolls());
                        if (result.wasModified()) {
                            anyModified = true;
                        }
                    }
                }
                
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
            case "Memoria II": return "memoria2";
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
        if (text.contains("Supercharge Technology")) return RollModifier.PLUS_ONE_ALL;
        if (text.contains("Commander Rickar Rickani")) return RollModifier.PLUS_TWO_ALL;
        if (text.contains("Salai Sai Corian's Ability")) return null;
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
            case "m" -> new Mech();
            case "ws" -> new WarSun();
            case "z_grav_eidolon" -> new Mech(2, 8);
            case "hil_colish" -> new Flagship(1, 5, "Hil Colish");
            case "arc_secundus" -> new Flagship(2, 5, "Arc Secundus");
            case "son_of_ragh" -> new Flagship(2, 5, "Son of Ragh");
            case "inferno" -> new Flagship(2, 5, "Inferno");
            case "dynamo" -> new Flagship(2, 5, "Dynamo");
            case "genesis" -> new Flagship(2, 5, "Genesis");
            case "001" -> new Flagship(2, 5, "0.0.1");
            case "arvicon_rex" -> new Flagship(2, 5, "Arvicon Rex");
            case "memoria2" -> new Flagship(2, 5, "Memoria II");
            case "terror_between" -> new Flagship(2, 5, "Terror Between");
            case "ysia_yssrila" -> new Flagship(2, 5, "Y'sia Y'ssrila");
            case "duha_menaimon" -> new Flagship(2, 7, "Duha Menaimon");
            case "quetzecoatl" -> new Flagship(2, 7, "Quetzcoatl");
            case "artemiris" -> new Flagship(2, 7, "Artemiris");
            case "wrath_of_kenara" -> new Flagship(2, 7, "Wrath of Kenara");
            case "fourth_moon" -> new Flagship(2, 7, "Fourth Moon");
            case "memoria" -> new Flagship(2, 7, "Memoria");
            case "ouranos" -> new Flagship(2, 7, "Ouranos");
            case "loncarra_ssodu" -> new Flagship(2, 7, "Loncarra Ssodu");
            case "matriarch" -> new Flagship(2, 9, "Matriarch");
            case "alastor" -> new Flagship(2, 9, "Alastor");
            case "van_hauge" -> new Flagship(2, 9, "Van Hauge");
            case "cmorran_norr" -> new Flagship(2, 6, "C'Morran N'orr");
            case "jns_hylarim" -> new Flagship(2, 6, "J.N.S. Hylarim");
            case "visz_el_vir" -> new Flagship(2, 9, "Visz el Vir");
            case "salai_sai_corian" -> new Flagship(1, 7, "Salai Sai Corian");
            default -> throw new IllegalArgumentException("Unknown ship type: " + type);
        };
    }

    private int performMentakAmbush(List<Ship> fleet) {
        List<Ship> eligibleShips = fleet.stream()
            .filter(ship -> ship instanceof Cruiser || ship instanceof Destroyer)
            .collect(Collectors.toList());
            
        // Only perform ambush if there are eligible ships
        if (eligibleShips.isEmpty()) {
            return 0;
        }
    
        // Ask user if they want to use ambush
        int response = JOptionPane.showConfirmDialog(
            this,
            "Do you want to use the Ambush ability?",
            "Ambush Ability",
            JOptionPane.YES_NO_OPTION
        );
        
        if (response == JOptionPane.NO_OPTION) {
            resultsArea.append("Ambush ability not used.\n\n");
            return 0;
        }
        
        // Ship selection for ambush
        List<Ship> selectedShips = new ArrayList<>();
        
        // If there are multiple eligible ships, let the user choose
        if (eligibleShips.size() > 1) {
            // Create ship selection dialog
            JDialog selectionDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Select Ships for Ambush", true);
            selectionDialog.setLayout(new BorderLayout());

            selectionDialog.setMinimumSize(new Dimension(225, 100));
            
            JPanel shipPanel = new JPanel();
            shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.Y_AXIS));
            
            JLabel instructionLabel = new JLabel("Select ships for Ambush (up to 2):");
            shipPanel.add(instructionLabel);
            
            Map<JCheckBox, Ship> checkBoxMap = new HashMap<>();
            
            // Add a checkbox for each eligible ship
            for (Ship ship : eligibleShips) {
                JCheckBox shipCheckBox = new JCheckBox(ship.getShipType() + " (Combat Value: " + ship.getCombatValue() + ")");
                checkBoxMap.put(shipCheckBox, ship);
                shipPanel.add(shipCheckBox);

                // Add key listener to respond to Enter key
                shipCheckBox.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            shipCheckBox.setSelected(!shipCheckBox.isSelected());
                        }
                    }
                });
            }
            
            JPanel buttonPanel = new JPanel();
            JButton confirmButton = new JButton("Confirm Selection");
            
            confirmButton.addActionListener(e -> {
                List<Ship> tempSelected = new ArrayList<>();
                
                // Get all selected ships
                for (Map.Entry<JCheckBox, Ship> entry : checkBoxMap.entrySet()) {
                    if (entry.getKey().isSelected()) {
                        tempSelected.add(entry.getValue());
                    }
                }
                
                // If more than 2 ships were selected, take the best 2 based on combat value
                if (tempSelected.size() > 2) {
                    // Inform the user that more than 2 ships were selected
                    resultsArea.append("More than 2 ships were selected. The 2 ships with the best combat values were automatically chosen.\n\n");

                    // Sort by combat value (lower is better in Twilight Imperium)
                    tempSelected.sort(Comparator.comparing(Ship::getCombatValue));
                    
                    // Take the 2 ships with the lowest combat values (best chance to hit)
                    selectedShips.add(tempSelected.get(0));
                    selectedShips.add(tempSelected.get(1));
                } else {
                    // If 2 or fewer ships were selected, use all of them
                    selectedShips.addAll(tempSelected);
                }
                
                selectionDialog.dispose();
            });
            
            buttonPanel.add(confirmButton);
            
            selectionDialog.add(shipPanel, BorderLayout.CENTER);
            selectionDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            selectionDialog.pack();
            selectionDialog.setLocationRelativeTo(this);
            selectionDialog.setVisible(true);
        } else {
            // If only one eligible ship, use it
            selectedShips.addAll(eligibleShips);
        }
        
        // If no ships were selected, return
        if (selectedShips.isEmpty()) {
            resultsArea.append("No ships were selected for Ambush.\n\n");
            return 0;
        }
        
        resultsArea.append("== MENTAK COALITION AMBUSH ==\n");
        
        int ambushHits = 0;
        
        for (Ship ship : selectedShips) {
            int roll = (int)(Math.random() * 10) + 1;
            int combatValue = ship.getCombatValue();
            
            resultsArea.append(String.format("%s rolled %d %s\n", 
                ship.getShipType(), 
                roll, 
                roll >= combatValue ? "with 1 hit" : "with 0 hits"));
                
            if (roll >= combatValue) {
                ambushHits++;
            }
        }
        
        resultsArea.append(String.format("Ambush total hits: %d\n\n", ambushHits));
        return ambushHits;
    }
    
    private int performAntiFighterBarrage(List<Ship> fleet) {
        // Find eligible ships (destroyers and flagships with AFB)
        List<Ship> eligibleShips = fleet.stream()
            .filter(ship -> ship instanceof Destroyer || 
                    (ship instanceof Flagship && 
                     (((Flagship)ship).isSaar() || ((Flagship)ship).isNomad())))
            .collect(Collectors.toList());
            
        // Only perform AFB if there are eligible ships
        if (eligibleShips.isEmpty()) {
            return 0;
        }
    
        // Ask user if they want to use Anti-Fighter Barrage
        int response = JOptionPane.showConfirmDialog(
            this,
            "Do you want to use Anti-Fighter Barrage?",
            "Anti-Fighter Barrage",
            JOptionPane.YES_NO_OPTION
        );
        
        if (response == JOptionPane.NO_OPTION) {
            resultsArea.append("Anti-Fighter Barrage not used.\n\n");
            return 0;
        }
        
        // Select number of destroyers for AFB
        int numDestroyers = eligibleShips.size();
        int selectedCount = numDestroyers;
        
        // If more than 1 destroyer, ask how many to use
        if (numDestroyers > 1) {
            // Create a map to count destroyer types
            Map<String, Integer> destroyerTypeCounts = new HashMap<>();
            
            // Count how many of each destroyer type
            for (Ship ship : eligibleShips) {
                String type = "";
                int combatValue = ship.getCombatValue();
                String selectedFaction = (String) factionComboBox.getSelectedItem();
                boolean isArgent = "The Argent Flight".equals(selectedFaction);
                
                if (combatValue == 9) {
                    type = "Destroyer I (AFB: 9, 2 dice)";
                } else if (combatValue == 8) {
                    if (isArgent) {
                        type = "Strike Wing Alpha I (AFB: 9, 2 dice)";
                    } else {
                        type = "Destroyer II (AFB: 6, 3 dice)";
                    }
                } else if (combatValue == 7) {
                    type = "Strike Wing Alpha II (AFB: 6, 3 dice)";
                } else {
                    type = "Unknown Destroyer";
                }
                
                destroyerTypeCounts.put(type, destroyerTypeCounts.getOrDefault(type, 0) + 1);
            }
            
            // Create info panel showing destroyer types available
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.add(new JLabel("Available destroyers:"));
            
            for (Map.Entry<String, Integer> entry : destroyerTypeCounts.entrySet()) {
                infoPanel.add(new JLabel(entry.getValue() + "x " + entry.getKey()));
            }
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(infoPanel);
            
            JPanel spinnerPanel = new JPanel(new FlowLayout());
            spinnerPanel.add(new JLabel("Number of destroyers to use for Anti-Fighter Barrage: "));
            
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(numDestroyers, 1, numDestroyers, 1);
            JSpinner spinner = new JSpinner(spinnerModel);
            spinnerPanel.add(spinner);
            
            mainPanel.add(spinnerPanel);
            
            int option = JOptionPane.showConfirmDialog(
                this, 
                mainPanel, 
                "Select Number of Destroyers", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (option == JOptionPane.OK_OPTION) {
                selectedCount = (Integer) spinner.getValue();
            } else {
                resultsArea.append("Anti-Fighter Barrage cancelled.\n\n");
                return 0;
            }
        }
        
        // Take the specified number of destroyers
        List<Ship> selectedShips = new ArrayList<>();
        if (selectedCount > 0) {
            for (int i = 0; i < selectedCount && i < eligibleShips.size(); i++) {
                selectedShips.add(eligibleShips.get(i));
            }
        }
        
        // If no ships were selected, return
        if (selectedShips.isEmpty()) {
            resultsArea.append("No destroyers were selected for Anti-Fighter Barrage.\n\n");
            return 0;
        }
        
        resultsArea.append("== ANTI-FIGHTER BARRAGE ==\n");
        
        int afbHits = 0;
        int infantryKills = 0;  // Track infantry kills from Strike Wing Alpha II
        
        for (Ship ship : selectedShips) {
            int combatValue = getAFBCombatValue(ship);
            int numDice = getAFBDiceCount(ship);
            
            List<Integer> rolls = new ArrayList<>();
            int shipHits = 0;
            
            for (int i = 0; i < numDice; i++) {
                int roll = (int)(Math.random() * 10) + 1;
                rolls.add(roll);
                if (roll >= combatValue) {
                    shipHits++;
                }
                
                // Check for Strike Wing Alpha II special ability
                if ((ship instanceof Destroyer) && (ship.getCombatValue() == 7) && (roll == 9 || roll == 10)) {
                    infantryKills++;
                }
            }
            
            resultsArea.append(String.format("%s rolled %s with %d hits\n", 
                ship.getShipType(), rolls, shipHits));
                
            afbHits += shipHits;
        }
        
        resultsArea.append(String.format("Anti-Fighter Barrage total hits: %d\n", afbHits));
        
        // Add infantry kills summary if applicable
        if (infantryKills > 0) {
            resultsArea.append(String.format("Infantry destroyed in the space area (Strike Wing Alpha II's ability): %d\n", infantryKills));
        }
        
        resultsArea.append("\n");
        return afbHits;
    } 
    // Helper method to determine AFB combat value
    private int getAFBCombatValue(Ship ship) {
        if (ship instanceof Destroyer) {
            int shipCombatValue = ship.getCombatValue();
            String selectedFaction = (String) factionComboBox.getSelectedItem();
            boolean isArgent = "The Argent Flight".equals(selectedFaction);
            
            // Base the AFB values on the combat value of the ship and faction
            if (shipCombatValue == 9) {
                // Destroyer I
                return 9;
            } else if (shipCombatValue == 8) {
                // Destroyer II or Strike Wing Alpha I
                if (isArgent) {
                    // Strike Wing Alpha I for Argent Flight
                    return 9;
                } else {
                    // Regular Destroyer II
                    return 6;
                }
            } else if (shipCombatValue == 7) {
                // Strike Wing Alpha II
                return 6;
            } else {
                // Default case
                return 9;
            }
        } else if (ship instanceof Flagship) {
            Flagship flagship = (Flagship) ship;
            if ("Son of Ragh".equals(flagship.getShipName())) {
                return 6; // Son of Ragh has AFB 6
            } else if ("Memoria".equals(flagship.getShipName())) {
                return 8; // Memoria has AFB 8
            } else if ("Memoria II".equals(flagship.getShipName())) {
                return 5; // Memoria II has AFB 5
            }
        }
        // Default case
        return 9;
    }
    
    // Helper method to determine AFB dice count
    private int getAFBDiceCount(Ship ship) {
        if (ship instanceof Destroyer) {
            int shipCombatValue = ship.getCombatValue();
            String selectedFaction = (String) factionComboBox.getSelectedItem();
            boolean isArgent = "The Argent Flight".equals(selectedFaction);
            
            // Base the AFB dice count on the combat value of the ship and faction
            if (shipCombatValue == 9) {
                // Destroyer I
                return 2;
            } else if (shipCombatValue == 8) {
                // Destroyer II or Strike Wing Alpha I
                if (isArgent) {
                    // Strike Wing Alpha I for Argent Flight
                    return 2;
                } else {
                    // Regular Destroyer II
                    return 3;
                }
            } else if (shipCombatValue == 7) {
                // Strike Wing Alpha II
                return 3;
            } else {
                // Default case
                return 2;
            }
        } else if (ship instanceof Flagship) {
            if (((Flagship)ship).isSaar()) {
                return 4; // Son of Ragh has 4 dice
            } else if (((Flagship)ship).isNomad()) {
                return 3; // Both Memoria and Memoria II have 3 dice
            }
        }
        // Default case
        return 2;
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