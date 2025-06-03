import java.awt.*;

/**
 * Wall class representing obstacles in the game world
 */
public class Wall extends GameObject {
    private Color color = Color.BLACK;
    private float alpha = 1.0f; // Full opacity by default

    /**
     * Create a new wall with position and size
     */
    public Wall(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Create a new wall with position, size and color
     */
    public Wall(double x, double y, double width, double height, Color color) {
        super(x, y, width, height);
        this.color = color;
    }

    /**
     * Create a new wall with position, size, color and transparency
     */
    public Wall(double x, double y, double width, double height, Color color, float alpha) {
        super(x, y, width, height);
        this.color = color;
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha)); // Clamp alpha between 0 and 1
    }

    @Override
    public void update() {
        // Walls don't need updating
    }

    @Override
    public void draw(Graphics g) {
        if (!active)
            return;

        Graphics2D g2d = (Graphics2D) g;

        // Apply transparency if alpha is less than 1.0
        if (alpha < 1.0f) {
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g2d.setColor(color);
            g2d.fillRect((int) x, (int) y, (int) width, (int) height);

            g2d.setComposite(originalComposite);
        } else {
            g.setColor(color);
            g.fillRect((int) x, (int) y, (int) width, (int) height);
        }
    }

    // Getters and setters
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }
}
