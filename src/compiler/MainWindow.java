package src.compiler;
import javax.swing.*;

import src.tables.ErrorTable;
import src.tables.SymbolTable;
import src.theme.DarkThemeColors;

import java.awt.*;
import java.io.IOException;

public class MainWindow extends JFrame {
    private JTextArea inputArea;
    private JButton analyzeButton;
    private JButton clearButton;
    public static SymbolTable symbolTable;  // Made static to access from ErrorTable
    private ErrorTable errorTable;

    public MainWindow() {
        setupDarkTheme();
        setupWindow();
        initializeComponents();
        setupEvents();
    }

    private void setupDarkTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            DarkThemeColors.setupUIDefaults();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupWindow() {
        setTitle("Java Compiler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void initializeComponents() {
        // Left panel for text area
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Text area configuration
        inputArea = new JTextArea();
        inputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollArea = new JScrollPane(inputArea);
        scrollArea.setPreferredSize(new Dimension(400, 600));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        analyzeButton = new JButton("Compile");
        clearButton = new JButton("Clear Tables");
        buttonPanel.add(analyzeButton);
        buttonPanel.add(clearButton);
        
        // Add components to left panel
        leftPanel.add(new JLabel("Enter code:"), BorderLayout.NORTH);
        leftPanel.add(scrollArea, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);  // Add button panel instead of single button

        // Center panel for tables
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.setPreferredSize(new Dimension(800, 600));

        // Symbol table panel
        JPanel symbolTablePanel = new JPanel(new BorderLayout(5, 5));
        symbolTable = new SymbolTable();
        symbolTablePanel.add(new JLabel("Symbol Table:"), BorderLayout.NORTH);
        symbolTablePanel.add(new JScrollPane(symbolTable), BorderLayout.CENTER);

        // Error table panel
        JPanel errorTablePanel = new JPanel(new BorderLayout(5, 5));
        errorTable = new ErrorTable();
        errorTablePanel.add(new JLabel("Error Table:"), BorderLayout.NORTH);
        errorTablePanel.add(new JScrollPane(errorTable), BorderLayout.CENTER);

        // Add tables to center panel
        centerPanel.add(symbolTablePanel);
        centerPanel.add(errorTablePanel);

        // Add main panels
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);

        // Configurar colores específicos
        inputArea.setCaretColor(DarkThemeColors.DARK_TEXT);
        inputArea.setBackground(DarkThemeColors.DARKER_BG);
        inputArea.setForeground(DarkThemeColors.DARK_TEXT);
        
        // Estilo para los botones
        analyzeButton.setBackground(DarkThemeColors.BUTTON_BG);
        analyzeButton.setForeground(DarkThemeColors.DARK_TEXT);
        analyzeButton.setFocusPainted(false);
        clearButton.setBackground(DarkThemeColors.BUTTON_BG);
        clearButton.setForeground(DarkThemeColors.DARK_TEXT);
        clearButton.setFocusPainted(false);
    }

    private void setupEvents() {
        analyzeButton.addActionListener(e -> {
            String input = inputArea.getText();
            if (!input.isEmpty()) {
                symbolTable.clearTable();
                errorTable.clearTable();
                
                // Process each line
                String[] lines = input.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (!line.isEmpty()) {
                        symbolTable.processInput(line, i + 1, errorTable);
                    }
                }
                
                // Generar triplo independientemente de los errores
                generateAndSaveTriplo(input);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please enter code to analyze",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Clear tables button action
        clearButton.addActionListener(e -> {
            symbolTable.clearTable();
            errorTable.clearTable();
        });
    }

    private void generateAndSaveTriplo(String code) {
        try {
            TripletGenerator triploGenerator = new TripletGenerator();
            triploGenerator.generateTriplo(code);
            
            String filePath = "triplet.txt";
            
            // Forzar la eliminación del archivo existente
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                file.delete();
            }
            
            // Guardar el nuevo triplo
            triploGenerator.saveToFile(filePath);
            
            JOptionPane.showMessageDialog(this,
                "Triplet generated successfully and saved in: " + filePath,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error saving triplet: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}