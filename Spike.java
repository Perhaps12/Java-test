import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Spike extends Entity {
    private BufferedImage[] crystalSprites; // sprites 0-2 for crystal sprites
    private boolean isHorizontal = false; // false = vertical (default), true = horizontal
    private boolean isReversed = false; // true = flipped orientation

    private int spriteIndex = 0; // Which crystal sprite to use (0-2)    // Performance optimization: Cache transformed sprites
    private BufferedImage cachedCrystalSprite; // Single cached transformed crystal sprite
    private boolean spritesNeedUpdate = true; // Flag to update cached sprites

    public Spike(double x, double y, double width, double height) {
        super(x, y, width, height, "");
        // Randomly select one of the 3 crystal sprites
        this.spriteIndex = (int)(Math.random() * 3);

        // Load crystal sprites
        loadCrystalSprites();
    }

    /**
     * Set the spike orientation
     * 
     * @param horizontal true for horizontal orientation, false for vertical
     * @param reversed   true to reverse the orientation (flip direction)
     */
    public void setOrientation(boolean horizontal, boolean reversed) {
        this.isHorizontal = horizontal;
        this.isReversed = reversed;
        this.spritesNeedUpdate = true; // Mark sprites for recaching
        System.out.println("Crystal orientation set - horizontal: " + horizontal + ", reversed: " + reversed);
    }

    /**
     * Load all crystal sprite frames
     */
    private void loadCrystalSprites() {
        // Load crystal sprites
        crystalSprites = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            try {
                crystalSprites[i] = ImageIO
                        .read(getClass().getResourceAsStream(String.format("/Sprites/Crystal/sprite_%d.png", i)));
                System.out.println("Loaded crystal sprite: " + i);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load crystal sprite " + i + ": " + e.getMessage());
            }
        }
    }    /**
     * Cache the sprite for performance optimization
     */
    private void cacheTransformedSprite() {
        if (!spritesNeedUpdate)
            return;

        // Only cache the selected sprite
        if (crystalSprites != null && spriteIndex < crystalSprites.length && 
            crystalSprites[spriteIndex] != null) {
            
            BufferedImage sprite = crystalSprites[spriteIndex];

            // Scale sprite to exactly match hitbox dimensions
            int targetWidth = (int) width;
            int targetHeight = (int) height;

            // Always scale to match hitbox size exactly
            BufferedImage scaledSprite = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledSprite.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(sprite, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // Apply orientation transformations to the scaled sprite
            BufferedImage transformedSprite = scaledSprite;

            if (isHorizontal) {
                // For horizontal crystal, rotate sprite 90 degrees
                transformedSprite = rotateImage90(transformedSprite);
            }

            if (isReversed) {
                if (isHorizontal) {
                    transformedSprite = flipImageHorizontally(transformedSprite);
                } else {
                    transformedSprite = flipImageVertically(transformedSprite);
                }
            }
            
            cachedCrystalSprite = transformedSprite;
        }

        spritesNeedUpdate = false;
    }

    public void update() {
        // Crystals don't need updates - they're static and permanent
        // No animation, no pulsing, no lifetime handling
    }

    /**
     * Reset/swap the player orientation
     */
    public void resetOrientation() {
        // Reset player's gravity orientation (swap variable) instead of crystal
        // orientation
        Player player = GameEngine.getPlayer();
        if (player != null) {
            // Reset player swap to normal gravity 
            player.setSwap(1);
        }
    }

    /**
     * Check if the crystal is dangerous (always true for crystals)
     * Used to determine if it should cause player respawn
     */
    public boolean isDangerous() {
        return isActive(); // Crystals are always dangerous when active
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
    }    @Override
    public void draw(Graphics g) {
        if (!isActive())
            return;

        Graphics2D g2d = (Graphics2D) g;

        // Cache transformed sprite if needed
        cacheTransformedSprite();

        // Draw crystal sprite to fill the entire hitbox
        if (cachedCrystalSprite != null) {
            // Convert from center coordinates to top left
            int drawX = (int) (x - width / 2);
            int drawY = (int) (y - height / 2);
            int drawWidth = (int) width;
            int drawHeight = (int) height;

            // Draw crystal sprite filling the entire hitbox
            g2d.drawImage(cachedCrystalSprite, drawX, drawY, drawWidth, drawHeight, null);
        }
    }
}
