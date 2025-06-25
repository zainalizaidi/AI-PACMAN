import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AI PACMAN");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            MainMenu menu = new MainMenu(frame);
            frame.add(menu);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

// to make jar file
// javac -d out src/*.java
// jar cfe PacManGame.jar Main -C out .
