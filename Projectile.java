import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Projectile {
    public boolean active = true;
    private BufferedImage image;
    public int ID;
    private final padr pos = new padr();
    private final pair hitbox;
    private final padr wallBox;
    private final padr vel;
    private padr acc;
    private long last;
    private long now;
    private long totalTime = 0;
    public pair box[];
    private int way = 0;

    public Projectile(double centerX, double centerY, int projID, padr vel) {
        
        this.hitbox = new pair();
        this.wallBox = new padr();
        this.pos.first = centerX;
        this.pos.second = centerY;
        this.ID = projID;
        this.vel = vel;
        this.box = new pair[2];
        box[0] = new pair();
        box[1] = new pair();
        acc = new padr();
        last = System.nanoTime();

        switch (projID) {
            case 1 -> {
                wallBox.set(45, 40);
                hitbox.set(15, 15);
                way = Main.player.hDirection;
            }

            case 2 -> {
                wallBox.set(30, 50);
                hitbox.set(15, 15);
                way = Main.player.hDirection;
            }

            case 3 -> {
                wallBox.set(30, 50);
                hitbox.set(15, 15);
                way = Main.player.hDirection;
            }

            case 4 -> {
                wallBox.set(35, 28);
                hitbox.set(15, 15);
                way = Main.player.hDirection;
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
            g.drawImage(image, (int)pos.first-(int)wallBox.first, (int)pos.second-(int)wallBox.second, null);
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
        double upDist = Math.abs(pos.second-wallBox.second - box[1].second);
        double downDist = Math.abs(pos.second+wallBox.second - box[1].first);
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
            if(  (pos.second+wallBox.second <= box[1].first && pos.second+wallBox.second >= box[1].first )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                return true;
            }
        }

        for(Projectile box : Main.proj){
            if(this==box)continue;
            if(  (pos.second+wallBox.second <= box.box[1].first && pos.second+wallBox.second >= box.box[1].first )&& !(pos.first-wallBox.first > box.box[0].second || pos.first+wallBox.first < box.box[0].first)   ){
                return true;
            }
        }
        return false;
    }

    public boolean touchingR(){
        for(pair[] box : walls.bounds){
            if(box[0].second <= pos.first-wallBox.first && pos.first-wallBox.first<=box[0].second
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first)){
                return true;
            }
        }

        for(Projectile box : Main.proj){
            if(this == box)continue;
            if(box.box[0].second <= pos.first-wallBox.first && pos.first-wallBox.first<=box.box[0].second
            &&!(pos.second-wallBox.second > box.box[1].second || pos.second+wallBox.second < box.box[1].first)){
                return true;
            }
        }
        return false;
    }

    public boolean touchingL(){
        for(pair[] box : walls.bounds){
            if(box[0].first >= pos.first+wallBox.first && pos.first+wallBox.first>=box[0].first
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first)){
                return true;
            }
        }

        for(Projectile box : Main.proj){
            if(this == box)continue;
            if(box.box[0].first >= pos.first+wallBox.first && pos.first+wallBox.first>=box.box[0].first
            &&!(pos.second-wallBox.second > box.box[1].second || pos.second+wallBox.second < box.box[1].first)){
                return true;
            }
        }
        return false;
    }

    public boolean touchingD(){
        for(pair[] box : walls.bounds){
            if(  (pos.second-wallBox.second >= box[1].second && pos.second-wallBox.second <= box[1].second )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                return true;
            }
        }
        for(Projectile box : Main.proj){
            if(this==box)continue;
            if(  (pos.second-wallBox.second >= box.box[1].second && pos.second-wallBox.second <= box.box[1].second )&& !(pos.first-wallBox.first > box.box[0].second || pos.first+wallBox.first < box.box[0].first)   ){
                return true;
            }
        }
        return false;
    }


    public void update() {
        now = System.nanoTime();
        totalTime+=(now-last);
        

        switch(ID){
            case 1->{
                if(way == Main.player.hDirection){
                    pos.first = Main.player.pos.first + 50*way;
                }
                pos.second = Main.player.pos.second;
                if(totalTime > 1000000000 * 0.2){
                    active = false;
                    // Main.player.shot[1] = false;
                }
            }
            case 2->{
                pos.first = Main.player.pos.first;
                pos.second = Main.player.pos.second-70;
                if(totalTime > 1000000000 * 0.2){
                    active = false;
                    // Main.player.shot[1] = false;
                }
            }
            case 3->{
                pos.first = Main.player.pos.first;
                pos.second = Main.player.pos.second+70;
                if(totalTime > 1000000000 * 0.2){
                    active = false;
                    // Main.player.shot[1] = false;
                }
            }
            case 4->{
                pos.first+=vel.first*way;
                for(int i = 0 ; i < walls.bounds.length; i++){
                    if(walls.active[i] && insideWall(walls.bounds[i])>0){
                        active = false;
                        return;   
                    }
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
        box[0].set((int)pos.first-(int)wallBox.first, (int)pos.first+(int)wallBox.first);
        box[1].set((int)pos.second-(int)wallBox.second, (int)pos.second+(int)wallBox.second);
        
    }
}