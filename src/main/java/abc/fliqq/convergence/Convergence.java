package abc.fliqq.convergence;

import org.bukkit.plugin.java.JavaPlugin;

import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.config.ConfigManager;
import abc.fliqq.convergence.core.services.DatabaseConnector;
import abc.fliqq.convergence.core.services.MessageService;
import abc.fliqq.convergence.modules.prison.PrisonModule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Convergence extends JavaPlugin {
    
    private static Convergence instance;
    
    @Getter private ConfigManager configManager;
    @Getter private MessageService messageService;
    @Getter private DatabaseConnector databaseConnector;
    
    private final List<abc.fliqq.convergence.core.PluginModule> modules = new ArrayList<>();
    
    @Override
    public void onEnable() {
        instance = this;


        // Initialize managers
        configManager = new ConfigManager(this);
        
        // Load configuration
        configManager.loadConfig("config.yml");
        configManager.loadConfig("messages.yml");
        
        //Initialize Db connection
        databaseConnector = new DatabaseConnector(configManager.getMainConfig());

        //initilize services
        messageService = new MessageService(this);


        // Initialize modules
        initializeModules();
        
        getLogger().info("Convergence has been enabled!");
    }
    
    @Override
    public void onDisable() {
        //Disable modules in reverse order
        for (int i = modules.size() - 1; i >= 0; i--) {
            modules.get(i).onDisable();
        }
        
        getLogger().info("Convergence has been disabled!");

        // close db connection
        databaseConnector.shutdown();
    }
    
    /**
     * Initializes all plugin modules
     */
    private void initializeModules() {
        // Check which modules are enabled in config
        if (configManager.getMainConfig().getBoolean("modules.prison", true)) {
            PrisonModule prisonModule = new PrisonModule(this);
            modules.add(prisonModule);
            prisonModule.onEnable();
        }
        
        // Add other modules here
    }
    
    /**
     * Reloads the plugin
     */
    public void reload() {
        // Reload configuration
        configManager.reloadConfig("config.yml");
        configManager.reloadConfig("messages.yml");
        
        // Reload message service
        messageService.reload();
        
        // Reload modules
        for (PluginModule module : modules) {
            module.onReload();
        }
        
        getLogger().info("Convergence has been reloaded!");
    }
    
    /**
     * Gets the plugin instance
     * 
     * @return The plugin instance
     */
    public static Convergence getInstance() {
        return instance;
    }
}