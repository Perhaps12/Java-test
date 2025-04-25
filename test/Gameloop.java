import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.*;
import javax.swing.*;

public class Gameloop extends Canvas implements Runnable, KeyListener {
    private final JFrame frame;
    private boolean running = false;
    public static final Set<Integer> keys = new HashSet<>();
    public static final int WIDTH = 1920, HEIGHT = 1080;
    

    public static void main(String[] args) {
        new Gameloop().start();
    }

    public Gameloop() {
        frame = new JFrame("BufferStrategy Game");
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
        final int fps = 60;
        final long frameTime = 1_000_000_000 / fps;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            if (now - lastTime >= frameTime) {
                render();
                update();
                lastTime = now;
            }

            try {
                Thread.sleep(1);
            } catch (Exception e) {}
        }
    }

    private void update() {
        
        Main.player.update();
        for(int i = 0 ; i < Main.proj.size(); i++){
            Main.proj.get(i).update();
            if(!Main.proj.get(i).active){
                Main.proj.remove(i);
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

        //wall
        g.setColor(Color.BLACK);
        int I = -1;
        for(pair[] i : walls.bounds){
            I++;
            if(!walls.active[I])continue;
            g.fillRect(i[0].first, i[1].first, i[0].second-i[0].first, i[1].second-i[1].first);
        }

        for(Projectile p : Main.proj){
            p.draw(g);
        }
        Main.player.draw(g);


        g.dispose(); // release Graphics
        bs.show();   // draw to screen
        Toolkit.getDefaultToolkit().sync(); // force render
    }

    @Override public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
    @Override public void keyReleased(KeyEvent e) {keys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}
}
