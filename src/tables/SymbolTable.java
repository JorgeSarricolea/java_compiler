package src.tables;

import src.validators.RegExPattern;
import src.tokens.TokenType;
import src.errors.ErrorType;

import java.util.HashMap;

public class SymbolTable extends BaseTable {
    private HashMap<String, String> symbolMap;

    public SymbolTable() {
        super();
        symbolMap = new HashMap<>();
        initializeColumns();
    }

    @Override
    protected void initializeColumns() {
        model.addColumn("Lexeme");
        model.addColumn("Type");

        // Configure column widths
        getColumnModel().getColumn(0).setPreferredWidth(150);
        getColumnModel().getColumn(1).setPreferredWidth(150);
    }

    @Override
    public void clearTable() {
        super.clearTable();
        symbolMap.clear();
    }

    public void processInput(String input, int lineNumber, ErrorTable errorTable) {
        // Split input by semicolon
        String[] declarations = input.split(";");

        for (String declaration : declarations) {
            declaration = declaration.trim();
            if (declaration.isEmpty()) continue;

            // Regular assignment (without type declaration)
            if (!declaration.matches("(IntegerType|FloatType|StringType)\\s+.*")) {
                if (declaration.contains("=")) {
                    processAssignment(declaration, lineNumber, errorTable);
                }
                continue;
            }

            // Get the first word (type)
            String[] parts = declaration.split("\\s+", 2);
            if (parts.length < 2) continue;

            String type = parts[0];
            String rest = parts[1].trim();

            // Add type to symbol table
            model.addRow(new Object[]{type, "Reserved Word"});

            // Split multiple declarations by comma
            String[] variables = rest.split(",");

            for (int i = 0; i < variables.length; i++) {
                String var = variables[i].trim();

                // Add comma as delimiter if not first variable
                if (i > 0) {
                    TokenType commaType = TokenType.getType(",");
                    model.addRow(new Object[]{",", commaType});
                }

                // Check if this variable has initialization
                if (var.contains("=")) {
                    String[] assignParts = var.split("=");
                    String identifier = assignParts[0].trim();

                    // Check if variable was already declared
                    if (symbolMap.containsKey(identifier)) {
                        errorTable.addError(ErrorType.DUPLICATE_DECLARATION, identifier, lineNumber);
                        continue;
                    }

                    // Process identifier
                    if (!RegExPattern.isValidIdentifier(identifier)) {
                        errorTable.addError(ErrorType.INVALID_IDENTIFIER, identifier, lineNumber);
                        continue;
                    }

                    // Add identifier to symbol table with its type
                    symbolMap.put(identifier, type);
                    model.addRow(new Object[]{identifier, type});

                    // Process initialization
                    if (assignParts.length < 2 || assignParts[1].trim().isEmpty()) {
                        errorTable.addError(ErrorType.SYNTAX_ERROR, identifier, lineNumber);
                        continue;
                    }

                    String value = assignParts[1].trim();

                    // Add assignment operator
                    model.addRow(new Object[]{"=", TokenType.ASSIGNMENT_OPERATOR});

                    // Validate and add value
                    if (TokenType.getType(type).isValidValue(value)) {
                        model.addRow(new Object[]{value, type});
                    } else {
                        errorTable.addError(ErrorType.TYPE_MISMATCH, value, lineNumber, value, type);
                    }
                } else {
                    // Just a declaration without initialization
                    if (!RegExPattern.isValidIdentifier(var)) {
                        errorTable.addError(ErrorType.INVALID_IDENTIFIER, var, lineNumber);
                        continue;
                    }

                    // Check if variable was already declared
                    if (symbolMap.containsKey(var)) {
                        errorTable.addError(ErrorType.DUPLICATE_DECLARATION, var, lineNumber);
                        continue;
                    }

                    symbolMap.put(var, type);
                    model.addRow(new Object[]{var, type});
                }
            }

            // Add semicolon
            model.addRow(new Object[]{";", TokenType.DELIMITER});
        }
    }

    private void processAssignment(String assignment, int lineNumber, ErrorTable errorTable) {
        String[] parts = assignment.split("=");
        if (parts.length != 2) return;

        String variable = parts[0].trim();
        String value = parts[1].trim();

        // Check if variable exists
        if (!symbolMap.containsKey(variable)) {
            errorTable.addError(ErrorType.UNDECLARED_VARIABLE, variable, lineNumber);
            return;
        }

        // Get the type of the variable
        String varType = symbolMap.get(variable);
        TokenType targetType = TokenType.getType(varType);

        // If value is an identifier, check its type
        if (RegExPattern.isValidIdentifier(value)) {
            if (!symbolMap.containsKey(value)) {
                errorTable.addError(ErrorType.UNDECLARED_VARIABLE, value, lineNumber);
                return;
            }
            String valueType = symbolMap.get(value);
            if (!valueType.equals(varType)) {
                errorTable.addError(ErrorType.TYPE_MISMATCH, variable, lineNumber, valueType, varType);
                return;
            }
        } else {
            // Check if literal value matches the variable type
            if (!targetType.isValidValue(value)) {
                errorTable.addError(ErrorType.TYPE_MISMATCH, variable, lineNumber, value, varType);
                return;
            }
        }

        // Add to symbol table display
        model.addRow(new Object[]{variable, varType});
        model.addRow(new Object[]{"=", TokenType.ASSIGNMENT_OPERATOR});
        model.addRow(new Object[]{value, varType});
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