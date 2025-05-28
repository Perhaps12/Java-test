import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to hold game settings
 */
public class GameSettings {
    private static GameSettings instance;

    // Game dimensions
    private int width = 1920;
    private int height = 1080;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private final int baseWidth = 1920; // Reference resolution
    private final int baseHeight = 1080;

    // Game settings
    private boolean soundEnabled = true;
    private float mainVolume = 1.0f;
    private float musicVolume = 0.8f;
    private float effectsVolume = 1.0f;

    // Debug settings
    private boolean showDebug = true;
    private boolean showFPS = true;
    private boolean showGrid = true;
    private boolean showHitboxes = true;

    // Color scheme
    private Color backgroundColor = Color.WHITE;
    private Color gridColor = Color.GRAY;
    private Color wallColor = Color.BLACK;
    private Color playerColor = Color.GREEN;
    private Color npcColor = Color.RED;

    // Key mappings with default values
    private Map<String, Integer> keyBindings = new HashMap<>();

    /**
     * Private constructor to enforce singleton pattern
     */
    private GameSettings() {
        // Initialize default key bindings
        keyBindings.put("UP", java.awt.event.KeyEvent.VK_UP);
        keyBindings.put("DOWN", java.awt.event.KeyEvent.VK_DOWN);
        keyBindings.put("LEFT", java.awt.event.KeyEvent.VK_LEFT);
        keyBindings.put("RIGHT", java.awt.event.KeyEvent.VK_RIGHT);
        keyBindings.put("JUMP", java.awt.event.KeyEvent.VK_SPACE);
        keyBindings.put("FIRE", java.awt.event.KeyEvent.VK_Z);
        keyBindings.put("ACTION", java.awt.event.KeyEvent.VK_X);

        // Initialize to screen resolution
        initializeResolution();
    }

    /**
     * Initialize resolution to match the monitor
     */
    private void initializeResolution() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();

            this.width = dm.getWidth();
            this.height = dm.getHeight();

            // Calculate scaling factors based on the reference resolution
            this.scaleX = (double) width / baseWidth;
            this.scaleY = (double) height / baseHeight;

            System.out.println("Detected resolution: " + width + "x" + height);
            System.out.println("Scale factors: X=" + scaleX + ", Y=" + scaleY);
        } catch (Exception e) {
            System.err.println("Failed to detect screen resolution, using default: " + e.getMessage());
            // Keep default 1920x1080 if detection fails
        }
    }

    /**
     * Get the singleton instance
     */
    public static synchronized GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    } // Getters for dimensions

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Getters for scaling
    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public int getBaseWidth() {
        return baseWidth;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    /**
     * Scale a coordinate from base resolution to current resolution
     */
    public double scaleX(double baseX) {
        return baseX * scaleX;
    }

    public double scaleY(double baseY) {
        return baseY * scaleY;
    }

    /**
     * Scale a size/dimension from base resolution to current resolution
     */
    public double scaleSize(double baseSize) {
        return baseSize * Math.min(scaleX, scaleY); // Use minimum to maintain aspect ratio
    }

    // Getters for sound settings
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public float getMainVolume() {
        return mainVolume;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getEffectsVolume() {
        return effectsVolume;
    }

    // Getters for debug settings
    public boolean isShowDebug() {
        return showDebug;
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public boolean isShowHitboxes() {
        return showHitboxes;
    }

    // Getters for colors
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public Color getWallColor() {
        return wallColor;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public Color getNpcColor() {
        return npcColor;
    }

    // Key binding methods
    public int getKeyBinding(String action) {
        return keyBindings.getOrDefault(action, 0);
    }

    public void setKeyBinding(String action, int keyCode) {
        keyBindings.put(action, keyCode);
    }

    // Setters for user-configurable settings
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public void setMainVolume(float mainVolume) {
        this.mainVolume = clamp(mainVolume, 0, 1);
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = clamp(musicVolume, 0, 1);
    }

    public void setEffectsVolume(float effectsVolume) {
        this.effectsVolume = clamp(effectsVolume, 0, 1);
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public void setShowFPS(boolean showFPS) {
        this.showFPS = showFPS;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void setShowHitboxes(boolean showHitboxes) {
        this.showHitboxes = showHitboxes;
    }

    /**
     * Update resolution settings (useful for window resize events)
     */
    public void updateResolution(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        this.scaleX = (double) width / baseWidth;
        this.scaleY = (double) height / baseHeight;

        System.out.println("Resolution updated to: " + width + "x" + height);
        System.out.println("New scale factors: X=" + scaleX + ", Y=" + scaleY);
    }

    // Helper method to clamp values
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
