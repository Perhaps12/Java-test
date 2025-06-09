import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.*;

/**
 * WaterBoundary class that manages water effects and physics
 */
public class WaterBoundary {
    private static WaterBoundary instance; // Water boundary properties
    private static final double WATER_LEVEL = 0.0; // Y coordinate of water surface (center line)
    private static final double WATER_DEPTH = 580.0; // How deep the water effect extends
    private static final int WATER_SEGMENTS = 150; // Number of segments for water surface
    private ArrayList<WaterSegment> waterSegments;
    private ArrayList<WaterParticle> waterParticles;
    private ArrayList<WaterSplash> splashes;
    private long lastUpdateTime; // Player interaction tracking    // Performance optimization constants
    private static final int MAX_PARTICLES = 150; // Maximum number of water particles
    private static final int MAX_SPLASHES = 20; // Maximum number of splash effects
    private static final int PARTICLE_CLEANUP_THRESHOLD = 120; // Start aggressive cleanup at this count
    private static final int BATCH_CLEANUP_SIZE = 10; // Number of particles to remove in one pass when over threshold// Water texture properties
    private BufferedImage waterTexture;
    private boolean textureLoaded = false; // Frame timing for shimmer effect
    // animations)
    private int shimmerAnimationTimer = 0;
    private static final int SHIMMER_ANIMATION_SPEED = 6; // Update every 8 frames to match character walk cycle
    private int shimmerFrame = 0; // Frame counter

    // Store random pattern for shimmer effect that only updates every few frames
    private boolean[][] shimmerPattern;
    private int shimmerPatternWidth = 0;
    private int shimmerPatternHeight = 0;

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
        private static final double DISPLACEMENT_DECAY = 0.95; // How quickly displacement decays
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
            // double oldDisplacement = displacement;
            displacement += displacementAmount * (0.8 + Math.random() * 0.4);
            displacement = Math.max(-MAX_DISPLACEMENT, Math.min(MAX_DISPLACEMENT, displacement));

            // System.out.println("segment X: " + x + " displaced " + displacementAmount +
            // " (old: " + oldDisplacement + ", new: " + displacement + ")");
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
            this.gravitySwap = gravitySwap; // Water pink color with transparency
            int alpha = (int) (180 + Math.random() * 75);
            this.color = new Color(240, 142, 254, alpha);

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

        // Initialize frame-based timing for shimmer effect
        shimmerAnimationTimer = 0;
        shimmerFrame = 0;

        initializeWaterSegments();
        loadWaterTexture();
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
     * Load the pink water texture
     */
    private void loadWaterTexture() {
        try {
            waterTexture = ImageIO.read(getClass().getResourceAsStream("/textures/Pink water.png"));
            textureLoaded = true;
            System.out.println("load pink water texture successfully");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Could not load pink water texture: " + e.getMessage());
            textureLoaded = false;
            waterTexture = null;
        }
    }

    /**
     * Initialize water segments across the level width
     */
    private void initializeWaterSegments() {
        GameSettings settings = GameSettings.getInstance();
        double levelLeft = settings.getLevelLeft();
        double levelRight = settings.getLevelRight() + 50; // extend by 50 because goofy coordinate system lol
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

        // Update frame-based timing for shimmer effect (same pattern as Player
        // animation)
        shimmerAnimationTimer++;
        if (shimmerAnimationTimer >= SHIMMER_ANIMATION_SPEED) {
            shimmerAnimationTimer = 0;
            shimmerFrame++;
        }        updateWaterSegments();
        updateWaterParticles();
        updateSplashes(deltaTime);
        spreadWaves();
        
        // Proactive performance management
        performProactiveCleanup();
    }
    
    /**
     * Proactive cleanup to maintain performance during heavy particle usage
     */
    private void performProactiveCleanup() {
        // If we have too many particles, remove the weakest ones proactively
        if (waterParticles.size() > PARTICLE_CLEANUP_THRESHOLD) {
            Iterator<WaterParticle> iterator = waterParticles.iterator();
            int removed = 0;
            int targetRemoval = (waterParticles.size() - PARTICLE_CLEANUP_THRESHOLD) / 2;
            
            while (iterator.hasNext() && removed < targetRemoval) {
                WaterParticle particle = iterator.next();
                // Remove particles with very low opacity or short remaining life
                if (particle.getOpacity() < 0.4 || particle.life < particle.maxLife * 0.3) {
                    iterator.remove();
                    removed++;
                }
            }
        }
        
        // Similar cleanup for splashes
        if (splashes.size() > MAX_SPLASHES * 0.75) {
            Iterator<WaterSplash> iterator = splashes.iterator();
            int removed = 0;
            int targetRemoval = (int)((splashes.size() - MAX_SPLASHES * 0.6) / 2);
            
            while (iterator.hasNext() && removed < targetRemoval) {
                WaterSplash splash = iterator.next();
                // Remove splashes with low opacity
                if (splash.getOpacity() < 0.3) {
                    iterator.remove();
                    removed++;
                }
            }
        }
    }    /**
     * Create water entry effect when player crosses boundary with optimized limits
     */
    public void createWaterEntry(double playerX, double playerY, double impact, int gravitySwap) { // must be public for
                                                                                                   // player effects
        // More conservative splash creation to prevent lag
        if (splashes.size() < MAX_SPLASHES * 0.8) { // Only create if below 80% of limit
            splashes.add(new WaterSplash(playerX, WATER_LEVEL, impact * 0.8));
        }

        displaceWaterSegments(playerX, impact * 4.0, 60, gravitySwap);

        // Further reduce particle count for character swaps to prevent lag
        int baseParticleCount = Math.min((int) impact, 12); // Cap at 12 particles instead of 15
        
        // Reduce particle count even more if we're close to limits
        if (waterParticles.size() > PARTICLE_CLEANUP_THRESHOLD) {
            baseParticleCount = Math.min(baseParticleCount, 5);
        }
        
        createWaterParticles(playerX, WATER_LEVEL, impact * 0.8, baseParticleCount, gravitySwap);
    }/*
       * Displace water segments around a point with wave-like propagation
       */

    private void displaceWaterSegments(double centerX, double force, double radius, int gravitySwap) {
        for (WaterSegment segment : waterSegments) {
            double distance = Math.abs(segment.x - centerX);
            if (distance < radius) {
                // Calculate displacement based on distance (closer = more displacement)
                double falloff = 1.0 - (distance / radius);
                double displacement = force * falloff * falloff; // Quadratic falloff

                // Mirror displacement direction based on gravity swap
                displacement *= gravitySwap;

                segment.applyDisplacement(displacement);
            }
        }

        // Add secondary wave effects for more realistic water behavior
        // Create outward ripples from the impact point
        for (int ripple = 1; ripple <= 2; ripple++) {
            double rippleRadius = radius * (1.0 + ripple * 0.3); // Expanding ripples
            double rippleForce = force * (0.3 / ripple); // Diminishing force

            for (WaterSegment segment : waterSegments) {
                double distance = Math.abs(segment.x - centerX);
                if (distance > radius && distance < rippleRadius) {
                    double rippleFalloff = 1.0 - ((distance - radius) / (rippleRadius - radius));
                    double rippleDisplacement = rippleForce * rippleFalloff * gravitySwap * 0.5;

                    // Alternate the direction for wave-like effect
                    if (ripple % 2 == 0) {
                        rippleDisplacement *= -0.6; // Opposite direction, reduced amplitude
                    }

                    segment.applyDisplacement(rippleDisplacement);
                }
            }
        }
    }    /**
     * Create water particles for splash effects with improved limits
     */
    private void createWaterParticles(double centerX, double centerY, double intensity, int count, int gravitySwap) {
        // Enforce stricter particle limits to prevent performance issues
        int availableSlots = MAX_PARTICLES - waterParticles.size();
        if (availableSlots <= 0) return; // Skip if we're at the limit
        
        // Reduce particle count based on current load
        int actualCount = Math.min(count, availableSlots);
        
        // If we're close to the limit, reduce the count even further
        if (waterParticles.size() > PARTICLE_CLEANUP_THRESHOLD) {
            actualCount = Math.min(actualCount, 5); // Only allow 5 new particles when near limit
        }
        
        if (actualCount <= 0) return;

        for (int i = 0; i < actualCount; i++) {
            // Random position spread around centers
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

            // Reduced particle lifetime for better performance and reduced visual clutter
            double life = 35 + Math.random() * 50; // 0.6-1.4 seconds instead of 0.75-1.75 seconds

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
    }    /**
     * Update all water particles with optimized cleanup
     */
    private void updateWaterParticles() {
        // First, update all particles
        for (WaterParticle particle : waterParticles) {
            particle.update();
        }
        
        // Then remove expired particles using iterator for safe removal
        Iterator<WaterParticle> iterator = waterParticles.iterator();
        int removedCount = 0;
        int targetRemoveCount = 0;
        
        // Calculate how many particles we need to remove for performance
        if (waterParticles.size() > PARTICLE_CLEANUP_THRESHOLD) {
            targetRemoveCount = Math.min(BATCH_CLEANUP_SIZE, waterParticles.size() - PARTICLE_CLEANUP_THRESHOLD);
        }
        
        while (iterator.hasNext()) {
            WaterParticle particle = iterator.next();
            
            // Always remove if particle is dead
            if (!particle.isAlive()) {
                iterator.remove();
                removedCount++;
                continue;
            }
            
            // If we're over the threshold, remove particles more aggressively
            if (waterParticles.size() > MAX_PARTICLES) {
                // Remove particles with low opacity or low life remaining
                if (particle.getOpacity() < 0.3 || particle.life < particle.maxLife * 0.2) {
                    iterator.remove();
                    removedCount++;
                    continue;
                }
            }
            
            // If we still need to remove more particles for performance, target oldest/weakest
            if (removedCount < targetRemoveCount && (particle.getOpacity() < 0.5 || particle.life < particle.maxLife * 0.4)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        // Emergency cleanup if we're still over the absolute limit
        if (waterParticles.size() > MAX_PARTICLES) {
            // Remove particles from the beginning of the list (oldest particles)
            int toRemove = waterParticles.size() - MAX_PARTICLES;
            for (int i = 0; i < toRemove && !waterParticles.isEmpty(); i++) {
                waterParticles.remove(0);
            }
        }
    }    /**
     * Update all splash effects with optimized cleanup
     */
    private void updateSplashes(double deltaTime) {
        // First, update all splashes
        for (WaterSplash splash : splashes) {
            splash.update(deltaTime);
        }
        
        // Then remove expired splashes using iterator for safe removal
        Iterator<WaterSplash> iterator = splashes.iterator();
        int removedCount = 0;
        int targetRemoveCount = 0;
        
        // Calculate how many splashes we need to remove for performance
        if (splashes.size() > MAX_SPLASHES * 0.8) { // Start cleanup earlier for splashes
            targetRemoveCount = Math.min(5, splashes.size() - (int)(MAX_SPLASHES * 0.7));
        }
        
        while (iterator.hasNext()) {
            WaterSplash splash = iterator.next();
            
            // Always remove if splash is expired
            if (splash.isExpired()) {
                iterator.remove();
                removedCount++;
                continue;
            }
            
            // If we're near the limit, remove splashes more aggressively
            if (splashes.size() > MAX_SPLASHES) {
                if (splash.getOpacity() < 0.2) {
                    iterator.remove();
                    removedCount++;
                    continue;
                }
            }
            
            // If we still need to remove more splashes for performance
            if (removedCount < targetRemoveCount && splash.getOpacity() < 0.4) {
                iterator.remove();
                removedCount++;
            }
        }
        
        // Emergency cleanup if we're still over the absolute limit
        if (splashes.size() > MAX_SPLASHES) {
            // Remove oldest splashes
            int toRemove = splashes.size() - MAX_SPLASHES;
            for (int i = 0; i < toRemove && !splashes.isEmpty(); i++) {
                splashes.remove(0);
            }
        }
    }

    /**
     * Create wave-like displacement propagation between segments
     */
    private void spreadWaves() {
        if (waterSegments.size() < 2)
            return;

        // First pass propagate displacement between neighboring segments
        for (int i = 1; i < waterSegments.size() - 1; i++) {
            WaterSegment current = waterSegments.get(i);
            WaterSegment left = waterSegments.get(i - 1);
            WaterSegment right = waterSegments.get(i + 1);

            // Displacement difference between neighbors
            double leftDiff = left.displacement - current.displacement;
            double rightDiff = right.displacement - current.displacement;

            // Propagate displacement based on neighbors
            // Basically wave physics
            double propagationForce = (leftDiff + rightDiff) * 0.15;
            current.displacement += propagationForce;

            current.displacement *= 0.98; // damp
        }

        // Second pass: smooth out sharp displacement differences
        for (int i = 1; i < waterSegments.size() - 1; i++) {
            WaterSegment current = waterSegments.get(i);
            WaterSegment left = waterSegments.get(i - 1);
            WaterSegment right = waterSegments.get(i + 1);

            // Average displacement with neighbors for smoother wave propagation
            double averageDisplacement = (left.displacement + current.displacement + right.displacement) / 3.0;
            double smoothingFactor = 0.05; // How much to smooth
            current.displacement += (averageDisplacement - current.displacement) * smoothingFactor;
        }

        // Add subtle random disturbances for natural wave movement (reduced frequency)
        for (int i = 1; i < waterSegments.size() - 1; i++) {
            WaterSegment current = waterSegments.get(i);

            // Reduced random disturbances since we have better wave propagation
            if (Math.random() < 0.01) { // Reduced from 0.03 to 0.01
                current.displacement += (Math.random() - 0.5) * 0.3; // Reduced amplitude
            }

            // Very occasional larger disturbances
            if (Math.random() < 0.002) { // Reduced from 0.005 to 0.002
                current.displacement += (Math.random() - 0.5) * 0.8; // Reduced amplitude
            }
        }
    }

    /**
     * Draw the water boundary effect
     */
    public void draw(Graphics2D g) {
        // Use pixelated rendering for retro effect
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

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

        // Set pixelated rendering for water surface
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (waterSegments.size() < 2)
            return;

        // Create pixelated water surface using stepped lines instead of smooth curves
        GeneralPath waterPath = new GeneralPath();

        // Start path
        WaterSegment first = waterSegments.get(0);
        waterPath.moveTo(first.x, first.y);

        // Create stepped/pixelated surface instead of smooth curves
        for (int i = 1; i < waterSegments.size(); i++) {
            WaterSegment current = waterSegments.get(i);

            // Round Y position to create pixelated steps
            double pixelatedY = Math.round(current.y / 2) * 2;
            waterPath.lineTo(current.x, pixelatedY);
        }

        // Close path for filling (extend down for water body)
        WaterSegment last = waterSegments.get(waterSegments.size() - 1);
        waterPath.lineTo(last.x, WATER_LEVEL + WATER_DEPTH);
        waterPath.lineTo(first.x, WATER_LEVEL + WATER_DEPTH);
        waterPath.closePath();

        // Fill water body with texture or gradient fallback
        if (textureLoaded && waterTexture != null) {
            // Get the actual water area bounds - entire level width
            GameSettings settings = GameSettings.getInstance();
            double levelLeft = settings.getLevelLeft();
            double levelRight = settings.getLevelRight() + 50; // Match the water segments extension
            double levelWidth = levelRight - levelLeft;

            // Create texture paint that covers the entire level width
            // This will stretch/tile the texture across the whole map
            Rectangle2D textureRect = new Rectangle2D.Double(levelLeft, WATER_LEVEL, levelWidth, WATER_DEPTH);
            TexturePaint texturePaint = new TexturePaint(waterTexture, textureRect);            // Apply more transparency to blend with the water effects and show elements underneath
            AlphaComposite originalComposite = (AlphaComposite) g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            g.setPaint(texturePaint);
            g.fill(waterPath);

            g.setComposite(originalComposite);
        } else {
            // Fallback to pixelated gradient if texture loading failed
            int gradientSteps = 8; // Number of distinct color bands
            double stepHeight = WATER_DEPTH / gradientSteps;

            for (int i = 0; i < gradientSteps; i++) {
                float ratio = (float) i / gradientSteps;
                int red = (int) (255 - ratio * 55); // 255 to 200
                int green = (int) (100 - ratio * 50); // 100 to 50
                int blue = (int) (150 - ratio * 50); // 150 to 100
                int alpha = (int) (80 + ratio * 40); // 80 to 120 (reduced from 120-180 for more translucency)

                Color bandColor = new Color(red, green, blue, alpha);
                g.setColor(bandColor);

                Rectangle2D band = new Rectangle2D.Double(
                        first.x, WATER_LEVEL + i * stepHeight,
                        last.x - first.x, stepHeight + 1 // +1 to avoid gaps
                );
                g.fill(band);
            }
        }        // Draw pixelated water surface line with reduced opacity
        g.setColor(new Color(240, 142, 254, 120)); // Reduced alpha from 200 to 120
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        // Draw stepped surface line
        for (int i = 0; i < waterSegments.size() - 1; i++) {
            WaterSegment current = waterSegments.get(i);
            WaterSegment next = waterSegments.get(i + 1);

            // Round Y positions for pixelated effect
            double y1 = Math.round(current.y / 2) * 2;
            double y2 = Math.round(next.y / 2) * 2;
            g.drawLine((int) current.x, (int) y1, (int) next.x, (int) y2);
        }
    }

    /**
     * Draw water particles
     */
    private void drawWaterParticles(Graphics2D g) {
        // Set pixelated rendering for particles
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        for (WaterParticle particle : waterParticles) {
            double opacity = particle.getOpacity();
            if (opacity <= 0)
                continue;

            // Set particle transparency
            AlphaComposite originalComposite = (AlphaComposite) g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));

            // Draw particle as pixelated squares instead of circles
            g.setColor(particle.color);
            int size = (int) (4 + opacity * 4); // Reduced size for pixel effect

            // Make size even for clean pixel squares
            size = (size / 2) * 2;
            if (size < 2)
                size = 2;

            int x = (int) particle.x - size / 2;
            int y = (int) particle.y - size / 2;

            // Round to pixel grid for crisp edges
            x = (x / 2) * 2;
            y = (y / 2) * 2;

            g.fillRect(x, y, size, size);

            // Add a white highlight pixel in the center
            g.setColor(new Color(255, 255, 255, (int) (opacity * 150)));
            int highlightSize = Math.max(2, size / 2);
            highlightSize = (highlightSize / 2) * 2; // Make even

            int highlightX = x + (size - highlightSize) / 2;
            int highlightY = y + (size - highlightSize) / 2;

            g.fillRect(highlightX, highlightY, highlightSize, highlightSize);

            g.setComposite(originalComposite);
        }
    }

    /**
     * Draw splash effects
     */
    private void drawSplashes(Graphics2D g) {
        // Set pixelated rendering for splashes
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        for (WaterSplash splash : splashes) {
            double opacity = splash.getOpacity();
            if (opacity <= 0)
                continue;

            // Set splash transparency
            AlphaComposite originalComposite = (AlphaComposite) g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));

            // Draw splash as pixelated ring using rectangles
            g.setColor(new Color(240, 142, 254, (int) (opacity * 100)));

            int radius = (int) splash.radius;
            int thickness = Math.max(2, (int) (4 * opacity)); // Pixelated thickness

            // Draw pixelated ring by drawing small rectangles around the circumference
            int segments = Math.max(8, radius / 3); // Number of segments based on radius
            for (int i = 0; i < segments; i++) {
                double angle = (2 * Math.PI * i) / segments;
                int x = (int) (splash.centerX + Math.cos(angle) * radius);
                int y = (int) (splash.centerY + Math.sin(angle) * radius);

                // Round to pixel grid
                x = (x / 2) * 2;
                y = (y / 2) * 2;

                g.fillRect(x - thickness / 2, y - thickness / 2, thickness, thickness);
            }

            g.setComposite(originalComposite);
        }
    }

    /**
     * Draw water depth and reflection effects
     */
    private void drawWaterDepth(Graphics2D g) {
        // Set pixelated rendering for water depth effects
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        GameSettings settings = GameSettings.getInstance();
        double levelLeft = settings.getLevelLeft();
        double levelRight = settings.getLevelRight(); // Draw pixelated reflection/shimmer lines using rectangles
                                                      // instead of smooth
        // lines

        // Calculate pattern dimensions
        int segmentWidth = 12;
        int patternWidth = (int) Math.ceil((levelRight - levelLeft) / segmentWidth);
        int patternHeight = settings.getLevelHeight() / 10;

        // Initialize or regenerate pattern every few frames
        if (shimmerPattern == null || shimmerAnimationTimer == 0 ||
                shimmerPatternWidth != patternWidth || shimmerPatternHeight != patternHeight) {
            shimmerPatternWidth = patternWidth;
            shimmerPatternHeight = patternHeight;
            shimmerPattern = new boolean[patternHeight][patternWidth];

            // Generate new random pattern
            for (int i = 0; i < patternHeight; i++) {
                for (int j = 0; j < patternWidth; j++) {
                    shimmerPattern[i][j] = Math.random() > 0.4; // chance to show segment
                }
            }
        }        // Get camera position for distance-based transparency calculations
        Camera camera = Camera.getInstance();
        double cameraX = camera.getCameraX();
        double cameraY = camera.getCameraY();
        
        // Get screen dimensions for camera center calculations
        int screenWidth = settings.getBaseWidth();
        int screenHeight = settings.getBaseHeight();

        // Draw using the stored pattern with distance-based transparency
        for (int i = 0; i < Math.min(patternHeight, shimmerPattern.length); i++) {
            if (30 - i <= 0) {
                break; // Stop when transparency is too low
            }

            double y = WATER_LEVEL + 10 + i * 8;
            double offsetX = Math.sin(shimmerFrame * 0.2 + i) * 3;

            // Round offset
            offsetX = Math.round(offsetX / 2) * 2;
            double offsetY = Math.sin(shimmerFrame * 0.6 + i) * 3;
            int lineY = (int) (y + offsetY);
            int lineHeight = 2; 

            int segmentIndex = 0;
            for (int x = (int) levelLeft; x < levelRight
                    && segmentIndex < shimmerPattern[i].length; x += segmentWidth, segmentIndex++) {
                // Use the stored random pattern that only updates every few frames
                if (shimmerPattern[i][segmentIndex]) {
                    // Calculate distance from camera to this shimmer segment
                    double shimmerX = x + offsetX;
                    double distanceX = shimmerX - (cameraX + screenWidth / 2.0); // Camera center X
                    double distanceY = lineY - (cameraY + screenHeight / 2.0); // Camera center Y
                    double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                    // Calculate distance-based transparency factor (closer = more opaque)
                    // Max visible distance of about 800 pixels from camera center
                    double maxDistance = 800.0;
                    double distanceFactor = Math.max(0.1, 1.0 - (distance / maxDistance));
                    
                    // Apply distance factor to the base alpha, maintaining the depth fade
                    int baseAlpha = 30 - i;
                    int finalAlpha = (int) (baseAlpha * distanceFactor);
                    
                    if (finalAlpha > 5) { // Only draw if alpha is significant enough
                        g.setColor(new Color(255, 200, 230, finalAlpha));
                        g.fillRect((int) shimmerX, lineY, segmentWidth, lineHeight);
                    }
                }
            }
        }
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
     * Get the number of active splash effects
     */
    public int getSplashCount() {
        return splashes.size();
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
    // createWaterParticles(-100, 0, 8.0, 15); // splashes.add(new WaterSplash(0,
    // WATER_LEVEL, 5.0));
    // displaceWaterSegments(0, 5.0, 100);
    // }
}
