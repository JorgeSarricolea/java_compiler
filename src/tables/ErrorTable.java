package src.tables;

import src.compiler.MainWindow;
import src.tokens.TokenType;
import src.errors.ErrorType;

import java.util.ArrayList;

public class ErrorTable extends BaseTable {
    private ArrayList<SemanticError> errors;
    private int errorCounter = 1; // Contador para tokens únicos

    public ErrorTable() {
        super();
        errors = new ArrayList<>();
        initializeColumns();
    }

    @Override
    protected void initializeColumns() {
        model.addColumn("Token");
        model.addColumn("Lexeme");
        model.addColumn("Line");
        model.addColumn("Description");

        // Configure column widths
        getColumnModel().getColumn(0).setPreferredWidth(100);  // Token
        getColumnModel().getColumn(1).setPreferredWidth(150);  // Lexeme
        getColumnModel().getColumn(2).setPreferredWidth(50);   // Line
        getColumnModel().getColumn(3).setPreferredWidth(400);  // Description
    }

    @Override
    protected void clearSpecificData() {
        errors.clear();
        errorCounter = 1; // Reiniciar el contador al limpiar
    }

    public void addError(ErrorType errorType, String lexeme, int line, Object... args) {
        String uniqueToken = errorType.getToken() + "_" + errorCounter++; // Token único con contador
        SemanticError error = new SemanticError(uniqueToken, lexeme, line,
            errorType.getMessage(args));
        errors.add(error);
        model.addRow(new Object[]{
            uniqueToken,
            lexeme,
            line,
            errorType.getMessage(args)
        });
        
        // Asegurarse de que el lexema que causó el error esté en la tabla de símbolos
        if (MainWindow.symbolTable != null) {
            MainWindow.symbolTable.addLexemeToTable(lexeme, TokenType.UNDEFINED.toString());
        }
    }

    public void checkSemanticError(String input, int line) {
        if (input.contains("=")) {
            checkAssignmentError(input, line);
        }
    }

    private void checkAssignmentError(String input, int line) {
        String[] parts = input.split("=");
        if (parts.length != 2) return;

        String variable = parts[0].trim();
        String value = parts[1].trim().replace(";", "");

        String varType = MainWindow.symbolTable.getType(variable);

        if (varType == null) {
            addError(ErrorType.UNDECLARED_VARIABLE, variable, line);
            return;
        }

        TokenType type = TokenType.getType(varType);
        if (!type.isValidValue(value)) {
            addError(ErrorType.TYPE_MISMATCH, variable, line,
                value, varType);
        }
    }

    // Inner class to represent a semantic error
    @SuppressWarnings("unused")
    private class SemanticError {
        String token;
        String lexeme;
        int line;
        String description;

        public SemanticError(String token, String lexeme, int line, String description) {
            this.token = token;
            this.lexeme = lexeme;
            this.line = line;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}