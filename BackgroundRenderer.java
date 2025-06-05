import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Background renderer that handles fixed background textures and parallax cave
 * layers
 */
public class BackgroundRenderer {
    private static BackgroundRenderer instance;
    private BufferedImage backgroundTexture;
    private String texturePath;
    private boolean isEnabled = true;
    private Color backgroundTint = null;

    // Cave layer textures for parallax scrolling
    private BufferedImage caveLayer1;
    private BufferedImage caveLayer2;
    private BufferedImage caveLayer3;
    private boolean caveLayersEnabled = true;

    /**
     * Private constructor for singleton pattern
     */
    private BackgroundRenderer() {
        // Set default background texture (we'll use Blue water.png as a background)
        setBackgroundTexture("/textures/Blue water.png");

        // Load cave layer textures for parallax scrolling
        loadCaveLayers();
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
     * Load the cave layer textures for parallax scrolling
     */
    private void loadCaveLayers() {
        try {
            caveLayer1 = ImageIO.read(getClass().getResourceAsStream("/textures/Cave layers/Cave layer 1.png"));
            System.out.println("loaded cave layer 1");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("could not load cave layer 1");
            caveLayer1 = null;
        }

        try {
            caveLayer2 = ImageIO.read(getClass().getResourceAsStream("/textures/Cave layers/Cave layer 2.png"));
            System.out.println("loaded cave layer 2");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("could not load cave layer 2");
            caveLayer2 = null;
        }

        try {
            caveLayer3 = ImageIO.read(getClass().getResourceAsStream("/textures/Cave layers/Cave layer 3.png"));
            System.out.println("loaded cave layer 3");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("could not load cave layer 3");
            caveLayer3 = null;
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
    }

    /**
     * Enable or disable cave layer parallax scrolling
     */
    public void setCaveLayersEnabled(boolean enabled) {
        this.caveLayersEnabled = enabled;
    }

    /**
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

        // Draw cave layers with parallax scrolling on top of background
        if (caveLayersEnabled) {
            drawCaveLayers(g, screenWidth, halfHeight);
        }
    }

    /**
     * Draw cave layers with parallax scrolling effect
     * Layer 1 = slowest (farthest back), Layer 3 = fastest (closest)
     */
    private void drawCaveLayers(Graphics2D g, int screenWidth, int halfHeight) {
        Camera camera = Camera.getInstance();
        double cameraX = camera.getCameraX();

        // Different parallax speeds for each layer (slower = farther)
        double layer1Speed = 0.7;
        double layer2Speed = 0.775;
        double layer3Speed = 0.8;

        // TODO: make cave layers better, cave 1 and cave 3 kinda overlap a lot and it looks goofy
                if (caveLayer3 != null) {
                    drawParallaxLayer(g, caveLayer3, cameraX, layer3Speed, screenWidth, halfHeight, 1.0f);
                }
                
                if (caveLayer2 != null) {
            drawParallaxLayer(g, caveLayer2, cameraX, layer2Speed, screenWidth, halfHeight, 1.0f);
        }
            if (caveLayer1 != null) {
                drawParallaxLayer(g, caveLayer1, cameraX, layer1Speed, screenWidth, halfHeight, 0.7f);
            }
    }

    /**
     * Draw a single parallax layer with the specified speed
     */
    private void drawParallaxLayer(Graphics2D g, BufferedImage layer, double cameraX, double speed, int screenWidth,
            int halfHeight, float alpha) {
        if (layer == null)
            return;

        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        // Calculate parallax offset
        double parallaxOffset = cameraX * speed;

        // Calculate how many times we need to tile horizontally to cover screen +
        // parallax movement
        int tilesX = (screenWidth / layerWidth) + 3; // +3 for extra coverage with parallax
        int tilesY = (halfHeight / layerHeight) + 2; // +2 for vertical coverage

        // Calculate starting position to account for parallax offset
        int startX = (int) (-parallaxOffset % layerWidth) - layerWidth;

        // Apply alpha transparency
        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Draw tiled layer only in the top half
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                int drawX = startX + (x * layerWidth);
                int drawY = y * layerHeight;

                // Only draw if the tile overlaps with the visible area
                if (drawX + layerWidth >= 0 && drawX < screenWidth && drawY < halfHeight) {
                    g.drawImage(layer, drawX, drawY, null);
                }
            }
        }

        // Restore original composite
        g.setComposite(originalComposite);
    }// Getters

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isCaveLayersEnabled() {
        return caveLayersEnabled;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public Color getBackgroundTint() {
        return backgroundTint;
    }
}
