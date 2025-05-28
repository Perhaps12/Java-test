import java.awt.*;

/**
 * Camera class that handles screen shake effects and player following
 */
public class Camera {
    private static Camera instance;

    // Camera position and following
    private double cameraX = 0;
    private double cameraY = 0;
    private double followSpeed = 0.3; // How quickly camera follows player (0.1 = smooth, 1.0 = instant) // Level
                                      // boundaries (based on border walls in GameEngine)
    private static final double LEVEL_LEFT = 7;
    private static final double LEVEL_RIGHT = 1527;
    private static final double LEVEL_TOP = 7;
    private static final double LEVEL_BOTTOM = 785;

    // Screen dimensions for camera bounds calculation
    private static final double SCREEN_WIDTH = 1920;
    private static final double SCREEN_HEIGHT = 1080;

    // Screen shake effect variables
    private double shakeX = 0;
    private double shakeY = 0;
    private double shakeIntensity = 0;
    private int shakeDuration = 0;
    private double shakeDecay = 0.95; // How quickly shake intensity decreases

    // Shake pattern types
    private ShakeType currentShakeType = ShakeType.RANDOM;

    public enum ShakeType {
        RANDOM, // Random shake in all directions
        HORIZONTAL, // Only horizontal shake
        VERTICAL, // Only vertical shake
        CIRCULAR // Circular shake pattern
    }

    /**
     * Private constructor for singleton pattern
     */
    private Camera() {
    }

    /**
     * Get the singleton camera instance
     */
    public static Camera getInstance() {
        if (instance == null) {
            instance = new Camera();
        }
        return instance;
    }

    /**
     * Update camera shake effects and player following (called each frame)
     */
    public void update() {
        updatePlayerFollowing();
        updateScreenShake();
    }

    /**
     * Update camera position to follow the player with boundary constraints
     */
    private void updatePlayerFollowing() {
        // Get player position from GameEngine
        Player player = GameEngine.getPlayer();
        if (player == null)
            return;

        double playerX = player.getX();
        double playerY = player.getY();

        // Calculate desired camera position (center player on screen)
        double targetCameraX = playerX - SCREEN_WIDTH / 2;
        double targetCameraY = playerY - SCREEN_HEIGHT / 2;

        // For smaller levels, we need to allow negative camera positions to keep player
        // centered
        // while ensuring we don't show beyond level boundaries

        // Calculate maximum extents where camera can go
        double maxCameraLeft = LEVEL_LEFT;
        double maxCameraRight = LEVEL_RIGHT - SCREEN_WIDTH;
        double maxCameraTop = LEVEL_TOP;
        double maxCameraBottom = LEVEL_BOTTOM - SCREEN_HEIGHT;

        // If level is smaller than screen, allow camera to go negative to center
        // content
        // but clamp to reasonable bounds to prevent showing too much empty space
        if (maxCameraRight < maxCameraLeft) {
            // Level width is smaller than screen - allow some flexibility
            double levelCenterX = (LEVEL_LEFT + LEVEL_RIGHT) / 2;
            double maxOffset = SCREEN_WIDTH * 0.4; // Allow showing 40% empty space on each side
            maxCameraLeft = levelCenterX - SCREEN_WIDTH / 2 - maxOffset;
            maxCameraRight = levelCenterX - SCREEN_WIDTH / 2 + maxOffset;
        }

        if (maxCameraBottom < maxCameraTop) {
            // Level height is smaller than screen - allow some flexibility
            double levelCenterY = (LEVEL_TOP + LEVEL_BOTTOM) / 2;
            double maxOffset = SCREEN_HEIGHT * 0.3; // Allow showing 30% empty space above/below
            maxCameraTop = levelCenterY - SCREEN_HEIGHT / 2 - maxOffset;
            maxCameraBottom = levelCenterY - SCREEN_HEIGHT / 2 + maxOffset;
        }

        // Apply boundary constraints
        targetCameraX = Math.max(targetCameraX, maxCameraLeft);
        targetCameraX = Math.min(targetCameraX, maxCameraRight);
        targetCameraY = Math.max(targetCameraY, maxCameraTop);
        targetCameraY = Math.min(targetCameraY, maxCameraBottom);

        // Smoothly interpolate to target position
        cameraX += (targetCameraX - cameraX) * followSpeed;
        cameraY += (targetCameraY - cameraY) * followSpeed;
    }

    /**
     * Apply camera transform to graphics context (includes following and shake)
     */
    public void applyTransform(Graphics2D g) {
        // Apply camera position offset (for following player) and shake offset
        g.translate(-(cameraX + shakeX), -(cameraY + shakeY));
    }

    /**
     * Remove camera transform from graphics context
     */
    public void removeTransform(Graphics2D g) {
        // Restore original transform by reversing both camera and shake offsets
        g.translate(cameraX + shakeX, cameraY + shakeY);
    }

    /**
     * Update screen shake effect based on current shake type
     */
    private void updateScreenShake() {
        if (shakeDuration > 0) {
            shakeDuration--;

            switch (currentShakeType) {
                case RANDOM:
                    shakeX = (Math.random() - 0.5) * shakeIntensity;
                    shakeY = (Math.random() - 0.5) * shakeIntensity;
                    break;

                case HORIZONTAL:
                    shakeX = (Math.random() - 0.5) * shakeIntensity;
                    shakeY = 0;
                    break;

                case VERTICAL:
                    shakeX = 0;
                    shakeY = (Math.random() - 0.5) * shakeIntensity;
                    break;

                case CIRCULAR:
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = Math.random() * shakeIntensity / 2;
                    shakeX = Math.cos(angle) * radius;
                    shakeY = Math.sin(angle) * radius;
                    break;
            }

            // Apply decay to shake intensity for smoother fade-out
            shakeIntensity *= shakeDecay;
        } else {
            // Reset shake when duration expires
            shakeX = 0;
            shakeY = 0;
            shakeIntensity = 0;
        }
    }

    /**
     * Start a screen shake effect with specified intensity and duration
     */
    public void shake(double intensity, int duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
    }

    /**
     * Start a screen shake effect with specific shake type
     */
    public void shake(double intensity, int duration, ShakeType shakeType) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.currentShakeType = shakeType;
    }

    /**
     * Set the decay rate for shake intensity (0.0 = no decay, 1.0 = instant stop)
     */
    public void setShakeDecay(double decay) {
        this.shakeDecay = Math.max(0.0, Math.min(1.0, decay));
    }

    /**
     * Stop any current shake effect
     */
    public void stopShake() {
        shakeX = 0;
        shakeY = 0;
        shakeIntensity = 0;
        shakeDuration = 0;
    }

    /**
     * Check if camera is currently shaking
     */
    public boolean isShaking() {
        return shakeDuration > 0 || shakeIntensity > 0.1;
    }

    /**
     * Check if a new shake effect would be significant enough to override current
     * shake
     */
    public boolean shouldOverrideShake(double newIntensity) {
        return !isShaking() || newIntensity > shakeIntensity * 1.5;
    }

    // Getters for shake information
    public double getShakeX() {
        return shakeX;
    }

    public double getShakeY() {
        return shakeY;
    }

    public double getShakeIntensity() {
        return shakeIntensity;
    }

    public int getShakeDuration() {
        return shakeDuration;
    }

    public ShakeType getShakeType() {
        return currentShakeType;
    }

    // Getters for camera position
    public double getCameraX() {
        return cameraX;
    }

    public double getCameraY() {
        return cameraY;
    }

    /**
     * Set the camera follow speed (0.0 = no following, 1.0 = instant following)
     */
    public void setFollowSpeed(double speed) {
        this.followSpeed = Math.max(0.0, Math.min(1.0, speed));
    }

    /**
     * Get the current follow speed
     */
    public double getFollowSpeed() {
        return followSpeed;
    }

    /**
     * Instantly snap camera to player position (useful for scene transitions)
     */
    public void snapToPlayer() {
        Player player = GameEngine.getPlayer();
        if (player == null)
            return;

        double playerX = player.getX();
        double playerY = player.getY();

        // Calculate camera position to center player
        double targetCameraX = playerX - SCREEN_WIDTH / 2;
        double targetCameraY = playerY - SCREEN_HEIGHT / 2;

        // Apply boundary constraints
        targetCameraX = Math.max(targetCameraX, LEVEL_LEFT);
        targetCameraX = Math.min(targetCameraX, LEVEL_RIGHT - SCREEN_WIDTH);
        targetCameraY = Math.max(targetCameraY, LEVEL_TOP);
        targetCameraY = Math.min(targetCameraY, LEVEL_BOTTOM - SCREEN_HEIGHT);

        // Set camera position directly
        cameraX = targetCameraX;
        cameraY = targetCameraY;
    }
}