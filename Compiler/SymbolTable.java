package Compiler;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;

public class SymbolTable extends JTable {
    private DefaultTableModel model;
    private HashMap<String, String> symbolMap;
    
    // HashMap for data types and their patterns
    private static final HashMap<String, String> TYPE_PATTERNS = new HashMap<String, String>() {{
        put("IntegerType", "JSJ[abc][0-9]+");
        put("FloatType", "JSJ[xyz][0-9]+");
        put("StringType", "JSJs[0-9]+");
    }};

    public SymbolTable() {
        symbolMap = new HashMap<>();
        initializeTable();
    }

    private void initializeTable() {
        model = new DefaultTableModel();
        model.addColumn("Lexeme");
        model.addColumn("Type");
        setModel(model);
    }

    public void processInput(String input) {
        clearTable();
        symbolMap.clear();

        // Split input by semicolon
        String[] declarations = input.split(";");

        for (String declaration : declarations) {
            declaration = declaration.trim();
            if (declaration.isEmpty()) continue;

            String[] parts = declaration.split("\\s+");
            if (parts.length == 2 && isValidDeclaration(parts[0], parts[1])) {
                String type = parts[0];    // IntegerType, FloatType, StringType
                String lexeme = parts[1];  // JSJx1, etc

                // Store in HashMap
                symbolMap.put(type, "Reserved Word");
                symbolMap.put(lexeme, type);
                symbolMap.put(";", "Delimiter");

                // Update table view
                model.addRow(new Object[]{type, "Reserved Word"});
                model.addRow(new Object[]{lexeme, type});
                model.addRow(new Object[]{";", "Delimiter"});
            }
        }
    }

    private boolean isValidDeclaration(String type, String identifier) {
        // Check if it's a valid type
        if (!TYPE_PATTERNS.containsKey(type)) {
            return false;
        }

        // Get the pattern for this type and check if identifier matches
        String pattern = TYPE_PATTERNS.get(type);
        return identifier.matches(pattern);
    }

    public void clearTable() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
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