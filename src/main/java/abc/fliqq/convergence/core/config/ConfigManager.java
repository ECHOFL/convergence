package abc.fliqq.convergence.core.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import lombok.Getter;

public class ConfigManager {
    private final Convergence plugin;
    @Getter private FileConfiguration mainConfig;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();

    public ConfigManager(Convergence plugin){
        this.plugin = plugin;
    }

    public void loadConfigs(){
        plugin.saveDefaultConfig();
        mainConfig = plugin.getConfig();
        //LOAD OTHER CONFIGS
        loadConfig("messages.yml");
        //MODULE SPECIFIC CONFIG WILL BE LOADED BY THEIR RESPECTIVE MODULES
        
    }

      /**
     * Loads a specific configuration file
     * 
     * @param fileName The name of the configuration file
     * @return The loaded FileConfiguration
     */
    public FileConfiguration loadConfig(String fileName){
        if(configs.containsKey(fileName)){
            return configs.get(fileName);
        }

        File file = new File(plugin.getDataFolder(), fileName);
        if(!file.exists()){
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
        configFiles.put(fileName, file);
        return config;
    }  
     /**
     * Gets a configuration file by name
     * 
     * @param fileName The name of the configuration file
     * @return The FileConfiguration, or null if not loaded
     */
    public FileConfiguration getConfig(String fileName) {
        return configs.getOrDefault(fileName, null);
    }


     /**
     * Saves a configuration file
     * 
     * @param fileName The name of the configuration file
     */

    public void saveConfig(String fileName){
        if(!configs.containsKey(fileName) || !configFiles.containsKey(fileName)){
            LoggerUtil.warning("Attempted to save config that isn't loaded: " + fileName);
            return;
        }
        try {
            configs.get(fileName).save(configFiles.get(fileName));
        } catch (IOException e) {
            LoggerUtil.severe("Failed to save config: " + fileName, e);
        }
    }

        
    /**
     * Reloads all configuration files
     */
    public void reloadConfigs(){
        //reload main config
        plugin.reloadConfig();
        mainConfig = plugin.getConfig();

        //all others
        for(String fileName : configs.keySet()){
            File file = configFiles.get(fileName);
            configs.put(fileName, YamlConfiguration.loadConfiguration(file));
        }
    }

    /**
    * Creates a module-specific configuration
    * 
    * @param module The module
    * @param fileName The configuration file name
    * @return The loaded FileConfiguration
    */ 

    public FileConfiguration createModuleConfiguration(PluginModule module, String fileName){
        String modulePath = "modules/"+module.getName().toLowerCase()+  "/";
        File moduleFolder = new File(plugin.getDataFolder(), modulePath);
        if(!moduleFolder.exists()){
            moduleFolder.mkdirs();
        }
        String fullPath = modulePath + fileName;
        File file = new File(plugin.getDataFolder(), fullPath);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                LoggerUtil.severe("Failed to create module configuration: " + fullPath, e);
                return null;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fullPath, config);
        configFiles.put(fullPath, file);
        return config;
    }
}
