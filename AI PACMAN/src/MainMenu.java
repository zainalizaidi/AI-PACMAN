import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel {
    private static final String[] LEVELS = {"EASY", "MEDIUM", "HARD"};
    private static final Color PAC_YELLOW = new Color(255, 223, 0);
    private static final Color BG_COLOR = new Color(0, 0, 40); // Dark blue background

    public MainMenu(JFrame frame) {
        // Set up basic frame icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/Assets/pacman_icon.png"));
        frame.setIconImage(icon.getImage());

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(19 * 24, 21 * 24));

        // Title Panel with gradient
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 215, 0), 
                                w/2, 0, new Color(255, 165, 0));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(0, 150));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel title = new JLabel("AI PACMAN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 60));
        title.setForeground(Color.BLACK);
        titlePanel.add(title, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);

        // Button panel with subtle shadow
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        for (String level : LEVELS) {
            JButton btn = createMenuButton(level);
            btn.addActionListener(e -> {
                frame.getContentPane().removeAll();
                PacMan game = new PacMan(frame, PacMan.Difficulty.valueOf(level));
                frame.add(game);
                frame.pack();
                game.requestFocusInWindow();
                frame.revalidate();
            });
            buttonsPanel.add(btn);
        }

        add(buttonsPanel, BorderLayout.CENTER);

        // Ghost footer with glow effect
        JPanel ghostPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        ghostPanel.setOpaque(false);
        
        String[] ghostFiles = {"/Assets/redGhost.png", "/Assets/pinkGhost.png", 
                              "/Assets/orangeGhost.png", "/Assets/blueGhost.png"};
        for (String ghostPath : ghostFiles) {
            JLabel ghostLabel = createGlowingGhostLabel(ghostPath);
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

    private JLabel createGlowingGhostLabel(String ghostPath) {
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(ghostPath));
        Image img = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        
        return new JLabel(new ImageIcon(img)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glow effect
                for (int i = 0; i < 5; i++) {
                    float opacity = 0.2f - (i * 0.04f);
                    g2d.setColor(new Color(255, 255, 255, (int)(opacity * 255)));
                    g2d.fillOval(25 - (i*5), 25 - (i*5), i*10, i*10);
                }
                
                g2d.drawImage(((ImageIcon) getIcon()).getImage(), 0, 0, null);
                g2d.dispose();
            }
        };
    }
}