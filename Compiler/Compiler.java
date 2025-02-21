package Compiler;

public class Compiler {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainWindow ventana = new MainWindow();
            ventana.setVisible(true);
        });
    }
}