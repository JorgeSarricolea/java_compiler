package src.tables;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import src.MainWindow;
import src.errors.ErrorHandler;
import src.theme.DarkThemeColors;
import src.validators.TypeValidator;

import java.awt.*;
import java.util.ArrayList;

public class ErrorTable extends JTable {
    private DefaultTableModel model;
    private ArrayList<SemanticError> errors;

    public ErrorTable() {
        errors = new ArrayList<>();
        initializeTable();
    }

    private void initializeTable() {
        model = new DefaultTableModel();
        model.addColumn("Token");
        model.addColumn("Lexeme");
        model.addColumn("Line");
        model.addColumn("Description");
        setModel(model);

        // Configure table appearance
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        getColumnModel().getColumn(0).setPreferredWidth(100);  // Token
        getColumnModel().getColumn(1).setPreferredWidth(150);  // Lexeme
        getColumnModel().getColumn(2).setPreferredWidth(50);   // Line
        getColumnModel().getColumn(3).setPreferredWidth(400);  // Description
        setRowHeight(getRowHeight() + 10);

        // Enable word wrap for all columns
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setCellRenderer(new WordWrapCellRenderer());
        }

        // Configurar colores de la tabla
        setBackground(DarkThemeColors.DARKER_BG);
        setForeground(DarkThemeColors.DARK_TEXT);
        getTableHeader().setBackground(DarkThemeColors.DARK_BG);
        getTableHeader().setForeground(DarkThemeColors.DARK_TEXT);
        setGridColor(DarkThemeColors.GRID_COLOR);
        setSelectionBackground(DarkThemeColors.SELECTION_BG);
        setSelectionForeground(DarkThemeColors.WHITE_TEXT);
    }

    // Custom renderer that actually wraps text
    private class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {
        public WordWrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Set colors based on selection
            if (isSelected) {
                setForeground(DarkThemeColors.WHITE_TEXT);
                setBackground(DarkThemeColors.SELECTION_BG);
            } else {
                setForeground(DarkThemeColors.DARK_TEXT);
                setBackground(DarkThemeColors.DARKER_BG);
            }

            // Set font and margins
            setFont(table.getFont());
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            setText((value == null) ? "" : value.toString());
            adjustRowHeight(table, row, column);

            return this;
        }

        private void adjustRowHeight(JTable table, int row, int column) {
            int cWidth = table.getColumnModel().getColumn(column).getWidth();
            setSize(new Dimension(cWidth, 1000));
            int prefH = getPreferredSize().height;
            if (table.getRowHeight(row) != prefH) {
                table.setRowHeight(row, prefH);
            }
        }
    }

    public void addError(String token, String lexeme, int line, String description) {
        SemanticError error = new SemanticError(token, lexeme, line, description);
        errors.add(error);
        model.addRow(new Object[]{
            token,
            lexeme,
            line,
            description
        });
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
            addError("Undeclared Variable", variable, line,
                ErrorHandler.getErrorMessage("UNDECLARED_VARIABLE"));
            return;
        }

        if (!TypeValidator.isValidValueForType(varType, value)) {
            addError("Type Mismatch", variable, line,
                ErrorHandler.getErrorMessage("INVALID_ASSIGNMENT", "non-" + varType.toLowerCase(), varType));
        }
    }

    public void clearTable() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        errors.clear();
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