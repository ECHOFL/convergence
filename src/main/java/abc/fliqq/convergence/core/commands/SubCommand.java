package abc.fliqq.convergence.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Represents a subcommand for the command framework
 */
public abstract class SubCommand {
    private final String name;
    private final String permission;
    private final boolean playerOnly;
    private final List<String> aliases;
    
    /**
     * Constructor for a subcommand
     * 
     * @param name The subcommand name
     * @param permission The permission required to use this subcommand
     * @param playerOnly Whether this subcommand can only be used by players
     */
    public SubCommand(String name, String permission, boolean playerOnly) {
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.aliases = new ArrayList<>();
    }
    
    /**
     * Gets the name of the subcommand
     * 
     * @return The subcommand name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the permission required to use this subcommand
     * 
     * @return The permission
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Checks if this subcommand can only be used by players
     * 
     * @return true if this subcommand can only be used by players
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }
    
    /**
     * Gets the aliases of this subcommand
     * 
     * @return The aliases
     */
    public List<String> getAliases() {
        return aliases;
    }
    
    /**
     * Sets the aliases of this subcommand
     * 
     * @param aliases The aliases
     * @return This subcommand instance for chaining
     */
    public SubCommand setAliases(String... aliases) {
        this.aliases.clear();
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }
    
    /**
     * Executes the subcommand
     * 
     * @param sender The command sender
     * @param args The subcommand arguments
     * @return true if the subcommand was executed successfully
     */
    public abstract boolean execute(CommandSender sender, String[] args);
    
    /**
     * Tab completes the subcommand
     * 
     * @param sender The command sender
     * @param args The subcommand arguments
     * @return A list of tab completions
     */
    public abstract List<String> tabComplete(CommandSender sender, String[] args);
}