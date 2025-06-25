import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.awt.image.BufferedImage;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    public enum Difficulty { EASY, MEDIUM, HARD }
    private final Difficulty difficulty;
    private static final int ROWS = 21, COLS = 19, TILE = 24;
    private final int boardWidth = COLS * TILE;
    private final int boardHeight = ROWS * TILE;
    private Timer timer;
    private JFrame frame;

    private final String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX", 
        "X        X        X", 
        "X XX XXX X XXX XX X",
        "X                 X", 
        "X XX X XXXXX X XX X", 
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX", 
        "OOOX X       X XOOO", 
        "XXXX X XXrXX X XXXX",
        "OOOX    bpo    XOOO", 
        "XXXX X XXXXX X XXXX", 
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX", 
        "X        X        X", 
        "X XX XXX X XXX XX X",
        "X  X     P     X  X", 
        "XX X X XXXXX X X XX", 
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X", 
        "X                 X", 
        "XXXXXXXXXXXXXXXXXXX"
    };

    private Set<Block> walls = new HashSet<>();
    private Set<Block> foods = new HashSet<>();
    private List<Block> ghosts = new ArrayList<>();
    private Block pacman;
    private Block powerPellet;
    private int score = 0, lives = 3;
    private boolean gameOver = false;
    private int powerPelletTimer = 0;
    private int ghostSpawnTimer = 0;
    private static final int POWER_PELLET_SPAWN_TIME = 10000;
    private static final int GHOST_SPAWN_TIME = 10000;
    private static final int MAX_GHOSTS = 8;
    private static final int PACMAN_SPEED = 6;
    private char nextDirection = ' ';

    // Images
    private Image wallImg, blueG, orangeG, pinkG, redG, powerPelletImg;
    private Image pacUp, pacDown, pacLeft, pacRight;

    private final char[] dirs = {'U','D','L','R'};
    private final int[] dX = {0,0,-1,1}, dY = {-1,1,0,0};
    private final Random rnd = new Random();

    public PacMan(JFrame frame, Difficulty difficulty) {
        this.frame = frame;
        this.difficulty = difficulty;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadImages();
        initGame();
    }

    private Image loadImage(String path) {
        try {
            URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL).getImage();
            } else {
                System.err.println("Couldn't find file: " + path);
                return createMissingImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            return createMissingImage();
        }
    }

    private Image createMissingImage() {
        BufferedImage img = new BufferedImage(TILE, TILE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, TILE, TILE);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", TILE/2, TILE/2);
        g2d.dispose();
        return img;
    }

    private void loadImages() {
        wallImg = loadImage("/Assets/wall.png");
        blueG = loadImage("/Assets/blueGhost.png");
        orangeG = loadImage("/Assets/orangeGhost.png");
        pinkG = loadImage("/Assets/pinkGhost.png");
        redG = loadImage("/Assets/redGhost.png");
        powerPelletImg = loadImage("/Assets/cherry.png");
        pacUp = loadImage("/Assets/pacmanUp.png");
        pacDown = loadImage("/Assets/pacmanDown.png");
        pacLeft = loadImage("/Assets/pacmanLeft.png");
        pacRight = loadImage("/Assets/pacmanRight.png");
    }

    private void initGame() {
        walls.clear(); foods.clear(); ghosts.clear();
        score = 0;
        lives = 3;
        gameOver = false;
        powerPellet = null;
        powerPelletTimer = 0;
        ghostSpawnTimer = 0;
        nextDirection = ' ';
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = tileMap[r].charAt(c);
                int x = c * TILE, y = r * TILE;
                switch (ch) {
                    case 'X': walls.add(new Block(wallImg, x, y)); break;
                    case ' ': foods.add(new Block(null, x + 14, y + 14, 4, 4, ' ')); break;
                    case 'P': pacman = new Block(pacRight, x, y, TILE, TILE, ' '); break;
                    case 'b': ghosts.add(new Block(blueG, x, y, TILE, TILE, 'b')); break;
                    case 'o': ghosts.add(new Block(orangeG, x, y, TILE, TILE, 'o')); break;
                    case 'p': ghosts.add(new Block(pinkG, x, y, TILE, TILE, 'p')); break;
                    case 'r': ghosts.add(new Block(redG, x, y, TILE, TILE, 'r')); break;
                }
            }
        }
        ghosts.forEach(Block::setRandomDir);
        timer = new Timer(50, this);
        timer.start();
    }

    private void spawnPowerPellet() {
        List<Point> availableSpots = new ArrayList<>();
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                final int currentR = r;
                final int currentC = c;
                char ch = tileMap[currentR].charAt(currentC);
                if (ch == ' ') {
                    boolean hasFood = foods.stream().anyMatch(f -> 
                        f.x == currentC * TILE + 14 && f.y == currentR * TILE + 14);
                    if (!hasFood) {
                        availableSpots.add(new Point(currentC * TILE, currentR * TILE));
                    }
                }
            }
        }
        
        if (!availableSpots.isEmpty()) {
            Point spot = availableSpots.get(rnd.nextInt(availableSpots.size()));
            powerPellet = new Block(powerPelletImg, spot.x, spot.y, TILE, TILE, ' ');
        }
    }

    private void spawnAdditionalGhost() {
        if (ghosts.size() >= MAX_GHOSTS) return;
        
        List<Point> availableSpots = new ArrayList<>();
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                final int currentR = r;
                final int currentC = c;
                char ch = tileMap[currentR].charAt(currentC);
                if (ch == ' ') {
                    boolean hasGhost = ghosts.stream().anyMatch(g -> 
                        g.x == currentC * TILE && g.y == currentR * TILE);
                    if (!hasGhost) {
                        availableSpots.add(new Point(currentC * TILE, currentR * TILE));
                    }
                }
            }
        }
        
        if (!availableSpots.isEmpty()) {
            Point spot = availableSpots.get(rnd.nextInt(availableSpots.size()));
            Block newGhost = new Block(redG, spot.x, spot.y, TILE, TILE, 'r');
            newGhost.setRandomDir();
            ghosts.add(newGhost);
        }
    }

    private List<Point> dijkstra(Point start, Point goal) {
        int[][] dist = new int[COLS][ROWS];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[start.x][start.y] = 0;
        Map<Point, Point> prev = new HashMap<>();
        PriorityQueue<Point> pq = new PriorityQueue<>(Comparator.comparingInt(p -> dist[p.x][p.y]));
        pq.add(start);

        while (!pq.isEmpty()) {
            Point u = pq.poll();
            if (u.equals(goal)) break;
            List<Point> neighbors = Arrays.asList(
                new Point(u.x -1,u.y), new Point(u.x+1,u.y),
                new Point(u.x,u.y-1), new Point(u.x,u.y+1)
            );
            Collections.shuffle(neighbors, rnd);

            for (Point v : neighbors) {
                if (v.x<0||v.x>=COLS||v.y<0||v.y>=ROWS) continue;
                if (tileMap[v.y].charAt(v.x)=='X') continue;
                int alt = dist[u.x][u.y] + 1;
                if (alt < dist[v.x][v.y]) {
                    dist[v.x][v.y] = alt;
                    prev.put(v,u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }

        if (!prev.containsKey(goal)) return null;
        LinkedList<Point> path = new LinkedList<>();
        for (Point p = goal; !p.equals(start); p = prev.get(p))
            path.addFirst(p);
        return path;
    }

    @Override public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            timer.stop();
            showGameOverScreen();
            return;
        }

        powerPelletTimer += 50;
        if (powerPellet == null && powerPelletTimer >= POWER_PELLET_SPAWN_TIME) {
            spawnPowerPellet();
            powerPelletTimer = 0;
        }

        ghostSpawnTimer += 50;
        if (ghostSpawnTimer >= GHOST_SPAWN_TIME) {
            spawnAdditionalGhost();
            ghostSpawnTimer = 0;
        }

        pacman.move();
        
        if (collisionWall(pacman)) {
            pacman.undo();
            if (pacman.vx != 0) {
                if (pacman.canMove('U')) {
                    pacman.updateDir('U');
                } else if (pacman.canMove('D')) {
                    pacman.updateDir('D');
                }
            } else if (pacman.vy != 0) {
                if (pacman.canMove('L')) {
                    pacman.updateDir('L');
                } else if (pacman.canMove('R')) {
                    pacman.updateDir('R');
                }
            }
        }

        if (powerPellet != null && pacman.collides(powerPellet)) {
            lives++;
            powerPellet = null;
            powerPelletTimer = 0;
        }

        for (Block g : ghosts) {
            if (!g.atTileCenter()) { g.move(); continue; }

            Point gp = g.grid();
            Point pp = pacman.grid();
            boolean chase = difficulty == Difficulty.HARD
                          || (difficulty == Difficulty.MEDIUM && g.type == 'r');

            if (chase) g.chaseDijkstra(gp, pp);
            else g.setRandomDir();

            g.move();
            if (collisionWall(g)) { g.undo(); g.setRandomDir(); }

            if (g.collides(pacman)) {
                lives--;
                if (lives == 0) gameOver = true;
                else resetPositions();
            }
        }

        foods.removeIf(f -> {
            if (pacman.collides(f)) { score += 10; return true; }
            return false;
        });

        if (foods.isEmpty()) initGame();

        repaint();
    }

    private void showGameOverScreen() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            GameOverPanel gameOverPanel = new GameOverPanel(frame, score);
            frame.add(gameOverPanel);
            frame.pack();
            frame.revalidate();
            gameOverPanel.requestFocusInWindow();
        });
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        walls.forEach(w -> g.drawImage(w.image, w.x, w.y, w.width, w.height, null));
        g.setColor(Color.WHITE);
        foods.forEach(f -> g.fillRect(f.x, f.y, f.width, f.height));
        if (powerPellet != null) {
            g.drawImage(powerPellet.image, powerPellet.x, powerPellet.y, 
                       powerPellet.width, powerPellet.height, null);
        }
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        ghosts.forEach(h -> g.drawImage(h.image, h.x, h.y, h.width, h.height, null));
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Lives: " + lives + "  Score: " + score, TILE, TILE);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {
        if (gameOver) return;
        
        char newDir = ' ';
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: newDir = 'U'; pacman.image = pacUp; break;
            case KeyEvent.VK_DOWN: newDir = 'D'; pacman.image = pacDown; break;
            case KeyEvent.VK_LEFT: newDir = 'L'; pacman.image = pacLeft; break;
            case KeyEvent.VK_RIGHT: newDir = 'R'; pacman.image = pacRight; break;
        }
        
        if (newDir != ' ') {
            if (pacman.canMove(newDir)) {
                pacman.updateDir(newDir);
                nextDirection = ' ';
            } else {
                nextDirection = newDir;
            }
        }
    }

    private void resetPositions() {
        pacman.reset();
        ghosts.forEach(Block::reset);
    }

    private boolean collisionWall(Block b) {
        Rectangle r = new Rectangle(b.x + 2, b.y + 2, b.width - 4, b.height - 4);
        return walls.stream().anyMatch(w -> r.intersects(new Rectangle(w.x + 2, w.y + 2, w.width - 4, w.height - 4)));
    }

    class Block {
        int x, y, width, height, startX, startY;
        char type, direction = 'U';
        int vx = 0, vy = 0;
        Image image;

        Block(Image img, int x, int y) { this(img, x, y, TILE, TILE, ' '); }
        Block(Image img, int x, int y, int w, int h, char t) {
            this.image = img; this.x = x; this.y = y; this.startX = x; this.startY = y;
            this.width = w; this.height = h; this.type = t;
        }

        Point grid() { return new Point(x / TILE, y / TILE); }

        boolean atTileCenter() {
            return x % TILE == 0 && y % TILE == 0;
        }

        void updateDir(char dir) {
            this.direction = dir;
            switch (dir) {
                case 'U': vy = -PACMAN_SPEED; vx = 0; break;
                case 'D': vy = PACMAN_SPEED; vx = 0; break;
                case 'L': vx = -PACMAN_SPEED; vy = 0; break;
                case 'R': vx = PACMAN_SPEED; vy = 0; break;
            }
        }

        void setRandomDir() {
            List<Character> opts = new ArrayList<>();
            for (char d : dirs) {
                if (canMove(d) && opposite(d) != direction) opts.add(d);
            }
            if (!opts.isEmpty()) updateDir(opts.get(rnd.nextInt(opts.size())));
        }

        void chaseDijkstra(Point gp, Point pp) {
            List<Point> path = dijkstra(gp, pp);
            if (path != null && !path.isEmpty())
                updateDirByPath(path.get(0), gp);
            else
                setRandomDir();
        }

        void updateDirByPath(Point next, Point cur) {
            if (next.x > cur.x) updateDir('R');
            else if (next.x < cur.x) updateDir('L');
            else if (next.y > cur.y) updateDir('D');
            else updateDir('U');
        }

        char opposite(char d) {
            switch (d) {
                case 'U': return 'D';
                case 'D': return 'U';
                case 'L': return 'R';
                case 'R': return 'L';
            }
            return ' ';
        }

        boolean canMove(char d) {
            int nx = x, ny = y;
            switch (d) {
                case 'U': ny -= PACMAN_SPEED; break;
                case 'D': ny += PACMAN_SPEED; break;
                case 'L': nx -= PACMAN_SPEED; break;
                case 'R': nx += PACMAN_SPEED; break;
            }
            Rectangle r = new Rectangle(nx + 2, ny + 2, width - 4, height - 4);
            return walls.stream().noneMatch(w -> r.intersects(new Rectangle(w.x + 2, w.y + 2, w.width - 4, w.height - 4)));
        }

        void move() { 
            if (this == pacman && nextDirection != ' ' && canMove(nextDirection)) {
                updateDir(nextDirection);
                nextDirection = ' ';
            }
            
            x += vx; 
            y += vy; 
            
            if (x < -width) x = boardWidth;
            if (x > boardWidth) x = -width;
        }

        void undo() { x -= vx; y -= vy; }
        void reset() { x = startX; y = startY; vx = vy = 0; }
        boolean collides(Block o) {
            return x < o.x + o.width && x + width > o.x
                && y < o.y + o.height && y + height > o.y;
        }
    }
}