import java.awt.*;


public class Laser extends Entity{
    private long creationTime = 0; 
    private boolean state = true; 
    public Laser(double x, double y, double width, double height){
        super(x,y,width,height, ""); 
        this.creationTime = System.nanoTime();
        


    }
    
    public void update() {

        // Time-based behaviors (like automatic deactivation after some time)
        handleLifetime();
    }

    private void handleLifetime(){
        long now = System.nanoTime();
        double lifetime = (now - creationTime) / 1_000_000_000.0; // Convert to seconds
        
      
        if (lifetime > 2){
            System.out.println(lifetime + " " + state);
            state = !state; 
            creationTime = now;
            setActive(state); // Longer duration forS ranged attacks
        }
    }


}
