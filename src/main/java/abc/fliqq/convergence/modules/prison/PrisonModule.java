package abc.fliqq.convergence.modules.prison;

import org.bukkit.configuration.file.FileConfiguration;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import abc.fliqq.convergence.modules.prison.mine.MineManager;
import lombok.Getter;

public class PrisonModule extends PluginModule {
    private final Convergence plugin;
    @Getter private FileConfiguration config;
    
    // Managers
    @Getter private MineManager mineManager;
    
    public PrisonModule(Convergence plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Prison";
    }
    
    @Override
    public void onEnable() {
        config = plugin.getConfigManager().createModuleConfiguration(this, "config.yml");
        setupDefaultConfig();

        // Initialize managers
        mineManager = new MineManager(this);

        // Register commands
        registerCommands();

        // Register Listeners
        registerListeners();

        LoggerUtil.info(getName() + " module has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save Data Managers
        if (mineManager != null) {
            mineManager.shutdown();
        }
        
        LoggerUtil.info(getName() + " module has been disabled!");
    }

    @Override
    public void onReload() {
        config = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        
        // Reload managers
        if (mineManager != null) {
            mineManager.reload();
        }

        LoggerUtil.info(getName() + " module has been reloaded!");
    }

    private void setupDefaultConfig() {
        if (config.getKeys(false).isEmpty()) {
            // Mine settings
            config.set("mines.reset-interval", 300); // 5 minutes
            config.set("mines.broadcast-reset", true);
            
            // Default rank settings
            config.set("ranks.use-vault-integration", true);
            config.set("ranks.default-rank", "A");
            
            // Default prestige settings
            config.set("prestige.personal-mine-unlock-level", 5);
            
            // Default personal mine settings
            config.set("personal-mines.base-size", 5);
            config.set("personal-mines.max-size-level", 10);
            config.set("personal-mines.max-reset-speed-level", 10);
            config.set("personal-mines.max-composition-level", 10);
            
            // Default enchant settings
            config.set("enchants.max-level", 1000);
            config.set("enchants.allow-combining", true);

            plugin.getConfigManager().saveConfig("modules/prison/config.yml");
        }
    }

    private void registerCommands() {
        // TODO: Register prison commands
        // Example: new MineCommand(this);
    }
    
    private void registerListeners() {
        // TODO: Register prison listeners
        // Example: plugin.getServer().getPluginManager().registerEvents(new MineListener(this), plugin);
    }
    
    /**
     * Gets the main plugin instance
     * 
     * @return The Convergence plugin
     */
    public Convergence getPlugin() {
        return plugin;
    }
}