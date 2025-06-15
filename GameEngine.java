import java.awt.*;
import java.io.*;
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
    private static ArrayList<boolean[][]> levelLayout = new ArrayList<>();
    private static Level currentLevel; // Level manages its own walls // Game settings
    private static final int MAX_PROJECTILES = 1000;
    private static final ConcurrentLinkedQueue<Projectile> queuedProjectiles = new ConcurrentLinkedQueue<>();
    private static final Set<Integer> keys = new HashSet<>();

    // Death screen system
    private static boolean isDeathScreenActive = false;
    private static double deathScreenStartTime = 0;
    private static double playerDeathX = 0;
    private static double playerDeathY = 0;
    private static final double DEATH_SCREEN_DURATION = 0.7; // 1 second

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
        currentLevel = new Level("Main Level", LEVEL_WIDTH, LEVEL_HEIGHT, 10);
        currentLevel.setPlayerSpawnPoint(50, -100);
        createLevelLayouts(2);
        createPlatformLayout(2);
        Vector2D playerSpawn = currentLevel.getPlayerSpawnPoint();
        player = new Player("/Sprites/Character/Idle/sprite_0.png", playerSpawn.getX(), playerSpawn.getY()); // Create
                                                                                                             // NPCs at
                                                                                                             // their
                                                                                                             // spawn
                                                                                                             // points
        ArrayList<Vector2D> npcSpawns = currentLevel.getNpcSpawnPoints();
        // if (npcSpawns.size() > 0) {
        // Vector2D coinSpawn = npcSpawns.get(0);
        // npcs.add(new Npc(coinSpawn.getX(), coinSpawn.getY(), 0)); // Coin NPC
        // }

        if (npcSpawns.size() > 1) {
            Vector2D cloneSpawn = npcSpawns.get(1);
            npcs.add(new Npc(cloneSpawn.getX(), cloneSpawn.getY(), 1)); // Clone NPC
        }
    }

    private static void createLevelLayouts(int ID) {
        BufferedReader reader;
        try {
            switch (ID) {
                case 1 -> {
                    reader = new BufferedReader(new FileReader("Static//Level1.txt"));
                }

                case 2 -> {
                    reader = new BufferedReader(new FileReader("Static//Level2.txt"));
                }
                case 3 -> {
                    reader = new BufferedReader(new FileReader("Static//Level3.txt"));
                }
                default -> {
                    reader = new BufferedReader(new FileReader("Static//Level0.txt"));
                }

            }
            // Read level layout from file
            while (reader.ready()) {
                String dimensions[] = reader.readLine().split(" ");
                int r = Integer.parseInt(dimensions[0]);
                int c = Integer.parseInt(dimensions[1]);
                boolean layout[][] = new boolean[r][c];
                if (dimensions[2].equals("0")) {
                    for (int i = 0; i < r; i++) {
                        String str = reader.readLine();
                        for (int j = 0; j < c; j++) {
                            layout[i][j] = str.charAt(j) == '1';
                        }
                    }
                } else {
                    // Fill in blocks for the entire layout
                    for (int i = 0; i < r; i++) {
                        for (int j = 0; j < c; j++) {
                            layout[i][j] = true;
                        }
                    }
                }
                levelLayout.add(layout);

            }
        } catch (Exception e) {
            System.out.println("err"); // kenny debug
            e.printStackTrace();
        }

    }

    /**
     * reate a platform layout directly in code
     */
    private static void createPlatformLayout(int ID) {
        switch (ID) {
            case 1 -> {
                currentLevel.addPlatformsFromLayout(levelLayout.get(0), -45, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(0), 500, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(1), 1100, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 1400, -600);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1350, 150);
                currentLevel.addPlatformsFromLayout(levelLayout.get(4), 1650, -500);
                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 1700, 100);
                currentLevel.addPlatformsFromLayout(levelLayout.get(1), 1650, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(5), 1900, 380);
                currentLevel.addPlatformsFromLayout(levelLayout.get(0), 2100, -60);
                addPermanentDualHeadLaser(1421.5, -75, 30, 225, false, false);
                addPermanentDualHeadLaser(1800, -15, 300, 30, true, false);
                addPermanentDualHeadLaser(2002.5, -127, 30, 507.5, false, false);
                // addPermanentDualHeadLaser(1800, 50, 300, 30, true, false);
                addSpike(700, -75, 36, 36, false, false);
                addSpike(736, -75, 36, 36, false, false);
                addSpike(1792, 300, 36, 36, true, false);
                addSpike(1792, 336, 36, 36, true, false);
                addSpike(1792, 372, 36, 36, true, false);
                addSpike(1792, 408, 36, 36, true, false);
                addSpike(1683, 346, 36, 36, true, true);
                addSpike(1683, 382, 36, 36, true, true);
                addSpike(1683, 418, 36, 36, true, true);

            }

            case 2 -> {
                currentLevel.addPlatformsFromLayout(levelLayout.get(0), 0, 0);
                currentLevel.addPlatformsFromLayout(levelLayout.get(1), 700, -375);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 625, -30);
                addSpike(675, -200, 50, 50, true, true);
                addSpike(675, -250, 50, 50, true, true);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 625, 150);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1100, -375);
                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 1400, -570);
                addLaser(0, 165 , 625 , 30 ,true , false);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1000, -100);
                addPermanentDualHeadLaser(850, -250, 550, 30, true, false);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1200, -100);
                currentLevel.addPlatformsFromLayout(levelLayout.get(4), 1200, 150);
                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 975, 200);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1000, 70);
                currentLevel.addPlatformsFromLayout(levelLayout.get(4), 1600, 250);
                addPermanentDualHeadLaser(1080, 225, 120, 25, true, false);
                addPermanentDualHeadLaser(1350, 270, 255, 25, true, false);
                currentLevel.addPlatformsFromLayout(levelLayout.get(5), 1600, -250);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1900, -500);
                addPermanentDualHeadLaser(1925, -445, 25, 200, false, false);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2050, 320);
                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 2250, 200);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2150, 80);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1850, 80);
                addLaser(1925, -445, 25, 200, false, false);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2350, -140);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2600, -240);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2550, 320);
                currentLevel.addPlatformsFromLayout(levelLayout.get(0), 2700, -400);
                addPermanentDualHeadLaser(1750, 325, 300, 25, true, false);
                addSpike(750, 400, 50, 50, false, true);
                addSpike(875, 250, 50, 50, true, false);
                addSpike(950, 250, 50, 50, true, true);
                //addSpike(675, -250, 50, 50, true, true);
                addPermanentDualHeadLaser(825, -565, 20, 200, false, false);

            }
            case 3-> {
                currentLevel.addPlatformsFromLayout(levelLayout.get(0), -45, -60);
                
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 270, 120);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 170, 270);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 320, 370);
                
                currentLevel.addPlatformsFromLayout(levelLayout.get(1), 400, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 423, -400);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 423, 470);
                addPermanentDualHeadLaser(445, -355, 30, 295, false, false);
                addPermanentDualHeadLaser(445, 60, 30, 410, false, false);


                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 550, -300);
                currentLevel.addPlatformsFromLayout(levelLayout.get(3), 1050, -300);
                addSpike(580, -330, 60, 60, false, false);
                addLaser(610, -285, 440, 30, true, false);

                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 800, 150);

                currentLevel.addPlatformsFromLayout(levelLayout.get(1), 1200, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1223, -400);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1223, 470);
                addPermanentDualHeadLaser(1245, -355, 30, 295, false, false);
                addPermanentDualHeadLaser(1245, 60, 30, 410, false, false);

                addPermanentDualHeadLaser(520, -15, 680, 30, true, false);

                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1400, -100);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 1700, 200);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2000, -300);

                currentLevel.addPlatformsFromLayout(levelLayout.get(1), 2100, -60);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2123, -400);
                currentLevel.addPlatformsFromLayout(levelLayout.get(2), 2123, 470);
                addPermanentDualHeadLaser(2145, -355, 30, 295, false, false);
                addPermanentDualHeadLaser(2145, 60, 30, 410, false, false);
                addPermanentDualHeadLaser(2220, -15, 900, 30, true, false);

                currentLevel.addPlatformsFromLayout(levelLayout.get(4), 2400, -600);

                currentLevel.addPlatformsFromLayout(levelLayout.get(5), 2500, 50);
                currentLevel.addPlatformsFromLayout(levelLayout.get(6), 2600-65, -520);
                addSpike(2500+45, 325+15-50, 30, 30, false, true);


            }
            default -> {
                System.out.println("No platform layout found for ID: " + ID);
            }
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
        } // Update laser
        for (int i = 0; i < lasers.size(); i++) {
            lasers.get(i).update();
            if (!lasers.get(i).isActive()) {
                lasers.remove(i);
                i--;
            }
        }

        // Update spikes
        for (int i = 0; i < spikes.size(); i++) {
            spikes.get(i).update();
            if (!spikes.get(i).isActive()) {
                spikes.remove(i);
                i--;
            }
        }
        // Update level
        if (currentLevel != null) {
            currentLevel.update();
        } // Update water boundary effects
        WaterBoundary.getInstance().update(); // Update camera
        Camera.getInstance().update();

        // Handle death screen timing
        if (isDeathScreenActive) {
            double currentTime = System.nanoTime() / 1_000_000_000.0; // Convert to seconds
            if (currentTime - deathScreenStartTime >= DEATH_SCREEN_DURATION) {
                // Death screen finished, respawn player
                isDeathScreenActive = false;
                if (player != null) {
                    player.setActive(true); // Make player visible again
                }
                loadLevel(getCurrentLevel()); // Reset level and player position
            }
        }

        // Respawn method (only check for death if not already in death screen)
        if (!isDeathScreenActive) {
            respawn();
        }
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
        } // Check if player has inverted gravity (bottom side)

        // Draw clone character
        for (Npc npc : npcs) {
            if (npc.getID() == 1) { // Clone character
                npc.draw(g);
            }
        }

        // Draw player (only if active)
        if (player != null && player.isActive()) {
            player.draw(g);
        }

        // Draw other NPCs (not the clone)
        for (Npc npc : npcs) {
            if (npc.getID() != 1) { // All NPCs except clone
                npc.draw(g);
            }
        }

        // Draw projectiles
        for (Projectile p : projectiles) {
            p.draw(g);
        }
        
        // Draw lasers
        for (Laser laser : lasers) {
            laser.draw(g);
            // Only draw hitbox when laser is dangerous
            // if (laser.isDangerous()) {
            // g2d.setColor(Color.RED);
            // g2d.setStroke(new BasicStroke(2.0f));
            // double[] box = laser.box();
            // double left = box[0];
            // double right = box[1];
            // double top = box[2];
            // double bottom = box[3];
            // int boxWidth = (int) (right - left);
            // int boxHeight = (int) (bottom - top);
            // g2d.drawRect((int) left, (int) top, boxWidth, boxHeight);
            // }
            // hitbox
        }

        // Draw spikes
        for (Spike spike : spikes) {
            spike.draw(g); // Always try to draw (spike handles visibility internally)
        }

        WaterBoundary.getInstance().draw(g2d);

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
     * Add a laser with orientation parameters (positioned by top-left corner)
     * 
     * @param headX      X position of the laser head (top-left corner of hitbox)
     * @param headY      Y position of the laser head (top-left corner of hitbox)
     * @param width      Width of the laser
     * @param height     Height of the laser
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public static void addLaser(double headX, double headY, double width, double height, boolean horizontal,
            boolean reversed) {
        Laser laser = new Laser(headX, headY, width, height);
        laser.setOrientation(horizontal, reversed);
        lasers.add(laser);
    }

    /**
     * Add a permanent laser (always on) with orientation parameters (positioned by
     * top-left corner)
     * 
     * @param headX      X position of the laser head (top-left corner of hitbox)
     * @param headY      Y position of the laser head (top-left corner of hitbox)
     * @param width      Width of the laser
     * @param height     Height of the laser
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public static void addPermanentLaser(double headX, double headY, double width, double height, boolean horizontal,
            boolean reversed) {
        Laser laser = new Laser(headX, headY, width, height);
        laser.setPermanent(true);
        laser.setOrientation(horizontal, reversed);
        lasers.add(laser);
    }

    /**
     * Add a crystal spike with orientation parameters
     * Note: All crystals are permanent (always visible)
     * 
     * @param x          X position of the crystal center
     * @param y          Y position of the crystal center
     * @param width      Width of the crystal
     * @param height     Height of the crystal
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public static void addSpike(double x, double y, double width, double height, boolean horizontal, boolean reversed) {
        Spike spike = new Spike(x, y, width, height);
        spike.setOrientation(horizontal, reversed);
        spikes.add(spike);
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

        // System.out.println("loaded level " + currentLevel.getLevelName());
    }

    public static void respawn() {
        // Check collision with any laser
        boolean hitLaser = false;
        for (Laser laser : lasers) {
            if (laser.isColliding(player) && laser.isDangerous()) {
                // Reset laser orientation on death
                laser.resetOrientation();
                hitLaser = true;
            }
        }

        // Check collision with any spike
        boolean hitSpike = false;
        for (Spike spike : spikes) {
            if (spike.isColliding(player) && spike.isDangerous()) {
                // Reset spike orientation on death
                spike.resetOrientation();
                hitSpike = true;
            }
        }

        if (hitLaser || hitSpike) {
            // Start death screen effect instead of immediately respawning
            startDeathScreen();
        }
    }

    /**
     * Start the death screen effect
     */
    private static void startDeathScreen() {
        if (player != null && !isDeathScreenActive) {
            // Store death position and start death screen
            playerDeathX = player.getX();
            playerDeathY = player.getY();
            isDeathScreenActive = true;
            deathScreenStartTime = System.nanoTime() / 1_000_000_000.0; // Convert to seconds

            // Hide the player during death screen
            player.setActive(false);

            // Add intense camera rumble on death
            Camera camera = Camera.getInstance();
            camera.shake(35, 25, Camera.ShakeType.RANDOM); // Strong rumble for death

            // Create death screen effect projectiles in 45-degree intervals
            for (int i = 0; i < 8; i++) {
                double angle = i * 45.0; // 45-degree intervals (0, 45, 90, 135, 180, 225, 270, 315)
                double initialLogDist = 1.0; // Start with small log distance

                Projectile deathEffect = new Projectile(playerDeathX, playerDeathY, angle, initialLogDist);
                addProjectile(deathEffect);
            }

            // Ensure player always respawns with normal orientation
            player.setSwap(1); // Force normal gravity
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

    public static ArrayList<Spike> getSpikes() {
        return spikes;
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

    /**
     * Check if the death screen is currently active
     */
    public static boolean isDeathScreenActive() {
        return isDeathScreenActive;
    }

    /**
     * Add a dual-head laser with heads at both ends (positioned by top-left corner)
     * 
     * @param headX      X position of the laser head (top-left corner of hitbox)
     * @param headY      Y position of the laser head (top-left corner of hitbox)
     * @param width      Width of the laser
     * @param height     Height of the laser
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public static void addDualHeadLaser(double headX, double headY, double width, double height, boolean horizontal,
            boolean reversed) {
        Laser laser = new Laser(headX, headY, width, height, true); // true = dual heads
        laser.setOrientation(horizontal, reversed);
        lasers.add(laser);
    }

    /**
     * Add a permanent dual-head laser with heads at both ends (positioned by
     * top-left corner)
     * 
     * @param headX      X position of the laser head (top-left corner of hitbox)
     * @param headY      Y position of the laser head (top-left corner of hitbox)
     * @param width      Width of the laser
     * @param height     Height of the laser
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public static void addPermanentDualHeadLaser(double headX, double headY, double width, double height,
            boolean horizontal,
            boolean reversed) {
        Laser laser = new Laser(headX, headY, width, height, true); // true = dual heads
        laser.setPermanent(true);
        laser.setOrientation(horizontal, reversed);
        lasers.add(laser);
    }

    /**
     * Trigger clone shake effect when swap fails
     */
    public static void triggerCloneShake() {
        for (Npc npc : npcs) {
            if (npc.getID() == 1) { // Clone NPC
                npc.triggerFailedSwapEffect();
                break;
            }
        }
    }

}
