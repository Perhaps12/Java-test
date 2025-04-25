import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Projectile {
    public boolean active = true;
    private BufferedImage image;
    private final int ID;
    private final padr pos = new padr();
    private final pair hitbox;
    private final pair wallBox;
    private final padr vel;
    private padr acc;
    private long last;
    private long now;
    private long totalTime = 0;

    public Projectile(double centerX, double centerY, int projID, padr vel) {
        if(Main.proj.size()>=1000){
            active=false;
        }
        this.hitbox = new pair();
        this.wallBox = new pair();
        this.pos.first = centerX;
        this.pos.second = centerY;
        this.ID = projID;
        this.vel = vel;
        acc = new padr();
        last = System.nanoTime();

        switch (projID) {
            case 1 -> {
                try{image = ImageIO.read(getClass().getResource("/Sprites/Arrow.png"));}
                catch(IOException | IllegalArgumentException e){}
                hitbox.set(28, 8);
                wallBox.set(13, 3);
            }
            case 2 -> {
                try{image = ImageIO.read(getClass().getResource("/Sprites/banana.gif"));}
                catch(IOException | IllegalArgumentException e){}
                hitbox.set(60, 50);
                wallBox.set(29, 24);
                acc.set(0, -0.4);
            }
            default -> {
                try{image = ImageIO.read(getClass().getResource("/Sprites/friendlinessPellet.png"));}
                catch(IOException | IllegalArgumentException e){}
                hitbox.set(32, 32);
                wallBox.set(15, 15);
                acc.set(0, -0.9);
            }
        }
    }

    //display ===========================================================================================================
    
    public void draw(Graphics g) {
        if (active && image != null) {
            g.drawImage(image, (int)pos.first-wallBox.first, (int)pos.second-wallBox.second, null);
        }
    }

    //movement ===========================================================================================================

    private boolean insideWall(pair[] box){

        if(box[0].first <= pos.first-wallBox.first && pos.first-wallBox.first <= box[0].second
            &&!(pos.second-wallBox.second >= box[1].second || pos.second+wallBox.second <= box[1].first))return true;

        if(box[0].first <= pos.first+wallBox.first && pos.first+wallBox.first <= box[0].second
            &&!(pos.second-wallBox.second >= box[1].second || pos.second+wallBox.second <= box[1].first))return true;

        if(box[1].first <= pos.second-wallBox.second && pos.second-wallBox.second <= box[1].second
            &&!(pos.first-wallBox.first >= box[0].second || pos.first+wallBox.first <= box[0].first))return true;

        return box[1].first <= pos.second+wallBox.second && pos.second+wallBox.second <= box[1].second
                && !(pos.first-wallBox.first >= box[0].second || pos.first+wallBox.first <= box[0].first);
    }

    private boolean touchWall = false;
    public void update() {
        now = System.nanoTime();
        totalTime+=(now-last);

        switch(ID){
            case 1 -> {
                pos.first+=vel.first;
                pos.second-=vel.second;

                for(int i = 0 ; i < walls.bounds.length; i++){
                    if(walls.active[i] && insideWall(walls.bounds[i])){
                        active = false;
                        return;   
                    }
                }
            }

            case 2 -> {
                if(!touchWall){
                    for(int i = 0 ; i < walls.bounds.length; i++){
                        if(walls.active[i] && insideWall(walls.bounds[i])){
                            touchWall = true;
                            vel.set(0, 0);
                            return;   
                        }
                    }
                }
                if(touchWall){
                    // System.out.println("e");
                    double angle = operations.angle(pos.first, pos.second, Main.player.pos.first, Main.player.pos.second );
                    acc.first = 0.7*Math.cos(angle);
                    acc.second = 0.7*Math.sin(angle);
                    vel.first+=acc.first;
                    vel.second+=acc.second;
                    pos.first+=vel.first;
                    pos.second-=vel.second;
                }
                else{
                    pos.first+=vel.first;
                    pos.second-=vel.second;
                    vel.second+=acc.second;
                }
                
                if(totalTime/1000000 > 10000){
                    active = false;
                    return;
                }
            }

            default -> {
                pos.first+=vel.first;
                pos.second-=vel.second;

                vel.second+=acc.second;

                for(int i = 0 ; i < walls.bounds.length; i++){
                    if(walls.active[i] && insideWall(walls.bounds[i])){
                        active = false;
                        return;   
                    }
                }
            }
        }

        last=now;
        
    }
}