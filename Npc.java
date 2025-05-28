import java.awt.*;

/**
 * Npc class representing non-player characters in the game
 */
public class Npc extends Entity {
    private int ID; // keep track of which NPC it is
    private int[] iFrames = new int[999]; // invincibility frames for each projectile type
    private int damage = 0; // damage accumulated (could be replaced with health)
    
    /**
     * Create a new NPC with position and type ID
     */
    public Npc(double centerX, double centerY, int npcID) {
        // Call the parent constructor with default values 
        // set specific values below
        super(centerX, centerY, 30, 30, "");
        
        this.ID = npcID;
        
        // Set sprite and hitbox based on NPC type
        switch (npcID) {
            case 1 -> {
                this.spritePath = "/Sprites/friendlinessPellet.png";
                this.width = 50;
                this.height = 64;
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
    
    @Override
    public void update() {
        long now = System.nanoTime();
        
        switch(ID) {
            case 1 -> {
                // Make this NPC mirror the player's position vertically if player exists
                Player player = GameEngine.getPlayer();
                if (player != null) {
                    x = player.getX();
                    y = 790 - player.getY(); // Mirror vertically
                }
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
        
        // Apply physics (like gravity) for certain NPC types
        if (ID != 1) { // Not for the clone NPC which is position-controlled
            applyPhysics();
            
            // Check for wall collisions
            for (Wall wall : GameEngine.getWalls()) {
                if (isCollidingWithWall(wall)) {
                    handleWallCollision(wall);
                }
            }
        }
        
        // Decrease invincibility frames
        for (int i = 0; i < iFrames.length; i++) {
            iFrames[i] = Math.max(iFrames[i] - 1, 0);
        }
    }
    
    // Getters
    public int getID() { return ID; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
}
