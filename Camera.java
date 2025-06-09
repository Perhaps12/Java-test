import java.awt.*;

/**
 * Camera class that handles screen shake effects and player following
 */
public class Camera {
    private static Camera instance; // Camera position and following
    private double cameraX = 0;
    private double cameraY = 0;
    private double followSpeed = 0.3; // How quickly camera follows player (0.1 = smooth, 1.0 = instant) // Level
                                      // boundaries (using scalable system from GameSettings)

    private double getLevelLeft() {
        return 0; // Camera left edge
    }

    private double getLevelRight() {
        return GameSettings.getInstance().getLevelWidth(); // Camera right edge
    }

    // Screen dimensions for camera bounds calculation
    private static final double SCREEN_WIDTH = 1920;
    private static final double SCREEN_HEIGHT = 1080; // Screen shake effect variables
    private double shakeX = 0;
    private double shakeY = 0;
    private double shakeIntensity = 0;
    private int shakeDuration = 0;
    private double shakeDecay = 0.95; // How quickly shake intensity decreases // Swap animation variables

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
     * Camera is fixed vertically to show both sections, only follows horizontally
     */
    private void updatePlayerFollowing() {
        // Get player position from GameEngine
        Player player = GameEngine.getPlayer();
        if (player == null)
            return;

        double playerX = player.getX();

        // Calculate desired camera position (center player horizontally)
        double targetCameraX = playerX - SCREEN_WIDTH / 2;

        // Keep camera vertically centered to show both top and bottom sections
        // Camera Y should be 0 (center line) minus half screen height to center the
        // view
        double targetCameraY = -SCREEN_HEIGHT / 2;

        // Get level boundaries from scalable system for horizontal constraints only
        double levelLeft = getLevelLeft();
        double levelRight = getLevelRight();

        // Calculate horizontal camera bounds
        double maxCameraLeft = levelLeft;
        double maxCameraRight = levelRight - SCREEN_WIDTH;

        // If level is smaller than screen horizontally, allow some flexibility
        if (maxCameraRight < maxCameraLeft) {
            // Level width is smaller than screen - allow some flexibility
            double levelCenterX = (levelLeft + levelRight) / 2;
            double maxOffset = SCREEN_WIDTH * 0.4; // Allow showing 40% empty space on each side
            maxCameraLeft = levelCenterX - SCREEN_WIDTH / 2 - maxOffset;
            maxCameraRight = levelCenterX - SCREEN_WIDTH / 2 + maxOffset;
        }

        // Apply horizontal boundary constraints only
        targetCameraX = Math.max(targetCameraX, maxCameraLeft);
        targetCameraX = Math.min(targetCameraX, maxCameraRight);

        // Adjust follow speed based on swap animation phase
        double currentFollowSpeed = followSpeed;

        // Smoothly interpolate horizontally, set vertical position directly
        cameraX += (targetCameraX - cameraX) * currentFollowSpeed;
        cameraY = targetCameraY; // Fixed vertical position to show both sections
    }

    /**
     * Apply camera transform to graphics context (includes following, shake, and
     * swap animation)
     */
    public void applyTransform(Graphics2D g) {
        // Apply camera position offset (for following player) and shake offset
        g.translate(-(cameraX + shakeX), -(cameraY + shakeY));
    }

    /**
     * Remove camera transform from graphics context
     */
    public void removeTransform(Graphics2D g) {
        // Restore camera and shake offsets
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
     * Cubic ease-in-out function for smooth transitions
     * Returns a value between 0 and 1 with smooth acceleration and deceleration
     */
    private double cubicEaseInOut(double t) {
        if (t < 0.5) {
            return 4 * t * t * t;
        } else {
            double f = (2 * t - 2);
            return 1 + f * f * f / 2;
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
     * Only snaps horizontally, vertical position remains fixed
     */
    public void snapToPlayer() {
        Player player = GameEngine.getPlayer();
        if (player == null)
            return;

        double playerX = player.getX();

        // Calculate camera position to center player horizontally
        double targetCameraX = playerX - SCREEN_WIDTH / 2;

        // Fixed vertical position to show both sections
        double targetCameraY = -SCREEN_HEIGHT / 2;

        // Get level boundaries from scalable system
        double levelLeft = getLevelLeft();
        double levelRight = getLevelRight();

        // Apply horizontal boundary constraints
        targetCameraX = Math.max(targetCameraX, levelLeft);
        targetCameraX = Math.min(targetCameraX, levelRight - SCREEN_WIDTH);

        // Set camera position directly
        cameraX = targetCameraX;
        cameraY = targetCameraY;
    }
}