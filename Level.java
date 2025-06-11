import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Level class that manages walls, spawn points, and level-specific
 * configurations
 */
public class Level {
    private ArrayList<Wall> walls;
    private ArrayList<Spike> spikes; 
    private ArrayList<Wall> platformWalls; // Platform collision boxes (separate from visual)
    private Vector2D playerSpawnPoint;
    private ArrayList<Vector2D> npcSpawnPoints;
    private String levelName;
    private int levelWidth;
    private int levelHeight;
    private int wallThickness;
    private Color backgroundColor;

    // Pre-rendered platform layer for performance
    private BufferedImage platformLayer;
    private boolean platformLayerReady = false;

    /**
     * Create a new level with basic parameters
     */
    public Level(String levelName, int levelWidth, int levelHeight, int wallThickness) {
        this.levelName = levelName;
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        this.wallThickness = wallThickness;
        this.backgroundColor = Color.WHITE;
        this.walls = new ArrayList<>();
        this.spikes = new ArrayList<>(); 
        this.platformWalls = new ArrayList<>(); // Initialize platform walls
        this.npcSpawnPoints = new ArrayList<>();

        // Set default player spawn point to center-left of top section
        int halfHeight = levelHeight / 2;
        this.playerSpawnPoint = new Vector2D(levelWidth * 0.25, halfHeight - 100);

        // Create default walls
        createDefaultWalls();

        // Set default NPC spawn points
        setDefaultNpcSpawns();
        
    }

    /**
     * Create a level with custom background color
     */
    public Level(String levelName, int levelWidth, int levelHeight, int wallThickness, Color backgroundColor) {
        this(levelName, levelWidth, levelHeight, wallThickness);
        this.backgroundColor = backgroundColor;
        // platformWalls is already initialized in the main constructor
    }

    /**
     * Create the default wall layout for a two-section level
     */
    private void createDefaultWalls() {
        int halfHeight = levelHeight / 2;
        int centerWallThickness = 1;

        // Create border walls positioned at the screen edges for proper playable area
        walls.add(new Wall(-wallThickness, // left
                -halfHeight,
                wallThickness,
                levelHeight, new Color(0, 0, 0, 0))); // transparent

        walls.add(new Wall(levelWidth, // right
                -halfHeight,
                wallThickness,
                levelHeight, new Color(0, 0, 0, 0))); // transparent

        walls.add(new Wall(0, // bottom
                halfHeight,
                levelWidth,
                wallThickness, new Color(0, 0, 0, 0)));
        walls.add(new Wall(0, // top
                -halfHeight - wallThickness,
                levelWidth,
                wallThickness, new Color(0, 0, 0, 0))); // transparent

        // Center dividing wall separates top and bottom sections
        // Positioned at y=0 origin point (transparent for visual effect)
        walls.add(new Wall(0,
                -centerWallThickness / 2,
                levelWidth,
                centerWallThickness,
                new Color(0, 0, 0),
                0.0f)); // transparent
    }

    /**
     * Set default NPC spawn points
     */
    private void setDefaultNpcSpawns() {
        int halfHeight = levelHeight / 2;
        double levelCenterX = levelWidth / 2.0;

        // Coin NPC in top section
        npcSpawnPoints.add(new Vector2D(levelCenterX, halfHeight - 200));

        // Clone NPC in bottom section
        npcSpawnPoints.add(new Vector2D(levelCenterX, -halfHeight + 100));
    }

    /**
     * Add a custom wall to the level
     */
    public void addWall(Wall wall) {
        walls.add(wall);
    }
    
    

    /**
     * Add a custom wall with specified parameters
     */
    public void addWall(double x, double y, double width, double height) {
        walls.add(new Wall(x, y, width, height));
    }

    public void addSpike(double x, double y, double width, double height) {
        spikes.add(new Spike(x, y, width, height));
    }

    /**
     * Add a custom colored wall
     */
    public void addWall(double x, double y, double width, double height, Color color) {
        walls.add(new Wall(x, y, width, height, color));
    }

    /**
     * Remove all walls and recreate default layout
     */
    public void resetWalls() {
        walls.clear();
        createDefaultWalls();
    }

    /**
     * Create a pre-rendered platform layer from the generated platforms
     */
    private void createPlatformLayer(ArrayList<Wall> platforms) {
        System.out.println("Creating platform layer: " + levelWidth + "x" + levelHeight);

        // Create a BufferedImage the size of the level
        platformLayer = new BufferedImage(levelWidth, levelHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = platformLayer.createGraphics();

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // Clear the image with transparent background
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, levelWidth, levelHeight);
        g2d.setComposite(AlphaComposite.SrcOver); // Draw each platform to the pre-rendered layer
        for (Wall platform : platforms) {
            // Platform coordinates are in top-left origin system
            int imageX = (int) platform.getX();
            int imageY = (int) platform.getY();

            // Draw the platform sprite to the layer
            platform.drawToImage(g2d, imageX, imageY);
        }

        g2d.dispose();
        platformLayerReady = true;

        System.out.println("Platform layer pre-rendered successfully");
    }

    /**
     * Draw the pre-rendered platform layer
     */
    public void drawPlatformLayer(Graphics2D g, Camera camera) {
        if (platformLayerReady && platformLayer != null) {
            // The platform layer is pre-rendered in top-left coordinates
            // Draw it directly without coordinate conversion
            g.drawImage(platformLayer, 0, 0, null);
        }
    }

    /**
     * Reset the platform generation flag to allow regeneration
     */
    public void resetPlatformGeneration() {
        platformLayerReady = false;
        platformWalls.clear(); // Clear platform collision walls
        if (platformLayer != null) {
            platformLayer.flush();
            platformLayer = null;
        }
    }

    /**
     * Set player spawn point
     */
    public void setPlayerSpawnPoint(double x, double y) {
        this.playerSpawnPoint = new Vector2D(x, y);
    }

    /**
     * Add an NPC spawn point
     */
    public void addNpcSpawnPoint(double x, double y) {
        npcSpawnPoints.add(new Vector2D(x, y));
    }

    /**
     * Clear all NPC spawn points
     */
    public void clearNpcSpawnPoints() {
        npcSpawnPoints.clear();
    }

    /**
     * Get NPC spawn point by index
     */
    public Vector2D getNpcSpawnPoint(int index) {
        if (index >= 0 && index < npcSpawnPoints.size()) {
            return npcSpawnPoints.get(index);
        }
        return null;
    }

    /**
     * Draw all walls in the level
     */
    public void drawWalls(Graphics g) {
        for (Wall wall : walls) {
            wall.draw(g);
        }
    }

    //Draw spikes in the level. 
    public void drawSpikes(Graphics g){
        for(Spike spike: spikes){
            spike.draw(g); 
        }
    }

    /**
     * Update all walls (if any have dynamic behavior)
     */
    public void update() {
        for (Wall wall : walls) {
            wall.update();
        }
    }

    /**
     * Check if a point is within the level boundaries
     */
    public boolean isWithinBounds(double x, double y) {
        int halfHeight = levelHeight / 2;
        return x >= 0 && x <= levelWidth && y >= -halfHeight && y <= halfHeight;
    }

    /**
     * Generate platforms from a custom 2D layout
     * This is the main method for creating custom platform layouts.
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     */
    public void generatePlatformsFromLayout(boolean[][] layout, int tileSize) {
        // Clear existing platforms
        resetPlatformGeneration(); // Use the main generatePlatforms method
        ArrayList<Wall> customPlatforms = PlatformGenerator.generatePlatforms(layout, tileSize);

        // Add to collision system
        platformWalls.clear();
        for (Wall platform : customPlatforms) {
            platformWalls.add(platform);
        }

        // Pre-render visual layer
        createPlatformLayer(customPlatforms);
    }

    /**
     * Generate platforms from a custom 2D layout with custom x,y positioning
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     * @param offsetX  X offset to apply to all platforms
     * @param offsetY  Y offset to apply to all platforms
     */
    public void generatePlatformsFromLayout(boolean[][] layout, int tileSize, double offsetX, double offsetY) {
        // Clear existing platforms
        resetPlatformGeneration(); // Use the offset generatePlatforms method
        ArrayList<Wall> customPlatforms = PlatformGenerator.generatePlatformsWithOffset(layout, tileSize, offsetX,
                offsetY);

        // Add to collision system
        platformWalls.clear();
        for (Wall platform : customPlatforms) {
            platformWalls.add(platform);
        }

        // Pre-render visual layer
        createPlatformLayer(customPlatforms);

        System.out.println("Generated platforms from layout with offset (" + offsetX + ", " + offsetY + ")");
    }

    /**
     * Generate platforms from individual pieces with custom coordinates
     * 
     * @param platformPieces List of platform pieces with custom coordinates
     */
    public void generatePlatformsFromPieces(ArrayList<PlatformGenerator.PlatformPiece> platformPieces) {
        // Clear existing platforms
        resetPlatformGeneration(); // Generate platforms using the coordinate-based method
        ArrayList<Wall> customPlatforms = PlatformGenerator.generatePlatformsWithCoordinates(platformPieces);

        // Add to collision system
        platformWalls.clear();
        for (Wall platform : customPlatforms) {
            platformWalls.add(platform);
        }

        // Pre-render visual layer
        createPlatformLayer(customPlatforms);

        System.out.println("Generated platforms from " + platformPieces.size() + " individual pieces");
    }

    // Getters

    public ArrayList<Wall> getWalls() {
        // Combine border walls and platform walls for collision detection
        ArrayList<Wall> allWalls = new ArrayList<>();
        allWalls.addAll(walls); // Border and structural walls
        allWalls.addAll(platformWalls); // Platform collision boxes
        return allWalls;
    }

    public ArrayList<Spike> getSpikes(){
        return spikes;
    }

    public Vector2D getPlayerSpawnPoint() {
        return playerSpawnPoint;
    }

    public ArrayList<Vector2D> getNpcSpawnPoints() {
        return npcSpawnPoints;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getLevelWidth() {
        return levelWidth;
    }

    public int getLevelHeight() {
        return levelHeight;
    }

    public int getWallThickness() {
        return wallThickness;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    // Setters
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setWallThickness(int wallThickness) {
        this.wallThickness = wallThickness;
    }
}
