package abc.fliqq.convergence.modules.prison;

import org.bukkit.configuration.file.FileConfiguration;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import abc.fliqq.convergence.modules.prison.connection.PlayerEnchantDataService;
import abc.fliqq.convergence.modules.prison.custompickaxe.listener.PlayerConnectionListener;
import abc.fliqq.convergence.modules.prison.custompickaxe.manager.EnchantsManager;
import abc.fliqq.convergence.modules.prison.mine.MineCommand;
import abc.fliqq.convergence.modules.prison.mine.MineManager;
import abc.fliqq.convergence.modules.prison.rang.connection.PlayerRankDataService;
import abc.fliqq.convergence.modules.prison.rang.manager.MineRankManager;
import lombok.Getter;

public class PrisonModule extends PluginModule {
    private final Convergence plugin;
    @Getter private FileConfiguration prisonConfig;
    
    // Managers
    @Getter private MineManager mineManager;
    @Getter private EnchantsManager enchantsManager;
    @Getter private MineRankManager mineRankManager;

    // Connection to enchant table
    @Getter private PlayerEnchantDataService playerEnchantDataService;

    // Connection to rank table
    @Getter
    private PlayerRankDataService playerRankDataService;
    
    public PrisonModule(Convergence plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Prison";
    }
    
    @Override
    public void onEnable() {
        prisonConfig = plugin.getConfigManager().createModuleConfiguration(this, "config.yml");
        setupDefaultConfig();

        // Initialize managers
        mineManager = new MineManager(this);
        enchantsManager = new EnchantsManager(this);
        mineRankManager = new MineRankManager(this);

        //Connnection to enchant table
        playerEnchantDataService = new PlayerEnchantDataService(plugin, plugin.getDatabaseConnector());
        playerRankDataService = new PlayerRankDataService(plugin, plugin.getDatabaseConnector(), mineRankManager);

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
        prisonConfig = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        
        // Reload managers
        if (mineManager != null) {
            mineManager.reload();
        }

        LoggerUtil.info(getName() + " module has been reloaded!");
    }

    private void setupDefaultConfig() {
        if (prisonConfig.getKeys(false).isEmpty()) {
            // Mine settings
            prisonConfig.set("mines.reset-interval", 300); // 5 minutes
            prisonConfig.set("mines.broadcast-reset", true);
            
            // Default rank settings
            prisonConfig.set("ranks.use-vault-integration", true);
            prisonConfig.set("ranks.default-rank", "A");
            
            // Default prestige settings
            prisonConfig.set("prestige.personal-mine-unlock-level", 5);
            
            // Default personal mine settings
            prisonConfig.set("personal-mines.base-size", 5);
            prisonConfig.set("personal-mines.max-size-level", 10);
            prisonConfig.set("personal-mines.max-reset-speed-level", 10);
            prisonConfig.set("personal-mines.max-composition-level", 10);
            
            // Default enchant settings
            prisonConfig.set("enchants.max-level", 1000);
            prisonConfig.set("enchants.allow-combining", true);

            plugin.getConfigManager().saveConfig("modules/prison/config.yml");
        }
    }

    private void registerCommands() {
        // TODO: Register prison commands
        new MineCommand(plugin, this);
    }  
    
    private void registerListeners() {
        // TODO: Register prison listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), plugin);
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