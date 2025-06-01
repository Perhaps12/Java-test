import java.awt.*;
import java.awt.event.*;

/**
 * Player class representing the user-controlled character
 */
public class Player extends Entity {
    // Player state
    private boolean jumped = false;
    private boolean airJump = false;
    private boolean pogo = false;
    private int pogoCool = 0;
    private boolean wallSlide = false;
    private int coyoteTime = 0;
    private int swap = 1; // For gravity swap mechanic

    // Direction user is facing
    private int hDirection = 1; // primary direction
    private int hDirection2 = 1; // secondary direction (for wall jumps)

    // Movement vectors
    private Vector2D velocity2 = new Vector2D(); // Secondary velocity (wall jump, dash)

    // Dash mechanics
    private int dashCool = 45;

    // Fall distance tracking for impact shake effects
    private double fallStartY = 0;
    private boolean wasFalling = false;
    private static final double HARD_LANDING_THRESHOLD = 200; // pixels fallen for hard landing shake

    // Combat
    private int[] cooldown = new int[99]; // Cooldowns for attacks
    private boolean[] shot = new boolean[99]; // Track button presses for attacks

    /**
     * Create a new player with position and sprite
     */
    public Player(String spritePath, double centerX, double centerY) {
        super(centerX, centerY, 50, 74, spritePath);

        // Set default acceleration (gravity)
        acceleration.setY(0.9);
    }

    @Override
    public void update() {
        // Process attacks
        processAttacks();

        // Process movement
        processMovement();

        // Apply physics with velocity system
        applyPlayerPhysics();

        // Check collision with walls
        handleWallCollisions();

        // Update cooldowns
        updateCooldowns();
    }

    @Override
    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, (int) (x - hitboxWidth / 2), (int) (y - hitboxHeight / 2), null);
        }
    }

    /**
     * Handle player movement based on keyboard input
     */
    private void processMovement() {
        Camera camera = Camera.getInstance();
        camera.setFollowSpeed(0.075);

        // Reset acceleration
        acceleration.setX(0); // Check if standing on ground
        boolean onGround = isTouchingGround();
        if (onGround) {
            // Check for hard landing shake effect - Y increases downward, so y > fallStartY
            // when falling
            if (wasFalling && Math.abs(y - fallStartY) > HARD_LANDING_THRESHOLD) {
                double fallDistance = Math.abs(y - fallStartY);
                double shakeIntensity = Math.min(25, fallDistance / 20); // Scale intensity with fall distance
                int shakeDuration = (int) Math.min(10, fallDistance / 30); // Scale duration with fall distance, cap at
                                                                           // 10 frames
                System.out.println("fall distance land " + fallDistance);

                // Only trigger if it's a significant landing or no current shake
                if (camera.shouldOverrideShake(shakeIntensity)) {
                    camera.shake(shakeIntensity, shakeDuration, Camera.ShakeType.CIRCULAR);
                }
            }
            velocity.setY(Math.max(-0.2, velocity.getY()));
            coyoteTime = 5;
            wasFalling = false; // Reset falling state
            fallStartY = y; // Reset fall start position when on ground
        } else {
            // Track falling state for hard landing detection
            // Start tracking when player has any downward velocity
            if (!wasFalling && (velocity.getY() < -4) && coyoteTime == 0) {
                wasFalling = true;
                fallStartY = y;
            }
        }

        // Left/Right movement
        if (GameEngine.isKeyPressed(KeyEvent.VK_LEFT)) {
            // Direction change handling
            if (hDirection == 1) {
                velocity.setX(0);
                acceleration.setX(-2);
            }
            if (velocity2.getY() <= 2) {
                hDirection = -1;
            }

            // Wall slide
            if (isTouchingRightWall()) {
                acceleration.setY(-0.05);
                wallSlide = true;
            } else {
                acceleration.setY(0);
                wallSlide = false;
            }

            // Movement
            acceleration.setX(acceleration.getX() + 1.2);
        }

        if (GameEngine.isKeyPressed(KeyEvent.VK_RIGHT)) {
            // Direction change handling
            if (hDirection == -1) {
                velocity.setX(0);
                acceleration.setX(-2);
            }
            if (velocity2.getY() <= 2) {
                hDirection = 1;
            }

            // Wall slide
            if (isTouchingLeftWall()) {
                acceleration.setY(-0.05);
                wallSlide = true;
            } else {
                acceleration.setY(0);
                wallSlide = false;
            }

            // Movement
            acceleration.setX(acceleration.getX() + 1.2);
        }

        // Jumping logic
        if (GameEngine.isKeyPressed(KeyEvent.VK_UP)) {
            // Wall jumps
            if (isTouchingLeftWall() && !jumped && velocity2.getY() <= 8) {
                y -= 3 * swap;
                velocity.setY(14.5);
                hDirection2 = -1;
                velocity2.setX(15);
                jumped = true;
            } else if (isTouchingRightWall() && !jumped && velocity2.getY() <= 8) {
                y -= 3 * swap;
                velocity.setY(14.5);
                hDirection2 = 1;
                velocity2.setX(15);
                jumped = true;
            }
            // Regular jump - must be touching ground or in coyote time
            else if (coyoteTime > 0 && !jumped && velocity2.getY() <= 8) {
                y -= 3 * swap;
                velocity.setY(16);
                jumped = true;
            }
            // Double jump
            else if (airJump && !jumped && velocity2.getY() <= 8) {
                y -= 3 * swap;
                velocity.setY(14);
                jumped = true;
                airJump = false;
            }

            // Hold up to jump higher
            if (velocity.getY() >= 2) {
                acceleration.setY(-0.7);
            } else {
                acceleration.setY(-1.2);
            }
        } else {
            // Reset jump state when key is released
            jumped = false;
        }

        // Fast fall
        if (GameEngine.isKeyPressed(KeyEvent.VK_DOWN)) {
            acceleration.setY(-1.6);
        } // Dash
        if (GameEngine.isKeyPressed(KeyEvent.VK_C)) {
            if (dashCool == 0) {
                velocity2.setY(22);
                velocity.setY(-2);
                dashCool = 45;
                // Add small shake effect when starting dash
                camera.shake(10, 6, Camera.ShakeType.RANDOM);
            }
        }

        // Disable wallslide if not holding direction keys
        if (!GameEngine.isKeyPressed(KeyEvent.VK_RIGHT) && !GameEngine.isKeyPressed(KeyEvent.VK_LEFT)) {
            wallSlide = false;
        }

        // Stop if not moving
        if (!(GameEngine.isKeyPressed(KeyEvent.VK_RIGHT) || GameEngine.isKeyPressed(KeyEvent.VK_LEFT))) {
            acceleration.setX(-1000);
            // Reset wall slide acceleration when not moving horizontally
            // acc2.second = 0; (handled in velocity calculation)
        }

        // Pogo on downwards strike
        if (pogo) {
            y -= 3 * swap;
            velocity.setY(22);
            pogo = false;
            pogoCool = 20;
            camera.shake(5, 15, Camera.ShakeType.VERTICAL);
        }
    }

    /**
     * Apply physics with velocity/acceleration system
     */
    private void applyPlayerPhysics() {
        // Cap acceleration
        acceleration.setX(Math.max(-4, acceleration.getX()));
        acceleration.setX(Math.min(4, acceleration.getX()));

        // Apply acceleration to velocity
        velocity.add(acceleration.getX(), 0);

        // Cap velocity
        velocity.setX(Math.max(0, velocity.getX()));
        velocity.setX(Math.min(7, velocity.getX()));

        // Wall jump velocity deceleration
        velocity2.setX(velocity2.getX() - 1.5);
        velocity2.setX(Math.max(0, velocity2.getX()));

        // Dash velocity deceleration - legacy: vel2.second
        velocity2.setY(velocity2.getY() - 2);
        velocity2.setY(Math.max(0, velocity2.getY()));

        // Speed adjustment after wall jump
        if (velocity2.getX() + velocity.getX() >= 3 && hDirection != hDirection2 && velocity2.getX() > 0) {
            velocity.setX(Math.max(3 - velocity2.getX(), 0));
        }
        if (velocity2.getX() + velocity.getX() >= 7 && hDirection == hDirection2) {
            velocity.setX(Math.max(7 - velocity2.getX(), 0));
        }

        // Movement based on velocities - legacy:
        // pos.first+=hDirection*(vel.first+vel2.second)+hDirection2*vel2.first;
        x += hDirection * (velocity.getX() + velocity2.getY()) + hDirection2 * velocity2.getX();

        // Reset gravity to normal if no special keys are pressed
        if (!(GameEngine.isKeyPressed(KeyEvent.VK_DOWN) || GameEngine.isKeyPressed(KeyEvent.VK_UP))) {
            acceleration.setY(-1.2);
        }

        // Various vertical velocity calculations - matching legacy exactly
        if (velocity2.getY() > 3) {
            // When dash Y velocity is active, use wall slide acceleration (acc2.second)
            double wallSlideAcc = wallSlide ? -0.05 : 0;
            velocity.setY(velocity.getY() + 2 * wallSlideAcc);
        } else if (!wallSlide || velocity.getY() > -2) {
            velocity.setY(velocity.getY() + acceleration.getY());
        } else {
            // Wall slide physics - use wall slide acceleration (acc2.second)
            double wallSlideAcc = -0.05;
            velocity.setY(velocity.getY() + wallSlideAcc);
            velocity.setY(Math.max(-4, velocity.getY()));
        }

        // Stop velocity when hitting ceiling
        if (isTouchingCeiling()) {
            velocity.setY(-2);
        }

        // Cap vertical velocity
        velocity.setY(Math.max(-100, velocity.getY()));
        velocity.setY(Math.min(100, velocity.getY()));

        // Apply vertical movement
        y -= velocity.getY() * swap;
    }

    /**
     * Handle collisions with walls
     */
    private void handleWallCollisions() {
        Camera camera = Camera.getInstance();

        for (Wall wall : GameEngine.getWalls()) {
            if (isCollidingWithWall(wall)) {
                // Check if player is dashing (has significant dash velocity) and hits a wall
                boolean isDashing = velocity2.getY() > 18; // Much higher threshold for dash detection
                boolean isMovingFast = Math.abs(velocity2.getX()) > 12; // Only wall jump velocity triggers this now

                if (isDashing || isMovingFast) {
                    // Calculate impact shake based on velocity
                    double totalVelocity = Math.sqrt(velocity.getX() * velocity.getX()
                            + velocity2.getX() * velocity2.getX() + velocity2.getY() * velocity2.getY());
                    double shakeIntensity = Math.min(15, totalVelocity / 2); // Reduced intensity scaling
                    int shakeDuration = (int) Math.min(12, totalVelocity); // Reduced duration scaling

                    // Only trigger shake if it's significant enough or no current shake
                    if (camera.shouldOverrideShake(shakeIntensity)) {
                        camera.shake(shakeIntensity, shakeDuration, Camera.ShakeType.RANDOM);
                    }
                }

                handleWallCollision(wall);
            }
        }
    }

    /**
     * Process player attacks and weapon use
     */
    private void processAttacks() { // Ranged attack
        if (GameEngine.isKeyPressed(KeyEvent.VK_X) && !shot[0] && cooldown[0] == 0) {
            shot[0] = true;

            // Recoil
            hDirection2 = -hDirection;
            velocity2.setX(10);
            velocity.setX(0);
            velocity.setY(3);

            // Create projectile
            Vector2D projectileVelocity = new Vector2D(100, 0);
            GameEngine.addProjectile(new Projectile(x, y, 4, projectileVelocity));
            cooldown[0] = 50;

            // Add subtle shake effect for shooting
            Camera camera = Camera.getInstance();
            camera.shake(2, 5, Camera.ShakeType.HORIZONTAL);
        }
        if (!GameEngine.isKeyPressed(KeyEvent.VK_X)) {
            shot[0] = false;
        }

        // Melee attacks
        if (GameEngine.isKeyPressed(KeyEvent.VK_Z) && !shot[1] && cooldown[1] == 0) {
            shot[1] = true;

            Vector2D attackVelocity = new Vector2D(0, 0);
            int attackID = 1; // Default horizontal attack

            if (GameEngine.isKeyPressed(KeyEvent.VK_SPACE)) {
                attackID = 2; // Upward attack
            } else if (GameEngine.isKeyPressed(KeyEvent.VK_DOWN) && !isTouchingGround()) {
                attackID = 3; // Downward attack
            }

            GameEngine.addProjectile(new Projectile(x, y, attackID, attackVelocity));
            cooldown[1] = 25;
        }
        if (!GameEngine.isKeyPressed(KeyEvent.VK_Z)) {
            shot[1] = false;
        } // Character swap
        if (GameEngine.isKeyPressed(KeyEvent.VK_S) && !shot[2]) {
            // Find clone NPC
            Npc clone = null;
            for (Npc npc : GameEngine.getNpcs()) {
                if (npc.getID() == 1) {
                    clone = npc;
                    break;
                }
            }

            if (clone != null) {
                // Swap positions
                double tempX = clone.getX();
                double tempY = clone.getY();
                clone.setPosition(x, y);
                x = tempX;
                y = tempY;
                shot[2] = true;
                swap *= -1;

                // Add shake effect for character swap
                Camera camera = Camera.getInstance();
                camera.shake(8, 12, Camera.ShakeType.CIRCULAR);
            }
        }
        if (!GameEngine.isKeyPressed(KeyEvent.VK_S)) {
            shot[2] = false;
        }
    }

    /**
     * Update all cooldown timers
     */
    private void updateCooldowns() {
        coyoteTime = Math.max(coyoteTime - 1, 0);

        for (int i = 0; i < cooldown.length; i++) {
            cooldown[i] = Math.max(0, cooldown[i] - 1);
        }

        dashCool = Math.max(dashCool - 1, 0);
        pogoCool = Math.max(pogoCool - 1, 0);
    }

    /**
     * Check if player is touching the ground
     */
    public boolean isTouchingGround() {
        // Increased tolerance for more reliable ground detection
        final double GROUND_TOLERANCE = 3.0;
        for (Wall wall : GameEngine.getWalls()) {
            double playerBottom, wallSurface;

            if (swap == 1) {
                // Normal gravity - check bottom of player against top of wall
                playerBottom = y + hitboxHeight / 2;
                wallSurface = wall.getY();
            } else {
                // Inverted gravity - check top of player against bottom of wall
                playerBottom = y - hitboxHeight / 2;
                wallSurface = wall.getY() + wall.getHeight();
            }

            // Check if player is touching the surface with tolerance
            if (Math.abs(playerBottom - wallSurface) < GROUND_TOLERANCE &&
                    x + hitboxWidth / 2 > wall.getX() + 0.1 &&
                    x - hitboxWidth / 2 < wall.getX() + wall.getWidth() - 0.1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if player is touching a ceiling
     */
    public boolean isTouchingCeiling() {
        // Increased tolerance for more reliable ceiling detection
        final double CEILING_TOLERANCE = 3.0;
        for (Wall wall : GameEngine.getWalls()) {
            double playerTop, wallSurface;

            if (swap == 1) {
                // Normal gravity - check top of player against bottom of wall
                playerTop = y - hitboxHeight / 2;
                wallSurface = wall.getY() + wall.getHeight();
            } else {
                // Inverted gravity - check bottom of player against top of wall
                playerTop = y + hitboxHeight / 2;
                wallSurface = wall.getY();
            }

            // Check if player is touching the surface with tolerance
            if (Math.abs(playerTop - wallSurface) < CEILING_TOLERANCE &&
                    x + hitboxWidth / 2 > wall.getX() + 0.1 &&
                    x - hitboxWidth / 2 < wall.getX() + wall.getWidth() - 0.1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if player is touching a wall on the right side
     */
    public boolean isTouchingRightWall() {
        // Increased tolerance for more reliable wall detection
        final double WALL_TOLERANCE = 3.0;
        for (Wall wall : GameEngine.getWalls()) {
            double playerLeft = x - hitboxWidth / 2;
            double wallRight = wall.getX() + wall.getWidth();

            // Check if left side of player is touching right side of wall with tolerance
            if (Math.abs(playerLeft - wallRight) < WALL_TOLERANCE &&
                    y + hitboxHeight / 2 > wall.getY() + 0.1 &&
                    y - hitboxHeight / 2 < wall.getY() + wall.getHeight() - 0.1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if player is touching a wall on the left side
     */
    public boolean isTouchingLeftWall() {
        // Increased tolerance for more reliable wall detection
        final double WALL_TOLERANCE = 3.0;
        for (Wall wall : GameEngine.getWalls()) {
            double playerRight = x + hitboxWidth / 2;
            double wallLeft = wall.getX();

            // Check if right side of player is touching left side of wall with tolerance
            if (Math.abs(playerRight - wallLeft) < WALL_TOLERANCE &&
                    y + hitboxHeight / 2 > wall.getY() + 0.1 &&
                    y - hitboxHeight / 2 < wall.getY() + wall.getHeight() - 0.1) {
                return true;
            }
        }
        return false;
    }

    // Getters and setters
    public int getDirection() {
        return hDirection;
    }

    public int getSwap() {
        return swap;
    }

    public void setPogo(boolean pogo) {
        if (pogoCool == 0) {
            this.pogo = pogo;
        }
    }
}
