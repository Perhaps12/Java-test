import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager handles all audio playback for the game
 * Supports both sound effects and background music
 */
public class AudioManager {
    private static AudioManager instance;
    private Map<String, Clip> soundEffects;
    private Clip backgroundMusic;
    private boolean soundEnabled = true;
    private float masterVolume = 1.0f;
    private float effectsVolume = 1.0f;
    private float musicVolume = 0.7f;
    
    // Audio file paths
    private static final String AUDIO_PATH = "Static/";
    
    private AudioManager() {
        soundEffects = new HashMap<>();
        loadAudioFiles();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
      /**
     * Load all audio files at startup
     */
    private void loadAudioFiles() {        try {              // Load sound effects
            loadSoundEffect("jump", AUDIO_PATH + "Jump15.wav");
            loadSoundEffect("explosion", AUDIO_PATH + "Explosion 3.wav");
            loadSoundEffect("death", AUDIO_PATH + "Boom2.wav");
            loadSoundEffect("dash", AUDIO_PATH + "dash.wav");
            loadSoundEffect("swap", AUDIO_PATH + "swap.wav");
            
            loadBackgroundMusic(AUDIO_PATH + "level.wav");
            
            System.out.println("Audio files loaded");
        } catch (Exception e) {
            System.err.println("Error loading audio files: " + e.getMessage());
            e.printStackTrace();
            soundEnabled = false; // Disable sound if loading fails
        }
    }
    
    /**
     * Load a sound effect
     */
    private void loadSoundEffect(String name, String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("Audio file not found: " + filePath);
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // Set volume
            setClipVolume(clip, effectsVolume * masterVolume);
            
            soundEffects.put(name, clip);
            System.out.println("Loaded sound effect: " + name);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to load sound effect " + name + ": " + e.getMessage());
        }
    }
    
    /**
     * Load background music
     */
    private void loadBackgroundMusic(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("Background music file not found: " + filePath);
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            
            // Set volume and loop
            setClipVolume(backgroundMusic, musicVolume * masterVolume);
            
            System.out.println("Loaded background music");
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to load background music: " + e.getMessage());
        }
    }
    
    /**
     * Set volume for a clip
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(Math.max(0.01f, volume)) / Math.log(10.0) * 20.0);
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        }
    }
    
    /**
     * Play a sound effect
     */
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        Clip clip = soundEffects.get(soundName);
        if (clip != null) {
            // Stop the clip if it's already playing
            if (clip.isRunning()) {
                clip.stop();
            }
            // Reset to beginning and play
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.err.println("Sound effect not found: " + soundName);
        }
    }
    
    /**
     * Play background music (looped)
     */
    public void playBackgroundMusic() {
        if (!soundEnabled || backgroundMusic == null) return;
        
        if (!backgroundMusic.isRunning()) {
            backgroundMusic.setFramePosition(0);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    
    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    /**
     * Play jump sound
     */
    public void playJumpSound() {
        playSound("jump");
    }
    
    /**
     * Play hard landing sound (explosion)
     */
    public void playHardLandingSound() {
        playSound("explosion");
    }
      /**
     * Play death sound
     */
    public void playDeathSound() {
        playSound("death");
    }
      /**
     * Play dash sound
     */
    public void playDashSound() {
        playSound("dash");
    }
    
    /**
     * Play swap sound
     */
    public void playSwapSound() {
        playSound("swap");
    }
    
    /**
     * Set master volume (0.0 to 1.0)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateAllVolumes();
    }
    
    /**
     * Set effects volume (0.0 to 1.0)
     */
    public void setEffectsVolume(float volume) {
        this.effectsVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateEffectsVolumes();
    }
    
    /**
     * Set music volume (0.0 to 1.0)
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (backgroundMusic != null) {
            setClipVolume(backgroundMusic, musicVolume * masterVolume);
        }
    }
    
    /**
     * Enable or disable sound
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        } else {
            playBackgroundMusic();
        }
    }
    
    /**
     * Update all effect volumes
     */
    private void updateEffectsVolumes() {
        for (Clip clip : soundEffects.values()) {
            setClipVolume(clip, effectsVolume * masterVolume);
        }
    }
    
    /**
     * Update all volumes
     */
    private void updateAllVolumes() {
        updateEffectsVolumes();
        if (backgroundMusic != null) {
            setClipVolume(backgroundMusic, musicVolume * masterVolume);
        }
    }
    
    /**
     * Clean up audio resources
     */
    public void cleanup() {
        stopBackgroundMusic();
        
        for (Clip clip : soundEffects.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        
        if (backgroundMusic != null) {
            backgroundMusic.close();
        }
        
        soundEffects.clear();
    }
    
    // Getters
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    public float getEffectsVolume() {
        return effectsVolume;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
}
