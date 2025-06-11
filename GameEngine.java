import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Central game engine class that manages game state
 */
public class GameEngine { // Game entities
    private static Player player;
    private static final ArrayList<Projectile> projectiles = new ArrayList<>();
    private static final ArrayList<Spike> spikes = new ArrayList<>();
    private static final ArrayList<Npc> npcs = new ArrayList<>();
    private static final ArrayList<Laser> lasers = new ArrayList<>();
    private static Level currentLevel; // Level manages its own walls

    // Game settings
    private static final int MAX_PROJECTILES = 1000;
    private static final ConcurrentLinkedQueue<Projectile> queuedProjectiles = new ConcurrentLinkedQueue<>();
    private static final Set<Integer> keys = new HashSet<>();

    // Dimensions
    public static final int WIDTH = GameSettings.getInstance().getBaseWidth();
    public static final int HEIGHT = GameSettings.getInstance().getBaseHeight();

    // Level dimensions
    public static final int LEVEL_WIDTH = GameSettings.getInstance().getLevelWidth();
    public static final int LEVEL_HEIGHT = GameSettings.getInstance().getLevelHeight();

    /**
     * Initialize game
     */
    public static void initializeGame() {
        // Create the level first
        currentLevel = new Level("Main Level", LEVEL_WIDTH, LEVEL_HEIGHT, 10);

        currentLevel.setPlayerSpawnPoint(50, -100);
        // currentLevel.addSpike(500, 500, 100, 100); // Add lasers to the game with
        // different orientations
        addLaser(500, 300, 1000, 30, true, false); // Horizontal laser
        addLaser(800, 200, 30, 400, false, false); // Vertical laser
        addLaser(200, 100, 600, 25, true, true); // Horizontal laser (reversed)

        Vector2D playerSpawn = currentLevel.getPlayerSpawnPoint();
        player = new Player("/Sprites/Character/Idle/sprite_0.png", playerSpawn.getX(), playerSpawn.getY());

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
        createPlatformLayout(); // Create player at the level spawn point
    }

    /**
     * reate a platform layout directly in code
     */
    private static void createPlatformLayout() {
        int[][] Layout = {
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1 },
        };

        ArrayList<PlatformGenerator.PlatformPiece> extraPieces = new ArrayList<>();
        extraPieces.add(PlatformGenerator.createSimplePlatformBlock(600, -30, 45, 15, 2));
        extraPieces.add(PlatformGenerator.createSimplePlatformBlock(700, -60, 30, 15,3));
        currentLevel.addPlatformsFromPieces(extraPieces);
        currentLevel.addPlatformsFromLayout(Layout,  15, 0, -60);

        /**
         * Example small platform layout
         * int[][] smallLayout = {
         * { 1, 1, 0, 1 },
         * { 0, 1, 1, 1 }
         * };
         * currentLevel.addPlatformsFromLayout(smallLayout, 15, 400, -45);
         * 
         * Example individual platform pieces
         * ArrayList<PlatformGenerator.PlatformPiece> extraPieces = new ArrayList<>();
         * extraPieces.add(PlatformGenerator.createSimplePlatformBlock(600, -30, 45, 15,
         * 2));
         * extraPieces.add(PlatformGenerator.createSimplePlatformBlock(700, -60, 30, 15,
         * 3));
         * currentLevel.addPlatformsFromPieces(extraPieces);
         */
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
        } // Update laser
        for (int i = 0; i < lasers.size(); i++) {
            lasers.get(i).update();
            if (!lasers.get(i).isActive()) {
                lasers.remove(i);
                i--;
            }
        }
        // Update level
        if (currentLevel != null) {
            currentLevel.update();
        } // Update water boundary effects
        WaterBoundary.getInstance().update();

        // Update camera
        Camera.getInstance().update();

        // Respawn method
        respawn();
    }

    /**
     * Draw all game elements
     */
    public static void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw fixed background texture (before camera transform so it doesn't move)
        BackgroundRenderer.getInstance().drawBackground(g2d); // Apply camera transform for world objects
        Camera.getInstance().applyTransform(g2d); // Draw level (walls and background)
        if (currentLevel != null) {
            currentLevel.drawWalls(g);
            // Draw the pre-rendered platform layer
            currentLevel.drawPlatformLayer(g2d, Camera.getInstance());
            currentLevel.drawSpikes(g);
        }

        // Draw clone character under water effects (if it exists)
        for (Npc npc : npcs) {
            if (npc.getID() == 1) { // Clone character
                npc.draw(g);
            }
        }

        // Draw water boundary effect (behind everything else except clone)
        WaterBoundary.getInstance().draw(g2d);

        // Draw player
        if (player != null) {
            player.draw(g);
        }

        // Draw other NPCs (not the clone)
        for (Npc npc : npcs) {
            if (npc.getID() != 1) { // All NPCs except clone
                npc.draw(g);
            }
        } // Draw projectiles
        for (Projectile p : projectiles) {
            p.draw(g);
            // Draw hitbox around projectile
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2.0f));
            double[] box = p.box();
            double left = box[0];
            double right = box[1];
            double top = box[2];
            double bottom = box[3];
            int boxWidth = (int) (right - left);
            int boxHeight = (int) (bottom - top);
            g2d.drawRect((int) left, (int) top, boxWidth, boxHeight);
        } // Draw lasers
        for (Laser laser : lasers) {
            laser.draw(g); // Always try to draw (laser handles visibility internally)

            // Only draw hitbox when laser is dangerous
            if (laser.isDangerous()) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2.0f));
                double[] box = laser.box();
                double left = box[0];
                double right = box[1];
                double top = box[2];
                double bottom = box[3];
                int boxWidth = (int) (right - left);
                int boxHeight = (int) (bottom - top);
                g2d.drawRect((int) left, (int) top, boxWidth, boxHeight);
            }
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
     * Add a laser with orientation parameters
     * 
     * @param x          X position of the laser center
     * @param y          Y position of the laser center
     * @param width      Width of the laser
     * @param height     Height of the laser
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public static void addLaser(double x, double y, double width, double height, boolean horizontal, boolean reversed) {
        Laser laser = new Laser(x, y, width, height);
        laser.setOrientation(horizontal, reversed);
        lasers.add(laser);
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
            // Always ensure player spawns with normal gravity orientation
            player.setSwap(1);
        }

        System.out.println("loaded level " + currentLevel.getLevelName());
    }

    public static void respawn() {
        // Check collision with any laser
        boolean hitLaser = false;
        for (Laser laser : lasers) {
            if (player.isColliding(laser) && laser.isDangerous()) {
                // Reset laser orientation on death
                laser.resetOrientation();
                hitLaser = true;
            }
        }

        if (hitLaser) {
            // Ensure player always respawns with normal orientation
            // This handles the case where player swaps and dies simultaneously
            if (player != null) {
                player.setSwap(1); // Force normal gravity before reloading level
            }
            loadLevel(getCurrentLevel());
        }
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

    public static ArrayList<Laser> getLasers() {
        return lasers;
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
