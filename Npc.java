import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Npc {
    public boolean active = true;
    private BufferedImage image;
    private int ID;
    private final padr pos = new padr();
    private final pair hitbox;
    private final padr wallBox;
    private long last;
    private long now;
    private long totalTime = 0;
    public pair box[];
    public int damage = 0; 
    public int iFrame[] = new int[999];

    public Npc(double centerX, double centerY, int npcID) {
        
        this.hitbox = new pair();
        this.wallBox = new padr();
        this.pos.first = centerX;
        this.pos.second = centerY;
        this.ID = npcID;
        this.box = new pair[2];
        box[0] = new pair();
        box[1] = new pair();
        last = System.nanoTime();

        switch (npcID) {
            default -> {
                try{image = ImageIO.read(getClass().getResource("/Sprites/thec oin.png"));}
                catch(IOException | IllegalArgumentException e){}
                hitbox.set(10, 13);
                wallBox.set(12, 15);
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

    private boolean insideWall(pair[] box){
        //1 left wall  2 right wall  3 top wall  4 bottom wall

        if(box[1].first < pos.second-wallBox.second && pos.second-wallBox.second < box[1].second
            &&!(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first))return true;

        if(box[1].first < pos.second+wallBox.second && pos.second+wallBox.second < box[1].second
                && !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first))return true;

        if(box[0].first < pos.first-wallBox.first && pos.first-wallBox.first < box[0].second
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first))return true;

        if(box[0].first < pos.first+wallBox.first && pos.first+wallBox.first < box[0].second
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first))return true;
        return false;
    }

    private void collideWall(pair[] box){
        
        if(insideWall(box))return;
        
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

        // for(Projectile box : Main.proj){
        //     if(  (pos.second+wallBox.second <= box.box[1].first && pos.second+wallBox.second >= box.box[1].first )&& !(pos.first-wallBox.first > box.box[0].second || pos.first+wallBox.first < box.box[0].first)   ){
        //         return true;
        //     }
        // }
        return false;
    }

    public boolean touchingR(){
        for(pair[] box : walls.bounds){
            if(box[0].second <= pos.first-wallBox.first && pos.first-wallBox.first<=box[0].second
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first)){
                return true;
            }
        }

        // for(Projectile box : Main.proj){
        //     if(box.box[0].second <= pos.first-wallBox.first && pos.first-wallBox.first<=box.box[0].second
        //     &&!(pos.second-wallBox.second > box.box[1].second || pos.second+wallBox.second < box.box[1].first)){
        //         return true;
        //     }
        // }
        return false;
    }

    public boolean touchingL(){
        for(pair[] box : walls.bounds){
            if(box[0].first >= pos.first+wallBox.first && pos.first+wallBox.first>=box[0].first
            &&!(pos.second-wallBox.second > box[1].second || pos.second+wallBox.second < box[1].first)){
                return true;
            }
        }

        // for(Projectile box : Main.proj){
        //     if(box.box[0].first >= pos.first+wallBox.first && pos.first+wallBox.first>=box.box[0].first
        //     &&!(pos.second-wallBox.second > box.box[1].second || pos.second+wallBox.second < box.box[1].first)){
        //         return true;
        //     }
        // }
        return false;
    }

    public boolean touchingD(){
        for(pair[] box : walls.bounds){
            if(  (pos.second-wallBox.second >= box[1].second && pos.second-wallBox.second <= box[1].second )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                return true;
            }
        }
        // for(Projectile box : Main.proj){
        //     if(  (pos.second-wallBox.second >= box.box[1].second && pos.second-wallBox.second <= box.box[1].second )&& !(pos.first-wallBox.first > box.box[0].second || pos.first+wallBox.first < box.box[0].first)   ){
        //         return true;
        //     }
        // }
        return false;
    }


    public void update() {
        now = System.nanoTime();
        totalTime+=(now-last);
        

        switch(ID){
            default -> {
                for(Projectile p : Main.proj){
                    if(p.ID==4&&iFrame[4]==0&& insideWall(p.box)){
                        damage+=5;
                        iFrame[4]=5;
                    } if((p.ID>=1&&p.ID<=3)&&iFrame[1]==0&&insideWall(p.box)){
                        damage++;
                        iFrame[1]=20;
                    }
                }
                if(damage > 100)active=false;
            }
        }



        last=now;
        box[0].set((int)pos.first-(int)hitbox.first, (int)pos.first+(int)hitbox.first);
        box[1].set((int)pos.second-(int)hitbox.second, (int)pos.second+(int)hitbox.second);
        for(int i = 0 ; i < iFrame.length; i++){
            iFrame[i]=Math.max(iFrame[i]-1, 0);
        }
    }
}