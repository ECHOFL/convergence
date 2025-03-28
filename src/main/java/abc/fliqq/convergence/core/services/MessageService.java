package abc.fliqq.convergence.core.services;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for handling all message-related functionality
 */
public class MessageService {
    private final Convergence plugin;
    private FileConfiguration messages;
    private String prefix;
    
    // Module-specific prefixes
    private final Map<String, String> modulePrefixes = new HashMap<>();
    
    // Pattern for RGB color codes in the format &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Constructor for the message service
     * 
     * @param plugin The plugin instance
     */
    public MessageService(Convergence plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * Reloads the message service
     */
    public void reload() {
        messages = plugin.getConfigManager().getConfig("messages.yml");
        if (messages == null) {
            plugin.getLogger().severe("Failed to load messages.yml - creating default messages");
            
            // Create a default configuration
            messages = new YamlConfiguration();
            messages.set("general.prefix", "&8[&bConvergence&8] &r");
            messages.set("general.no-permission", "&cYou do not have permission to use this command.");
            messages.set("general.player-only", "&cThis command can only be used by players.");
            messages.set("general.invalid-command", "&cInvalid command. Use &7/{command} help &cfor help.");
            
            // Add prison module messages
            messages.set("prison.prefix", "&8[&bPrison&8] &r");
            messages.set("prison.mine.created", "&aMine &7{name} &ahas been created successfully.");
            messages.set("prison.mine.deleted", "&aMine &7{name} &ahas been deleted successfully.");
            messages.set("prison.mine.invalid-mine", "&cMine &7{name} &cdoes not exist.");
            messages.set("prison.mine.invalid-type", "&cInvalid mine type: &7{type}");
            messages.set("prison.mine.already-exists", "&cMine &7{name} &calready exists.");
            messages.set("prison.mine.no-region", "&cMine &7{name} &cdoes not have a valid region.");
            messages.set("prison.mine.set-pos1", "&aPosition 1 set for mine &7{name}&a.");
            messages.set("prison.mine.set-pos2", "&aPosition 2 set for mine &7{name}&a.");
            messages.set("prison.mine.reset", "&aMine &7{name} &ahas been reset.");
            messages.set("prison.mine.teleported", "&aYou have been teleported to &7{name}&a.");
            messages.set("prison.mine.composition-updated", "&aComposition for mine &7{name} &ahas been updated.");
      
            // Rank commands messages
            messages.set("rank.info", "&aPlayer {player} has rank: &b{rank} &aand prestige level: &b{prestige}");
            messages.set("rank.not-found", "&cNo rank data found for player {player}.");
            messages.set("rank.set-success", "&aSuccessfully updated {player}'s rank to &b{rank} &awith prestige &b{prestige}");
            messages.set("rank.invalid-rank", "&cInvalid rank: {rank}");
            messages.set("rank.invalid-prestige", "&cInvalid prestige value: {prestige}");
            messages.set("rank.already-max", "&cPlayer {player} already has the maximum rank.");
            messages.set("rank.up-success", "&aPlayer {player}'s rank has been upgraded to &b{newrank} &awith prestige &b{prestige}");
            // Save the default configuration
            try {
                File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
                messages.save(messagesFile);
                plugin.getLogger().info("Created default messages.yml file");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save default messages.yml file");
                e.printStackTrace();
            }
        }
        
        // Now we can safely use messages
        prefix = colorize(messages.getString("general.prefix", "&8[&bConvergence&8] &r"));
        
        // Load module prefixes
        modulePrefixes.clear();
        for (String key : messages.getKeys(false)) {
            if (messages.isConfigurationSection(key) && messages.contains(key + ".prefix")) {
                String modulePrefix = colorize(messages.getString(key + ".prefix"));
                modulePrefixes.put(key, modulePrefix);
            }
        }
    }
    
    /**
     * Registers a module with the message service
     * 
     * @param module The module to register
     */
    public void registerModule(PluginModule module) {
        String moduleName = module.getName().toLowerCase();
        if (!modulePrefixes.containsKey(moduleName) && messages.contains(moduleName + ".prefix")) {
            String modulePrefix = colorize(messages.getString(moduleName + ".prefix"));
            modulePrefixes.put(moduleName, modulePrefix);
        }
    }
    
    /**
     * Gets the prefix for a module
     * 
     * @param module The module name
     * @return The module prefix, or the default prefix if not found
     */
    public String getModulePrefix(String module) {
        return modulePrefixes.getOrDefault(module.toLowerCase(), prefix);
    }
    
    /**
     * Sends a message to a command sender
     * 
     * @param sender The command sender
     * @param key The message key
     */
    public void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, new HashMap<>());
    }
    
    /**
     * Sends a message to a command sender with placeholders
     * 
     * @param sender The command sender
     * @param key The message key
     * @param placeholders The placeholders to replace
     */
    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
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
    public void broadcastMessage(String key) {
        broadcastMessage(key, new HashMap<>());
    }
    
    /**
     * Broadcasts a message to all players with placeholders
     * 
     * @param key The message key
     * @param placeholders The placeholders to replace
     */
    public void broadcastMessage(String key, Map<String, String> placeholders) {
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
    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }
    
    /**
     * Gets a message from the configuration with placeholders
     * 
     * @param key The message key
     * @param placeholders The placeholders to replace
     * @return The message
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getString(key);
        if (message == null) {
            return "";
        }
        
        // Determine which prefix to use
        String currentPrefix = prefix;
        String[] parts = key.split("\\.");
        if (parts.length > 1 && modulePrefixes.containsKey(parts[0])) {
            currentPrefix = modulePrefixes.get(parts[0]);
        }
        
        // Add prefix if message doesn't already have one and isn't a prefix itself
        if (!message.startsWith(currentPrefix) && !key.endsWith(".prefix")) {
            message = currentPrefix + message;
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return colorize(message);
    }
    
    /**
     * Colorizes a string with color codes
     * 
     * @param text The text to colorize
     * @return The colorized text
     */
    public String colorize(String text) {
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
     * Strips color codes from a string
     * 
     * @param text The text to strip
     * @return The stripped text
     */
    public String stripColor(String text) {
        if (text == null) {
            return "";
        }
        
        // First strip hex colors
        text = HEX_PATTERN.matcher(text).replaceAll("");
        
        // Then strip standard colors
        return ChatColor.stripColor(text);
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
    public void sendTitle(Player player, String titleKey, String subtitleKey, int fadeIn, int stay, int fadeOut) {
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
    public void sendTitle(Player player, String titleKey, String subtitleKey, Map<String, String> placeholders, int fadeIn, int stay, int fadeOut) {
        String title = getMessage(titleKey, placeholders);
        String subtitle = getMessage(subtitleKey, placeholders);
        
        // Remove any prefixes for titles
        if (title.startsWith(prefix)) {
            title = title.substring(prefix.length());
        }
        
        if (subtitle.startsWith(prefix)) {
            subtitle = subtitle.substring(prefix.length());
        }
        
        // Remove module prefixes
        for (String modulePrefix : modulePrefixes.values()) {
            if (title.startsWith(modulePrefix)) {
                title = title.substring(modulePrefix.length());
            }
            if (subtitle.startsWith(modulePrefix)) {
                subtitle = subtitle.substring(modulePrefix.length());
            }
        }
        
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    /**
     * Sends an action bar message to a player
     * 
     * @param player The player
     * @param key The message key
     */
    public void sendActionBar(Player player, String key) {
        sendActionBar(player, key, new HashMap<>());
    }
    
    /**
     * Sends an action bar message to a player with placeholders
     * 
     * @param player The player
     * @param key The message key
     * @param placeholders The placeholders to replace
     */
    public void sendActionBar(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        
        // Remove any prefixes for action bars
        if (message.startsWith(prefix)) {
            message = message.substring(prefix.length());
        }
        
        // Remove module prefixes
        for (String modulePrefix : modulePrefixes.values()) {
            if (message.startsWith(modulePrefix)) {
                message = message.substring(modulePrefix.length());
            }
        }
        
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                                   net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }
    
    /**
     * Formats a message with placeholders
     * 
     * @param message The message to format
     * @param placeholders The placeholders to replace
     * @return The formatted message
     */
    public String format(String message, Map<String, String> placeholders) {
        if (message == null) {
            return "";
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return colorize(message);
    }
}
