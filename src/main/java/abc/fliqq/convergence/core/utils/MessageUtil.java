package abc.fliqq.convergence.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling messages
 */
public class MessageUtil {
    private static Convergence plugin;
    private static FileConfiguration messages;
    private static String prefix;
    
    // Pattern for RGB color codes in the format &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Initializes the message utility
     * 
     * @param plugin The plugin instance
     */
    public static void init(Convergence plugin) {
        MessageUtil.plugin = plugin;
        reload();
    }
    
    /**
     * Reloads the messages configuration
     */
    public static void reload() {
        messages = plugin.getConfigManager().getConfig("messages.yml");
        prefix = getColoredString(messages.getString("general.prefix", "&8[&bConvergence&8] &r"));
    }
    
    /**
     * Sends a message to a command sender
     * 
     * @param sender The command sender
     * @param key The message key
     */
    public static void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, new HashMap<>());
    }
    
    /**
     * Sends a message to a command sender with placeholders
     * 
     * @param sender The command sender
     * @param key The message key
     * @param placeholders The placeholders to replace
     */
    public static void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }
    
    /**
     * Broadcasts a message to all players
     * 
     * @param key The message key
     */
    public static void broadcastMessage(String key) {
        broadcastMessage(key, new HashMap<>());
    }
    
    /**
     * Broadcasts a message to all players with placeholders
     * 
     * @param key The message key
     * @param placeholders The placeholders to replace
     */
    public static void broadcastMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        if (!message.isEmpty()) {
            Bukkit.broadcastMessage(message);
        }
    }
    
    /**
     * Gets a message from the configuration
     * 
     * @param key The message key
     * @return The message
     */
    public static String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }
    
    /**
     * Gets a message from the configuration with placeholders
     * 
     * @param key The message key
     * @param placeholders The placeholders to replace
     * @return The message
     */
    public static String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getString(key);
        if (message == null) {
            return "";
        }
        
        // Add prefix if message doesn't already have one
        if (!message.startsWith(prefix) && !key.contains("prefix")) {
            message = prefix + message;
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return getColoredString(message);
    }
    
    /**
     * Colorizes a string with color codes
     * 
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String getColoredString(String text) {
        if (text == null) {
            return "";
        }
        
        // Replace hex color codes
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString());
        }
        
        matcher.appendTail(buffer);
        text = buffer.toString();
        
        // Replace standard color codes
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Sends a title to a player
     * 
     * @param player The player
     * @param titleKey The title message key
     * @param subtitleKey The subtitle message key
     * @param fadeIn The fade in time in ticks
     * @param stay The stay time in ticks
     * @param fadeOut The fade out time in ticks
     */
    public static void sendTitle(Player player, String titleKey, String subtitleKey, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, titleKey, subtitleKey, new HashMap<>(), fadeIn, stay, fadeOut);
    }
    
    /**
     * Sends a title to a player with placeholders
     * 
     * @param player The player
     * @param titleKey The title message key
     * @param subtitleKey The subtitle message key
     * @param placeholders The placeholders to replace
     * @param fadeIn The fade in time in ticks
     * @param stay The stay time in ticks
     * @param fadeOut The fade out time in ticks
     */
    public static void sendTitle(Player player, String titleKey, String subtitleKey, Map<String, String> placeholders, int fadeIn, int stay, int fadeOut) {
        String title = getMessage(titleKey, placeholders);
        String subtitle = getMessage(subtitleKey, placeholders);
        
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    /**
     * Sends an action bar message to a player
     * 
     * @param player The player
     * @param key The message key
     */
    public static void sendActionBar(Player player, String key) {
        sendActionBar(player, key, new HashMap<>());
    }
    
    /**
     * Sends an action bar message to a player with placeholders
     * 
     * @param player The player
     * @param key The message key
     * @param placeholders The placeholders to replace
     */
    public static void sendActionBar(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                                   net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }
}