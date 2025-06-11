import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Laser extends Entity {
    private long creationTime = 0;
    private boolean state = true; // Animation system for laser effects
    private BufferedImage[] laserAnimationSprites; // sprites 0-4 for laser beam animation
    private BufferedImage[] laserBaseSprites; // sprites 5-9 for laser base
    private int currentAnimationFrame = 0;
    private int animationTimer = 0;
    private static final int LASER_ANIMATION_SPEED = 10; // Same as character idle animation

    // Orientation system
    private boolean isHorizontal = true; // true = horizontal, false = vertical
    private boolean isReversed = false; // true = flipped orientation

    // Performance optimization: Cache transformed sprites
    private BufferedImage[] cachedLaserSprites; // Cached transformed laser sprites
    private BufferedImage[] cachedBaseSprites; // Cached transformed base sprites
    private boolean spritesNeedUpdate = true; // Flag to update cached sprites

    // Sprite sizing constants
    private static final int MAX_SPRITE_SIZE = 32; // Max sprite size to fit in hitbox
    private static final int BASE_SPRITE_SIZE = 24; // Smaller base sprite size

    public Laser(double x, double y, double width, double height) {
        super(x, y, width, height, "");
        this.creationTime = System.nanoTime();

        // Load laser sprites
        loadLaserSprites();
    }

    /**
     * Set the laser orientation
     * 
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public void setOrientation(boolean horizontal, boolean reversed) {
        this.isHorizontal = horizontal;
        this.isReversed = reversed;
        this.spritesNeedUpdate = true; // Mark sprites for recaching
        System.out.println("Laser orientation set - horizontal: " + horizontal + ", reversed: " + reversed);
    }

    /**
     * Load all laser sprite frames
     */
    private void loadLaserSprites() {
        // Load laser animation sprites (sprite_00.png to sprite_04.png)
        laserAnimationSprites = new BufferedImage[5];
        for (int i = 0; i < 5; i++) {
            try {
                laserAnimationSprites[i] = ImageIO
                        .read(getClass().getResourceAsStream(String.format("/Sprites/Labseor/sprite_%02d.png", i)));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load laser animation sprite " + i + ": " + e.getMessage());
            }
        }

        // Load laser base sprites (sprite_05.png to sprite_09.png)
        laserBaseSprites = new BufferedImage[5];
        for (int i = 0; i < 5; i++) {
            try {
                laserBaseSprites[i] = ImageIO
                        .read(getClass().getResourceAsStream(String.format("/Sprites/Labseor/sprite_%02d.png", i + 5)));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load laser base sprite " + (i + 5) + ": " + e.getMessage());
            }
        }
    }

    /**
     * Cache transformed sprites for performance optimization
     */
    private void cacheTransformedSprites() {
        if (!spritesNeedUpdate)
            return;

        cachedLaserSprites = new BufferedImage[laserAnimationSprites.length];
        cachedBaseSprites = new BufferedImage[laserBaseSprites.length];

        // Transform and cache laser sprites
        for (int i = 0; i < laserAnimationSprites.length; i++) {
            if (laserAnimationSprites[i] != null) {
                BufferedImage sprite = laserAnimationSprites[i];

                // Resize sprite to fit within hitbox
                int targetWidth = Math.min(sprite.getWidth(), MAX_SPRITE_SIZE);
                int targetHeight = Math.min(sprite.getHeight(), MAX_SPRITE_SIZE);

                if (sprite.getWidth() != targetWidth || sprite.getHeight() != targetHeight) {
                    BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = resized.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(sprite, 0, 0, targetWidth, targetHeight, null);
                    g2d.dispose();
                    sprite = resized;
                }

                // Apply orientation transformations
                BufferedImage transformedSprite = sprite;

                if (!isHorizontal) {
                    // For vertical laser, rotate sprites 90 degrees
                    transformedSprite = rotateImage90(transformedSprite);
                }

                if (isReversed) {
                    if (isHorizontal) {
                        transformedSprite = flipImageHorizontally(transformedSprite);
                    } else {
                        transformedSprite = flipImageVertically(transformedSprite);
                    }
                }

                cachedLaserSprites[i] = transformedSprite;
            }
        }

        // Transform and cache base sprites
        for (int i = 0; i < laserBaseSprites.length; i++) {
            if (laserBaseSprites[i] != null) {
                BufferedImage sprite = laserBaseSprites[i];

                // Resize base sprite to smaller size
                int targetSize = BASE_SPRITE_SIZE;
                if (sprite.getWidth() != targetSize || sprite.getHeight() != targetSize) {
                    BufferedImage resized = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = resized.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(sprite, 0, 0, targetSize, targetSize, null);
                    g2d.dispose();
                    sprite = resized;
                }

                // Apply orientation transformations
                BufferedImage transformedSprite = sprite;

                if (!isHorizontal) {
                    // For vertical laser, rotate base sprite 90 degrees
                    transformedSprite = rotateImage90(transformedSprite);
                }

                if (isReversed) {
                    if (isHorizontal) {
                        transformedSprite = flipImageHorizontally(transformedSprite);
                    } else {
                        transformedSprite = flipImageVertically(transformedSprite);
                    }
                }

                cachedBaseSprites[i] = transformedSprite;
            }
        }

        spritesNeedUpdate = false;
    }

    /**
     * Update laser animation
     */
    private void updateLaserAnimation() {
        // Update animation timer
        animationTimer++;

        // Change frame when timer reaches animation speed
        if (animationTimer >= LASER_ANIMATION_SPEED) {
            animationTimer = 0;
            // Randomly cycle through animation frames for dynamic effect
            currentAnimationFrame = (int) (Math.random() * laserAnimationSprites.length);
        }
    }

    public void update() {
        // Update laser animation
        updateLaserAnimation();

        // Time-based behaviors (like automatic deactivation after some time)
        handleLifetime();
    }

    private void handleLifetime() {
        long now = System.nanoTime();
        double lifetime = (now - creationTime) / 1_000_000_000.0; // Convert to seconds

        if (lifetime > 2) {
            // System.out.println(lifetime + " " + state);
            state = !state;
            creationTime = now;
            // Don't call setActive(state) - keep laser in game but hide it
        }
    }

    /**
     * Reset/swap the player orientation (called on player death)
     */
    public void resetOrientation() {
        // Reset player's gravity orientation (swap variable) instead of laser
        // orientation
        Player player = GameEngine.getPlayer();
        if (player != null) {
            // Reset player swap to normal gravity (1)
            player.setSwap(1);
            System.out.println("Player gravity orientation reset to normal");
        }
    }

    /**
     * Check if the laser is dangerous (both active and visible)
     * Used to determine if it should cause player respawn
     */
    public boolean isDangerous() {
        return isActive() && state;
    }

    /**
     * Helper method to flip a BufferedImage horizontally
     */
    private BufferedImage flipImageHorizontally(BufferedImage original) {
        if (original == null)
            return null;

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, original.getType());
        Graphics2D g2d = flipped.createGraphics();

        // Flip horizontally by scaling with negative width and translating
        g2d.scale(-1, 1);
        g2d.translate(-width, 0);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return flipped;
    }

    /**
     * Helper method to flip a BufferedImage vertically
     */
    private BufferedImage flipImageVertically(BufferedImage original) {
        if (original == null)
            return null;

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, original.getType());
        Graphics2D g2d = flipped.createGraphics();

        // Flip vertically by scaling with negative height and translating
        g2d.scale(1, -1);
        g2d.translate(0, -height);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return flipped;
    }

    /**
     * Helper method to rotate a BufferedImage 90 degrees
     */
    private BufferedImage rotateImage90(BufferedImage original) {
        if (original == null)
            return null;

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage rotated = new BufferedImage(height, width, original.getType());
        Graphics2D g2d = rotated.createGraphics();

        // Rotate 90 degrees clockwise
        g2d.rotate(Math.toRadians(90), height / 2.0, height / 2.0);
        g2d.drawImage(original, (height - width) / 2, (width - height) / 2, null);
        g2d.dispose();

        return rotated;
    }

    @Override
    public void draw(Graphics g) {
        if (!isActive() || !state)
            return;

        Graphics2D g2d = (Graphics2D) g;

        // Cache transformed sprites if needed
        cacheTransformedSprites();

        // Get current cached laser sprite
        if (cachedLaserSprites != null && currentAnimationFrame < cachedLaserSprites.length) {
            BufferedImage currentSprite = cachedLaserSprites[currentAnimationFrame];

            if (currentSprite != null) {
                // Calculate sprite dimensions
                int spriteWidth = currentSprite.getWidth();
                int spriteHeight = currentSprite.getHeight();

                // Calculate how many sprites we need to fill the laser (optimized)
                int tilesX = (int) Math.ceil(width / (double) spriteWidth);
                int tilesY = (int) Math.ceil(height / (double) spriteHeight);

                // Limit number of tiles for performance
                tilesX = Math.min(tilesX, 50); // Max 50 tiles per dimension
                tilesY = Math.min(tilesY, 50);

                // Draw multiple sprites to fill the laser area
                int startX = (int) (x - width / 2);
                int startY = (int) (y - height / 2);

                for (int tileX = 0; tileX < tilesX; tileX++) {
                    for (int tileY = 0; tileY < tilesY; tileY++) {
                        int drawX = startX + (tileX * spriteWidth);
                        int drawY = startY + (tileY * spriteHeight);

                        // Only draw if within laser bounds
                        if (drawX < startX + width && drawY < startY + height) {
                            g2d.drawImage(currentSprite, drawX, drawY, null);
                        }
                    }
                }
            }
        }

        // Draw laser base
        if (cachedBaseSprites != null && cachedBaseSprites.length > 0) {
            // Use a fixed base sprite instead of random for performance
            BufferedImage baseSprite = cachedBaseSprites[0];

            if (baseSprite != null) {
                int baseSize = BASE_SPRITE_SIZE;
                int baseX, baseY;
                if (isHorizontal) {
                    // Position base 10 pixels from the start of the laser
                    if (isReversed) {
                        // Head at right start, move 10 pixels left from the right edge
                        baseX = (int) (x + width / 2 - 10 - baseSize / 2);
                    } else {
                        // Head at left start, move 10 pixels right from the left edge
                        baseX = (int) (x - width / 2 + 10 - baseSize / 2);
                    }
                    baseY = (int) (y - baseSize / 2);
                } else {
                    // Position base 10 pixels from the start of the laser for vertical
                    baseX = (int) (x - baseSize / 2);
                    if (isReversed) {
                        // Head at bottom start, move 10 pixels up from the bottom edge
                        baseY = (int) (y + height / 2 - 10 - baseSize / 2);
                    } else {
                        // Head at top start, move 10 pixels down from the top edge
                        baseY = (int) (y - height / 2 + 10 - baseSize / 2);
                    }
                }

                g2d.drawImage(baseSprite, baseX, baseY, baseSize, baseSize, null);
            }
        }
    }
}
