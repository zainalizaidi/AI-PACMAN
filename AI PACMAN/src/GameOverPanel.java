import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {
    private static final Color PAC_YELLOW = new Color(255, 223, 0);
    private JFrame frame;
    private int finalScore;

    public GameOverPanel(JFrame frame, int finalScore) {
        this.frame = frame;
        this.finalScore = finalScore;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(19 * 24, 21 * 24));

        // Title label
        JLabel title = new JLabel("GAME OVER", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 64));
        title.setForeground(Color.RED);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));

        // Score label
        JLabel scoreLabel = new JLabel("Final Score: " + finalScore, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 36));
        scoreLabel.setForeground(PAC_YELLOW);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        contentPanel.add(scoreLabel, BorderLayout.NORTH);

        // Button panel - now using vertical BoxLayout
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(Color.BLACK);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 80, 10, 80));

        // Add rigid area for spacing between buttons
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Main Menu button
        JButton menuButton = createMenuButton("MAIN MENU");
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            MainMenu menu = new MainMenu(frame);
            frame.add(menu);
            frame.pack();
            frame.revalidate();
        });
        buttonsPanel.add(menuButton);

        // Add spacing between buttons
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Quit button
        JButton quitButton = createMenuButton("QUIT GAME");
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.addActionListener(e -> System.exit(0));
        buttonsPanel.add(quitButton);

        contentPanel.add(buttonsPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Ghost icons panel
        JPanel ghostPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        ghostPanel.setBackground(Color.BLACK);
        String[] ghostFiles = {"/Assets/redGhost.png", "/Assets/pinkGhost.png", "/Assets/orangeGhost.png", "/Assets/blueGhost.png"};
        for (String ghostPath : ghostFiles) {
            ImageIcon ghostIcon = new ImageIcon(getClass().getResource(ghostPath));
            JLabel ghostLabel = new JLabel(resizeIcon(ghostIcon, 40, 40));
            ghostPanel.add(ghostLabel);
        }
        add(ghostPanel, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 15, 15);
                
                // Button body
                if (getModel().isRollover()) {
                    g2.setColor(Color.WHITE);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, PAC_YELLOW, 0, getHeight(), 
                                                       new Color(255, 215, 0));
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 15, 15);
                
                // Button border
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-3, 15, 15);
                
                // Text
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 28));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 60));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    private Icon resizeIcon(ImageIcon icon, int w, int h) {
        return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}