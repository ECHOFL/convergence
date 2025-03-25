package abc.fliqq.convergence.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.utils.ColorUtil;

public class MessageManager{

    private final Convergence plugin;
    private FileConfiguration messagesConfig;
    private final Pattern placeholderPattern = Pattern.compile("%([^%]+)%"); 
    
    public MessageManager(Convergence plugin){
        this.plugin = plugin;
        reloadMessages();
    }
    /**
     * Reloads the messages configuration
     */
    public void reloadMessages(){
        messagesConfig=plugin.getConfigManager().getConfig("messages.yml");
        if(messagesConfig != null){
            messagesConfig = plugin.getConfigManager().loadConfig("messages.yml");
        }
    }

        /**
     * Gets a raw message from the configuration
     * 
     * @param path The path to the message
     * @return The raw message
     */
    public String getRawMessage(String path){
        String message = messagesConfig.getString(path);
        if(message==null){
            return "Message not found: " + path;
        }
        return message;
    }

    /**
     * Gets a formatted message with placeholders replaced
     * 
     * @param path The path to the message
     * @param placeholders The placeholders to replace (key-value pairs)
     * @return The formatted message
     */
    public String getMessage(String path, Object... placeholders){
        String message = getRawMessage(path);

        //convert vararg to map
        Map<String, String> placeholderMap = new HashMap<>();
        for(int i=0; i<placeholders.length; i +=2){
            if(i+1 < placeholders.length){
                placeholderMap.put(placeholders[i].toString(), placeholders[i+1].toString());
            }
        }

        //replace placeholders
        Matcher matcher = placeholderPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()){
            String placeholder = matcher.group(1);
            String replacement = placeholderMap.getOrDefault(placeholder, matcher.group(0));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);

        //Apply colors codes
        return ColorUtil.colorize(buffer.toString());
    }

        /**
     * Sends a message to a player or command sender
     * 
     * @param sender The recipient
     * @param path The message path
     * @param placeholders The placeholders to replace
     */
    public void sendMessage(CommandSender sender, String path, Object... placeholders) {
        String message = getMessage(path, placeholders);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }
    /**
     * Sends a message to a player with a prefix
     * 
     * @param player The player
     * @param path The message path
     * @param placeholders The placeholders to replace
     */
    public void sendPrefixedMessage(Player player, String path, Object... placeholders) {
        String prefix = getMessage("prefix");
        String message = getMessage(path, placeholders);
        if (!message.isEmpty()) {
            player.sendMessage(prefix + " " + message);
        }
    }

        
    /**
     * Gets a module-specific message
     * 
     * @param module The module
     * @param path The message path
     * @param placeholders The placeholders to replace
     * @return The formatted message
     */
    public String getModuleMessage(PluginModule module, String path, Object... placeholders) {
        String fullPath = "modules." + module.getName().toLowerCase() + "." + path;
        return getMessage(fullPath, placeholders);
    }

        /**
     * Sends a module-specific message to a player
     * 
     * @param module The module
     * @param player The player
     * @param path The message path
     * @param placeholders The placeholders to replace
     */
    public void sendModuleMessage(PluginModule module, Player player, String path, Object... placeholders) {
        String message = getModuleMessage(module, path, placeholders);
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }
    
}