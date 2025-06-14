import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

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
    private boolean isReversed = false; // true = flipped orientation // Pulse control system
    private boolean isPermanent = false; // true = always on, false = pulses on/off

    // Dual heads system
    private boolean hasDualHeads = false; // true = dual heads, false = single head// Performance optimization: Cache
                                          // transformed sprites
    private BufferedImage[] cachedLaserSprites; // Cached transformed laser sprites
    private BufferedImage[] cachedBaseSprites; // Cached transformed base sprites
    private boolean spritesNeedUpdate = true; // Flag to update cached sprites // Sprite sizing constants
    private static final int MAX_SPRITE_SIZE = 32; // Max sprite size to fit in hitbox
    private static final int BASE_SPRITE_SIZE = 42; // Bigger base sprite size for the head

    public Laser(double headX, double headY, double width, double height) {
        // Position by top-left corner (head positioning)
        super(headX, headY, width, height, "");
        this.creationTime = System.nanoTime();
        this.hasDualHeads = false;

        // Load laser sprites
        loadLaserSprites();
    }

    /**
     * Constructor for creating a dual-head laser
     * 
     * @param headX    X position (top-left corner of hitbox)
     * @param headY    Y position (top-left corner of hitbox)
     * @param width    Width of the laser beam
     * @param height   Height of the laser beam
     * @param dualHead true for dual heads at both ends, false for single head
     */
    public Laser(double headX, double headY, double width, double height, boolean dualHead) {
        // Position by top-left corner (head positioning)
        super(headX, headY, width, height, "");
        this.creationTime = System.nanoTime();
        this.hasDualHeads = dualHead;

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

        System.out.println("Laser orientation set - horizontal: " + horizontal + ", reversed: " + reversed);
    }

    /**
     * Set whether the laser is permanent (always on) or pulses on/off
     * 
     * @param permanent true for permanent laser, false for pulsing laser
     */
    public void setPermanent(boolean permanent) {
        this.isPermanent = permanent;
        if (permanent) {
            this.state = true; // Ensure laser is visible when set to permanent
        }
        System.out.println("Laser set to " + (permanent ? "permanent" : "pulsing") + " mode");
    }

    /**
     * Check if the laser is permanent (always on) or pulsing
     * 
     * @return true if permanent, false if pulsing
     */
    public boolean isPermanent() {
        return isPermanent;
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
        // Skip pulsing if laser is set to permanent mode
        if (isPermanent) {
            state = true; // Always keep permanent lasers visible
            return;
        }

        long now = System.nanoTime();
        double lifetime = (now - creationTime) / 1_000_000_000.0; // Convert to seconds

        if (lifetime > 1.25) {
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
            // System.out.println("gravity reset");
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
     * Get the X position of the laser head (emitter)
     */
    public double getHeadX() {
        if (isHorizontal) {
            if (isReversed) {
                // Head is at the right side
                return x + width;
            } else {
                // Head is at the left side (top-left corner)
                return x;
            }
        } else {
            // For vertical lasers, head X is at the left edge of hitbox
            return x;
        }
    }

    /**
     * Get the Y position of the laser head (emitter)
     */
    public double getHeadY() {
        if (isHorizontal) {
            // For horizontal lasers, head Y is at the top edge of hitbox
            return y;
        } else {
            if (isReversed) {
                // Head is at the bottom
                return y + height;
            } else {
                // Head is at the top (top-left corner)
                return y;
            }
        }
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
        if (!isActive())
            return;

        Graphics2D g2d = (Graphics2D) g; // Cache transformed sprites if needed
        cacheTransformedSprites();

        // Calculate hitbox bounds (positioned by top-left corner)
        int hitboxLeft = (int) x;
        int hitboxRight = (int) (x + width);
        int hitboxTop = (int) y;
        int hitboxBottom = (int) (y + height);

        // Calculate head size - can extend beyond hitbox perpendicular to laser
        // direction
        int headSize = BASE_SPRITE_SIZE; // Use the constant for consistent sizing

        // Calculate head position - aligned with hitbox edge in laser direction
        double headCenterX, headCenterY;
        if (isHorizontal) {
            // Head can extend beyond hitbox vertically, but aligned with hitbox edge
            // horizontally
            headCenterY = y + height / 2; // Center vertically in hitbox
            if (isReversed) {
                // Head at right edge of hitbox
                headCenterX = hitboxRight - headSize / 2;
            } else {
                // Head at left edge of hitbox
                headCenterX = hitboxLeft + headSize / 2;
            }
        } else {
            // Head can extend beyond hitbox horizontally, but aligned with hitbox edge
            // vertically
            headCenterX = x + width / 2; // Center horizontally in hitbox
            if (isReversed) {
                // Head at bottom edge of hitbox
                headCenterY = hitboxBottom - headSize / 2;
            } else {
                // Head at top edge of hitbox
                headCenterY = hitboxTop + headSize / 2;
            }
        }

        // Draw laser beam first (behind the head) - fills entire hitbox when state is
        // true
        if (state && cachedLaserSprites != null && currentAnimationFrame < cachedLaserSprites.length) {
            BufferedImage currentSprite = cachedLaserSprites[currentAnimationFrame];

            if (currentSprite != null) {
                // Beam fills the entire hitbox
                int beamWidth = (int) width;
                int beamHeight = (int) height;

                // Calculate sprite tiling for the entire hitbox
                int spriteWidth = currentSprite.getWidth();
                int spriteHeight = currentSprite.getHeight();

                int tilesX = (int) Math.ceil(beamWidth / (double) spriteWidth);
                int tilesY = (int) Math.ceil(beamHeight / (double) spriteHeight);

                // Draw beam sprites to fill entire hitbox
                for (int tileX = 0; tileX < tilesX; tileX++) {
                    for (int tileY = 0; tileY < tilesY; tileY++) {
                        int drawX = hitboxLeft + (tileX * spriteWidth);
                        int drawY = hitboxTop + (tileY * spriteHeight);

                        // Only draw if within hitbox bounds
                        if (drawX < hitboxRight && drawY < hitboxBottom) {
                            // Clip the sprite if it extends beyond hitbox bounds
                            int clipWidth = Math.min(spriteWidth, hitboxRight - drawX);
                            int clipHeight = Math.min(spriteHeight, hitboxBottom - drawY);

                            g2d.drawImage(currentSprite, drawX, drawY, drawX + clipWidth, drawY + clipHeight,
                                    0, 0, clipWidth, clipHeight, null);
                        }
                    }
                }
            }
        } // Draw laser head(s) on top (always visible, regardless of pulse state)
          // Head(s) can extend beyond hitbox perpendicular to laser direction
          // Use animated head sprite
        if (cachedBaseSprites != null && cachedBaseSprites.length > 0) {
            // Use the same animation frame as the laser beam for synchronized animation
            int headAnimationFrame = currentAnimationFrame % cachedBaseSprites.length;
            BufferedImage baseSprite = cachedBaseSprites[headAnimationFrame];

            if (baseSprite != null) {
                if (hasDualHeads) {
                    // Draw heads at both ends
                    double head1CenterX, head1CenterY, head2CenterX, head2CenterY;
                    if (isHorizontal) {
                        // Horizontal laser: heads at left and right edges
                        head1CenterY = head2CenterY = y + height / 2; // Center vertically in hitbox

                        // Head 1 at left edge - align sprite left edge with laser left edge
                        head1CenterX = hitboxLeft + headSize / 2;
                        // Head 2 at right edge - align sprite right edge with laser right edge
                        head2CenterX = hitboxRight - headSize / 2; // Draw both heads with proper alignment
                        int head1DrawX = (int) (head1CenterX - headSize / 2);
                        int head1DrawY = (int) (head1CenterY - headSize / 2);
                        // For right head, position so its right edge aligns with laser right edge
                        int head2DrawX = (int) (hitboxRight - headSize);
                        int head2DrawY = (int) (head2CenterY - headSize / 2);

                        // Draw left head (normal orientation)
                        g2d.drawImage(baseSprite, head1DrawX, head1DrawY, headSize, headSize, null);

                        // Draw right head (flipped horizontally to face opposite direction)
                        BufferedImage flippedSprite = flipImageHorizontally(baseSprite);
                        g2d.drawImage(flippedSprite, head2DrawX, head2DrawY, headSize, headSize, null);
                    } else {
                        // heads at top and bottom edges
                        head1CenterX = head2CenterX = x + width / 2; // Center horizontally in hitbox

                        // Head 1 at top edge (normal orientation)
                        head1CenterY = hitboxTop + headSize / 2;
                        // Head 2 at bottom edge (flipped 180 degrees)
                        head2CenterY = hitboxBottom - headSize / 2;

                        // Draw top head (normal)
                        int head1DrawX = (int) (head1CenterX - headSize / 2);
                        int head1DrawY = (int) (head1CenterY - headSize / 2);
                        g2d.drawImage(baseSprite, head1DrawX, head1DrawY, headSize, headSize, null);

                        // Draw bottom head (flipped 180 degrees)
                        int head2DrawX = (int) (head2CenterX - headSize / 2);
                        int head2DrawY = (int) (head2CenterY - headSize / 2);

                        // Create a flipped version of the sprite for the bottom head
                        BufferedImage flippedSprite = new BufferedImage(headSize, headSize,
                                BufferedImage.TYPE_INT_ARGB);
                        Graphics2D flipG2d = flippedSprite.createGraphics();

                        // Flip 180 degrees by rotating around the center
                        flipG2d.rotate(Math.toRadians(180), headSize / 2.0, headSize / 2.0);
                        flipG2d.drawImage(baseSprite, 0, 0, headSize, headSize, null);
                        flipG2d.dispose();

                        g2d.drawImage(flippedSprite, head2DrawX, head2DrawY, headSize, headSize, null);
                    }
                } else {
                    // Single head (existing behavior)
                    int headDrawX = (int) (headCenterX - headSize / 2);
                    int headDrawY = (int) (headCenterY - headSize / 2);

                    g2d.drawImage(baseSprite, headDrawX, headDrawY, headSize, headSize, null);
                }
            }
        }
    }

    /**
     * Override box() method to use top-left positioning instead of center-based
     * This ensures collision detection aligns with the visual laser positioning
     */
    @Override
    public double[] box() {
        // Use top-left positioning to match the visual laser
        double laserLeft = x;
        double laserRight = x + width;
        double laserTop = y;
        double laserBottom = y + height;

        return new double[] { laserLeft, laserRight, laserTop, laserBottom };
    }

    /**
     * Override isColliding method to handle top-left positioned laser
     * properly with center-based positioned entities
     */
    @Override
    public boolean isColliding(GameObject other) {
        // Laser uses top-left positioning
        double laserLeft = x;
        double laserRight = x + width;
        double laserTop = y;
        double laserBottom = y + height;

        // Other entity uses center-based positioning
        double otherLeft = other.getX() - other.getWidth() / 2;
        double otherRight = other.getX() + other.getWidth() / 2;
        double otherTop = other.getY() - other.getHeight() / 2;
        double otherBottom = other.getY() + other.getHeight() / 2;

        return laserRight > otherLeft &&
                laserLeft < otherRight &&
                laserBottom > otherTop &&
                laserTop < otherBottom;
    }

    /**
     * Override isCollidingWithWall method to handle top-left positioned laser
     * properly with top-left positioned walls
     */
    @Override
    protected boolean isCollidingWithWall(Wall wall) {
        // Small tolerance to account for floating-point precision
        final double TOLERANCE = 0.01;

        // Laser uses top-left positioning
        double laserLeft = x;
        double laserRight = x + width;
        double laserTop = y;
        double laserBottom = y + height;

        // Wall also uses top-left positioning
        double wallLeft = wall.getX();
        double wallRight = wall.getX() + wall.getWidth();
        double wallTop = wall.getY();
        double wallBottom = wall.getY() + wall.getHeight();

        // Use tolerance to prevent edge-clipping issues
        return (laserRight - TOLERANCE) > wallLeft &&
                (laserLeft + TOLERANCE) < wallRight &&
                (laserBottom - TOLERANCE) > wallTop &&
                (laserTop + TOLERANCE) < wallBottom;
    }
}
