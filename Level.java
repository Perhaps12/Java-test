import java.awt.*;
import java.util.ArrayList;

/**
 * Level class that manages walls, spawn points, and level-specific
 * configurations
 */
public class Level {
    private ArrayList<Wall> walls;
    private Vector2D playerSpawnPoint;
    private ArrayList<Vector2D> npcSpawnPoints;
    private String levelName;
    private int levelWidth;
    private int levelHeight;
    private int wallThickness;
    private Color backgroundColor;

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
                levelHeight));

        walls.add(new Wall(levelWidth, // right
                -halfHeight,
                wallThickness,
                levelHeight));

        walls.add(new Wall(0, // bottom
                halfHeight,
                levelWidth,
                wallThickness));
        walls.add(new Wall(0, // top
                -halfHeight - wallThickness,
                levelWidth,
                wallThickness));

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
     * Create a platform wall (horizontal)
     */
    public void addPlatform(double x, double y, double width) {
        addWall(x, y, width, wallThickness);
    }

    /**
     * Create a vertical barrier wall
     */
    public void addBarrier(double x, double y, double height) {
        addWall(x, y, wallThickness, height);
    }

    /**
     * Create a room/chamber with walls on all sides
     */
    public void addRoom(double x, double y, double width, double height) {
        // Left wall
        addWall(x, y, wallThickness, height);
        // Right wall
        addWall(x + width - wallThickness, y, wallThickness, height);
        // Top wall
        addWall(x, y, width, wallThickness);
        // Bottom wall
        addWall(x, y + height - wallThickness, width, wallThickness);
    }

    /**
     * Draw all walls in the level
     */
    public void drawWalls(Graphics g) {
        for (Wall wall : walls) {
            wall.draw(g);
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
     * Get the center point of the level
     */
    public Vector2D getCenterPoint() {
        return new Vector2D(levelWidth / 2.0, 0); // Y=0 is the center line
    }

    /**
     * Clone this level
     */
    public Level clone(String newName) {
        Level clonedLevel = new Level(newName, levelWidth, levelHeight, wallThickness, backgroundColor);

        // Copy spawn points
        clonedLevel.playerSpawnPoint = new Vector2D(playerSpawnPoint);
        clonedLevel.npcSpawnPoints.clear();
        for (Vector2D spawn : npcSpawnPoints) {
            clonedLevel.npcSpawnPoints.add(new Vector2D(spawn));
        }

        // Copy walls (excluding default ones since they're already created)
        clonedLevel.walls.clear();
        for (Wall wall : walls) {
            // Create new wall with same properties
            clonedLevel.walls.add(new Wall(wall.getX(), wall.getY(), wall.getWidth(), wall.getHeight()));
        }

        return clonedLevel;
    }

    /**
     * Create a level with platforms mirrored across the center dividing wall
     */
    public void createMirroredPlatforms() {
        // Clear existing walls and recreate base structure
        resetWalls();

        // Add some example platforms in the top section
        // These will be automatically mirrored to the bottom section

        // Left side platform
        addPlatform(200, 200, 300);

        // Right side platform
        addPlatform(levelWidth - 500, 150, 350);

        // Center floating platform
        addPlatform(levelWidth / 2 - 100, 100, 200);

        // Small stepping platforms on the left
        addPlatform(100, 300, 120);
        addPlatform(250, 350, 120);
        addPlatform(400, 280, 120);

        // Small stepping platforms on the right
        addPlatform(levelWidth - 220, 320, 120);
        addPlatform(levelWidth - 370, 370, 120);
        addPlatform(levelWidth - 520, 300, 120);

        // Vertical barriers for interesting wall-jumping opportunities
        addBarrier(600, 80, 200);
        addBarrier(levelWidth - 650, 120, 180);

        // Now mirror all non-border platforms to the bottom section
        mirrorPlatformsToBottomSection();
    }

    /**
     * Mirror all platforms from top section to bottom section across the center
     * line (Y=0)
     */
    private void mirrorPlatformsToBottomSection() {
        ArrayList<Wall> platformsToMirror = new ArrayList<>();

        // Find all walls that are platforms (not border walls or center wall)
        for (Wall wall : walls) {
            double wallY = wall.getY();
            double wallBottom = wallY + wall.getHeight();

            // Only mirror platforms that are completely in the top section (positive Y
            // values)
            // Skip border walls and center dividing wall
            if (wallY > 20 && wallBottom < levelHeight / 2 - 20) {
                platformsToMirror.add(wall);
            }
        }

        // Create mirrored versions in the bottom section
        for (Wall platform : platformsToMirror) {
            double originalY = platform.getY();
            double mirroredY = -originalY - platform.getHeight(); // Mirror across Y=0

            // Add the mirrored platform
            Wall mirroredPlatform = new Wall(
                    platform.getX(),
                    mirroredY,
                    platform.getWidth(),
                    platform.getHeight());
            walls.add(mirroredPlatform);
        }
    }

    /**
     * Create a new level with mirrored platforms across the center dividing wall
     */
    public static Level createMirroredLevel(String levelName, int levelWidth, int levelHeight, int wallThickness) {
        Level level = new Level(levelName, levelWidth, levelHeight, wallThickness);
        level.createMirroredPlatforms();
        return level;
    }

    /**
     * Create a new level with mirrored platforms and custom background color
     */
    public static Level createMirroredLevel(String levelName, int levelWidth, int levelHeight, int wallThickness,
            Color backgroundColor) {
        Level level = new Level(levelName, levelWidth, levelHeight, wallThickness, backgroundColor);
        level.createMirroredPlatforms();
        return level;
    }

    // Getters
    public ArrayList<Wall> getWalls() {
        return walls;
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
