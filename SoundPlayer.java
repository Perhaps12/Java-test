
import java.io.*;
import javax.sound.sampled.*;

public class SoundPlayer {

    public static void playSound(String filename) {
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
