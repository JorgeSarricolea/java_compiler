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
    protected void clearSpecificData() {
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
            addLexemeToTable(type, "Reserved Word");

            // Split multiple declarations by comma
            String[] variables = rest.split(",");

            for (int i = 0; i < variables.length; i++) {
                String var = variables[i].trim();

                // Add comma as delimiter if not first variable
                if (i > 0) {
                    addLexemeToTable(",", TokenType.DELIMITER.toString());
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
                    addLexemeToTable(identifier, type);

                    // Process initialization
                    if (assignParts.length < 2 || assignParts[1].trim().isEmpty()) {
                        errorTable.addError(ErrorType.SYNTAX_ERROR, identifier, lineNumber);
                        continue;
                    }

                    String value = assignParts[1].trim();

                    // Add assignment operator
                    addLexemeToTable("=", TokenType.ASSIGNMENT_OPERATOR.toString());

                    // Validate and add value
                    if (TokenType.getType(type).isValidValue(value)) {
                        addLexemeToTable(value, type);
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
                    addLexemeToTable(var, type);
                }
            }

            // Add semicolon
            addLexemeToTable(";", TokenType.DELIMITER.toString());
        }
    }

    private void processAssignment(String assignment, int lineNumber, ErrorTable errorTable) {
        String[] parts = assignment.split("=");
        if (parts.length != 2) return;

        String variable = parts[0].trim();
        String expression = parts[1].trim();

        // Check if variable exists
        if (!symbolMap.containsKey(variable)) {
            errorTable.addError(ErrorType.UNDECLARED_VARIABLE, variable, lineNumber);
            addLexemeToTable(variable, TokenType.UNDEFINED.toString());
            return;
        }

        String varType = symbolMap.get(variable);
        TokenType targetType = TokenType.getType(varType);
        addLexemeToTable(variable, varType);
        addLexemeToTable("=", TokenType.ASSIGNMENT_OPERATOR.toString());

        // Split expression by arithmetic operators
        String[] operands = expression.split("\\s*[+\\-*/]\\s*");
        String[] operators = expression.split("[^+\\-*/]+");

        // Procesar operandos
        for (String operand : operands) {
            operand = operand.trim();
            if (!operand.isEmpty()) {
                if (RegExPattern.isValidIdentifier(operand)) {
                    if (symbolMap.containsKey(operand)) {
                        addLexemeToTable(operand, symbolMap.get(operand));
                    } else {
                        addLexemeToTable(operand, TokenType.UNDEFINED.toString());
                    }
                } else {
                    // Determinar el tipo del literal
                    if (TokenType.FLOAT_TYPE.isValidValue(operand)) {
                        addLexemeToTable(operand, TokenType.FLOAT_TYPE.toString());
                    } else if (TokenType.INTEGER_TYPE.isValidValue(operand)) {
                        addLexemeToTable(operand, TokenType.INTEGER_TYPE.toString());
                    } else if (TokenType.STRING_TYPE.isValidValue(operand)) {
                        addLexemeToTable(operand, TokenType.STRING_TYPE.toString());
                    } else {
                        addLexemeToTable(operand, TokenType.UNDEFINED.toString());
                    }
                }
            }
        }

        // Procesar operadores
        for (String operator : operators) {
            operator = operator.trim();
            if (!operator.isEmpty()) {
                addLexemeToTable(operator, TokenType.ARITHMETIC_OPERATOR.toString());
            }
        }

        // Validar la expresión
        if (operands.length == 1) {
            handleSingleOperand(variable, operands[0], varType, targetType, lineNumber, errorTable);
        } else {
            handleArithmeticExpression(variable, operands, varType, lineNumber, errorTable);
        }
    }

    private void handleSingleOperand(String variable, String value, String varType, TokenType targetType, 
                                   int lineNumber, ErrorTable errorTable) {
        if (RegExPattern.isValidIdentifier(value)) {
            if (!symbolMap.containsKey(value)) {
                errorTable.addError(ErrorType.UNDECLARED_VARIABLE, value, lineNumber);
                return;
            }
            String valueType = symbolMap.get(value);
            if (!valueType.equals(varType)) {
                errorTable.addError(ErrorType.TYPE_MISMATCH, value, lineNumber, valueType, varType);
                return;
            }
        } else {
            if (!targetType.isValidValue(value)) {
                errorTable.addError(ErrorType.TYPE_MISMATCH, value, lineNumber, value, varType);
                return;
            }
        }
    }

    private void handleArithmeticExpression(String variable, String[] operands, String varType, 
                                          int lineNumber, ErrorTable errorTable) {
        // Check each operand
        for (String operand : operands) {
            operand = operand.trim();
            if (RegExPattern.isValidIdentifier(operand)) {
                // Check if operand exists and has compatible type
                if (!symbolMap.containsKey(operand)) {
                    errorTable.addError(ErrorType.UNDECLARED_VARIABLE, operand, lineNumber);
                    return;
                }
                String operandType = symbolMap.get(operand);
                if (!operandType.equals(varType)) {
                    errorTable.addError(ErrorType.TYPE_MISMATCH, operand, lineNumber, operandType, varType);
                    return;
                }
            } else {
                // Check if literal value matches the variable type
                TokenType targetType = TokenType.getType(varType);
                if (!targetType.isValidValue(operand)) {
                    errorTable.addError(ErrorType.TYPE_MISMATCH, operand, lineNumber, operand, varType);
                    return;
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
}