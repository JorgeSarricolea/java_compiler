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

            // Get the first word (type)
            String[] parts = declaration.split("\\s+", 2);
            if (parts.length < 2) continue;

            String type = parts[0];
            // Validate the type first
            if (!isValidType(type)) {
                errorTable.addError(ErrorType.INVALID_TYPE, type, lineNumber);
                continue;
            }

            // Check if it's a declaration with initialization
            if (declaration.matches("(IntegerType|FloatType|StringType)\\s+.*")) {
                String rest = parts[1].trim();

                // Add type to symbol table
                TokenType typeToken = TokenType.getType(type);
                symbolMap.put(type, typeToken.toString());
                model.addRow(new Object[]{type, typeToken});

                // Split multiple declarations by comma
                String[] variables = rest.split(",");

                for (int i = 0; i < variables.length; i++) {
                    String var = variables[i].trim();

                    // Add comma as delimiter if not first variable
                    if (i > 0) {
                        TokenType commaType = TokenType.getType(",");
                        symbolMap.put(",", commaType.toString());
                        model.addRow(new Object[]{",", commaType});
                    }

                    // Check if this variable has initialization
                    if (var.contains("=")) {
                        String[] assignParts = var.split("=");
                        String identifier = assignParts[0].trim();

                        // Check if there's a value after the equals sign
                        if (assignParts.length < 2 || assignParts[1].trim().isEmpty()) {
                            errorTable.addError(ErrorType.SYNTAX_ERROR, identifier, lineNumber,
                                "Missing value after assignment operator");
                            continue;
                        }

                        String value = assignParts[1].trim();

                        // Process identifier
                        if (!RegExPattern.isValidIdentifier(identifier)) {
                            errorTable.addError(ErrorType.INVALID_IDENTIFIER, identifier, lineNumber);
                            continue;
                        }

                        // Add identifier to symbol table
                        symbolMap.put(identifier, type);
                        model.addRow(new Object[]{identifier, type});

                        // Add assignment operator
                        TokenType assignType = TokenType.getType("=");
                        symbolMap.put("=", assignType.toString());
                        model.addRow(new Object[]{"=", assignType});

                        // Add value with the same type as the identifier
                        if (TokenType.getType(type).isValidValue(value)) {
                            model.addRow(new Object[]{value, type});
                        } else {
                            errorTable.addError(ErrorType.TYPE_MISMATCH, value, lineNumber,
                                value, type);
                        }
                    } else {
                        // Just a declaration without initialization
                        if (!RegExPattern.isValidIdentifier(var)) {
                            errorTable.addError(ErrorType.INVALID_IDENTIFIER, var, lineNumber);
                            continue;
                        }
                        symbolMap.put(var, type);
                        model.addRow(new Object[]{var, type});
                    }
                }

                // Add semicolon
                TokenType semicolonType = TokenType.getType(";");
                symbolMap.put(";", semicolonType.toString());
                model.addRow(new Object[]{";", semicolonType});
            }
            // Regular assignment
            else if (declaration.contains("=")) {
                processAssignment(declaration, lineNumber, errorTable);
            }
        }
    }

    private void processAssignment(String assignment, int lineNumber, ErrorTable errorTable) {
        String[] parts = assignment.split("((?<=[=+\\-*/])|(?=[=+\\-*/]))");
        String currentIdentifier = null;

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            TokenType tokenType = TokenType.getType(part);
            if (tokenType != null) {
                // Add operator to symbol table
                symbolMap.put(part, tokenType.toString());
                model.addRow(new Object[]{part, tokenType});
            } else if (RegExPattern.isValidIdentifier(part)) {
                // Is an identifier
                if (!symbolMap.containsKey(part)) {
                    errorTable.addError(ErrorType.UNDECLARED_VARIABLE, part, lineNumber);
                    return;
                }
                currentIdentifier = part;
            } else {
                // Literal value
                if (currentIdentifier != null) {
                    TokenType targetType = TokenType.getType(symbolMap.get(currentIdentifier));
                    if (!targetType.isValidValue(part)) {
                        errorTable.addError(ErrorType.TYPE_MISMATCH, part, lineNumber,
                            part, targetType);
                        return;
                    }
                    // Add literal value to symbol table
                    model.addRow(new Object[]{part, targetType});
                }
            }
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

    private boolean isValidType(String type) {
        return type.equals("IntegerType") || 
               type.equals("FloatType") || 
               type.equals("StringType");
    }
}