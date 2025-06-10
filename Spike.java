import java.awt.*;


public class Spike extends Entity{
     
    public Spike(double x, double y, double width, double height){
        super(x,y,width,height,"/Sprites/sprite_0.png"); 
        spritePath = "/Sprites/sprite_0.png"; // Add sprite for upward melee
            setSpriteSize(     100, 100); // Visual sprite size
            setHitboxSize(100, 100); // Smaller hitbox
        
        loadSprite();

    }
    
    public void update() {
        
        // Time-based behaviors (like automatic deactivation after some time
    }


}
