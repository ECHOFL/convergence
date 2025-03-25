package abc.fliqq.convergence.modules.prison;

import org.bukkit.configuration.file.FileConfiguration;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import lombok.Getter;

public class PrisonModule extends PluginModule{
    private final Convergence plugin;
    @Getter private FileConfiguration config;
    
    public PrisonModule(Convergence plugin){
        this.plugin = plugin;
    }

    @Override
    public String getName(){
        return "Prison";
    }
    @Override
    public void onEnable(){
        config = plugin.getConfigManager().createModuleConfiguration(this, "config.yml");
        setupDefaultConfig();

        //Initialize managers

        //Register commands
        registerCommands();

        //Register Listeners
        registerListeners();

        LoggerUtil.info(getName() + "module has been enabled!");
    }
    @Override
    public void onDisable(){
        //Save Data Managers
        
        LoggerUtil.info(getName() + "module has been disabled!");
    }

    @Override
    public void onReload(){
        config = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        

        //RELOAD managers

        LoggerUtil.info(getName()+ "module has been reloaded!");
    }

    private void setupDefaultConfig(){
        if(config.getKeys(false).isEmpty()){
            config.set("mines.reset-interval", 300); // 5 minutes
            config.set("mines.broadcast-reset", true);
            
            // Default rank settings
            config.set("ranks.use-vault-integration", true);
            config.set("ranks.default-rank", "A");
            
            // Default enchant settings
            config.set("enchants.max-level", 1000);
            config.set("enchants.allow-combining", true);

            plugin.getConfigManager().saveConfig("modules/prison/config.yml");
        }
    }

    private void registerCommands(){
        //TODO
    }
    private void registerListeners(){
        //TODO
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
