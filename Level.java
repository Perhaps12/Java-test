import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Level class that manages walls, spawn points, and level-specific
 * configurations
 */
public class Level {
    private ArrayList<Wall> walls;    private ArrayList<Spike> spikes;
    private ArrayList<Wall> platformWalls; // Platform collision boxes (separate from visual)
    private Vector2D playerSpawnPoint;
    private ArrayList<Vector2D> npcSpawnPoints;
    private String levelName;
    private int levelWidth;
    private int levelHeight;
    private int wallThickness;
    private Color backgroundColor; 
    // Platform visual data (separate from collision) - tracks ALL sprites
    private ArrayList<PlatformGenerator.PlatformSpriteData> allPlatformSprites;
    private BufferedImage platformLayer; // Pre-rendered visual layer
    private boolean platformLayerReady = false;

    /**
     * Create a new level with basic parameters
     */
    public Level(String levelName, int levelWidth, int levelHeight, int wallThickness) {
        this.levelName = levelName;
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        this.wallThickness = wallThickness;
        this.backgroundColor = Color.WHITE;        this.walls = new ArrayList<>();
        this.spikes = new ArrayList<>();
        this.platformWalls = new ArrayList<>(); // Initialize platform walls
        this.allPlatformSprites = new ArrayList<>(); // Initialize sprite tracking
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
        int centerWallThickness = 1; // Create border walls positioned OUTSIDE the playable area to prevent clipping
        walls.add(new Wall(-wallThickness, // left - positioned completely outside play area
                -halfHeight - wallThickness,
                wallThickness,
                levelHeight + (2 * wallThickness), new Color(0, 0, 0, 0))); // transparent

        walls.add(new Wall(levelWidth, // right - positioned completely outside play area
                -halfHeight - wallThickness,
                wallThickness,
                levelHeight + (2 * wallThickness), new Color(0, 0, 0, 0))); // transparent

        walls.add(new Wall(-wallThickness, // bottom - positioned completely outside play area
                halfHeight,
                levelWidth + (2 * wallThickness),
                wallThickness, new Color(0, 0, 0, 0)));
        walls.add(new Wall(-wallThickness, // top - positioned completely outside play area
                -halfHeight - wallThickness,
                levelWidth + (2 * wallThickness),
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
     * Create a pre-rendered platform layer from the sprite data
     */
    private void createPlatformLayer(ArrayList<PlatformGenerator.PlatformSpriteData> sprites) {
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
        g2d.setComposite(AlphaComposite.SrcOver);

        // Calculate coordinate offset to handle negative Y coordinates
        // Level coordinate system: Y ranges from -levelHeight/2 to +levelHeight/2
        // BufferedImage coordinate system: Y ranges from 0 to levelHeight
        int halfHeight = levelHeight / 2;

        // Draw each sprite to the pre-rendered layer
        for (PlatformGenerator.PlatformSpriteData sprite : sprites) {
            // Convert world coordinates to image coordinates
            int imageX = (int) sprite.x;
            int imageY = (int) sprite.y + halfHeight; // Offset negative Y coordinates

            // Only draw if the sprite is within the image bounds
            if (imageX >= 0 && imageX < levelWidth &&
                    imageY >= 0 && imageY < levelHeight &&
                    imageX + sprite.width >= 0 &&
                    imageY + sprite.height >= 0) {

                // Load and draw the sprite
                try {
                    BufferedImage spriteImage = javax.imageio.ImageIO.read(
                            getClass().getResourceAsStream(sprite.spritePath));

                    if (spriteImage != null) {
                        // Apply rotation if needed
                        if (sprite.rotation != 0) {
                            spriteImage = rotateImage(spriteImage, sprite.rotation);
                        }

                        g2d.drawImage(spriteImage, imageX, imageY,
                                (int) sprite.width, (int) sprite.height, null);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading sprite: " + sprite.spritePath);
                    // Draw a colored rectangle as fallback
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(imageX, imageY, (int) sprite.width, (int) sprite.height);
                }
            }
        }

        g2d.dispose();
        platformLayerReady = true;

        System.out.println("Platform layer pre-rendered successfully");
    }

    /**
     * Rotate a BufferedImage by the specified angle
     */
    private BufferedImage rotateImage(BufferedImage original, int angle) {
        if (angle == 0)
            return original;

        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage rotated = new BufferedImage(width, height, original.getType());
        Graphics2D g2d = rotated.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    /**
     * Draw the pre-rendered platform layer
     */
    public void drawPlatformLayer(Graphics2D g, Camera camera) {
        if (platformLayerReady && platformLayer != null) {
            // The platform layer is pre-rendered with Y coordinates offset by halfHeight
            // to handle negative Y coordinates, so we need to offset it back when drawing
            int halfHeight = levelHeight / 2;
            g.drawImage(platformLayer, 0, -halfHeight, null);
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

    // Draw spikes in the level.
    public void drawSpikes(Graphics g) {
        for (Spike spike : spikes) {
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
    }    /**
     * Generate platforms from a custom 2D layout
     * Creates separate collision walls and visual sprites
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     */
    public void generatePlatformsFromLayout(int[][] layout, int tileSize) {
        // Clear existing platforms
        resetPlatformGeneration();

        // Generate single collision box for entire layout (not per tile)
        platformWalls.clear();
        ArrayList<Wall> generatedCollisionWalls = PlatformGenerator.generateCollisionPlatforms(layout, tileSize);
        platformWalls.addAll(generatedCollisionWalls);

        // Generate visual sprites (purely decorative)
        ArrayList<PlatformGenerator.PlatformSpriteData> visualSprites = PlatformGenerator.generateVisualSprites(layout,
                tileSize);

        // Store ALL sprites for future additions
        allPlatformSprites.clear();
        allPlatformSprites.addAll(visualSprites);

        // Pre-render visual layer
        createPlatformLayer(allPlatformSprites);

        System.out.println("Generated " + platformWalls.size() + " collision walls and " +
                visualSprites.size() + " visual sprites");
    }

    /**
     * Generate platforms from a custom 2D layout with custom x,y positioning
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     * @param offsetX  X offset to apply to all platforms
     * @param offsetY  Y offset to apply to all platforms
     */
    public void generatePlatformsFromLayout(int[][] layout, int tileSize, double offsetX, double offsetY) {
        // Clear existing platforms
        resetPlatformGeneration();

        // Generate single collision box for entire layout with offset (not per tile)
        platformWalls.clear();
        ArrayList<Wall> generatedCollisionWalls = PlatformGenerator.generateCollisionPlatformsWithOffset(layout,
                tileSize, offsetX, offsetY);
        platformWalls.addAll(generatedCollisionWalls);        // Generate visual sprites with offset (purely decorative)
        ArrayList<PlatformGenerator.PlatformSpriteData> visualSprites = PlatformGenerator
                .generateVisualSpritesWithOffset(layout, tileSize, offsetX, offsetY);

        // Store ALL sprites for future additions
        allPlatformSprites.clear();
        allPlatformSprites.addAll(visualSprites);

        // Pre-render visual layer
        createPlatformLayer(allPlatformSprites);

        System.out.println("Generated platforms from layout with offset (" + offsetX + ", " + offsetY + ")");
        System.out.println("Created " + platformWalls.size() + " collision walls and " +
                visualSprites.size() + " visual sprites");
    }

    /**
     * Generate platforms from individual pieces with custom coordinates
     * 
     * @param platformPieces List of platform pieces with custom coordinates
     */
    public void generatePlatformsFromPieces(ArrayList<PlatformGenerator.PlatformPiece> platformPieces) {
        // Clear existing platforms
        resetPlatformGeneration();

        // Generate collision walls and visual sprites separately
        platformWalls.clear();
        ArrayList<PlatformGenerator.PlatformSpriteData> visualSprites = new ArrayList<>();

        for (PlatformGenerator.PlatformPiece piece : platformPieces) {
            // Create invisible collision wall (simple rectangle for clean physics)
            Wall collisionWall = new Wall(piece.x, piece.y, piece.width, piece.height,
                    new Color(0, 0, 0, 0)); // Transparent
            platformWalls.add(collisionWall);

            // Create visual sprite data (purely decorative)
            String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png",
                    Math.max(0, Math.min(9, piece.spriteIndex)));
            visualSprites.add(new PlatformGenerator.PlatformSpriteData(
                    piece.x, piece.y, piece.width, piece.height, spritePath, piece.rotation));        }

        // Store ALL sprites for future additions
        allPlatformSprites.clear();
        allPlatformSprites.addAll(visualSprites);

        // Pre-render visual layer
        createPlatformLayer(allPlatformSprites);

        System.out.println("Generated " + platformPieces.size() + " platform pieces:");
        System.out.println("- " + platformWalls.size() + " collision walls (invisible)");
        System.out.println("- " + visualSprites.size() + " visual sprites (decorative)");
    }

    /**
     * Generate single collision box for entire platform layout
     * Creates one Wall object per platform layout, not per tile
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     * @param offsetX  X offset to apply to platform
     * @param offsetY  Y offset to apply to platform
     */
    public void generateSinglePlatformCollision(int[][] layout, int tileSize, double offsetX, double offsetY) {
        // Clear existing platforms
        resetPlatformGeneration();

        // Find bounding box of the entire platform layout
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        // Find the bounds of solid tiles
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                if (layout[row][col] == 1) {
                    minRow = Math.min(minRow, row);
                    maxRow = Math.max(maxRow, row);
                    minCol = Math.min(minCol, col);
                    maxCol = Math.max(maxCol, col);
                }
            }
        }

        // Create single collision box encompassing the entire platform
        if (minRow != Integer.MAX_VALUE) { // If we found any solid tiles
            double platformX = minCol * tileSize + offsetX;
            double platformY = minRow * tileSize + offsetY;
            double platformWidth = (maxCol - minCol + 1) * tileSize;
            double platformHeight = (maxRow - minRow + 1) * tileSize;

            // Create single invisible collision wall for entire platform
            Wall platformCollision = new Wall(platformX, platformY, platformWidth, platformHeight,
                    new Color(0, 0, 0, 0)); // Transparent
            platformWalls.add(platformCollision);

            System.out.println("Created single platform collision box:");
            System.out.println("  Position: (" + platformX + ", " + platformY + ")");
            System.out.println("  Size: " + platformWidth + "x" + platformHeight);        }

        // Generate visual sprites (purely decorative)
        ArrayList<PlatformGenerator.PlatformSpriteData> visualSprites = PlatformGenerator
                .generateVisualSpritesWithOffset(layout, tileSize, offsetX, offsetY);

        // Store ALL sprites for future additions
        allPlatformSprites.clear();
        allPlatformSprites.addAll(visualSprites);

        // Pre-render visual layer
        createPlatformLayer(allPlatformSprites);

        System.out.println("Generated single platform collision with " + platformWalls.size() +
                " collision walls and " + visualSprites.size() + " visual sprites");
    }

    /**
     * Generate optimized collision boxes for platform layout
     * Creates minimal number of collision boxes to cover all solid areas
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels
     * @param offsetX  X offset to apply to platforms
     * @param offsetY  Y offset to apply to platforms
     */
    public void generateOptimizedPlatformCollision(int[][] layout, int tileSize, double offsetX, double offsetY) {
        // Clear existing platforms
        resetPlatformGeneration();

        if (layout == null || layout.length == 0) {
            return;
        }

        // Find connected regions of solid tiles and create one collision box per region
        boolean[][] visited = new boolean[layout.length][layout[0].length];

        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                if (layout[row][col] == 1 && !visited[row][col]) {
                    // Found unvisited solid tile - trace the connected region
                    ArrayList<int[]> region = new ArrayList<>();
                    traceConnectedRegion(layout, visited, row, col, region);

                    if (!region.isEmpty()) {
                        // Create bounding box for this region
                        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
                        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

                        for (int[] tile : region) {
                            minRow = Math.min(minRow, tile[0]);
                            maxRow = Math.max(maxRow, tile[0]);
                            minCol = Math.min(minCol, tile[1]);
                            maxCol = Math.max(maxCol, tile[1]);
                        }

                        // Create collision box for this platform region
                        double platformX = minCol * tileSize + offsetX;
                        double platformY = minRow * tileSize + offsetY;
                        double platformWidth = (maxCol - minCol + 1) * tileSize;
                        double platformHeight = (maxRow - minRow + 1) * tileSize;

                        Wall platformCollision = new Wall(platformX, platformY, platformWidth, platformHeight,
                                new Color(0, 0, 0, 0)); // Transparent
                        platformWalls.add(platformCollision);
                    }
                }
            }
        }        // Generate visual sprites (purely decorative)
        ArrayList<PlatformGenerator.PlatformSpriteData> visualSprites = PlatformGenerator
                .generateVisualSpritesWithOffset(layout, tileSize, offsetX, offsetY);

        // Store ALL sprites for future additions
        allPlatformSprites.clear();
        allPlatformSprites.addAll(visualSprites);

        // Pre-render visual layer
        createPlatformLayer(allPlatformSprites);

        System.out.println("Generated optimized platform collision with " + platformWalls.size() +
                " collision regions and " + visualSprites.size() + " visual sprites");
    }

    /**
     * Helper method to trace connected solid tiles using flood fill
     */
    private void traceConnectedRegion(int[][] layout, boolean[][] visited, int startRow, int startCol,
            ArrayList<int[]> region) {
        if (startRow < 0 || startRow >= layout.length ||
                startCol < 0 || startCol >= layout[0].length ||
                visited[startRow][startCol] || layout[startRow][startCol] == 0) {
            return;
        }

        visited[startRow][startCol] = true;
        region.add(new int[] { startRow, startCol });

        // Check 4-connected neighbors (up, down, left, right)
        traceConnectedRegion(layout, visited, startRow - 1, startCol, region);
        traceConnectedRegion(layout, visited, startRow + 1, startCol, region);
        traceConnectedRegion(layout, visited, startRow, startCol - 1, region);
        traceConnectedRegion(layout, visited, startRow, startCol + 1, region);
    }

    // Getters

    public ArrayList<Wall> getWalls() {
        // Combine border walls and platform walls for collision detection
        ArrayList<Wall> allWalls = new ArrayList<>();
        allWalls.addAll(walls); // Border and structural walls
        allWalls.addAll(platformWalls); // Platform collision boxes
        return allWalls;
    }

    public ArrayList<Spike> getSpikes() {
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

    /**
     * Add platforms from a custom 2D layout to the existing level
     * Creates additional collision walls and visual sprites without clearing
     * existing ones
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     */
    public void addPlatformsFromLayout(int[][] layout, int tileSize) {
        // Generate single collision box for entire layout (not per tile)
        ArrayList<Wall> generatedCollisionWalls = PlatformGenerator.generateCollisionPlatforms(layout, tileSize);
        platformWalls.addAll(generatedCollisionWalls);

        // Generate visual sprites (purely decorative)
        ArrayList<PlatformGenerator.PlatformSpriteData> newVisualSprites = PlatformGenerator.generateVisualSprites(
                layout,
                tileSize);

        // Re-render visual layer with new sprites added
        addSpritesToPlatformLayer(newVisualSprites);

        System.out.println("Added " + generatedCollisionWalls.size() + " collision walls and " +
                newVisualSprites.size() + " visual sprites to existing level");
        System.out.println("Total platform walls: " + platformWalls.size());
    }

    /**
     * Add platforms from a custom 2D layout with custom x,y positioning to the
     * existing level
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     * @param offsetX  X offset to apply to all platforms
     * @param offsetY  Y offset to apply to all platforms
     */
    public void addPlatformsFromLayout(int[][] layout, int tileSize, double offsetX, double offsetY) {
        // Generate single collision box for entire layout with offset (not per tile)
        ArrayList<Wall> generatedCollisionWalls = PlatformGenerator.generateCollisionPlatformsWithOffset(layout,
                tileSize, offsetX, offsetY);
        platformWalls.addAll(generatedCollisionWalls);

        // Generate visual sprites with offset (purely decorative)
        ArrayList<PlatformGenerator.PlatformSpriteData> newVisualSprites = PlatformGenerator
                .generateVisualSpritesWithOffset(layout, tileSize, offsetX, offsetY);

        // Re-render visual layer with new sprites added
        addSpritesToPlatformLayer(newVisualSprites);

        System.out.println("Added platforms from layout with offset (" + offsetX + ", " + offsetY + ")");
        System.out.println("Added " + generatedCollisionWalls.size() + " collision walls and " +
                newVisualSprites.size() + " visual sprites to existing level");
        System.out.println("Total platform walls: " + platformWalls.size());
    }

    /**
     * Add platform pieces to the existing level
     * 
     * @param platformPieces List of platform pieces with custom coordinates
     */
    public void addPlatformsFromPieces(ArrayList<PlatformGenerator.PlatformPiece> platformPieces) {
        // Generate collision walls and visual sprites separately
        ArrayList<PlatformGenerator.PlatformSpriteData> newVisualSprites = new ArrayList<>();

        for (PlatformGenerator.PlatformPiece piece : platformPieces) {
            // Create invisible collision wall (simple rectangle for clean physics)
            Wall collisionWall = new Wall(piece.x, piece.y, piece.width, piece.height,
                    new Color(0, 0, 0, 0)); // Transparent
            platformWalls.add(collisionWall);

            // Create visual sprite data (purely decorative)
            String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png",
                    Math.max(0, Math.min(9, piece.spriteIndex)));
            newVisualSprites.add(new PlatformGenerator.PlatformSpriteData(
                    piece.x, piece.y, piece.width, piece.height, spritePath, piece.rotation));
        }

        // Re-render visual layer with new sprites added
        addSpritesToPlatformLayer(newVisualSprites);

        System.out.println("Added " + platformPieces.size() + " platform pieces to existing level:");
        System.out.println("- " + platformPieces.size() + " collision walls (invisible)");
        System.out.println("- " + newVisualSprites.size() + " visual sprites (decorative)");
        System.out.println("Total platform walls: " + platformWalls.size());
    }    /**
     * Helper method to add new sprites to the existing platform layer
     * This regenerates the entire visual layer with existing + new sprites
     */
    private void addSpritesToPlatformLayer(ArrayList<PlatformGenerator.PlatformSpriteData> newSprites) {
        // Add new sprites to the master list of all platform sprites
        allPlatformSprites.addAll(newSprites);

        // Regenerate the entire visual layer with ALL sprites (existing + new)
        createPlatformLayer(allPlatformSprites);

        System.out.println("Updated platform layer with " + newSprites.size() + " new sprites");
        System.out.println("Total platform sprites now: " + allPlatformSprites.size());
    }
}
