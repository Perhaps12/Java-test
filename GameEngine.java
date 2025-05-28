import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Central game engine class that manages game state
 */
public class GameEngine {
    // Game entities
    private static Player player;
    private static final ArrayList<Projectile> projectiles = new ArrayList<>();
    private static final ArrayList<Npc> npcs = new ArrayList<>();
    private static final ArrayList<Wall> walls = new ArrayList<>();

    // Game settings
    private static final int MAX_PROJECTILES = 1000;
    private static final ConcurrentLinkedQueue<Projectile> queuedProjectiles = new ConcurrentLinkedQueue<>();
    private static final Set<Integer> keys = new HashSet<>(); // Dimensions (from GameSettings)
    public static final int WIDTH = GameSettings.getInstance().getBaseWidth();
    public static final int HEIGHT = GameSettings.getInstance().getBaseHeight();

    /**
     * Initialize the game world
     */
    public static void initializeGame() {
        // Create player
        player = new Player("/Sprites/O-4.png", 600, 100);

        // Create NPCs
        npcs.add(new Npc(640, 380, 0)); // Coin in middle
        npcs.add(new Npc(1000, 100, 1)); // Clone NPC

        // Create walls
        createWalls();
    }

    /**
     * Update game state for a single frame
     */
    public static void update() {
        // Process projectiles from queue
        while (!queuedProjectiles.isEmpty() && projectiles.size() < MAX_PROJECTILES) {
            projectiles.add(queuedProjectiles.poll());
        }

        // Update player
        if (player != null) {
            player.update();
        }

        // Update NPCs
        for (int i = 0; i < npcs.size(); i++) {
            npcs.get(i).update();
            if (!npcs.get(i).isActive()) {
                npcs.remove(i);
                i--;
            }
        } // Update projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            projectiles.get(i).update();
            if (!projectiles.get(i).isActive()) {
                projectiles.remove(i);
                i--;
            }
        }

        // Update camera
        Camera.getInstance().update();
    }

    /**
     * Draw all game elements
     */
    public static void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Apply camera transform for world objects
        Camera.getInstance().applyTransform(g2d);

        // Draw walls
        for (Wall wall : walls) {
            wall.draw(g);
        }

        // Draw player
        if (player != null) {
            player.draw(g);
        }

        // Draw NPCs
        for (Npc npc : npcs) {
            npc.draw(g);
        }

        // Draw projectiles
        for (Projectile proj : projectiles) {
            proj.draw(g);
        }

        // Remove camera transform (for UI elements if needed)
        Camera.getInstance().removeTransform(g2d);
    }

    /**
     * Create all walls in the game world
     */
    private static void createWalls() {
        // Border walls
        walls.add(new Wall(0, -400, 507, 3900)); // Left wall
        walls.add(new Wall(0, -500, 3900, 507)); // Top wall
        walls.add(new Wall(1920, -400, 1973, 3900)); // Right wall
        walls.add(new Wall(0, 785, 3900, 2715)); // Bottom wall

        // Interior walls and platforms
        walls.add(new Wall(0, 490, 500, 210)); // Big rectangle
        walls.add(new Wall(440, 490, 60, 230)); // Rectangle extension

        // Jail area
        walls.add(new Wall(1190, 340, 410, 10)); // Jail top
        walls.add(new Wall(1130, 400, 10, 680)); // Jail side

        // Hanging fan
        walls.add(new Wall(150, 200, 200, 10)); // Fan blade
        walls.add(new Wall(245, 0, 10, 210)); // Fan stem

        // Floating platforms for camera testing
        walls.add(new Wall(700, 150, 200, 50)); // Square platform
        walls.add(new Wall(900, 250, 150, 20)); // Small platform
        walls.add(new Wall(1200, 100, 100, 20)); // High platform
        walls.add(new Wall(1400, 300, 120, 20)); // Mid platform

        // Ground level platforms
        walls.add(new Wall(705, 510, 60, 10)); // Small ground platform
        walls.add(new Wall(800, 600, 200, 20)); // Medium ground platform
        walls.add(new Wall(1100, 650, 300, 30)); // Large ground platform

        // Elevated platforms for vertical movement
        walls.add(new Wall(200, 350, 100, 15)); // Mid-level platform
        walls.add(new Wall(350, 280, 80, 15)); // Higher platform
        walls.add(new Wall(500, 220, 120, 15)); // Even higher platform
    }

    /**
     * Add a projectile to the game
     */
    public static void addProjectile(Projectile projectile) {
        queuedProjectiles.add(projectile);
    }

    /**
     * Process a key press
     */
    public static void keyPressed(int keyCode) {
        keys.add(keyCode);
    }

    /**
     * Process a key release
     */
    public static void keyReleased(int keyCode) {
        keys.remove(keyCode);
    } // Getters

    public static Player getPlayer() {
        return player;
    }

    public static ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    public static ArrayList<Npc> getNpcs() {
        return npcs;
    }

    public static ArrayList<Wall> getWalls() {
        return walls;
    }

    public static Set<Integer> getKeys() {
        return keys;
    }

    public static int getMaxProjectiles() {
        return MAX_PROJECTILES;
    }

    /**
     * Check if a key is currently pressed
     */
    public static boolean isKeyPressed(int keyCode) {
        return keys.contains(keyCode);
    }
}
