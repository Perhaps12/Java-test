import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * PlatformGenerator handles generation of platform tiles with appropriate
 * sprites and rotations
 * based on their position and neighboring tiles.
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
        platformSprites = new BufferedImage[10]; // 10 platform sprites (0-9)

        try {
            for (int i = 0; i < 10; i++) {
                // Use zero-based indexing to match sprite file names (sprite_00.png to
                // sprite_09.png)
                String path = String.format("/Sprites/Platforms (1)/sprite_0%d.png", i);
                platformSprites[i] = ImageIO.read(PlatformGenerator.class.getResourceAsStream(path));
                System.out.println("Loaded platform sprite: " + path);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading platform sprites: " + e.getMessage());
        }
    }

    /**
     * Generate platform configuration from a binary array representing solid vs
     * empty areas.
     * This matches the exact algorithm provided by the user.
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
                if (!a&&!d) {
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
                            if (response[newI][newJ][0] >=1 && response[newI][newJ][0] <= 5) {
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
     * Generate platforms with custom x,y coordinates for individual pieces
     * This allows precise positioning of each platform piece
     * 
     * @param platformPieces List of PlatformPiece objects with custom coordinates
     *                       and properties
     * @return List of Wall objects representing the platforms
     */
    public static ArrayList<Wall> generatePlatformsWithCoordinates(ArrayList<PlatformPiece> platformPieces) {
        ArrayList<Wall> walls = new ArrayList<>();

        if (platformPieces == null || platformPieces.isEmpty()) {
            return walls;
        }

        for (PlatformPiece piece : platformPieces) {
            // Ensure spriteIndex is within valid range (0-9)
            int spriteIndex = Math.max(0, Math.min(9, piece.spriteIndex));

            // Format the sprite path
            String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png", spriteIndex);

            // Create platform wall with sprite and rotation at specified coordinates
            Wall platformWall = new Wall(piece.x, piece.y, piece.width, piece.height, spritePath, piece.rotation);
            walls.add(platformWall);
        }

        System.out.println("Generated " + walls.size() + " platforms with custom coordinates");
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

    /**
     * Create individual platform pieces with full control over positioning and
     * properties
     * 
     * @param pieces Array of PlatformPieceData with coordinates, sprite, and
     *               rotation info
     * @return List of Wall objects representing the platforms
     */
    public static ArrayList<Wall> createIndividualPlatformPieces(PlatformPieceData[] pieces) {
        ArrayList<Wall> walls = new ArrayList<>();

        if (pieces == null || pieces.length == 0) {
            System.err.println("No platform piece data provided!");
            return walls;
        }

        System.out.println("Creating " + pieces.length + " individual platform pieces");

        for (PlatformPieceData pieceData : pieces) {
            // Ensure spriteIndex is within valid range (0-9)
            int spriteIndex = Math.max(0, Math.min(9, pieceData.spriteIndex));

            // Format the sprite path
            String spritePath = String.format("/Sprites/Platforms (1)/sprite_0%d.png", spriteIndex);

            // Create platform wall with all custom properties
            Wall platformWall = new Wall(
                    pieceData.x,
                    pieceData.y,
                    pieceData.width,
                    pieceData.height,
                    spritePath,
                    pieceData.rotation);
            walls.add(platformWall);
        }

        System.out.println("Created " + walls.size() + " individual platform pieces");
        return walls;
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
}
