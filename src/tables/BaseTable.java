package src.tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Color;

public abstract class BaseTable extends JTable {
    protected DefaultTableModel model;

    // Common colors for dark theme
    protected static final Color DARK_BG = new Color(43, 43, 43);
    protected static final Color DARK_TEXT = new Color(169, 183, 198);
    protected static final Color DARKER_BG = new Color(30, 30, 30);
    protected static final Color SELECTION_BG = new Color(82, 109, 165);
    protected static final Color GRID_COLOR = new Color(70, 70, 70);
    protected static final Color WHITE_TEXT = Color.WHITE;

    public BaseTable() {
        initializeBaseTable();
    }

    protected void initializeBaseTable() {
        model = new DefaultTableModel();
        setModel(model);

        // Configure table appearance
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setRowHeight(getRowHeight() + 10);

        // Set dark theme colors
        setBackground(DARKER_BG);
        setForeground(DARK_TEXT);
        getTableHeader().setBackground(DARK_BG);
        getTableHeader().setForeground(DARK_TEXT);
        setGridColor(GRID_COLOR);
        setSelectionBackground(SELECTION_BG);
        setSelectionForeground(WHITE_TEXT);

        // Set default renderer
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

    public void clearTable() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    // Abstract methods that child classes must implement
    protected abstract void initializeColumns();
}