import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Handles generation of platform tiles
 */
public class PlatformGenerator {
    private static BufferedImage[] platformSprites;

    // Cache the loaded sprites
    static {
        loadPlatformSprites();
    }

    /**
     * Load all platform sprites from resources
     */
    private static void loadPlatformSprites() {
        platformSprites = new BufferedImage[10]; // 10 platform sprites

        try {
            for (int i = 0; i < 10; i++) {
                // Use zero-based indexing to match sprite file names
                String path = String.format("/Sprites/Platforms (1)/sprite_0%d.png", i);
                platformSprites[i] = ImageIO.read(PlatformGenerator.class.getResourceAsStream(path));
                System.out.println("Loaded platform sprite: " + path);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading platform sprites: " + e.getMessage());
        }
    }

    /**
     * Generate platform configuration from a binary array
     * 
     * @param arr 2D array where 1 = solid platform, 0 = empty space
     * @return 3D array containing [platformID, rotationAngle] for each position
     */
    public static int[][][] generatePlatformConfig(boolean[][] arr) {
        int n = arr.length;
        int m = arr[0].length;
        boolean arr2[][] = new boolean[n + 2][m + 2];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                arr2[i + 1][j + 1] = arr[i][j];
            }
        }
        int response[][][] = new int[n][m][2];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (!arr2[i][j])
                    continue;
                boolean a = arr2[i - 1][j];
                boolean b = arr2[i][j + 1];
                boolean c = arr2[i + 1][j];
                boolean d = arr2[i][j - 1];
                // corner block
                if (!a && !d) {
                    response[i - 1][j - 1][0] = 1;
                    response[i - 1][j - 1][1] = 0;
                    continue;
                }
                if (a == false && b == false) {
                    response[i - 1][j - 1][0] = 1;
                    response[i - 1][j - 1][1] = 90;
                    continue;
                }
                if (b == false && c == false) {
                    response[i - 1][j - 1][0] = 1;
                    response[i - 1][j - 1][1] = 180;
                    continue;
                }
                if (c == false && d == false) {
                    response[i - 1][j - 1][0] = 1;
                    response[i - 1][j - 1][1] = 270;
                    continue;
                }
                // edge block
                if (a == false) {
                    response[i - 1][j - 1][0] = (int) (3 * Math.random()) + 2;
                    response[i - 1][j - 1][1] = 0;
                    continue;
                }
                if (b == false) {
                    response[i - 1][j - 1][0] = (int) (3 * Math.random()) + 2;
                    response[i - 1][j - 1][1] = 90;
                    continue;
                }
                if (c == false) {
                    response[i - 1][j - 1][0] = (int) (3 * Math.random()) + 2;
                    response[i - 1][j - 1][1] = 180;
                    continue;
                }
                if (d == false) {
                    response[i - 1][j - 1][0] = (int) (3 * Math.random()) + 2;
                    response[i - 1][j - 1][1] = 270;
                    continue;
                }
                // inwards corner block
                if (arr2[i - 1][j - 1] == false) {
                    response[i - 1][j - 1][0] = 5;
                    response[i - 1][j - 1][1] = 0;
                    continue;
                }
                if (arr2[i - 1][j + 1] == false) {
                    response[i - 1][j - 1][0] = 5;
                    response[i - 1][j - 1][1] = 90;
                    continue;
                }
                if (arr2[i + 1][j + 1] == false) {
                    response[i - 1][j - 1][0] = 5;
                    response[i - 1][j - 1][1] = 180;
                    continue;
                }
                if (arr2[i + 1][j - 1] == false) {
                    response[i - 1][j - 1][0] = 5;
                    response[i - 1][j - 1][1] = 270;
                    continue;
                }
                response[i - 1][j - 1][0] = 10;
                response[i - 1][j - 1][1] = 0;
            }
        } // inner blocks
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (response[i][j][0] != 10)
                    continue;
                boolean special = false;
                for (int I = -1; I <= 1; I++) {
                    for (int k = -1; k <= 1; k++) {
                        // Add bounds checking to prevent array out of bounds
                        int newI = i + I;
                        int newJ = j + k;
                        if (newI >= 0 && newI < n && newJ >= 0 && newJ < m) {
                            if (response[newI][newJ][0] >= 1 && response[newI][newJ][0] <= 5) {
                                special = true;
                                break;
                            }
                        }
                    }
                    if (special)
                        break;
                }
                if (special) {
                    response[i][j][0] = (int) (4 * Math.random()) + 6;
                    response[i][j][1] = 90 * (int) (4 * Math.random());
                }
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (response[i][j][0] == 10) {
                    response[i][j][0] = 11; // Use sprite 9 for inner blocks
                }
            }
        }

        return response;
    }

    /**
     * Generate platforms from a 2D layout array
     * This is the primary method for creating platform layouts.
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels (recommended: 15)
     * @return List of Wall objects representing the platforms
     */
    public static ArrayList<Wall> generatePlatforms(boolean[][] layout, int tileSize) {
        ArrayList<Wall> walls = new ArrayList<>();

        if (layout == null || layout.length == 0 || layout[0].length == 0) {
            System.err.println("Invalid layout provided!");
            return walls;
        }

        int height = layout.length;
        int width = layout[0].length;

        System.out.println("Generating platforms from layout: " + width + "x" + height + " tiles");
        System.out.println("Using tile size: " + tileSize + "x" + tileSize + " pixels");

        // Generate platform configuration using the algorithm
        int[][][] platformConfig = generatePlatformConfig(layout);

        // Create Wall objects for each platform
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x] == false) {
                    continue; // Skip empty spaces
                }

                int platformId = platformConfig[y][x][0];
                int rotation = platformConfig[y][x][1];

                // Convert from 1-based algorithm IDs to 0-based sprite indices
                int spriteIndex = platformId - 1;
                if (platformId == 10) {
                    spriteIndex = 8; // Convert temp inner (10) to sprite 8
                }

                // Ensure spriteIndex is within valid range (0-9)
                spriteIndex = Math.max(0, Math.min(9, spriteIndex));

                // Format the sprite path
                String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png", spriteIndex);

                // Convert tile coordinates to world coordinates
                int worldX = x * tileSize;
                int worldY = y * tileSize;

                // Create platform wall with sprite and rotation
                Wall platformWall = new Wall(worldX, worldY, tileSize, tileSize, spritePath, rotation);
                walls.add(platformWall);
            }
        }

        System.out.println("Generated " + walls.size() + " platforms");
        return walls;
    }

    /**
     * Generate individual platform pieces from a layout with custom offset
     * coordinates
     * This combines the algorithm with custom positioning
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels
     * @param offsetX  X offset to apply to all pieces
     * @param offsetY  Y offset to apply to all pieces
     * @return List of Wall objects representing the platforms
     */
    public static ArrayList<Wall> generatePlatformsWithOffset(boolean[][] layout, int tileSize, double offsetX,
            double offsetY) {
        ArrayList<Wall> walls = new ArrayList<>();

        if (layout == null || layout.length == 0 || layout[0].length == 0) {
            System.err.println("Invalid layout provided!");
            return walls;
        }

        int height = layout.length;
        int width = layout[0].length;

        // Generate platform configuration using the algorithm
        int[][][] platformConfig = generatePlatformConfig(layout);

        // Create Wall objects for each platform with custom offset
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x] == false) {
                    continue; // Skip empty spaces
                }

                int platformId = platformConfig[y][x][0];
                int rotation = platformConfig[y][x][1];

                // Convert from 1-based algorithm IDs to 0-based sprite indices
                int spriteIndex = platformId - 1;
                if (platformId == 10) {
                    spriteIndex = 8; // Convert temp inner (10) to sprite 8
                }

                // Ensure spriteIndex is within valid range (0-9)
                spriteIndex = Math.max(0, Math.min(9, spriteIndex));

                // Format the sprite path
                String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png", spriteIndex);

                // Convert tile coordinates to world coordinates with custom offset
                double worldX = (x * tileSize) + offsetX;
                double worldY = (y * tileSize) + offsetY;

                // Create platform wall with sprite and rotation
                Wall platformWall = new Wall(worldX, worldY, tileSize, tileSize, spritePath, rotation);
                walls.add(platformWall);
            }
        }

        System.out.println("Generated " + walls.size() + " platforms with offset");
        return walls;
    }

    // Deprecated since this creates a wall for each solid tile, render times 10
    // times slower
    // public static ArrayList<Wall> generateCollisionPlatforms(boolean[][] layout,
    // int tileSize) {
    // ArrayList<Wall> walls = new ArrayList<>();

    // if (layout == null || layout.length == 0 || layout[0].length == 0) {
    // System.err.println("Invalid layout provided!");
    // return walls;
    // }

    // // Create individual collision walls for each solid tile
    // for (int y = 0; y < layout.length; y++) {
    // for (int x = 0; x < layout[y].length; x++) {
    // if (layout[y][x]) { // Solid tile
    // // Convert tile coordinates to world coordinates
    // double tileX = x * tileSize;
    // double tileY = y * tileSize;

    // // Create invisible collision wall for this tile
    // Wall tileCollision = new Wall(tileX, tileY, tileSize, tileSize,
    // new Color(0, 0, 0, 0)); // Transparent
    // walls.add(tileCollision);
    // }
    // }
    // }

    // System.out.println("Generated " + walls.size() + " individual collision walls
    // (one per solid tile)");
    // return walls;
    //

    // public static ArrayList<Wall>
    // generateCollisionPlatformsWithOffset(boolean[][] layout, int tileSize,
    // double offsetX, double offsetY) {
    // ArrayList<Wall> walls = new ArrayList<>();

    // if (layout == null || layout.length == 0 || layout[0].length == 0) {
    // System.err.println("Invalid layout provided!");
    // return walls;
    // }

    // // Create individual collision walls for each solid tile with offset
    // for (int y = 0; y < layout.length; y++) {
    // for (int x = 0; x < layout[y].length; x++) {
    // if (layout[y][x]) { // Solid tile
    // // Convert tile coordinates to world coordinates with offset
    // double tileX = (x * tileSize) + offsetX;
    // double tileY = (y * tileSize) + offsetY;

    // // Create invisible collision wall for this tile
    // Wall tileCollision = new Wall(tileX, tileY, tileSize, tileSize,
    // new Color(0, 0, 0, 0)); // Transparent
    // walls.add(tileCollision);
    // }
    // }
    // }
    // return walls;
    // }

    /**
     * Generate visual sprite data using the platform configuration algorithm
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels
     * @return List of PlatformSpriteData representing visual sprites
     */
    public static ArrayList<PlatformSpriteData> generateVisualSprites(boolean[][] layout, int tileSize) {
        ArrayList<PlatformSpriteData> sprites = new ArrayList<>();

        if (layout == null || layout.length == 0 || layout[0].length == 0) {
            System.err.println("Invalid layout provided!");
            return sprites;
        }

        int height = layout.length;
        int width = layout[0].length;

        // Generate platform configuration using the algorithm
        int[][][] platformConfig = generatePlatformConfig(layout);

        // Create sprite data for each platform
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!layout[y][x]) {
                    continue; // Skip empty spaces
                }

                int platformId = platformConfig[y][x][0];
                int rotation = platformConfig[y][x][1];

                // Convert from 1-based algorithm IDs to 0-based sprite indices
                int spriteIndex = platformId - 1;
                if (platformId == 10) {
                    spriteIndex = 8; // Convert temp inner (10) to sprite 8
                }

                // Ensure spriteIndex is within valid range (0-9)
                spriteIndex = Math.max(0, Math.min(9, spriteIndex));

                // Format the sprite path
                String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png", spriteIndex);

                // Convert tile coordinates to world coordinates
                int worldX = x * tileSize;
                int worldY = y * tileSize;

                // Create sprite data
                sprites.add(new PlatformSpriteData(worldX, worldY, tileSize, tileSize,
                        spritePath, rotation));
            }
        }

        System.out.println("Generated " + sprites.size() + " visual sprites");
        return sprites;
    }

    /**
     * Generate visual sprite data with offset
     * 
     * @param layout   2D array where 0 = empty space, 1 = platform/wall
     * @param tileSize Size of each platform tile in pixels
     * @param offsetX  X offset to apply to all sprites
     * @param offsetY  Y offset to apply to all sprites
     * @return List of PlatformSpriteData representing visual sprites
     */
    public static ArrayList<PlatformSpriteData> generateVisualSpritesWithOffset(boolean[][] layout, int tileSize,
            double offsetX, double offsetY) {
        ArrayList<PlatformSpriteData> sprites = new ArrayList<>();

        if (layout == null || layout.length == 0 || layout[0].length == 0) {
            System.err.println("Invalid layout provided!");
            return sprites;
        }

        int height = layout.length;
        int width = layout[0].length;

        // Generate platform configuration using the algorithm
        int[][][] platformConfig = generatePlatformConfig(layout); // Create sprite data for each platform
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!layout[y][x]) {
                    continue; // Skip empty spaces
                }

                int platformId = platformConfig[y][x][0];
                int rotation = platformConfig[y][x][1];

                // Convert from 1-based algorithm IDs to 0-based sprite indices
                int spriteIndex = platformId - 1;
                if (platformId == 10) {
                    spriteIndex = 8; // Convert temp inner (10) to sprite 8
                }

                // Ensure spriteIndex is within valid range (0-9)
                spriteIndex = Math.max(0, Math.min(9, spriteIndex));

                // Format the sprite path
                String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png", spriteIndex);

                // Convert tile coordinates to world coordinates with offset
                double worldX = (x * tileSize) + offsetX;
                double worldY = (y * tileSize) + offsetY;

                // Create sprite data
                sprites.add(new PlatformSpriteData(worldX, worldY, tileSize, tileSize,
                        spritePath, rotation));
            }
        }

        System.out.println("Generated " + sprites.size() + " visual sprites with offset");
        return sprites;
    }

    /**
     * Helper class to store platform sprite information for rendering
     */
    public static class PlatformSpriteData {
        public double x, y;
        public double width, height;
        public String spritePath;
        public int rotation;

        public PlatformSpriteData(double x, double y, double width, double height,
                String spritePath, int rotation) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spritePath = spritePath;
            this.rotation = rotation;
        }
    }

    /**
     * Helper class to store platform piece information with coordinates
     */
    public static class PlatformPiece {
        public double x, y;
        public double width, height;
        public int spriteIndex;
        public int rotation;

        public PlatformPiece(double x, double y, double width, double height, int spriteIndex, int rotation) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spriteIndex = spriteIndex;
            this.rotation = rotation;
        }

        public PlatformPiece(double x, double y, int tileSize, int spriteIndex, int rotation) {
            this(x, y, tileSize, tileSize, spriteIndex, rotation);
        }
    }

    /**
     * Helper class for more detailed platform piece data
     */
    public static class PlatformPieceData {
        public double x, y;
        public double width, height;
        public int spriteIndex;
        public int rotation;
        public String customSpritePath; // Optional custom sprite path

        public PlatformPieceData(double x, double y, double width, double height, int spriteIndex, int rotation) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spriteIndex = spriteIndex;
            this.rotation = rotation;
            this.customSpritePath = null;
        }

        public PlatformPieceData(double x, double y, double width, double height, String customSpritePath,
                int rotation) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spriteIndex = 0; // Default, will be ignored if customSpritePath is used
            this.rotation = rotation;
            this.customSpritePath = customSpritePath;
        }
    }

    /**
     * Generate optimized collision platforms by grouping adjacent tiles into larger
     * rectangles
     * 
     * @param layout   2D array where false = empty space, true = platform/wall
     * @param tileSize Size of each platform tile in pixels
     * @return List of Wall objects representing optimized collision platforms
     */
    public static ArrayList<Wall> generateCollisionPlatforms(boolean[][] layout, int tileSize) {
        return generateCollisionPlatformsWithOffset(layout, tileSize, 0, 0);
    }

    /**
     * Generate optimized collision platforms with offset by grouping adjacent tiles
     * into larger rectangles. Uses fast scanning algorithm for better performance.
     * 
     * @param layout   2D array where false is space true is wall
     * @param tileSize Size of each platform tile in pixels
     * @param offsetX  X offset to apply to all platforms
     * @param offsetY  Y offset to apply to all platforms
     * @return List of Wall objects representing optimized collision platforms
     */
    public static ArrayList<Wall> generateCollisionPlatformsWithOffset(boolean[][] layout, int tileSize,
            double offsetX, double offsetY) {
        ArrayList<Wall> walls = new ArrayList<>();

        if (layout == null || layout.length == 0 || layout[0].length == 0) {
            System.err.println("Invalid layout provided!");
            return walls;
        }

        int height = layout.length;
        int width = layout[0].length;

        // Use faster algorithm for larger layouts
        if (width * height > 1000) {
            return generateCollisionPlatformsOptimized(layout, tileSize, offsetX, offsetY);
        }

        // Create a copy of the layout to mark processed tiles
        boolean[][] processed = new boolean[height][width];

        // Process each unprocessed solid tile
        for (int startY = 0; startY < height; startY++) {
            for (int startX = 0; startX < width; startX++) {
                if (!layout[startY][startX] || processed[startY][startX]) {
                    continue; // Skip empty spaces or already processed tiles
                }

                // Find the largest rectangle starting from this position
                Rectangle rect = findLargestRectangle(layout, processed, startX, startY, width, height);

                if (rect != null) {
                    // Convert tile coordinates to world coordinates with offset
                    double worldX = (rect.x * tileSize) + offsetX;
                    double worldY = (rect.y * tileSize) + offsetY;
                    double worldWidth = rect.width * tileSize;
                    double worldHeight = rect.height * tileSize;

                    // Create optimized collision wall
                    Wall optimizedWall = new Wall(worldX, worldY, worldWidth, worldHeight,
                            new Color(0, 0, 0, 0)); // Transparent
                    walls.add(optimizedWall);

                    // Mark all tiles in this rectangle as processed
                    markRectangle(processed, rect);
                }
            }
        }

        System.out.println("Generated " + walls.size() + " optimized collision platforms (grouped from "
                + countSolidTiles(layout) + " individual tiles)");
        return walls;
    }

    /**
     * Highly optimized collision platform generation for large layouts
     * Uses scan-line algorithm with connected component analysis
     */
    private static ArrayList<Wall> generateCollisionPlatformsOptimized(boolean[][] layout, int tileSize,
            double offsetX, double offsetY) {
        ArrayList<Wall> walls = new ArrayList<>();
        int height = layout.length;
        int width = layout[0].length;

        boolean[][] processed = new boolean[height][width];

        // Use scan-line algorithm to find rectangular regions efficiently
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!layout[y][x] || processed[y][x]) {
                    continue;
                }

                // Find horizontal extent
                int endX = x;
                while (endX < width && layout[y][endX] && !processed[y][endX]) {
                    endX++;
                }

                // Find vertical extent for this horizontal strip
                int endY = y + 1;
                boolean canExtendVertically = true;

                while (endY < height && canExtendVertically) {
                    // Check if entire horizontal strip can be extended down
                    for (int checkX = x; checkX < endX; checkX++) {
                        if (!layout[endY][checkX] || processed[endY][checkX]) {
                            canExtendVertically = false;
                            break;
                        }
                    }
                    if (canExtendVertically) {
                        endY++;
                    }
                }

                // Create rectangle for this region
                int rectWidth = endX - x;
                int rectHeight = endY - y;

                double worldX = (x * tileSize) + offsetX;
                double worldY = (y * tileSize) + offsetY;
                double worldWidth = rectWidth * tileSize;
                double worldHeight = rectHeight * tileSize;

                Wall optimizedWall = new Wall(worldX, worldY, worldWidth, worldHeight,
                        new Color(0, 0, 0, 0)); // Transparent
                walls.add(optimizedWall);

                // Mark this rectangle as processed
                for (int markY = y; markY < endY; markY++) {
                    for (int markX = x; markX < endX; markX++) {
                        processed[markY][markX] = true;
                    }
                }
            }
        }

        System.out.println("Generated " + walls.size()
                + " highly optimized collision platforms (scan-line algorithm, grouped from "
                + countSolidTiles(layout) + " individual tiles)");
        return walls;
    }

    /**
     * Find the largest rectangle of solid tiles
     * https://www.geeksforgeeks.org/largest-rectangular-area-in-a-histogram-using-stack/ reference
     * 
     * @param layout    2D layout array
     * @param processed 2D array tracking which tiles are already processed
     * @param startX    Starting X coordinate
     * @param startY    Starting Y coordinate
     * @param width     Total width of the layout
     * @param height    Total height of the layout
     * @return Rectangle representing the largest solid rectangle found, or null if
     *         none
     */
    private static Rectangle findLargestRectangle(boolean[][] layout, boolean[][] processed,
            int startX, int startY, int width, int height) { // O(r*c) tc
        // Build height histogram for each row
        int[] heights = new int[width - startX];
        Rectangle bestRect = new Rectangle(startX, startY, 1, 1);
        int bestArea = 1;

        for (int y = startY; y < height; y++) {
            // Update histogram heights for current row
            for (int x = startX; x < width; x++) {
                int idx = x - startX;
                if (layout[y][x] && !processed[y][x]) {
                    heights[idx]++;
                } else {
                    heights[idx] = 0;
                }
            }

            // Find largest rectangle in current histogram
            Rectangle rect = largestRectangleInHistogram(heights, startX, y);
            int area = rect.width * rect.height;

            if (area > bestArea) {
                bestArea = area;
                bestRect = rect;
            }
        }

        return bestRect;
    }

    /**
     * Largest rectangle in histogram using stack-based algorithm
     * O(n) time complexity
     */
    private static Rectangle largestRectangleInHistogram(int[] heights, int baseX, int currentY) {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        int maxArea = 0;
        Rectangle bestRect = new Rectangle(baseX, currentY, 1, 1);

        for (int i = 0; i <= heights.length; i++) {
            int h = (i == heights.length) ? 0 : heights[i];

            // Maintain a non-decreasing stack of heights
            while (!stack.isEmpty() && h < heights[stack.peek()]) {
                int height = heights[stack.pop()]; // Height of the rectangle
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                int area = height * width;

                if (area > maxArea) {
                    maxArea = area;
                    int startX = stack.isEmpty() ? 0 : stack.peek() + 1;
                    bestRect = new Rectangle(baseX + startX, currentY - height + 1, width, height);
                }
            }
            stack.push(i);
        }

        return bestRect;
    }

    /**
     * Mark all tiles in a rectangle as processed
     * 
     * @param processed 2D array tracking processed tiles
     * @param rect      Rectangle to mark as processed
     */
    private static void markRectangle(boolean[][] processed, Rectangle rect) {
        for (int y = rect.y; y < rect.y + rect.height; y++) {
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                processed[y][x] = true;
            }
        }
    }

    /**
     * Count the total number of solid tiles in a layout
     * 
     * @param layout 2D layout array
     * @return Number of solid (true) tiles
     */
    private static int countSolidTiles(boolean[][] layout) {
        int count = 0;
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                if (layout[y][x]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Internal use for rectangle
     */
    private static class Rectangle {
        public int x, y, width, height;

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
