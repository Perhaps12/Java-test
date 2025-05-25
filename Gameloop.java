import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.*;
import javax.swing.*;

public class Gameloop extends Canvas implements Runnable, KeyListener {
    private final JFrame frame; //
    public static final int WIDTH = 1920, HEIGHT = 1080; //dimensions of frame
    public static boolean running = false;
    public static final Set<Integer> keys = new HashSet<>(); //key presses
    //fps calculations
    private int frames = 0;
    private int fps = 0;
    private long fpsTimer = System.currentTimeMillis();

    //creates the window in the constructor
    public Gameloop() {
        frame = new JFrame("CS CPT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(true); 
        // frame.setResizable(false);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setExtendedState(frame.MAXIMIZED_BOTH);

        createBufferStrategy(2); 
    }

    //starts running the thread
    public void start() {
        running = true;
        new Thread(this).start();
    }

    //main system that processes every tick at about 60fps
    @Override
    public void run() {
        //frame calculations
        final long frameTime = 1_000_000_000 / 60;
        long lastTime = System.nanoTime();

        //keep gameloop running
        while (running) {

            long now = System.nanoTime();
            if (now - lastTime >= frameTime) {
                //draw/process each element
                render();
                update();

                // more fps calculations
                frames++;
                
                if (System.currentTimeMillis() - fpsTimer >= 1000) {
                    fps = frames;
                    frames = 0;
                    fpsTimer += 1000;
                }
                lastTime = now;
            }

            try {
                Thread.sleep(1);//do this to not fry the cpu
            } catch (Exception e) {}
        }
    }

    public void update() {
        //move all projectiles from multithread safe queue to main arraylist
        while (!Main.queuedProjectiles.isEmpty()) {
            if (Main.proj.size() < Main.max_proj) {
                Main.proj.add(Main.queuedProjectiles.poll());
            } else {
                // Avoid going over the limit
                Main.queuedProjectiles.poll();
            }
        }
        
        //update the player
        Main.player.update();

        //update each projectile, remove if inactive
        for(int i = 0 ; i < Main.proj.size(); i++){
            Main.proj.get(i).update();
            if(!Main.proj.get(i).active){
                Main.proj.remove(i);
                i--;
            }
        }

        //update each npc, remove if inactive
        for(int i = 0 ; i < Main.npc.size(); i++){
            Main.npc.get(i).update();
            if(!Main.npc.get(i).active){
                Main.npc.remove(i);
                i--;
            }
        }
    }



    private void render() {
        //necessary for graphics somehow
        BufferStrategy bs = getBufferStrategy();
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        // Clear screen
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Grid
        g.setColor(Color.GRAY);
        for (int x = 0; x < WIDTH; x += 50)
            g.drawLine(x, 0, x, HEIGHT);
        for (int y = 0; y < HEIGHT; y += 50)
            g.drawLine(0, y, WIDTH, y);

        g.setStroke(new BasicStroke(3));
        
        //walls
        g.setColor(Color.BLACK);
        int I = -1;
        for(pair[] i : walls.bounds){
            I++;
            if(!walls.active[I])continue;
            g.fillRect(i[0].first, i[1].first, i[0].second-i[0].first, i[1].second-i[1].first);
        }

        //draw all npcs and projectiles + their hitboxes
        g.setColor(Color.RED);   
        for(Projectile p : Main.proj){
            p.draw(g);
            g.drawRect(p.box[0].first, p.box[1].first, p.box[0].second-p.box[0].first, p.box[1].second-p.box[1].first);        
        }

        for(Npc p : Main.npc){
            p.draw(g);
            g.drawRect(p.box[0].first, p.box[1].first, p.box[0].second-p.box[0].first, p.box[1].second-p.box[1].first);        
        }
        g.setStroke(new BasicStroke(1));

        //render player
        Main.player.draw(g);

        //add some text on screen, mostly debugging for now
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("FPS: " + fps, 13, 30);
        g.drawString("Projectiles: " + Main.proj.size(), 13, 60);
        if(Main.npc.size()>0)g.drawString("Damage: " + Main.npc.get(0).damage, 13, 90);

        //also necessary for graphics
        g.dispose(); // release Graphics
        bs.show();   // draw to screen
        Toolkit.getDefaultToolkit().sync(); // force render
    }

    //Add/remove pressed keys to a hashset to detect what the user is pressing
    @Override public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
    @Override public void keyReleased(KeyEvent e) {keys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}
}
