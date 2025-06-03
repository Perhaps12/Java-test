import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.*;
import javax.swing.*;

/**
 * Gameloop class that handles the rendering and timing of the game
 */
public class Gameloop extends Canvas implements Runnable, KeyListener, ComponentListener {
    private final JFrame frame;
    public static boolean running = false;

    // FPS calculations
    private int frames = 0;
    private int fps = 0;
    private long fpsTimer = System.currentTimeMillis();

    // Set to track pressed keys
    private final Set<Integer> keys = new HashSet<>();

    /**
     * Create the game window and initialize the canvas
     */
    public Gameloop() {
        frame = new JFrame("Game Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(true);
        GameSettings settings = GameSettings.getInstance();
        setPreferredSize(new Dimension(settings.getWidth(), settings.getHeight()));
        setFocusable(true);
        addKeyListener(this);
        addComponentListener(this);

        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        createBufferStrategy(2);
    }

    /**
     * Start the game loop
     */
    public void start() {
        running = true;
        new Thread(this).start();
    }

    /**
     * Main game loop that runs at approximately 60fps
     */
    @Override
    public void run() {
        // Frame calculations
        final long frameTime = 1_000_000_000 / 60; // Target 60 FPS
        long lastTime = System.nanoTime();

        // Initialize game state
        GameEngine.initializeGame(); // Keep gameloop running
        while (running) {
            long now = System.nanoTime();
            if (now - lastTime >= frameTime) {
                // Update game state
                GameEngine.update();

                // Render the current frame
                render();

                // FPS calculations
                frames++;
                if (System.currentTimeMillis() - fpsTimer >= 1000) {
                    fps = frames;
                    frames = 0;
                    fpsTimer += 1000;
                }
                lastTime = now;
            }

            try {
                Thread.sleep(1); // Yield to prevent CPU overuse
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Render the current game state
     */
    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2);
            return;
        }

        // Use Graphics2D for better rendering quality
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        // Enable antialiasing for better quality scaling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Apply scaling transform to render at current resolution
        GameSettings settings = GameSettings.getInstance();
        g.scale(settings.getScaleX(), settings.getScaleY());

        // Clear the screen with scaled coordinates
        g.setColor(settings.getBackgroundColor());
        g.fillRect(0, 0, settings.getBaseWidth(), settings.getBaseHeight()); // Draw grid for visual reference if
                                                                             // enabled

        g.setStroke(new BasicStroke(3));

        // Render game elements
        GameEngine.render(g); // Display debug information if enabled
        if (GameSettings.getInstance().isShowDebug()) {
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 24));

            int lineY = 30;
            int lineHeight = 30;

            if (GameSettings.getInstance().isShowFPS()) {
                g.drawString("FPS: " + fps, 13, lineY);
                lineY += lineHeight;
            }

            g.drawString("Projectiles: " + GameEngine.getProjectiles().size(), 13, lineY);
            lineY += lineHeight;

            if (!GameEngine.getNpcs().isEmpty()) {
                g.drawString("NPCs: " + GameEngine.getNpcs().size(), 13, lineY);
                lineY += lineHeight;
            }

            // Display active keys for debugging
            g.drawString("Active Keys: " + GameEngine.getKeys().size(), 13, lineY);
            lineY += lineHeight;

            // Display resolution and scaling info
            g.drawString("Resolution: " + settings.getWidth() + "x" + settings.getHeight(), 13, lineY);
            lineY += lineHeight;
            g.drawString("Scale: " + String.format("%.2f", settings.getScaleX()) + "x"
                    + String.format("%.2f", settings.getScaleY()), 13, lineY);
        }

        // Clean up
        g.dispose();
        bs.show();
        Toolkit.getDefaultToolkit().sync(); // force render
    } // Add/remove pressed keys to a hashset to detect what the user is pressing

    @Override
    public void keyPressed(KeyEvent e) {
        keys.add(e.getKeyCode());
        GameEngine.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode());
        GameEngine.keyReleased(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // ComponentListener methods for handling window resize
    @Override
    public void componentResized(ComponentEvent e) {
        // Update GameSettings when window is resized
        Dimension newSize = getSize();
        if (newSize.width > 0 && newSize.height > 0) {
            GameSettings.getInstance().updateResolution(newSize.width, newSize.height);
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // Not needed
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // Not needed
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // Not needed
    }
}
