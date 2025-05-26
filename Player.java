import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;



public class Player{

    private BufferedImage image;
    public final padr pos = new padr(); //position centered
    //hitboxes
    public final pair hitbox = new pair();
    public final pair wallBox = new pair();
    public padr box = new padr();
    public int swap = 1;
    
    private boolean jumped = false; //check if the user has jumped already

    public Player(String imagePath, int centerX, int centerY) {

        this.pos.first = centerX;
        this.pos.second = centerY;

        try {this.image = ImageIO.read(getClass().getResourceAsStream(imagePath));} 
        catch (IOException e) {}

        hitbox.set(10, 22);
        wallBox.set(25, 37);
        //set default acceleration downwards
        acc.second = -0.9;
        
    }

    //display ===========================================================================================================
    
    //same methods as in NPC.java
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, (int)pos.first-wallBox.first, (int)pos.second-wallBox.second, null);
        }
    }
    
    //movement ===========================================================================================================

    //same methods as in NPC.java
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

    private void collideWall(pair[] box){
        
        if(!insideWall(box))return;
        
        double leftDist = Math.abs(pos.first-wallBox.first - box[0].second);
        double rightDist = Math.abs(pos.first+wallBox.first - box[0].first);
        double upDist = Math.abs(pos.second-wallBox.second - box[1].second) + 10*swap;
        double downDist = Math.abs(pos.second+wallBox.second - box[1].first) - 10*swap;
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
            pos.second = box[1].first - wallBox.second-1;
        }

    }

    //direction user is facing
    public int hDirection = 1; //primary direction
    public int hDirection2 = 1; //used to adjust velocity in specific scenarios seperate to where player is facing (ex walljump)
    //accelerations + velocities
    public final padr vel = new padr();
    public final padr vel2 = new padr(); //both are used for hotizontal movement
    public final padr acc = new padr();
    public final padr acc2 = new padr(); //x axis unused
    public boolean airJump = false; //double jump unused
    public boolean pogo = false;
    public int pogoCool = 0;
    private boolean wallSlide = false; //wall slide
    //dash
    private int dashCool = 45; 
    //coyote time essentially allows the user to jump for a few frames after leaving the ground, improving the user experience in case they mistime slightly
    private int coyoteTime = 0;

    //update player logic
    public void update() {
        shoot();//check if player attacks at all
        
        if(touchingL()||touchingR()||touchingU()){ //double jump disabled for now
            // airJump = true;
        }
        if(touchingU()){ //reset downwards velocity/coyote time when touching the ground
            vel.second = Math.max(-0.2, vel.second);
            coyoteTime = 5;
        }
        
        //left/right movement
        if (Gameloop.keys.contains(KeyEvent.VK_LEFT)){
            //swap direction slowdown
            if(hDirection==1){
                vel.first = 0;
                acc.first = -2;
            }
            if(vel2.second<=2)hDirection = -1;
            //wall slide
            if(touchingR()){
                acc2.second =-0.05;
                wallSlide = true;
            }else{
                acc2.second = 0;
                wallSlide = false;
            }
            //movement
            acc.first+=1.2;
            // released.first = 0;
        }
        if (Gameloop.keys.contains(KeyEvent.VK_RIGHT)){
            //swap direction slowdown
            if(hDirection==-1){
                vel.first = 0;
                acc.first = -2;
            }
            if(vel2.second<=2)hDirection = 1;
            //wall slide
            if(touchingL()){
                acc2.second = -0.05;
                wallSlide = true;
            }else{
                acc2.second = 0;
                wallSlide = false;
            }
            //movement
            acc.first+=1.2;
            // released.second = 0;
        }

        //jumping logic
        if (Gameloop.keys.contains(KeyEvent.VK_UP)){
            //wall jumps - must be touching wall and causes the player to jump away from the wall
            if(touchingL()&!jumped&&vel2.second<=8){
                pos.second-=3*swap;
                vel.second=14.5;
                hDirection2 = -1;
                vel2.first = 15;
                jumped = true;
            }
            else if(touchingR()&&!jumped&&vel2.second<=8){
                pos.second-=3*swap;
                vel.second=14.5;
                hDirection2 = 1;
                vel2.first = 15;
                jumped = true;

            }
            
            //regular jump - must be touching ground
            else if(coyoteTime>0&&!jumped&&vel2.second<=8){
                pos.second-=3*swap;
                vel.second = 16;
                jumped = true;
            }
            else if (airJump&&!jumped&&vel2.second<=8){ //unused double jump
                pos.second-=3*swap;
                vel.second = 14;
                jumped = true;
                airJump=false;
            }
            //hold up to jump higher
            if(vel.second>=2)acc.second=-0.7;
            else acc.second=-1.2;
        }
        else{
            //make sure continuous holding of jump key doesnt keep jumping
            jumped = false;
        }
        //accelerate downwards faster
        if (Gameloop.keys.contains(KeyEvent.VK_DOWN)) acc.second=-1.6;

        //dash
        if (Gameloop.keys.contains(KeyEvent.VK_C)){
            if(dashCool==0){
                vel2.second = 22; //also prevents a lot of other movement when the dash is active so dash is uninterrupted
                vel.second = -2;
                dashCool = 45;
            }
        }

        //disable wallslide if not holding keys
        if(!Gameloop.keys.contains(KeyEvent.VK_RIGHT) && !Gameloop.keys.contains(KeyEvent.VK_LEFT)){
            wallSlide = false;
        }

        //stop if not moving
        if(!(Gameloop.keys.contains(KeyEvent.VK_RIGHT) || Gameloop.keys.contains(KeyEvent.VK_LEFT))){
            acc.first=-1000;
            acc2.second = 0;
        }

        //pogo on downwards strike
        if (pogo){ 
            pos.second-=3*swap;
            vel.second = 22;
            pogo=false;
            pogoCool = 20;
        }

        //horizontal movement acceleration
        acc.first = Math.max(-4, acc.first);
        acc.first = Math.min(4, acc.first); //acceleration cap

        //horizontal movement velocity
        vel.first+=acc.first;
        vel.first = Math.max(0, vel.first);
        vel.first = Math.min(7, vel.first); //velocity cap
        //wall jump velocity deacceleration
        vel2.first-=1.5;
        vel2.first=Math.max(0, vel2.first);
        //dash velocity deacceleration
        vel2.second-=2;
        vel2.second=Math.max(0, vel2.second);
            
        //slightly adjust speed after a walljump
        if(vel2.first+vel.first >= 3 && hDirection!=hDirection2 && vel2.first>0){
            vel.first = Math.max(3-vel2.first, 0);
        }
        if(vel2.first+vel.first >= 7 && hDirection==hDirection2){
            vel.first = Math.max(7-vel2.first, 0);
        }

        //horizontal movement position
        pos.first+=hDirection*(vel.first+vel2.second)+hDirection2*vel2.first;

        //reset gravity to normal
        if(!(Gameloop.keys.contains(KeyEvent.VK_DOWN) || Gameloop.keys.contains(KeyEvent.VK_UP)))acc.second=-1.2;

        
        //various vertical velocity calculations
        //tbh i dont remember what this does but its probably important
        if(vel2.second > 3){
            vel.second+=2*acc2.second;
        }
        else if((!wallSlide || vel.second>-2))vel.second+=acc.second;

        //wallslide velocity/acceleration
        else{
            vel.second+=acc2.second;
            vel.second = Math.max(-4, vel.second);
        }
        if(touchingD())vel.second = -2; //stop velocity when hit ceiling
        vel.second = Math.max(-100, vel.second);
        vel.second = Math.min(100, vel.second); //vertical velocity cap (probably unecessary but just in case)

        //vertical movement position
        pos.second-=vel.second*swap;

        //collision
        int ind = -1;
        for(pair[] box : walls.bounds){
            ind++;
            if(!walls.active[ind])continue;
            collideWall(box);
        }

        //adjust various cooldowns
        coyoteTime = Math.max(coyoteTime-1, 0);

        for(int i = 0 ; i < cooldown.length; i++){
            cooldown[i] = Math.max(0, cooldown[i]-1);
        }

        dashCool = Math.max(dashCool-1, 0);

        pogoCool = Math.max(pogoCool-1, 0);

        //debug
        // System.out.println(vel.first + " " + acc.first);
        // System.out.println(image.getHeight(this) + " " + image.getWidth(this));
        // System.out.println(touchingL());
        // System.out.println(vel.second);
        // System.out.println("0 -4");


        box.first = pos.first-wallBox.first;
        box.second=pos.first-wallBox.second;
    }


    //same method as in NPC.java
    public boolean touchingU(){
        if(swap==-1){
            for(pair[] box : walls.bounds){
                if(  (pos.second-wallBox.second >= box[1].second && pos.second-wallBox.second <= box[1].second+2 )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                    return true;
                }
            }
            return false;
        }

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
        if(swap==-1){
            for(pair[] box : walls.bounds){
                if(  (pos.second+wallBox.second <= box[1].first && pos.second+wallBox.second >= box[1].first-2 )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                    return true;
                }
            }
            return false;
        }
        
        for(pair[] box : walls.bounds){
            if(  (pos.second-wallBox.second >= box[1].second && pos.second-wallBox.second <= box[1].second+2 )&& !(pos.first-wallBox.first > box[0].second || pos.first+wallBox.first < box[0].first)   ){
                return true;
            }
        }
        return false;
    }
    
    // ===========================================================================================================
    
    public int cooldown[] = new int[99];//store cooldowns for each projectile type
    public boolean shot[] = new boolean[99]; //used to prevent autofire when holding down buttons
    public void shoot(){

        //horizontal ranged attack
        if(Gameloop.keys.contains(KeyEvent.VK_X) && !shot[0] && cooldown[0]==0){
            shot[0]=true;
            //launches player back a bit when fired
            hDirection2 = -hDirection;
            vel2.first = 10;
            //reset velocities
            vel.first = 0;
            vel.second = 3;

            padr velocity = new padr();
            velocity.set(40, 0); //set initial projectile velocity
            Main.proj.add(new Projectile(pos.first, pos.second, 4, velocity));
            cooldown[0] = 50;

        }
        if(!Gameloop.keys.contains(KeyEvent.VK_X)){
            shot[0] = false; //detects when letting go of the button to prevent holding to attack
        }


        //melee swings
        if(Gameloop.keys.contains(KeyEvent.VK_Z) && !shot[1] && cooldown[1] == 0 ){
            shot[1]=true;
            // pos.first+=-hDirection*35;
            // vel.first = 0;
            // System.out.println("hey");
            padr velocity = new padr();
            velocity.set(0, 0);
            int ID = 1; //default to left/right attack
            if(Gameloop.keys.contains(KeyEvent.VK_SPACE))ID = 2; //if user is holiding space, upwards attack
            if(Gameloop.keys.contains(KeyEvent.VK_DOWN) && !touchingU()) ID=3; //if user is holding down and is not touching any floor, downwards attack
            Main.proj.add(new Projectile(pos.first, pos.second, ID, velocity));
            cooldown[1] = 25; 
        }
        if(!Gameloop.keys.contains(KeyEvent.VK_Z)){
            shot[1] = false;
        }


        //swap
        if(Gameloop.keys.contains(KeyEvent.VK_S) && !shot[2]){
            padr temp = new padr();
            temp.set(Main.clone.pos.first, Main.clone.pos.second);
            Main.clone.pos.set(pos.first, pos.second);
            pos.first=temp.first;
            pos.second=temp.second;
            shot[2] = true;
            swap*=-1;
            System.out.println(temp.first + " " + temp.second + " | " + pos.first + " " + pos.second + " | " + Main.clone.pos.first + " " + Main.clone.pos.second);
        }
        if(!Gameloop.keys.contains(KeyEvent.VK_S)){
            shot[2] = false; 
        }




    }


    //code to summon multiple projectiles at once using threads
        // new Thread(() -> {
        //         for (int i = 0; i < 10; i++) {
        //             if(Main.proj.size()>Main.max_proj)break;
                    
        //             padr velocity = new padr();
        //             double tempAngle = (Math.random()*0.3)+0.9;
        //             if(Main.proj.size()>Main.max_proj)return;
        //             velocity.set(hDirection * (20 * Math.cos(tempAngle) + vel.first), Math.sin(tempAngle) *20 + vel.second/2);
        //             Projectile p = new Projectile(pos.first, pos.second,0,  velocity);
        //             Main.queuedProjectiles.add(p);
        //             try {
        //                 Thread.sleep(1); // small delay to spread out load
        //             } catch (InterruptedException ignored) {}
        //         }
        //     }).start();
    //===========================================================================================================



}