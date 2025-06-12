import java.awt.*;

/**
 * Projectile class representing weapons, spells, and other active game elements
 */
public class Projectile extends Entity {
    private int ID;
    private int direction = 0;
    private long creationTime;
    private Vector2D velocity; // Initial velocity of the projectile

    // Death screen effect variables
    private double initX = 0; // Initial X position for death effect
    private double initY = 0; // Initial Y position for death effect
    private double scale = 1.0; // Scale factor for death effect

    /**
     * Create a new projectile
     */
    public Projectile(double centerX, double centerY, int projectileID, Vector2D initialVelocity) {
        super(centerX, centerY, 30, 30, "");

        this.ID = projectileID;
        this.velocity = new Vector2D(initialVelocity);
        this.creationTime = System.nanoTime(); // Set properties based on projectile type
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
            } // Player clone
            case 5 -> {
                spritePath = "/Sprites/O-4.png";
                setSpriteSize(50, 74); // Visual sprite size
                setHitboxSize(40, 60); // Slightly smaller hitbox than visual
            } // Death screen effect circles
            case 6 -> {
                // No sprite needed - will draw white circles manually
                setSpriteSize(30, 30); // Small initial size
                setHitboxSize(30, 30); // Same hitbox as visual
                // Store initial position and scale in velocity for the effect
                // velocity.x = logDistance, velocity.y = angle, scale stored separately
            }
            // Default
            default -> {
                spritePath = "/Sprites/friendlinessPellet.png";
                setSpriteSize(64, 30); // Visual sprite size
                setHitboxSize(48, 24); // Smaller hitbox
                acceleration.setY(0.9); // Apply gravity to this projectile type
            }
        } // Load the sprite
        loadSprite();
    }

    /**
     * Create a death screen effect projectile
     */
    public Projectile(double deathX, double deathY, double angle, double initialLogDist) {
        super(deathX, deathY, 30, 30, "");

        this.ID = 6; // Death screen effect
        this.velocity = new Vector2D(initialLogDist, angle); // Store logDist and angle in velocity
        this.creationTime = System.nanoTime();
        this.initX = deathX; // Store initial death position
        this.initY = deathY;
        this.scale = 1.0;

        // Set up death effect projectile - no sprite needed, we'll draw white circles
        setSpriteSize(30, 30); // Small initial size
        setHitboxSize(30, 30); // Same hitbox as visual

        // Don't load sprite for death effect - we'll draw circles manually
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
            case 6 -> {
                // Death screen effect - treat velocity as (log(distance), angle) instead of
                // actual velocity
                // Summon in 45deg intervals expand outward following logarithmic equation
                double logDist = velocity.getX();
                double dist = Math.log10(logDist) / Math.log10(1.07); // Calculate distance from log as per your formula
                double angle = velocity.getY();

                // Calculate new position based on initial death position
                x = initX + dist * Math.cos(Math.toRadians(angle));
                y = initY + dist * Math.sin(Math.toRadians(angle));

                // Update angle and log distance for next frame
                double lifetime = (System.nanoTime() - creationTime) / 1_000_000_000.0; 
                angle = (angle + (0.7 - lifetime) * 6) % 360;
                logDist += 40;
                scale += 2;

                // Update velocity for next frame
                velocity.setX(logDist);
                velocity.setY(angle);

                // Update visual size based on scale
                setSpriteSize((int) (30 * scale / 100.0), (int) (30 * scale / 100.0)); // Scale down the growth
                setHitboxSize((int) (30 * scale / 100.0), (int) (30 * scale / 100.0));
            }
        }
    }

    /**
     * Handle collision with walls
     */
    private void checkWallCollisions() {
        if (ID != 6) {
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
                    if (ID == 3 && GameEngine.getPlayer() != null) {
                        GameEngine.getPlayer().setPogo(true);
                    }
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
            case 6 -> {
                if (lifetime > 0.7)
                    setActive(false);
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

    @Override
    public void draw(Graphics g) {
        if (!isActive())
            return;

        // Special rendering for death screen effect
        if (ID == 6) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.WHITE);

            // Draw a filled white circle
            int diameter = (int) spriteWidth;
            int drawX = (int) (x - diameter / 2);
            int drawY = (int) (y - diameter / 2);

            g2d.fillOval(drawX, drawY, diameter, diameter);
        } else {
            // Use default sprite rendering for other projectiles
            super.draw(g);
        }
    }
}
