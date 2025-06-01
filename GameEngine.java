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
    private static Level currentLevel; // Level manages its own walls

    // Game settings
    private static final int MAX_PROJECTILES = 1000;
    private static final ConcurrentLinkedQueue<Projectile> queuedProjectiles = new ConcurrentLinkedQueue<>();
    private static final Set<Integer> keys = new HashSet<>();

    // Dimensions (from GameSettings)
    public static final int WIDTH = GameSettings.getInstance().getBaseWidth();
    public static final int HEIGHT = GameSettings.getInstance().getBaseHeight();

    // Level dimensions (scalable world size)
    public static final int LEVEL_WIDTH = GameSettings.getInstance().getLevelWidth();
    public static final int LEVEL_HEIGHT = GameSettings.getInstance().getLevelHeight();

    /**
     * Initialize the game world
     */
    public static void initializeGame() {
        GameSettings settings = GameSettings.getInstance(); // Create the level (uncomment one of these options)

        // Option 1: Default level layout
        // currentLevel = new Level("Main Level",
        // settings.getLevelWidth(),
        // settings.getLevelHeight(),
        // settings.getWallThickness());

        // Option 2: Level with mirrored platforms across center dividing wall
        currentLevel = Level.createMirroredLevel("Mirrored Level",
                settings.getLevelWidth(),
                settings.getLevelHeight(),
                settings.getWallThickness());

        // Create player at the level's spawn point
        currentLevel.setPlayerSpawnPoint(0, -100);
        Vector2D playerSpawn = currentLevel.getPlayerSpawnPoint();
        player = new Player("/Sprites/O-4.png", playerSpawn.getX(), playerSpawn.getY());

        // Create NPCs at their spawn points
        ArrayList<Vector2D> npcSpawns = currentLevel.getNpcSpawnPoints();

        if (npcSpawns.size() > 0) {
            Vector2D coinSpawn = npcSpawns.get(0);
            npcs.add(new Npc(coinSpawn.getX(), coinSpawn.getY(), 0)); // Coin NPC
        }

        if (npcSpawns.size() > 1) {
            Vector2D cloneSpawn = npcSpawns.get(1);
            npcs.add(new Npc(cloneSpawn.getX(), cloneSpawn.getY(), 1)); // Clone NPC
        }
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
        }

        // Update projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            projectiles.get(i).update();
            if (!projectiles.get(i).isActive()) {
                projectiles.remove(i);
                i--;
            }
        } // Update level
        if (currentLevel != null) {
            currentLevel.update();
        } // Update water boundary effects
        WaterBoundary.getInstance().update();

        // Update camera
        Camera.getInstance().update();
    }

    /**
     * Draw all game elements
     */
    public static void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Apply camera transform for world objects
        Camera.getInstance().applyTransform(g2d); // Draw level (walls and background)
        if (currentLevel != null) {
            currentLevel.drawWalls(g);
        } // Draw water boundary effect (behind everything else)
        WaterBoundary.getInstance().draw(g2d);

        // Draw player
        if (player != null) {
            player.draw(g);
        }

        // Draw NPCs
        for (Npc npc : npcs) {
            npc.draw(g);
        } // Draw projectiles
        for (Projectile proj : projectiles) {
            proj.draw(g);
        }

        // Remove camera transform for ui
        Camera.getInstance().removeTransform(g2d);
    }

    /**
     * Add a projectile to the game
     */
    public static void addProjectile(Projectile projectile) {
        queuedProjectiles.add(projectile);
    }

    /**
     * Load a new level
     */
    public static void loadLevel(Level newLevel) {
        currentLevel = newLevel;

        // Reset entities when loading new level
        Vector2D playerSpawn = currentLevel.getPlayerSpawnPoint();
        if (player != null) {
            player.setPosition(playerSpawn.getX(), playerSpawn.getY());
        }

        System.out.println("loaded level " + currentLevel.getLevelName());
    }

    /**
     * Get the current level
     */
    public static Level getCurrentLevel() {
        return currentLevel;
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
        return currentLevel != null ? currentLevel.getWalls() : new ArrayList<>();
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
