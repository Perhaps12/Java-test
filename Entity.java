import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Base class for game entities with sprite rendering, collision handling and
 * physics
 */
public abstract class Entity extends GameObject {
    // Image and rendering
    protected BufferedImage sprite;
    protected String spritePath;
    protected double hitboxWidth;
    protected double hitboxHeight;

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
                System.out.println("Could not load sprite: " + spritePath);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (active && sprite != null) {
            g.drawImage(sprite, (int) (x - width / 2), (int) (y - height / 2), null);
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
     * Check if this entity is colliding with a wall
     */
    protected boolean isCollidingWithWall(Wall wall) {
        double entityLeft = x - hitboxWidth / 2;
        double entityRight = x + hitboxWidth / 2;
        double entityTop = y - hitboxHeight / 2;
        double entityBottom = y + hitboxHeight / 2;

        double wallLeft = wall.getX();
        double wallRight = wall.getX() + wall.getWidth();
        double wallTop = wall.getY();
        double wallBottom = wall.getY() + wall.getHeight();

        return entityRight > wallLeft &&
                entityLeft < wallRight &&
                entityBottom > wallTop &&
                entityTop < wallBottom;
    }

    /**
     * Handle collision with a wall by repositioning the entity
     */
    protected void handleWallCollision(Wall wall) {
        if (!isCollidingWithWall(wall))
            return;

        double entityLeft = x - hitboxWidth / 2;
        double entityRight = x + hitboxWidth / 2;
        double entityTop = y - hitboxHeight / 2;
        double entityBottom = y + hitboxHeight / 2;

        double wallLeft = wall.getX();
        double wallRight = wall.getX() + wall.getWidth();
        double wallTop = wall.getY();
        double wallBottom = wall.getY() + wall.getHeight();

        // Calculate collision depths
        double leftDepth = entityRight - wallLeft;
        double rightDepth = wallRight - entityLeft;
        double topDepth = entityBottom - wallTop;
        double bottomDepth = wallBottom - entityTop;

        // Find smallest penetration depth
        double minDepth = Math.min(Math.min(leftDepth, rightDepth), Math.min(topDepth, bottomDepth));

        // find collision based on minimum penetration
        if (minDepth == leftDepth) {
            x = wallLeft - hitboxWidth / 2;
            velocity.setX(0); // Stop horizontal movement
        } else if (minDepth == rightDepth) {
            x = wallRight + hitboxWidth / 2;
            velocity.setX(0); // Stop horizontal movement
        } else if (minDepth == topDepth) {
            y = wallTop - hitboxHeight / 2;
            velocity.setY(0); // Stop vertical movement
        } else if (minDepth == bottomDepth) {
            y = wallBottom + hitboxHeight / 2;
            velocity.setY(0); // Stop vertical movement
        }
    } // Getters and setters

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

    /**
     * Set the position of this entity
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
