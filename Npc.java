import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Npc {
    public boolean active = true; //false if want to delete the npc
    private BufferedImage image;
    private int ID; //keep track of which NPC it is
    public final padr pos = new padr(); //position is centered in the middle of an image

    //hit/image boxes (width, height)
    private final pair hitbox; //hit detection box
    private final padr wallBox; //image edge box
    public pair box[]; //box datatype that works with collision detection methods

    //keeps track of time the npc is present
    private long last;
    private long now;
    private long totalTime = 0; //unused rn

    public int iFrame[] = new int[999]; //keep track of invincibility-frames for each projectile type
    public int damage = 0; //keeps track of damage dealt (could probably replace with health at some point)

    //constructor
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

        //set hitbox/other stats based on the type of ID
        switch (npcID) {
            //coin npc
            case 1 ->{
                try{image = ImageIO.read(getClass().getResource("/Sprites/friendlinessPellet.png"));}
                catch(IOException | IllegalArgumentException e){}
                hitbox.set(25, 32);
                wallBox.set(15, 15);
            }
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
            //draws the image at the correct position based on the position/hitboxes
            g.drawImage(image, (int)pos.first-(int)wallBox.first, (int)pos.second-(int)wallBox.second, null);
        }
    }

    //movement ===========================================================================================================

    //check if the current hitbox is inside another hitbox
    private boolean insideWall(pair[] box){
        //basically if an edge can be found within the other box then they are inside eachother
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

    //move the object if it is colliding with something (unused rn)
    private void collideWall(pair[] box){
        
        if(insideWall(box))return; //check if a hitbox is inside, if not then return
        
        double leftDist = Math.abs(pos.first-wallBox.first - box[0].second);
        double rightDist = Math.abs(pos.first+wallBox.first - box[0].first);
        double upDist = Math.abs(pos.second-wallBox.second - box[1].second);
        double downDist = Math.abs(pos.second+wallBox.second - box[1].first);
        double minDist = Math.min(Math.min(leftDist, rightDist), Math.min(upDist, downDist)); //moves to whichever edge is closest

        //snap the position to the nearest edge based on which is closest
        if(minDist==leftDist){
            pos.first = box[0].second + wallBox.first;
        }
        if(minDist==rightDist){
            pos.first = box[0].first - wallBox.first;
        }
        if(minDist==upDist){
            pos.second = box[1].second + wallBox.second;
        }
        if(minDist==downDist){
            pos.second = box[1].first - wallBox.second;
        }

    }

    //check if it is touching a specific side of any hitbox (should probably change to be specific hitboxes)
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
        now = System.nanoTime(); //update time
        totalTime+=(now-last);
        

        switch(ID){
            case 1->{
                pos.first = Main.player.pos.first;
                pos.second = 790-Main.player.pos.second;
            }
            //coin npc
            default -> {
                //npc doesn't move at the moment
                //assign specific damage values to each projectile
                for(Projectile p : Main.proj){
                    if(p.ID==4&&iFrame[4]==0&& insideWall(p.box)){
                        damage+=5;
                        iFrame[4]=5;
                    } if((p.ID>=1&&p.ID<=3)&&iFrame[1]==0&&insideWall(p.box)){
                        damage++;
                        iFrame[1]=20; 
                    }
                }
                if(damage > 100)active=false; //remove if damage accumulated is over 100
            }
        }

        last=now;//update time

        //update hitboxes
        box[0].set((int)pos.first-(int)hitbox.first, (int)pos.first+(int)hitbox.first);
        box[1].set((int)pos.second-(int)hitbox.second, (int)pos.second+(int)hitbox.second);

        //reduce iframes as necessary
        for(int i = 0 ; i < iFrame.length; i++){
            iFrame[i]=Math.max(iFrame[i]-1, 0);
        }
    }
}