import java.io.File;
import javax.sound.sampled.*;

public class SoundPlayer {

    //apparently this works, kevin can you change this

    public static void playSound(String filename) {
        playSound(filename, 1.0);
    }

    public static void playSound(String filename, final double volume) {
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                final Clip[] clipHolder = new Clip[1];
                clipHolder[0] = AudioSystem.getClip();
                clipHolder[0].open(audioIn);

                // Set volume
                FloatControl gainControl = (FloatControl) clipHolder[0].getControl(FloatControl.Type.MASTER_GAIN);
                double clampedVolume = Math.min(1.0, Math.max(0.0001, volume)); // prevent log(0)
                float dB = (float) (Math.log10(clampedVolume) * 20);
                gainControl.setValue(dB);

                // Auto-cleanup
                clipHolder[0].addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clipHolder[0].close(); // release resources
                    }
                });

                clipHolder[0].start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
