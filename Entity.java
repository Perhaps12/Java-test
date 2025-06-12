import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Base class for game entities with sprite rendering, collision handling and
 * physics
 */
public abstract class Entity extends GameObject { // Image and rendering
    protected BufferedImage sprite;
    protected String spritePath;
    protected double hitboxWidth;
    protected double hitboxHeight;
    protected double spriteWidth;
    protected double spriteHeight;

    // Physics
    protected Vector2D velocity; // current velocity vector of entity
    protected Vector2D acceleration; // current acceleration vector of entity

    // Timing
    protected long lastUpdateTime;

    /**
     * Create a new entity with position, size and sprite
     */
    public Entity(double x, double y, double hitboxWidth, double hitboxHeight, String spritePath) {
        super(x, y, hitboxWidth, hitboxHeight);
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.spriteWidth = hitboxWidth; // Default sprite size same as hitbox
        this.spriteHeight = hitboxHeight; // Default sprite size same as hitbox
        this.spritePath = spritePath;
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
        this.lastUpdateTime = System.nanoTime();

        loadSprite();
    }

    /**
     * Create a new entity with position, separate hitbox and sprite dimensions
     */
    public Entity(double x, double y, double hitboxWidth, double hitboxHeight,
            double spriteWidth, double spriteHeight, String spritePath) {
        super(x, y, hitboxWidth, hitboxHeight);
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.spritePath = spritePath;
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
        this.lastUpdateTime = System.nanoTime();

        loadSprite();
    }

    /**
     * Load the sprite image from the resource path
     */
    protected void loadSprite() {
        if (spritePath != null && !spritePath.isEmpty()) {
            try {
                this.sprite = ImageIO.read(getClass().getResourceAsStream(spritePath));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("couldn't load sprite " + spritePath);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (active && sprite != null) {
            // Check if we need to scale the sprite
            if (spriteWidth == sprite.getWidth() && spriteHeight == sprite.getHeight()) {
                // No scaling needed - use the faster draw method
                g.drawImage(sprite, (int) (x - spriteWidth / 2), (int) (y - spriteHeight / 2), null);
            } else {
                // Scaling needed - use the slower but necessary scaled draw method
                g.drawImage(sprite, (int) (x - spriteWidth / 2), (int) (y - spriteHeight / 2),
                        (int) spriteWidth, (int) spriteHeight, null);
            }
        }
    }

    /**
     * Apply physics to the entity (velocity, acceleration, etc.)
     */
    protected void applyPhysics() {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // convert to seconds
        lastUpdateTime = currentTime;

        // Apply acceleration to velocity
        velocity.add(acceleration.getX() * deltaTime, acceleration.getY() * deltaTime);

        // Apply velocity to position
        x += velocity.getX() * deltaTime;
        y += velocity.getY() * deltaTime;
    }

    /**
     * Apply physics with high-velocity collision detection to prevent clipping
     * Uses raycast-like approach for fast-moving entities
     */
    protected void applyPhysicsWithCollisionStepping(double maxStepSize) {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // convert to seconds
        lastUpdateTime = currentTime;

        // Apply acceleration to velocity
        velocity.add(acceleration.getX() * deltaTime, acceleration.getY() * deltaTime);

        // Calculate intended movement
        double moveX = velocity.getX() * deltaTime;
        double moveY = velocity.getY() * deltaTime;

        // Calculate total movement distance
        double totalDistance = Math.sqrt(moveX * moveX + moveY * moveY);

        // If moving fast, break movement into smaller steps
        if (totalDistance > maxStepSize) {
            int steps = (int) Math.ceil(totalDistance / maxStepSize);
            double stepX = moveX / steps;
            double stepY = moveY / steps;

            // Move in small increments, checking for collisions each step
            for (int i = 0; i < steps; i++) {
                double oldX = x;
                double oldY = y;

                // Take a small step
                x += stepX;
                y += stepY;

                // Check for wall collisions after each step
                boolean collided = false;
                for (Wall wall : GameEngine.getWalls()) {
                    if (isCollidingWithWall(wall)) {
                        // Collision detected, handle it and stop movement
                        x = oldX; // Revert position
                        y = oldY;
                        handleWallCollision(wall);
                        collided = true;
                        break;
                    }
                }

                if (collided) {
                    break; // Stop movement if we hit something
                }
            }
        } else {
            // Normal movement for slow speeds
            x += moveX;
            y += moveY;
        }
    }

    /**
     * Check if this entity is colliding with a wall
     */
    protected boolean isCollidingWithWall(Wall wall) {
        // Small tolerance to account for floating-point precision
        final double TOLERANCE = 0.01;

        double entityLeft = x - hitboxWidth / 2;
        double entityRight = x + hitboxWidth / 2;
        double entityTop = y - hitboxHeight / 2;
        double entityBottom = y + hitboxHeight / 2;

        double wallLeft = wall.getX();
        double wallRight = wall.getX() + wall.getWidth();
        double wallTop = wall.getY();
        double wallBottom = wall.getY() + wall.getHeight();

        // Use tolerance to prevent edge-clipping issues
        return (entityRight - TOLERANCE) > wallLeft &&
                (entityLeft + TOLERANCE) < wallRight &&
                (entityBottom - TOLERANCE) > wallTop &&
                (entityTop + TOLERANCE) < wallBottom;
    }

    /**
     * Handle collision with a wall by repositioning the entity
     */
    protected void handleWallCollision(Wall wall) {
        if (!isCollidingWithWall(wall))
            return;

        // Small buffer to prevent floating-point precision issues and immediate
        // re-collision
        final double COLLISION_BUFFER = 0.1;

        double entityLeft = x - hitboxWidth / 2;
        double entityRight = x + hitboxWidth / 2;
        double entityTop = y - hitboxHeight / 2;
        double entityBottom = y + hitboxHeight / 2;

        double wallLeft = wall.getX();
        double wallRight = wall.getX() + wall.getWidth();
        double wallTop = wall.getY();
        double wallBottom = wall.getY() + wall.getHeight();

        // Calculate collision depths (how far the entity penetrated into the wall)
        double leftDepth = entityRight - wallLeft;
        double rightDepth = wallRight - entityLeft;
        double topDepth = entityBottom - wallTop;
        double bottomDepth = wallBottom - entityTop;

        // Find smallest penetration depth to determine collision direction
        double minDepth = Math.min(Math.min(leftDepth, rightDepth), Math.min(topDepth, bottomDepth));

        // Resolve collision based on minimum penetration with buffer for clean
        // separation
        if (minDepth == leftDepth && leftDepth > 0) {
            // Entity hit wall from the right side
            x = wallLeft - hitboxWidth / 2 - COLLISION_BUFFER;
            // Stop velocity in collision direction and opposing acceleration
            if (velocity.getX() > 0)
                velocity.setX(0);
            if (acceleration.getX() > 0)
                acceleration.setX(0);
        } else if (minDepth == rightDepth && rightDepth > 0) {
            // Entity hit wall from the left side
            x = wallRight + hitboxWidth / 2 + COLLISION_BUFFER;
            // Stop velocity in collision direction and opposing acceleration
            if (velocity.getX() < 0)
                velocity.setX(0);
            if (acceleration.getX() < 0)
                acceleration.setX(0);
        } else if (minDepth == topDepth && topDepth > 0) {
            // Entity hit wall from below
            y = wallTop - hitboxHeight / 2 - COLLISION_BUFFER;
            // Stop velocity in collision direction and opposing acceleration
            if (velocity.getY() > 0)
                velocity.setY(0);
            if (acceleration.getY() > 0)
                acceleration.setY(0);
        } else if (minDepth == bottomDepth && bottomDepth > 0) {
            // Entity hit wall from above
            y = wallBottom + hitboxHeight / 2 + COLLISION_BUFFER;
            // Stop velocity in collision direction and opposing acceleration
            if (velocity.getY() < 0)
                velocity.setY(0);
            if (acceleration.getY() < 0)
                acceleration.setY(0);
        }
    }// Getters and setters

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public void setVelocity(double x, double y) {
        this.velocity.set(x, y);
    }

    public void setAcceleration(double x, double y) {
        this.acceleration.set(x, y);
    }

    public double[] box() {
        double entityLeft = x - hitboxWidth / 2;
        double entityRight = x + hitboxWidth / 2;
        double entityTop = y - hitboxHeight / 2;
        double entityBottom = y + hitboxHeight / 2;

        return new double[] { entityLeft, entityRight, entityTop, entityBottom };
    }

    /**
     * Set the position of this entity
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get sprite width
     */
    public double getSpriteWidth() {
        return spriteWidth;
    }

    /**
     * Set sprite width
     */
    public void setSpriteWidth(double spriteWidth) {
        this.spriteWidth = spriteWidth;
    }

    /**
     * Get sprite height
     */
    public double getSpriteHeight() {
        return spriteHeight;
    }

    /**
     * Set sprite height
     */
    public void setSpriteHeight(double spriteHeight) {
        this.spriteHeight = spriteHeight;
    }

    /**
     * Set both sprite dimensions
     */
    public void setSpriteSize(double width, double height) {
        this.spriteWidth = width;
        this.spriteHeight = height;
    }

    /**
     * Get hitbox width
     */
    public double getHitboxWidth() {
        return hitboxWidth;
    }

    /**
     * Set hitbox width
     */
    public void setHitboxWidth(double hitboxWidth) {
        this.hitboxWidth = hitboxWidth;
        this.width = hitboxWidth; // Update GameObject width
    }

    /**
     * Get hitbox height
     */
    public double getHitboxHeight() {
        return hitboxHeight;
    }

    /**
     * Set hitbox height
     */
    public void setHitboxHeight(double hitboxHeight) {
        this.hitboxHeight = hitboxHeight;
        this.height = hitboxHeight; // Update GameObject height
    }

    /**
     * Set both hitbox dimensions
     */
    public void setHitboxSize(double width, double height) {
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.width = width; // Update GameObject width
        this.height = height; // Update GameObject height
    }
}
