import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Background renderer that handles fixed background textures that don't move with the camera
 */
public class BackgroundRenderer {
    private static BackgroundRenderer instance;
    private BufferedImage backgroundTexture;
    private String texturePath;
    private boolean isEnabled = true;
    private Color backgroundTint = null;

    /**
     * Private constructor for singleton pattern
     */
    private BackgroundRenderer() {
        // Set default background texture (we'll use Blue water.png as a background)
        setBackgroundTexture("/textures/Blue water.png");
    }

    /**
     * Get the singleton instance
     */
    public static BackgroundRenderer getInstance() {
        if (instance == null) {
            instance = new BackgroundRenderer();
        }
        return instance;
    }

    /**
     * Set the background texture from a resource path
     */
    public void setBackgroundTexture(String texturePath) {
        this.texturePath = texturePath;
        loadBackgroundTexture();
    }

    /**
     * Load the background texture from the resource path
     */
    private void loadBackgroundTexture() {
        if (texturePath != null && !texturePath.isEmpty()) {
            try {
                this.backgroundTexture = ImageIO.read(getClass().getResourceAsStream(texturePath));
                System.out.println("loaded background texture: " + texturePath);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("could not load background texture: " + texturePath);
                this.backgroundTexture = null;
            }
        }
    }

    /**
     * Set a tint color to apply over the background texture
     */
    public void setBackgroundTint(Color tint) {
        this.backgroundTint = tint;
    }

    /**
     * Enable or disable background rendering
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }    /**
     * Draw the background texture in screen coordinates (not affected by camera)
     * This should be called before applying camera transforms
     */
    public void drawBackground(Graphics2D g) {
        if (!isEnabled) {
            return;
        }

        GameSettings settings = GameSettings.getInstance();
        int screenWidth = settings.getBaseWidth();
        int screenHeight = settings.getBaseHeight();
        int halfHeight = screenHeight / 2; // Only draw on top half

        if (backgroundTexture != null) {
            // Calculate how many times to tile the texture to fill the top half of screen
            int textureWidth = backgroundTexture.getWidth();
            int textureHeight = backgroundTexture.getHeight();
            
            int tilesX = (screenWidth / textureWidth) + 2; // +2 for extra coverage
            int tilesY = (halfHeight / textureHeight) + 2; // +2 for extra coverage

            // Draw tiled background texture only on top half
            for (int x = 0; x < tilesX; x++) {
                for (int y = 0; y < tilesY; y++) {
                    int drawX = x * textureWidth;
                    int drawY = y * textureHeight;
                    // Only draw if the tile is within the top half
                    g.drawImage(backgroundTexture, drawX, drawY, null);
                }
            }

            // Apply tint if specified (only to top half)
            if (backgroundTint != null) {
                Composite originalComposite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g.setColor(backgroundTint);
                g.fillRect(0, 0, screenWidth, halfHeight);
                g.setComposite(originalComposite);
            }
        } else {
            // Fallback: draw solid color background (only top half)
            g.setColor(settings.getBackgroundColor());
            g.fillRect(0, 0, screenWidth, halfHeight);
        }
    }

    // Getters
    public boolean isEnabled() {
        return isEnabled;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public Color getBackgroundTint() {
        return backgroundTint;
    }
}
