import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    public static ArrayList<Projectile> proj = new ArrayList<>();
    public static ArrayList<Npc> npc = new ArrayList<>();
    public static int max_proj = 1000;
    public static int ind = 0;
    public static Player player = new Player("/Sprites/O-4.png", 600, 100);
    public static final ConcurrentLinkedQueue<Projectile> queuedProjectiles = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        createWall();
        npc.add(new Npc(740, 380, 0));
        
        new Gameloop().start();
    }  

    public static void delay(int n) {
        try {Thread.sleep(n);} 
        catch (Exception e) {}
    }

    public static void addWall(int ind, int x1, int x2, int y1, int y2){
        walls.bounds[ind][0].set(x1, x2);
        walls.bounds[ind][1].set(y1, y2);
        walls.active[ind]=true;
    }

    public static void createWall(){
        for(int i = 0 ; i < walls.numWall; i++){
            for(int j = 0 ; j < 2; j++){
                walls.bounds[i][j] = new pair();
            }
        }


        //Wall left
        addWall(walls.numWall-1, -500, 7, -400, 3500);
        //Wall up
        addWall(walls.numWall-2, -400, 3500, -500, 7);
        //Wall right
        addWall(walls.numWall-3, 1527, 3500, -400, 3500);
        //wall down
        addWall(walls.numWall-4, -400, 3500, 785, 3500);

        // big rectangle
        addWall(4, 0, 500, 490, 700);
        addWall(3, 440, 500, 490, 720);

        // jail
        addWall(6, 1190, 1600, 340, 350);
        addWall(5, 1130, 1140, 400, 1080);

        // hanging fan
        addWall(7, 150, 350, 200, 210);
        addWall(8, 245, 255, 0, 210);

        // floating square
        addWall(9, 700, 900, 150, 200);
        
        addWall(10, 705, 765, 510, 520);


    }

    
    

}





