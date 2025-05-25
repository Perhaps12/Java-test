import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.*;
import javax.swing.*;

public class Gameloop extends Canvas implements Runnable, KeyListener {
    private final JFrame frame;
    public static boolean running = false;
    public static final Set<Integer> keys = new HashSet<>();
    public static final int WIDTH = 1920, HEIGHT = 1080;
    private int frames = 0;
    private int fps = 0;
    private long fpsTimer = System.currentTimeMillis();

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

    public void start() {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        final long frameTime = 1_000_000_000 / 60;
        long lastTime = System.nanoTime();

        while (running) {

            long now = System.nanoTime();
            if (now - lastTime >= frameTime) {
                render();
                update();
                // System.out.println(Main.proj.size());
                frames++;

                if (System.currentTimeMillis() - fpsTimer >= 1000) {
                    fps = frames;
                    frames = 0;
                    fpsTimer += 1000;
                }

                lastTime = now;
                lastTime = now;
            }

            try {
                Thread.sleep(1);
            } catch (Exception e) {}
        }
    }

    public void update() {
        while (!Main.queuedProjectiles.isEmpty()) {
            if (Main.proj.size() < Main.max_proj) {
                Main.proj.add(Main.queuedProjectiles.poll());
            } else {
                // break; // Avoid going over the limit
                Main.queuedProjectiles.poll();
            }
        }

        // for(Projectile p : Main.proj){
        //     System.out.print(p.ID + " ");
        // }System.out.println("");
        
        Main.player.update();

        for(int i = 0 ; i < Main.proj.size(); i++){
            Main.proj.get(i).update();
            if(!Main.proj.get(i).active){
                Main.proj.remove(i);
                i--;
            }
        }

        

        for(int i = 0 ; i < Main.npc.size(); i++){
            Main.npc.get(i).update();
            if(!Main.npc.get(i).active){
                Main.npc.remove(i);
                i--;
            }
        }
    }

    private void render() {
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
        
        //wall
        g.setColor(Color.BLACK);
        int I = -1;
        for(pair[] i : walls.bounds){
            I++;
            if(!walls.active[I])continue;
            g.fillRect(i[0].first, i[1].first, i[0].second-i[0].first, i[1].second-i[1].first);
        }

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

        Main.player.draw(g);

        // String keyPress = "| ";
        // for(int i : keys){
        //     keyPress+=i + " ";
        // }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("FPS: " + fps, 13, 30);
        g.drawString("Projectiles: " + Main.proj.size(), 13, 60);
        if(Main.npc.size()>0)g.drawString("Damage: " + Main.npc.get(0).damage, 13, 90);
        // g.drawString("Keys: " + keyPress, 13, 120);


        g.dispose(); // release Graphics
        bs.show();   // draw to screen
        Toolkit.getDefaultToolkit().sync(); // force render
    }

    @Override public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
    @Override public void keyReleased(KeyEvent e) {keys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}
}
