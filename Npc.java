import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Npc class representing non-player characters in the game
 */
public class Npc extends Entity {
    private int ID; // keep track of which NPC it is
    private int[] iFrames = new int[999]; // invincibility frames for each projectile type
    private int damage = 0; // damage accumulated (could be replaced with health)

    // Animation system for clone NPC
    private BufferedImage[] idleSprites;
    private BufferedImage[] walkSprites;
    private BufferedImage[] squashStretchSprites;
    private int currentFrame = 0;
    private int animationTimer = 0;
    private static final int IDLE_ANIMATION_SPEED = 10; // frames per sprite change
    private static final int WALK_ANIMATION_SPEED = 8; // frames per sprite change
    private boolean isWalking = false;
    private int hDirection = 1; // 1 for right, -1 for left
    private int swap = 1; // Gravity direction for sprite flipping

    // Squash and stretch thresholds
    private static final double SQUASH_VELOCITY_THRESHOLD = 6.0;
    private static final double STRETCH_VELOCITY_THRESHOLD = -6.0;

    // Failed swap effect for clone
    private boolean failedSwapActive = false;
    private int failedSwapDuration = 0;
    private double shakeOffsetX = 0;
    private int shakeTimer = 0;
    private static final int FAILED_SWAP_DURATION = 30; // 0.5 seconds at 60fps
    private static final double SHAKE_INTENSITY = 3.0; // Side-to-side shake amount

    /**
     * Create a new NPC with position and type ID
     */
    public Npc(double centerX, double centerY, int npcID) {
        // Call the parent constructor with default values
        // set specific values below
        super(centerX, centerY, 30, 30, "");

        this.ID = npcID; // Set sprite and hitbox based on NPC type
        switch (npcID) {
            case 1 -> {
                this.spritePath = "/Sprites/Clone/Idle/sprite_0.png"; // Default to first idle sprite
                setSpriteSize(50, 74); // Visual sprite size same as player
                setHitboxSize(40, 60); // Slightly smaller hitbox than visual for better gameplay
                // Load animation sprites for clone
                loadCloneAnimationSprites();
            }
            default -> {
                this.spritePath = "/Sprites/thec oin.png";
                this.width = 24;
                this.height = 30;
            }
        }

        // Load the sprite based on the path set above
        loadSprite();
    }

    /**
     * Load all animation sprite frames for clone NPC
     */
    private void loadCloneAnimationSprites() {
        // Load idle animation sprites (sprite_0.png to sprite_3.png)
        idleSprites = new BufferedImage[4];
        for (int i = 0; i < 4; i++) {
            try {
                idleSprites[i] = ImageIO
                        .read(getClass().getResourceAsStream("/Sprites/Clone/Idle/sprite_" + i + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load clone idle sprite " + i + ": " + e.getMessage());
            }
        }

        // Load walk animation sprites (sprite_0.png to sprite_5.png)
        walkSprites = new BufferedImage[6];
        for (int i = 0; i < 6; i++) {
            try {
                walkSprites[i] = ImageIO
                        .read(getClass().getResourceAsStream("/Sprites/Clone/Walk cycle/sprite_" + i + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load clone walk sprite " + i + ": " + e.getMessage());
            }
        }

        // Load squash and stretch sprites (sprite_0.png = squash, sprite_1.png =
        // stretch)
        squashStretchSprites = new BufferedImage[2];
        for (int i = 0; i < 2; i++) {
            try {
                squashStretchSprites[i] = ImageIO
                        .read(getClass()
                                .getResourceAsStream("/Sprites/Clone/squash and stretch/sprite_" + i + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Could not load clone squash/stretch sprite " + i + ": " + e.getMessage());
            }
        }
    }

    /**
     * Update animation frame based on clone state (mirrors player)
     */
    private void updateCloneAnimation() {
        Player player = GameEngine.getPlayer();
        if (player == null)
            return;

        // Mirror player's velocity for squash/stretch
        double verticalVelocity = -player.getVelocity().getY(); // Inverted since clone is mirrored

        // Squash and stretch take priority over other animations
        if (verticalVelocity >= SQUASH_VELOCITY_THRESHOLD) {
            // Squash when jumping up
            if (squashStretchSprites[0] != null) {
                sprite = squashStretchSprites[0]; // sprite_0.png squash
            }
            return;
        } else if (verticalVelocity <= STRETCH_VELOCITY_THRESHOLD) {
            // Stretch when falling fast
            if (squashStretchSprites[1] != null) {
                sprite = squashStretchSprites[1]; // sprite_1.png stretch
            }
            return;
        }

        // Mirror player's walking state
        boolean playerWalking = Math.abs(player.getVelocity().getX()) > 0.1;
        boolean wasWalking = isWalking;
        isWalking = playerWalking;

        // Mirror player's direction
        hDirection = player.getDirection();

        // Mirror player's gravity state
        swap = -player.getSwap(); // Inverted since clone is mirrored

        // Reset animation if state changed
        if (wasWalking != isWalking) {
            currentFrame = 0;
            animationTimer = 0;
        }

        // Update animation timer
        animationTimer++;

        if (isWalking) {
            // Walking animation
            if (animationTimer >= WALK_ANIMATION_SPEED) {
                animationTimer = 0;
                currentFrame = (currentFrame + 1) % walkSprites.length;
            }
            // Use walk sprites
            if (walkSprites[currentFrame] != null) {
                sprite = walkSprites[currentFrame];
            }
        } else {
            // Idle animation
            if (animationTimer >= IDLE_ANIMATION_SPEED) {
                animationTimer = 0;
                currentFrame = (currentFrame + 1) % idleSprites.length;
            }
            // Use idle sprites
            if (idleSprites[currentFrame] != null) {
                sprite = idleSprites[currentFrame];
            }
        }
    }

    @Override
    public void update() {
        switch (ID) {
            case 1 -> {
                // Clone NPC - mirrors player position across center line (Y=0)
                Player player = GameEngine.getPlayer();
                if (player != null) {
                    x = player.getX();
                    y = -player.getY(); // Mirror across center wall at Y=0
                }
                // Update clone animation
                updateCloneAnimation();
            }
            // Coin NPC
            default -> {
                // Check collisions with projectiles and take damage
                for (Projectile p : GameEngine.getProjectiles()) {
                    // Different damage values for different projectile types
                    if (p.getID() == 4 && iFrames[4] == 0 && isColliding(p)) {
                        damage += 5;
                        iFrames[4] = 5;
                    } else if ((p.getID() >= 1 && p.getID() <= 3) && iFrames[1] == 0 && isColliding(p)) {
                        damage++;
                        iFrames[1] = 20;
                    }
                }

                // Deactivate if damage threshold reached
                if (damage > 100) {
                    setActive(false);
                }
            }
        }

        // Apply physics (like gravity) for non-clone NPC types
        if (ID != 1) { // Clone NPC handles its own physics above
            applyPhysics();

            // Check for wall collisions
            for (Wall wall : GameEngine.getWalls()) {
                if (isCollidingWithWall(wall)) {
                    handleWallCollision(wall);
                }
            }
        } // Decrease invincibility frames
        for (int i = 0; i < iFrames.length; i++) {
            iFrames[i] = Math.max(iFrames[i] - 1, 0);
        }

        // Update failed swap effect for clone
        if (ID == 1 && failedSwapActive) {
            failedSwapDuration--;
            shakeTimer++;

            // Create side-to-side shake animation
            shakeOffsetX = Math.sin(shakeTimer * 0.8) * SHAKE_INTENSITY;

            // End effect when duration expires
            if (failedSwapDuration <= 0) {
                failedSwapActive = false;
                shakeOffsetX = 0;
                shakeTimer = 0;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (ID == 1 && sprite != null) {
            // Custom drawing for clone with sprite flipping
            Graphics2D g2d = (Graphics2D) g;
            int drawX = (int) (x - spriteWidth / 2);
            int drawY = (int) (y - spriteHeight / 2);

            // Calculate sprite flipping based on both direction and gravity
            boolean flipHorizontal = (hDirection == -1); // Flip when facing left
            boolean flipVertical = (swap == -1); // Flip when gravity is inverted

            int spriteRenderWidth = (int) spriteWidth;
            int spriteRenderHeight = (int) spriteHeight;

            // Adjust drawing position and dimensions based on flipping
            int finalDrawX = drawX;
            int finalDrawY = drawY;
            int finalWidth = spriteRenderWidth;
            int finalHeight = spriteRenderHeight;

            if (flipHorizontal) {
                finalDrawX = drawX + spriteRenderWidth; // Move draw point to right edge
                finalWidth = -spriteRenderWidth; // Negative width flips horizontally
            }
            if (flipVertical) {
                finalDrawY = drawY + spriteRenderHeight; // Move draw point to bottom edge
                finalHeight = -spriteRenderHeight; // Negative height flips vertically
            } // Apply shake offset for failed swap effect
            if (failedSwapActive) {
                finalDrawX += (int) shakeOffsetX;
            }

            // Apply transparency to make clone more transparent
            AlphaComposite originalComposite = (AlphaComposite) g2d.getComposite();
            float opacity = failedSwapActive ? 0.8f : 0.7f; // Slightly more visible during failed swap
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            // Apply red tint to sprite for failed swap effect
            if (failedSwapActive) {
                // Calculate tint intensity based on remaining duration
                float tintIntensity = (float) failedSwapDuration / FAILED_SWAP_DURATION;

                // Create a red-tinted version of the sprite
                BufferedImage tintedSprite = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D tintGraphics = tintedSprite.createGraphics();

                // Draw the original sprite
                tintGraphics.drawImage(sprite, 0, 0, null);

                // Apply red tint using multiply blend mode
                tintGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, tintIntensity * 0.6f));
                tintGraphics.setColor(Color.RED);
                tintGraphics.fillRect(0, 0, sprite.getWidth(), sprite.getHeight());

                tintGraphics.dispose();

                // Draw the tinted sprite
                g2d.drawImage(tintedSprite, finalDrawX, finalDrawY, finalWidth, finalHeight, null);
            } else {
                // Draw the normal sprite with calculated flipping
                g2d.drawImage(sprite, finalDrawX, finalDrawY, finalWidth, finalHeight, null);
            }

            // Restore original composite
            g2d.setComposite(originalComposite);
        } else {
            // Use default drawing for non-clone NPCs
            super.draw(g);
        }
    }

    // Getters
    public int getID() {
        return ID;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Trigger failed swap effect for clone (red tint and shake)
     */
    public void triggerFailedSwapEffect() {
        if (ID == 1) { // Only for clone NPC
            failedSwapActive = true;
            failedSwapDuration = FAILED_SWAP_DURATION;
            shakeTimer = 0;
            shakeOffsetX = 0;
        }
    }
}
