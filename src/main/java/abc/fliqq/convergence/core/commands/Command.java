package abc.fliqq.convergence.core.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base command class for the command framework
 */
public abstract class Command implements CommandExecutor, TabCompleter {
    protected final Convergence plugin;
    protected final String name;
    protected final String permission;
    protected final boolean playerOnly;
    protected String description;
    protected String usage;
    protected List<String> aliases;
    
    // Subcommands
    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    
    /**
     * Constructor for a command
     * 
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use this command
     * @param playerOnly Whether this command can only be used by players
     */
    public Command(Convergence plugin, String name, String permission, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.description = "";
        this.usage = "/" + name;
        this.aliases = new ArrayList<>();
        
        // Register the command
        register();
    }
    
    /**
     * Registers the command with Bukkit
     */
    private void register() {
        org.bukkit.command.PluginCommand command = plugin.getCommand(name);
        
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
            
            if (!description.isEmpty()) {
                command.setDescription(description);
            }
            
            if (!usage.isEmpty()) {
                command.setUsage(usage);
            }
            
            if (!aliases.isEmpty()) {
                command.setAliases(aliases);
            }
        } else {
            plugin.getLogger().warning("Failed to register command: " + name);
        }
    }
    
    /**
     * Sets the description of the command
     * 
     * @param description The description
     * @return This command instance for chaining
     */
    public Command setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the usage of the command
     * 
     * @param usage The usage
     * @return This command instance for chaining
     */
    public Command setUsage(String usage) {
        this.usage = usage;
        return this;
    }
    
    /**
     * Sets the aliases of the command
     * 
     * @param aliases The aliases
     * @return This command instance for chaining
     */
    public Command setAliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
    }
    
    /**
     * Sets the aliases of the command
     * 
     * @param aliases The aliases as an array
     * @return This command instance for chaining
     */
    public Command setAliases(String... aliases) {
        this.aliases = Arrays.asList(aliases);
        return this;
    }
    
    /**
     * Adds a subcommand to this command
     * 
     * @param subCommand The subcommand to add
     * @return This command instance for chaining
     */
    public Command addSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
        return this;
    }
    
    /**
     * Gets a subcommand by name
     * 
     * @param name The name of the subcommand
     * @return The subcommand, or null if not found
     */
    public SubCommand getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }
    
    /**
     * Executes the command
     */
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        // Check if sender is a player if command is player-only
        if (playerOnly && !(sender instanceof Player)) {
            plugin.getMessageService().sendMessage(sender, "general.player-only");
            return true;
        }
        
        // Check permission
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            plugin.getMessageService().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        // Handle subcommands
        if (args.length > 0 && !subCommands.isEmpty()) {
            SubCommand subCommand = getSubCommand(args[0]);
            if (subCommand != null) {
                // Check subcommand permission
                if (!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
                    plugin.getMessageService().sendMessage(sender, "general.no-permission");
                    return true;
                }
                
                // Check if subcommand is player-only
                if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
                    plugin.getMessageService().sendMessage(sender, "general.player-only");
                    return true;
                }
                
                // Execute subcommand
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.execute(sender, subArgs);
            }
        }
        
        // Execute main command
        return execute(sender, args);
    }
    
    /**
     * Tab completes the command
     */
    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        // Check permission
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            return new ArrayList<>();
        }
        
        // Handle subcommand tab completion
        if (args.length == 1 && !subCommands.isEmpty()) {
            List<String> completions = new ArrayList<>();
            for (SubCommand subCommand : subCommands.values()) {
                // Only add each subcommand once (avoid duplicates from aliases)
                if (!completions.contains(subCommand.getName()) && 
                    (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission()))) {
                    completions.add(subCommand.getName());
                }
            }
            return completions;
        } else if (args.length > 1 && !subCommands.isEmpty()) {
            SubCommand subCommand = getSubCommand(args[0]);
            if (subCommand != null) {
                // Check subcommand permission
                if (!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
                    return new ArrayList<>();
                }
                
                // Tab complete subcommand
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subArgs);
            }
        }
        
        // Tab complete main command
        return tabComplete(sender, args);
    }
    
    /**
     * Executes the command
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was executed successfully
     */
    public abstract boolean execute(CommandSender sender, String[] args);
    
    /**
     * Tab completes the command
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return A list of tab completions
     */
    public abstract List<String> tabComplete(CommandSender sender, String[] args);
}