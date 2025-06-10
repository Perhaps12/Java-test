import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Wall class representing obstacles in the game world
 */
public class Wall extends GameObject {
    private Color color = Color.BLACK;
    private float alpha = 1.0f; // Full opacity by default

    // Sprite support for platform tiles
    private BufferedImage sprite;
    private String spritePath;
    private int rotation = 0; // Rotation in degrees (0, 90, 180, 270)
    private boolean useSprite = false;

    /**
     * Create a new wall with position and size
     */
    public Wall(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Create a new wall with position, size and color
     */
    public Wall(double x, double y, double width, double height, Color color) {
        super(x, y, width, height);
        this.color = color;
    }

    /**
     * Create a new wall with position, size, color and transparency
     */
    public Wall(double x, double y, double width, double height, Color color, float alpha) {
        super(x, y, width, height);
        this.color = color;
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha)); // Clamp alpha between 0 and 1
    }

    /**
     * Create a new wall with sprite support (for platform tiles)
     */
    public Wall(double x, double y, double width, double height, String spritePath, int rotation) {
        super(x, y, width, height);
        this.spritePath = spritePath;
        this.rotation = rotation;
        this.useSprite = true;
        loadSprite();
    }

    /**
     * Load the sprite image from the resource path
     */
    private void loadSprite() {
        if (spritePath != null && !spritePath.isEmpty()) {
            try {
                this.sprite = ImageIO.read(getClass().getResourceAsStream(spritePath));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load wall sprite: " + spritePath);
                this.useSprite = false;
            }
        }
    }

    @Override
    public void update() {
        // Walls don't need updating
    }

    @Override
    public void draw(Graphics g) {
        if (!active)
            return;

        Graphics2D g2d = (Graphics2D) g;

        if (useSprite && sprite != null) {
            // Draw sprite with rotation
            AffineTransform oldTransform = g2d.getTransform();

            // Apply transparency if alpha is less than 1.0
            Composite originalComposite = null;
            if (alpha < 1.0f) {
                originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }

            // Calculate center point for rotation
            double centerX = x + width / 2;
            double centerY = y + height / 2;

            // Apply rotation transform if needed
            if (rotation != 0) {
                g2d.rotate(Math.toRadians(rotation), centerX, centerY);
            }

            // Draw the sprite scaled to fit the wall dimensions
            g2d.drawImage(sprite, (int) x, (int) y, (int) width, (int) height, null);

            // Restore original transform and composite
            g2d.setTransform(oldTransform);
            if (originalComposite != null) {
                g2d.setComposite(originalComposite);
            }
        } else {
            // Draw colored rectangle (original behavior)
            // Apply transparency if alpha is less than 1.0
            if (alpha < 1.0f) {
                Composite originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                g2d.setColor(color);
                g2d.fillRect((int) x, (int) y, (int) width, (int) height);

                g2d.setComposite(originalComposite);
            } else {
                g.setColor(color);
                g.fillRect((int) x, (int) y, (int) width, (int) height);
            }
        }
    }

    /**
     * Draw this wall to a specific image at given coordinates
     * Used for pre-rendering platform layers
     */
    public void drawToImage(Graphics2D g2d, int x, int y) {
        if (sprite != null) {
            // Save current transform
            AffineTransform oldTransform = g2d.getTransform();

            // Apply rotation if needed
            if (rotation != 0) {
                // Rotate around the center of the sprite
                g2d.rotate(Math.toRadians(rotation), x + width / 2, y + height / 2);
            }

            // Draw the sprite
            g2d.drawImage(sprite, x, y, (int) width, (int) height, null);

            // Restore transform
            g2d.setTransform(oldTransform);
        } else {
            // Fallback to colored rectangle if no sprite
            Color oldColor = g2d.getColor();
            Composite oldComposite = g2d.getComposite();

            g2d.setColor(color);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.fillRect(x, y, (int) width, (int) height);

            g2d.setColor(oldColor);
            g2d.setComposite(oldComposite);
        }
    }

    // Getters and setters
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    public String getSpritePath() {
        return spritePath;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
        this.useSprite = true;
        loadSprite();
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public boolean isUsingSprite() {
        return useSprite;
    }

    public void setUseSprite(boolean useSprite) {
        this.useSprite = useSprite;
    }
}
