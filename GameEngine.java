import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Central game engine class that manages game state
 */
public class GameEngine {
    // Game entities
    private static Player player;
    private static final ArrayList<Projectile> projectiles = new ArrayList<>();
    private static final Laser laser = new Laser(500,50,30,1000);
    private static final ArrayList<Spike> spikes = new ArrayList<>();
    private static final ArrayList<Npc> npcs = new ArrayList<>();
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

        createLevelLayouts(1);
        // for(boolean[][] i : Layout){
        //     for(boolean[] j : i){
        //         for(boolean k : j){
        //             System.out.print(((k)? 1:0 )+ " ");
        //         }System.out.println();
        //     }System.out.println();
        // }
        createPlatformLayout(1);


        // Create player at the level spawn point
        currentLevel.setPlayerSpawnPoint(0, -100);
        currentLevel.addSpike(500, 500, 100, 100);
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
    }
    
    private static ArrayList<boolean[][]> Layout = new ArrayList<>();

    private static void createLevelLayouts(int ID){
        BufferedReader reader;
        try{
            switch(ID){
                case 1 -> {
                    reader = new BufferedReader(new FileReader("Static//Level1.txt"));
                }
                
                default -> {
                    reader = new BufferedReader(new FileReader("Static//Level0.txt"));
                }

                
            }
            while(reader.ready()){
            String dimensions[] = reader.readLine().split(" ");
            int r = Integer.parseInt(dimensions[0]);
            int c = Integer.parseInt(dimensions[1]);
            boolean layout[][] = new boolean[r][c];
            if(dimensions[2].equals("0")){
                for(int i = 0 ; i < r; i++){
                    String str = reader.readLine();
                    for(int j = 0 ; j < c;j++){
                        layout[i][j] = str.charAt(j)=='1';
                    }
                }
            }
            else{
                for(int i = 0 ; i < r; i++){
                    for(int j = 0 ; j < c; j++){
                        layout[i][j]=true;
                    }
                }
            }
            Layout.add(layout);

        }
        }catch(Exception e){
            System.out.println("err");
        }
        
        
    }

    /**
     * reate a platform layout directly in code
     */
    private static void createPlatformLayout(int ID) {
        switch(ID){
            case 1->{
                currentLevel.generatePlatformsFromLayout(Layout.get(2), 15, 1500, 0);
                currentLevel.generatePlatformsFromLayout(Layout.get(0), 15, 200, 0);
            }
            default -> {

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
        } 

        // Update laser
        if(laser != null){
            laser.update(); 
        }
        // Update level
        if (currentLevel != null) {
            currentLevel.update();
        } // Update water boundary effects
        WaterBoundary.getInstance().update();

        
        // Update camera
        Camera.getInstance().update();
        
        //Respawn method
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
        }
        
        if(laser.isActive()){
            laser.draw(g);
                // Draw hitbox around projectile
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
            // Remove camera transform for ui
            Camera.getInstance().removeTransform(g2d);
        }
    }

        


    /**
     * Add a projectile to the game
     */
    public static void addProjectile(Projectile projectile) {
        queuedProjectiles.add(projectile);
    }

    
    /**
     * Add a laser
     */

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

    public static void respawn(){
        if(player.isColliding(laser) && laser.isActive()){
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
