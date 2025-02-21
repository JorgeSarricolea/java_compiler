package Compiler;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private JTextArea inputArea;
    private JButton analyzeButton;
    private SymbolTable symbolTable;

    public MainWindow() {
        setupWindow();
        initializeComponents();
        setupEvents();
    }

    private void setupWindow() {
        setTitle("Java Compiler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
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
        scrollArea.setPreferredSize(new Dimension(300, 500));

        // Analysis button
        analyzeButton = new JButton("Compile");

        // Add components to left panel
        leftPanel.add(new JLabel("Enter code:"), BorderLayout.NORTH);
        leftPanel.add(scrollArea, BorderLayout.CENTER);
        leftPanel.add(analyzeButton, BorderLayout.SOUTH);

        // Right panel for symbol table
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        symbolTable = new SymbolTable();
        rightPanel.add(new JLabel("Symbol Table:"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(symbolTable), BorderLayout.CENTER);

        // Add main panels
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void setupEvents() {
        analyzeButton.addActionListener(e -> {
            String input = inputArea.getText();
            if (!input.isEmpty()) {
                symbolTable.clearTable();
                symbolTable.processInput(input);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please enter code to analyze",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}