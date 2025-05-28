/**
 * A class representing a 2D vector with double precision
 */
public class Vector2D {
    private double x;
    private double y;

    /**
     * Create a new vector with default coordinates (0,0)
     */
    public Vector2D() {
        this(0, 0);
    }

    /**
     * Create a new vector with the specified coordinates
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor
     */
    public Vector2D(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Set new coordinates for this vector
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Add another vector to this vector
     */
    public void add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    /**
     * Add x and y values to this vector
     */
    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    /**
     * Scale this vector by a constant factor
     */
    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
    }

    /**
     * Calculate the distance to another vector
     */
    public double distanceTo(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculate the angle in radians between this vector and the positive x-axis
     */
    public double angle() {
        return Math.atan2(y, x);
    }

    /**
     * Calculate the angle between this point and another point (treating both as
     * positions)
     */
    public double angleTo(Vector2D other) {
        return Math.atan2(other.y - this.y, other.x - this.x);
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

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
