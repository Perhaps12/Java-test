import java.io.*;

/**
 * Utility class for file operations and logging
 */
public class GameUtils {
    private static final String LOG_FILE = "game_log.txt";

    /**
     * Private constructor to prevent instantiation
     */
    private GameUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Log a message to the log file
     */
    public static void log(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(System.currentTimeMillis() + ": " + message);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Log an error with stack trace
     */
    public static void logError(String message, Exception e) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(System.currentTimeMillis() + " ERROR: " + message);
            e.printStackTrace(out);
        } catch (IOException ex) {
            System.err.println("Error writing to log file: " + ex.getMessage());
        }
    }

    /**
     * Calculate distance between two points
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Generate a random value between min and max
     */
    public static double random(double min, double max) {
        return min + Math.random() * (max - min);
    }

    /**
     * Clamp a value between min and max
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
