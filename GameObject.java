import java.awt.*;

/**
 * Base abstract class for all game objects with common functionality
 */
public abstract class GameObject {
    protected double x; // x position
    protected double y; // y position
    protected double width; // width of hitbox
    protected double height; // height of hitbox
    protected boolean active = true; // whether this object is active

    /**
     * Create a new game object with position and size
     */
    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Update the game object state. To be implemented by subclasses.
     */
    public abstract void update();

    /**
     * Draw the game object, to be implemented by subclasses
     */
    public abstract void draw(Graphics g);

    /**
     * Check if this object is colliding with another game object
     */
    public boolean isColliding(GameObject other) {
        // Basic AABB collision detection
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    /**
     * Check if a point is inside this game object
     */
    public boolean containsPoint(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width &&
                pointY >= y && pointY <= y + height;
    }

    // Getters and setters
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
