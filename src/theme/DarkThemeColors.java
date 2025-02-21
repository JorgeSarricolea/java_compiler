package src.theme;

import java.awt.Color;

public class DarkThemeColors {
    // Main colors
    public static final Color DARK_BG = new Color(43, 43, 43);
    public static final Color DARK_TEXT = new Color(169, 183, 198);
    public static final Color DARKER_BG = new Color(30, 30, 30);

    // UI elements
    public static final Color SELECTION_BG = new Color(82, 109, 165);
    public static final Color BUTTON_BG = new Color(60, 63, 65);
    public static final Color GRID_COLOR = new Color(70, 70, 70);

    // Text colors
    public static final Color WHITE_TEXT = Color.WHITE;

    // Configure UI Manager defaults
    public static void setupUIDefaults() {
        javax.swing.UIManager.put("Panel.background", DARK_BG);
        javax.swing.UIManager.put("TextField.background", DARKER_BG);
        javax.swing.UIManager.put("TextField.foreground", DARK_TEXT);
        javax.swing.UIManager.put("TextArea.background", DARKER_BG);
        javax.swing.UIManager.put("TextArea.foreground", DARK_TEXT);
        javax.swing.UIManager.put("Label.foreground", DARK_TEXT);
        javax.swing.UIManager.put("Button.background", BUTTON_BG);
        javax.swing.UIManager.put("Button.foreground", DARK_TEXT);
        javax.swing.UIManager.put("Table.background", DARKER_BG);
        javax.swing.UIManager.put("Table.foreground", DARK_TEXT);
        javax.swing.UIManager.put("Table.selectionBackground", SELECTION_BG);
        javax.swing.UIManager.put("Table.selectionForeground", WHITE_TEXT);
        javax.swing.UIManager.put("TableHeader.background", DARK_BG);
        javax.swing.UIManager.put("TableHeader.foreground", DARK_TEXT);
        javax.swing.UIManager.put("ScrollPane.background", DARK_BG);
        javax.swing.UIManager.put("ScrollBar.background", DARK_BG);
        javax.swing.UIManager.put("ScrollBar.thumb", BUTTON_BG);
    }
}