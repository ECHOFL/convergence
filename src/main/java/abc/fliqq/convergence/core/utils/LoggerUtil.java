package abc.fliqq.convergence.core.utils;

import abc.fliqq.convergence.Convergence;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {
    
    private static Logger logger;
    
    /**
     * Initializes the logger
     */
    private static void initLogger() {
        if (logger == null) {
            logger = Convergence.getInstance().getLogger();
        }
    }
    
    /**
     * Logs an info message
     * 
     * @param message The message to log
     */
    public static void info(String message) {
        initLogger();
        logger.info(message);
    }
    
    /**
     * Logs a warning message
     * 
     * @param message The message to log
     */
    public static void warning(String message) {
        initLogger();
        logger.warning(message);
    }
    
    /**
     * Logs a severe message
     * 
     * @param message The message to log
     */
    public static void severe(String message) {
        initLogger();
        logger.severe(message);
    }
    
    /**
     * Logs a severe message with an exception
     * 
     * @param message The message to log
     * @param throwable The exception
     */
    public static void severe(String message, Throwable throwable) {
        initLogger();
        logger.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Logs a debug message (only if debug is enabled)
     * 
     * @param message The message to log
     */
    public static void debug(String message) {
        initLogger();
        if (Convergence.getInstance().getConfigManager().getMainConfig().getBoolean("debug", false)) {
            logger.info("[DEBUG] " + message);
        }
    }
    
    /**
     * Logs a message to the console
     * 
     * @param message The message to log
     */
    public static void console(String message) {
        // Use MessageService instead of ColorUtil
        Bukkit.getConsoleSender().sendMessage(Convergence.getInstance().getMessageService().colorize(message));
    }
}