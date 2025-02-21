package src.tables;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import src.errors.ErrorHandler;
import src.validators.IdentifierValidator;
import src.validators.TypeValidator;
import src.validators.OperatorValidator;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.util.HashMap;
import java.awt.Color;

public class SymbolTable extends JTable {
    private DefaultTableModel model;
    private HashMap<String, String> symbolMap;

    private static final Color DARK_BG = new Color(43, 43, 43);
    private static final Color DARK_TEXT = new Color(169, 183, 198);
    private static final Color DARKER_BG = new Color(30, 30, 30);
    private static final Color SELECTION_BG = new Color(82, 109, 165);

    public SymbolTable() {
        symbolMap = new HashMap<>();
        initializeTable();
    }

    private void initializeTable() {
        model = new DefaultTableModel();
        model.addColumn("Lexeme");
        model.addColumn("Type");
        setModel(model);

        // Configure table appearance
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        getColumnModel().getColumn(0).setPreferredWidth(150);
        getColumnModel().getColumn(1).setPreferredWidth(150);
        setRowHeight(getRowHeight() + 10);

        setBackground(DARKER_BG);
        setForeground(DARK_TEXT);
        getTableHeader().setBackground(DARK_BG);
        getTableHeader().setForeground(DARK_TEXT);
        setGridColor(new Color(70, 70, 70));
        setSelectionBackground(SELECTION_BG);
        setSelectionForeground(Color.WHITE);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(DARKER_BG);
                    c.setForeground(DARK_TEXT);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        });
    }

    public void processInput(String input, int lineNumber, ErrorTable errorTable) {
        // Split input by semicolon
        String[] declarations = input.split(";");

        for (String declaration : declarations) {
            declaration = declaration.trim();
            if (declaration.isEmpty()) continue;

            // Check if it's a declaration with initialization (contains type and =)
            if (declaration.matches("(IntegerType|FloatType|StringType)\\s+.*=.*")) {
                String[] parts = declaration.split("=");
                // Process declaration part
                processDeclaration(parts[0], lineNumber, errorTable);
                // Process assignment part
                if (parts.length > 1) {
                    String identifier = parts[0].split("\\s+")[1].trim();
                    processAssignment(identifier + "=" + parts[1], lineNumber, errorTable);
                }
            }
            // Check for regular assignment
            else if (declaration.contains("=")) {
                processAssignment(declaration, lineNumber, errorTable);
            }
            // Regular declaration
            else {
                processDeclaration(declaration, lineNumber, errorTable);
            }
        }
    }

    private void processDeclaration(String declaration, int lineNumber, ErrorTable errorTable) {
        String[] parts = declaration.trim().split("\\s+", 2); // Split only first space

        // Check basic format
        if (parts.length != 2) {
            errorTable.addError("Syntax Error", declaration, lineNumber,
                "Invalid declaration format");
            return;
        }

        String type = parts[0];
        String[] identifiers = parts[1].split(",(?=\\s*JSJ)"); // Split by comma but keep it

        // Validate type using TypeValidator
        if (!TypeValidator.isValidType(type)) {
            errorTable.addError("Invalid Type", type, lineNumber,
                ErrorHandler.getErrorMessage("INVALID_TYPE"));
            return;
        }

        // Add type to symbol table
        symbolMap.put(type, "Reserved Word");
        model.addRow(new Object[]{type, "Reserved Word"});

        // Process each identifier
        for (int i = 0; i < identifiers.length; i++) {
            String id = identifiers[i].trim();

            // Add comma from previous identifier if not first
            if (i > 0) {
                symbolMap.put(",", "Delimiter");
                model.addRow(new Object[]{",", "Delimiter"});
            }

            // Remove trailing comma if present
            String identifier = id.replace(",", "").trim();

            // Validate identifier using IdentifierValidator
            if (!IdentifierValidator.isValidIdentifier(identifier)) {
                errorTable.addError("Invalid Identifier", identifier, lineNumber,
                    ErrorHandler.getErrorMessage("INVALID_IDENTIFIER"));
                continue;
            }

            // Check for duplicate declarations
            if (symbolMap.containsKey(identifier)) {
                errorTable.addError("Duplicate Declaration", identifier, lineNumber,
                    "Variable already declared");
                continue;
            }

            // Add identifier to symbol table
            symbolMap.put(identifier, type);
            model.addRow(new Object[]{identifier, type});
        }

        // Add semicolon
        symbolMap.put(";", "Delimiter");
        model.addRow(new Object[]{";", "Delimiter"});
    }

    private void processAssignment(String assignment, int lineNumber, ErrorTable errorTable) {
        String[] parts = assignment.split("((?<=[=+\\-*/])|(?=[=+\\-*/]))");
        String currentIdentifier = null;

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            if (OperatorValidator.isOperator(part)) {
                // Add operator to symbol table
                symbolMap.put(part, OperatorValidator.getOperatorType(part));
                model.addRow(new Object[]{part, OperatorValidator.getOperatorType(part)});
            } else if (IdentifierValidator.isValidIdentifier(part)) {
                // Es un identificador
                if (!symbolMap.containsKey(part)) {
                    errorTable.addError("Undeclared Variable", part, lineNumber,
                        "Variable must be declared before use");
                    return;
                }
                currentIdentifier = part;
            } else {
                // Literal value
                if (currentIdentifier != null) {
                    String targetType = symbolMap.get(currentIdentifier);
                    if (!TypeValidator.isValidValueForType(targetType, part)) {
                        errorTable.addError("Type Mismatch", part, lineNumber,
                            "Value does not match variable type " + targetType);
                        return;
                    }
                    // Add literal value to symbol table
                    model.addRow(new Object[]{part, targetType});
                }
            }
        }
    }

    private boolean isValidValueForType(String type, String value) {
        return TypeValidator.isValidValueForType(type, value);
    }

    public void clearTable() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        symbolMap.clear();
    }

    /**
     * Obtains the type of a lexeme from the symbol map
     * @param lexeme The lexeme to look up
     * @return The type of the lexeme
     */
    public String getType(String lexeme) {
        return symbolMap.get(lexeme);
    }

    /**
     * Checks if a lexeme exists in the symbol map
     * @param lexeme The lexeme to check
     * @return true if the lexeme exists
     */
    public boolean containsLexeme(String lexeme) {
        return symbolMap.containsKey(lexeme);
    }
}