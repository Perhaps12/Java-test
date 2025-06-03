import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * WaterBoundary class that creates a realistic liquid water effect
 * acting as the boundary between top and bottom game sections.
 * Features realistic physics simulation with water displacement,
 * surface tension, waves, and particle effects.
 */
public class WaterBoundary {
    private static WaterBoundary instance;

    // Water boundary properties
    private static final double WATER_LEVEL = 0.0; // Y coordinate of water surface (center line)
    private static final double WATER_DEPTH = 540.0; // How deep the water effect extends
    private static final int WATER_SEGMENTS = 150; // Number of segments for water surface
    private ArrayList<WaterSegment> waterSegments;
    private ArrayList<WaterParticle> waterParticles;
    private ArrayList<WaterSplash> splashes;
    private long lastUpdateTime; // Player interaction tracking

    /**
     * Individual water segment for random surface simulation
     */
    private static class WaterSegment {
        public double x;
        public double y;
        public double baseY;
        public double randomOffset;
        public double randomVelocity;
        public double randomPhase;
        public double displacement;
        public double displacementDecay; // Random water properties
        private static final double MAX_RANDOM_OFFSET = 1.1; // Increased for more visible movement
        private static final double RANDOM_SPEED = 0.05; // Increased speed of random movement
        private static final double DISPLACEMENT_DECAY = 0.95; // How quickly displacement fades (slower decay)
        private static final double MAX_DISPLACEMENT = 30.0; // Increased maximum displacement

        public WaterSegment(double x, double y) {
            this.x = x;
            this.y = y;
            this.baseY = y;
            this.randomOffset = 0;
            this.randomVelocity = (Math.random() - 0.5) * 0.2; // Increased initial velocity
            this.randomPhase = Math.random() * Math.PI * 4; // Random starting phase
            this.displacement = 0;
            this.displacementDecay = DISPLACEMENT_DECAY;
        }

        public void update() {
            // Update random phase for sine wave movement
            randomPhase += RANDOM_SPEED + randomVelocity;

            // Generate random offset using sine wave with noise
            randomOffset = Math.sin(randomPhase) * MAX_RANDOM_OFFSET;

            // Add some noise for more chaotic movement
            if (Math.random() < 0.10) { // 5% chance for random impulse
                randomOffset += (Math.random() - 0.5) * 2.0;
            }

            // Update displacement from player interactions
            displacement *= displacementDecay;

            // Calculate final position
            y = baseY + randomOffset + displacement;

            // Occasionally change velocity for variety
            if (Math.random() < 0.01) {
                randomVelocity += (Math.random() - 0.5) * 0.05;
                randomVelocity = Math.max(-0.1, Math.min(0.1, randomVelocity));
            }
        }

        public void applyDisplacement(double displacementAmount) {
            // Add player interaction displacement with some randomness
            double oldDisplacement = displacement;
            displacement += displacementAmount * (0.8 + Math.random() * 0.4);
            displacement = Math.max(-MAX_DISPLACEMENT, Math.min(MAX_DISPLACEMENT, displacement));

            // Debug output for significant displacements
            if (Math.abs(displacementAmount) > 1.0) {
                System.out.println("Segment at X: " + x + " displaced by " + displacementAmount +
                        " (old: " + oldDisplacement + ", new: " + displacement + ")");
            }
        }
    }

    /**
     * Water particle for splash effects
     */
    private static class WaterParticle {
        public double x, y;
        public double vx, vy;
        public double life;
        public double maxLife;
        public Color color;
        public int gravitySwap; // Track gravity direction for this particle

        private static final double GRAVITY = 0.4; // Gravity strength
        private static final double AIR_RESISTANCE = 0.995; // Reduced air resistance for longer movement

        public WaterParticle(double x, double y, double vx, double vy, double life, int gravitySwap) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.gravitySwap = gravitySwap;

            // Water blue color with transparency
            int alpha = (int) (180 + Math.random() * 75);
            this.color = new Color(100, 150, 255, alpha);
        }

        public void update() {
            // Apply physics with gravity direction based on swap
            // Normal gravity (swap = 1): gravity pulls down (positive Y)
            // Inverted gravity (swap = -1): gravity pulls up (negative Y)
            vy += GRAVITY * gravitySwap; // Gravity direction depends on swap
            vx *= AIR_RESISTANCE;
            vy *= AIR_RESISTANCE;

            // Update position
            x += vx;
            y += vy;

            // Decrease life
            life--;
        }

        public boolean isAlive() {
            return life > 0;
        }

        public double getOpacity() {
            return Math.max(0, life / maxLife);
        }
    }

    /**
     * Water splash effect for major water interactions
     */
    private static class WaterSplash {
        public double centerX, centerY;
        public double radius;
        public double maxRadius;
        public long creationTime;

        private static final long SPLASH_LIFETIME = 1500; // 1.5 seconds
        private static final double EXPANSION_SPEED = 80.0; // pixels per second

        public WaterSplash(double x, double y, double intensity) {
            this.centerX = x;
            this.centerY = y;
            this.radius = 0;
            this.maxRadius = 40 + intensity * 20; // Larger splashes for more intense impacts
            this.creationTime = System.currentTimeMillis();
        }

        public void update(double deltaTime) {
            radius += EXPANSION_SPEED * deltaTime;
        }

        public boolean isExpired() {
            long age = System.currentTimeMillis() - creationTime;
            return age > SPLASH_LIFETIME || radius > maxRadius;
        }

        public double getOpacity() {
            long age = System.currentTimeMillis() - creationTime;
            double ageRatio = (double) age / SPLASH_LIFETIME;
            return Math.max(0, 0.8 * (1.0 - ageRatio));
        }
    }

    /**
     * Private constructor for singleton pattern
     */
    private WaterBoundary() {
        waterSegments = new ArrayList<>();
        waterParticles = new ArrayList<>();
        splashes = new ArrayList<>();
        lastUpdateTime = System.currentTimeMillis();

        initializeWaterSegments();
    }

    /**
     * Get the singleton instance
     */
    public static WaterBoundary getInstance() {
        if (instance == null) {
            instance = new WaterBoundary();
        }
        return instance;
    }

    /**
     * Initialize water segments across the level width
     */
    private void initializeWaterSegments() {
        GameSettings settings = GameSettings.getInstance();
        double levelLeft = settings.getLevelLeft();
        double levelRight = settings.getLevelRight();
        double levelWidth = levelRight - levelLeft;

        double segmentWidth = levelWidth / WATER_SEGMENTS;

        for (int i = 0; i < WATER_SEGMENTS; i++) {
            double x = levelLeft + i * segmentWidth;
            WaterSegment segment = new WaterSegment(x, WATER_LEVEL);
            waterSegments.add(segment);
        }
    }

    /**
     * Update water physics and effects
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
        lastUpdateTime = currentTime;

        updateWaterSegments();
        updateWaterParticles();
        updateSplashes(deltaTime);
        spreadWaves();
    }

    /**
     * Create water entry effect when player crosses boundary
     */
    public void createWaterEntry(double playerX, double playerY, double impact, int gravitySwap) { // must be public for
                                                                                                   // player effects
        splashes.add(new WaterSplash(playerX, WATER_LEVEL, impact * 0.8));
        displaceWaterSegments(playerX, impact * 4.0, 60, gravitySwap);
        createWaterParticles(playerX, WATER_LEVEL, impact * 0.8, (int) impact, gravitySwap);
    }/*
      * Displace water segments around a point
      */

    private void displaceWaterSegments(double centerX, double force, double radius, int gravitySwap) {
        for (WaterSegment segment : waterSegments) {
            double distance = Math.abs(segment.x - centerX);
            if (distance < radius) {
                // Calculate displacement based on distance (closer more displacement)
                double falloff = 1.0 - (distance / radius);
                double displacement = force * falloff * falloff; // Quadratic falloff

                // Mirror displacement direction based on gravity swap
                displacement *= gravitySwap; // Positive displacement since when swapping it will invert gravity 
                // before it actually displaces, so when landing we will apply a negative swap

                segment.applyDisplacement(displacement);
            }
        }
    }

    /**
     * Create water particles for splash effects
     */
    private void createWaterParticles(double centerX, double centerY, double intensity, int count, int gravitySwap) {
        for (int i = 0; i < count; i++) {
            // Random position spread around center
            double offsetX = (Math.random() - 0.5) * 25;
            double offsetY = (Math.random() - 0.5) * 15;

            // Random velocity with reduced scattering
            double vx = (Math.random() - 0.5) * intensity * 0.8;

            // Modify vertical velocity based on gravity swap
            double vy;
            if (gravitySwap == 1) {
                vy = -(Math.random() * intensity * 0.8 + intensity * 0.3); // Upward bias
            } else {
                vy = (Math.random() * intensity * 0.8 + intensity * 0.3); // Downward bias
            }

            // Particle lifetime
            double life = 60 + Math.random() * 90; // 1-2.5 seconds at 60fps

            WaterParticle particle = new WaterParticle(
                    centerX + offsetX,
                    centerY + offsetY,
                    vx,
                    vy,
                    life,
                    gravitySwap);

            waterParticles.add(particle);
        }
    }

    /**
     * Update all water segments
     */
    private void updateWaterSegments() {
        for (WaterSegment segment : waterSegments) {
            segment.update();
        }
    }

    /**
     * Update all water particles
     */
    private void updateWaterParticles() {
        Iterator<WaterParticle> iterator = waterParticles.iterator();
        while (iterator.hasNext()) {
            WaterParticle particle = iterator.next();
            particle.update();

            if (!particle.isAlive()) {
                iterator.remove();
            }
        }
    }

    /**
     * Update all splash effects
     */
    private void updateSplashes(double deltaTime) {
        Iterator<WaterSplash> iterator = splashes.iterator();
        while (iterator.hasNext()) {
            WaterSplash splash = iterator.next();
            splash.update(deltaTime);

            if (splash.isExpired()) {
                iterator.remove();
            }
        }
    }

    /**
     * Create random wave patterns between segments
     */
    private void spreadWaves() {
        if (waterSegments.size() < 2)
            return;

        // Create random wave influences between neighboring segments
        for (int i = 1; i < waterSegments.size() - 1; i++) {
            WaterSegment current = waterSegments.get(i);
            WaterSegment left = waterSegments.get(i - 1);
            WaterSegment right = waterSegments.get(i + 1);

            // Add slight influence from neighboring segments for more wave-like patterns
            if (Math.random() < 0.02) { // 2% chance per frame
                double influence = ((left.randomOffset + right.randomOffset) / 2) * 0.1;
                current.displacement += influence;
            }

            // Add random disturbances for natural wave movement
            if (Math.random() < 0.03) { // 3% chance per segment per frame
                current.displacement += (Math.random() - 0.5) * 0.5;
            }

            // Add occasional larger disturbances for more interesting patterns
            if (Math.random() < 0.005) { // 0.5% chance for larger disturbance
                current.displacement += (Math.random() - 0.5) * 1.2;
            }
        }
    }

    /**
     * Draw the water boundary effect
     */
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawWaterSurface(g);
        drawWaterParticles(g);
        drawSplashes(g);
        drawWaterDepth(g);
    }

    /**
     * Draw the animated water surface
     */
    private void drawWaterSurface(Graphics g2d) {
        Graphics2D g = (Graphics2D) g2d;

        if (waterSegments.size() < 2)
            return;

        // Create water surface path
        GeneralPath waterPath = new GeneralPath();

        // Start path
        WaterSegment first = waterSegments.get(0);
        waterPath.moveTo(first.x, first.y);

        // Create smooth curve through all segments using quadratic curves
        for (int i = 1; i < waterSegments.size(); i++) {
            WaterSegment current = waterSegments.get(i);

            if (i == waterSegments.size() - 1) {
                // Last segment
                waterPath.lineTo(current.x, current.y);
            } else {
                WaterSegment next = waterSegments.get(i + 1); // smooth curve to next segment
                double controlX = current.x;
                double controlY = current.y;
                double endX = (current.x + next.x) / 2;
                double endY = (current.y + next.y) / 2;

                waterPath.quadTo(controlX, controlY, endX, endY);
            }
        }

        // Close path for filling (extend down for water body)
        WaterSegment last = waterSegments.get(waterSegments.size() - 1);
        waterPath.lineTo(last.x, WATER_LEVEL + WATER_DEPTH);
        waterPath.lineTo(first.x, WATER_LEVEL + WATER_DEPTH);
        waterPath.closePath();

        // Create water gradient
        GradientPaint gradient = new GradientPaint(
                0, (float) WATER_LEVEL, new Color(100, 150, 255, 120),
                0, (float) (WATER_LEVEL + WATER_DEPTH), new Color(50, 100, 200, 180));

        // Fill water body
        g.setPaint(gradient);
        g.fill(waterPath);

        // Draw water surface line
        g.setColor(new Color(80, 130, 255, 200));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        GeneralPath surfaceLine = new GeneralPath();
        surfaceLine.moveTo(first.x, first.y);
        for (int i = 1; i < waterSegments.size(); i++) {
            WaterSegment current = waterSegments.get(i);
            surfaceLine.lineTo(current.x, current.y);
        }
        g.draw(surfaceLine);
    }

    /**
     * Draw water particles
     */
    private void drawWaterParticles(Graphics2D g) {
        for (WaterParticle particle : waterParticles) {
            double opacity = particle.getOpacity();
            if (opacity <= 0)
                continue;

            // Set particle transparency
            AlphaComposite originalComposite = (AlphaComposite) g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));

            // Draw particle as small circle with enhanced visibility
            g.setColor(particle.color);
            int size = (int) (6 + opacity * 6); // Increased size for better visibility
            g.fillOval((int) (particle.x - size / 2), (int) (particle.y - size / 2), size, size);

            // Add a white highlight for better visibility
            g.setColor(new Color(255, 255, 255, (int) (opacity * 100)));
            int highlightSize = size / 2;
            g.fillOval((int) (particle.x - highlightSize / 2), (int) (particle.y - highlightSize / 2), highlightSize,
                    highlightSize);

            g.setComposite(originalComposite);
        }
    }

    /**
     * Draw splash effects
     */
    private void drawSplashes(Graphics2D g) {
        for (WaterSplash splash : splashes) {
            double opacity = splash.getOpacity();
            if (opacity <= 0)
                continue;

            // Set splash transparency
            AlphaComposite originalComposite = (AlphaComposite) g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));

            // Draw splash as expanding ring
            g.setColor(new Color(150, 200, 255, (int) (opacity * 150)));
            g.setStroke(new BasicStroke(2.0f * (float) opacity, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int diameter = (int) (splash.radius * 2);
            g.drawOval(
                    (int) (splash.centerX - splash.radius),
                    (int) (splash.centerY - splash.radius),
                    diameter,
                    diameter);

            g.setComposite(originalComposite);
        }
    }

    /**
     * Draw water depth and reflection effects
     */
    private void drawWaterDepth(Graphics2D g) {
        GameSettings settings = GameSettings.getInstance();
        double levelLeft = settings.getLevelLeft();
        double levelRight = settings.getLevelRight();

        // Draw subtle reflection/shimmer lines
        g.setColor(new Color(200, 230, 255, 40));
        g.setStroke(new BasicStroke(1.0f));

        for (int i = 0; i < 5; i++) {
            double y = WATER_LEVEL + 10 + i * 8;
            double offset = Math.sin(System.currentTimeMillis() * 0.002 + i) * 3;

            g.drawLine(
                    (int) levelLeft,
                    (int) (y + offset),
                    (int) levelRight,
                    (int) (y + offset));
        }

        // Draw water depth gradient overlay
        GradientPaint depthGradient = new GradientPaint(
                0, (float) WATER_LEVEL, new Color(0, 0, 0, 0),
                0, (float) (WATER_LEVEL + WATER_DEPTH), new Color(0, 50, 100, 60));

        g.setPaint(depthGradient);
        g.fillRect(
                (int) levelLeft,
                (int) WATER_LEVEL,
                (int) (levelRight - levelLeft),
                (int) WATER_DEPTH);
    }

    /**
     * Get the water level Y coordinate
     */
    public static double getWaterLevel() {
        return WATER_LEVEL;
    }

    /**
     * Check if a point is in the water
     */
    public boolean isPointInWater(double x, double y) {
        return Math.abs(y - WATER_LEVEL) < WATER_DEPTH / 2;
    }

    /**
     * Get the number of active water particles
     */
    public int getParticleCount() {
        return waterParticles.size();
    }

    /**
     * Clear all water effects (useful for level transitions)
     */
    public void clearEffects() {
        waterParticles.clear();
        splashes.clear();

        // Reset all water segments to neutral position
        for (WaterSegment segment : waterSegments) {
            segment.y = WATER_LEVEL;
            segment.baseY = WATER_LEVEL;
            segment.randomOffset = 0;
            segment.displacement = 0;
            segment.randomPhase = Math.random() * Math.PI * 2; // Reset with new random phase
        }
    }
    // public void testWaterEffects() {
    // System.out.println("Testing water effects...");

    // createWaterParticles(0, 0, 10.0, 20);
    // createWaterParticles(100, 0, 8.0, 15);
    // createWaterParticles(-100, 0, 8.0, 15);

    // splashes.add(new WaterSplash(0, WATER_LEVEL, 5.0));
    // displaceWaterSegments(0, 5.0, 100);
    // }
}
