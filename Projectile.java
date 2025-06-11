import java.awt.*;

/**
 * Projectile class representing weapons, spells, and other active game elements
 */
public class Projectile extends Entity {
    private int ID;
    private int direction = 0;
    private long creationTime;
    private Vector2D velocity; // Initial velocity of the projectile

    /**
     * Create a new projectile
     */
    public Projectile(double centerX, double centerY, int projectileID, Vector2D initialVelocity) {
        super(centerX, centerY, 30, 30, "");

        this.ID = projectileID;
        this.velocity = new Vector2D(initialVelocity);
        this.creationTime = System.nanoTime();        // Set properties based on projectile type
        switch (projectileID) {
            // Horizontal melee attack
            case 1 -> {
                spritePath = "/Sprites/friendlinessPellet.png"; // Add sprite for melee
                setSpriteSize(90, 80); // Large visual sprite
                setHitboxSize(50, 40); // Smaller hitbox for precision
                direction = GameEngine.getPlayer() != null ? GameEngine.getPlayer().getDirection() : 1;
            }
            // Upwards melee attack
            case 2 -> {
                spritePath = "/Sprites/friendlinessPellet.png"; // Add sprite for upward melee
                setSpriteSize(60, 100); // Visual sprite size
                setHitboxSize(40, 60); // Smaller hitbox
                direction = GameEngine.getPlayer() != null ? GameEngine.getPlayer().getDirection() : 1;
            }
            // Downwards melee attack
            case 3 -> {
                spritePath = "/Sprites/friendlinessPellet.png"; // Add sprite for downward melee
                setSpriteSize(60, 100); // Visual sprite size
                setHitboxSize(40, 60); // Smaller hitbox
                direction = GameEngine.getPlayer() != null ? GameEngine.getPlayer().getDirection() : 1;
            }
            // Horizontal ranged attack
            case 4 -> {
                spritePath = "/Sprites/thec oin.png"; // Add sprite for ranged attack
                setSpriteSize(70, 56); // Visual sprite size
                setHitboxSize(35, 28); // Smaller hitbox for precision
                direction = GameEngine.getPlayer() != null ? GameEngine.getPlayer().getDirection() : 1;
            }
            // Player clone
            case 5 -> {
                spritePath = "/Sprites/O-4.png";
                setSpriteSize(50, 74); // Visual sprite size
                setHitboxSize(40, 60); // Slightly smaller hitbox than visual
            }
            // Default
            default -> {
                spritePath = "/Sprites/friendlinessPellet.png";
                setSpriteSize(64, 30); // Visual sprite size
                setHitboxSize(48, 24); // Smaller hitbox
                acceleration.setY(0.9); // Apply gravity to this projectile type
            }
        }

        // Load the sprite
        loadSprite();
    }

    @Override
    public void update() {
        // Handle projectile-specific movement
        handleProjectileMovement();

        // Apply physics (velocity, acceleration)
        applyPhysics();

        // Check for wall collisions
        checkWallCollisions();

        // Time-based behaviors (like automatic deactivation after some time)
        handleLifetime();
    }

    /**
     * Handle projectile-specific movement patterns
     */
    private void handleProjectileMovement() {
        switch (ID) {
            case 1, 2, 3 -> {
                // Melee attacks stay at player position but with offset based on direction
                Player player = GameEngine.getPlayer();
                if (player != null) {
                    double offsetX = 0;
                    double offsetY = 0;

                    if (ID == 1) { // Horizontal attack
                        offsetX = direction * 45; // 45 pixels in front of player
                    } else if (ID == 2) { // Upward attack
                        offsetY = -50; // 50 pixels above player
                    } else if (ID == 3) { // Downward attack
                        offsetY = 50; // 50 pixels below player
                    }

                    x = player.getX() + offsetX;
                    y = player.getY() + offsetY;
                }
            }
            case 4 -> {
                // Ranged projectile moves in the direction it was fired
                x += velocity.getX();
            }
        }
    }

    /**
     * Handle collision with walls
     */
    private void checkWallCollisions() {
        for (Wall wall : GameEngine.getWalls()) {
            if (isCollidingWithWall(wall)) {
                // For most projectiles, deactivate on wall collision
                if (ID != 5) { // Except player clone type
                    setActive(false);
                } else {
                    handleWallCollision(wall);
                }
            }
        } // Check for collisions with NPCs if this is an attack projectile
        if (ID >= 1 && ID <= 4) {
            for (Npc npc : GameEngine.getNpcs()) {
                if (isColliding(npc)) {
                    // Handle damage to NPC here if needed
                    // Special case: downward strike triggers pogo
                    if (ID == 3 && GameEngine.getPlayer() != null) {
                        GameEngine.getPlayer().setPogo(true);
                    }
                }
            }
        }
    }

    /**
     * Handle projectile lifetime and expiration
     */
    private void handleLifetime() {
        long now = System.nanoTime();
        double lifetime = (now - creationTime) / 1_000_000_000.0; // Convert to seconds

        // Different lifetime for different projectile types
        switch (ID) {
            case 1, 2, 3 -> {
                if (lifetime > 0.2)
                    setActive(false); // Short duration for melee attacks
            }
            case 4 -> {
                if (lifetime > 2.0)
                    setActive(false); // Longer duration for ranged attacks
            }
            
        }

        // Deactivate if off-screen
        if (x < -500 || x > 3000 || y < -500 || y > 1500) {
            setActive(false);
        }
    }

    // Getters
    public int getID() {
        return ID;
    }

    public int getDirection() {
        return direction;
    }
}
