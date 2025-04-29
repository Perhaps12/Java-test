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

    private String[] hrt = {"/Sprites/horse race test/jovial merryment.png",
                            "/Sprites/horse race test/bullet n board.png",
                            "/Sprites/horse race test/comely material morning.png",
                            "/Sprites/horse race test/door knob.png",
                            "/Sprites/horse race test/resolute mind afternoon.png",
                            "/Sprites/horse race test/superstitional realism.png",
                            "/Sprites/horse race test/yellow.png",};

    public Projectile(double centerX, double centerY, int projID, padr vel) {
        
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
            case 3 -> {
                try{image = ImageIO.read(getClass().getResource(hrt[0]));}
                catch(IOException | IllegalArgumentException e){}
                // hitbox.set(60, 50);
                wallBox.set(16, 18);
            }
            case 4-> {
                try{image = ImageIO.read(getClass().getResource(hrt[(int)(Math.random()*7)]));}
                catch(IOException | IllegalArgumentException e){}
                // hitbox.set(32, 32);
                wallBox.set(17, 17);
                acc.set(0, -0.9);
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

    private int insideWall(pair[] box){
        //1 left wall  2 right wall  3 top wall  4 bottom wall

        if(box[1].first < pos.second-wallBox.second && pos.second-wallBox.second < box[1].second
            &&!(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first))return 3;

        if(box[1].first < pos.second+wallBox.second && pos.second+wallBox.second < box[1].second
                && !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first))return 4;

        if(box[0].first < pos.first-wallBox.first && pos.first-wallBox.first < box[0].second
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first))return 1;

        if(box[0].first < pos.first+wallBox.first && pos.first+wallBox.first < box[0].second
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first))return 2;
        return 0;
    }

    private void collideWall(pair[] box){
        
        if(insideWall(box) == 0)return;
        
        double leftDist = Math.abs(pos.first-wallBox.first - box[0].second);
        double rightDist = Math.abs(pos.first+wallBox.first - box[0].first);
        double upDist = Math.abs(pos.second-wallBox.second - box[1].second) + 10;
        double downDist = Math.abs(pos.second+wallBox.second - box[1].first) - 10;
        double minDist = Math.min(Math.min(leftDist, rightDist), Math.min(upDist, downDist));

        if(minDist==leftDist){
            pos.first = box[0].second+wallBox.first;
        }
        if(minDist==rightDist){
            pos.first = box[0].first - wallBox.first;
        }
        if(minDist==upDist){
            pos.second = box[1].second+wallBox.second;
        }
        if(minDist==downDist){
            pos.second = box[1].first - wallBox.second;
        }

    }

    public boolean touchingU(){
        for(pair[] box : walls.bounds){
            if(  (pos.second+wallBox.second <= box[1].first && pos.second+wallBox.second >= box[1].first-2 )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                return true;
            }
        }
        return false;
    }

    public boolean touchingR(){
        for(pair[] box : walls.bounds){
            if(box[0].second <= pos.first-wallBox.first && pos.first-wallBox.first<=box[0].second+2
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first)){
                return true;
            }
        }
        return false;
    }

    public boolean touchingL(){
        for(pair[] box : walls.bounds){
            if(box[0].first >= pos.first+wallBox.first && pos.first+wallBox.first>=box[0].first-2
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first)){
                return true;
            }
        }
        return false;
    }

    public boolean touchingD(){
        for(pair[] box : walls.bounds){
            if(  (pos.second-wallBox.second >= box[1].second && pos.second-wallBox.second <= box[1].second+2 )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                return true;
            }
        }
        return false;
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
                    if(walls.active[i] && insideWall(walls.bounds[i])>0){
                        active = false;
                        return;   
                    }
                }
            }

            case 2 -> {
                if(!touchWall){
                    for(int i = 0 ; i < walls.bounds.length; i++){
                        if(walls.active[i] && insideWall(walls.bounds[i])>0){
                            touchWall = true;
                            // vel.set(0, 0);
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

            //jovial merryment
            case 3 -> {
                pos.first+=vel.first;
                pos.second-=vel.second;
                for(int i = 0 ; i < walls.bounds.length; i++){
                    if(walls.active[i]){
                        collideWall(walls.bounds[i]);
                    }
                }
                if(touchingL() || touchingR()){
                    vel.first*=-1;
                }
                if(touchingD() || touchingU()){
                    vel.second*=-1;
                }
                

                if(totalTime/1000000 > 30000){
                    active = false;
                    return;
                }

            }

            //bouncing 
            case 4 -> {
                pos.first+=vel.first;
                pos.second-=vel.second;

                vel.second+=acc.second;

                for(int i = 0 ; i < walls.bounds.length; i++){
                    if(walls.active[i]){
                        collideWall(walls.bounds[i]);
                    }
                }

                if(totalTime/1000000 > 10000){
                    active = false;
                    return;
                }

                if(touchingL() || touchingR()){
                    vel.first*=-0.6;
                }
                if( touchingU()){
                    vel.second*=-0.75;
                    if(vel.second<4)vel.second=0;
                    vel.first*=0.92;
                }
                if(touchingD()){
                    vel.second = -1;
                }


            }

            default -> {
                pos.first+=vel.first;
                pos.second-=vel.second;

                vel.second+=acc.second;

                for(int i = 0 ; i < walls.bounds.length; i++){
                    if(walls.active[i] && insideWall(walls.bounds[i])>0){
                        active = false;
                        return;   
                    }
                }
            }
        }

        last=now;
        
    }
}