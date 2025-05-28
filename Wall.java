import java.awt.*;

/**
 * Wall class representing obstacles in the game world
 */
public class Wall extends GameObject {
    private Color color = Color.BLACK;

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

    @Override
    public void update() {
        // Walls don't need updating
    }

    @Override
    public void draw(Graphics g) {
        if (!active)
            return;

        g.setColor(color);
        g.fillRect((int) x, (int) y, (int) width, (int) height);
    }
}
