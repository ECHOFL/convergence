package abc.fliqq.convergence.core.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.PluginModule;
import abc.fliqq.convergence.core.utils.LoggerUtil;

/**
 * Manages configuration files for the plugin
 */
public class ConfigManager {
    private final Convergence plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();
    
    /**
     * Constructor for the config manager
     * 
     * @param plugin The plugin instance
     */
    public ConfigManager(Convergence plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets the main configuration file
     * 
     * @return The main configuration
     */
    public FileConfiguration getMainConfig() {
        return getConfig("config.yml");
    }
    
    /**
     * Gets a configuration file
     * 
     * @param name The name of the configuration file
     * @return The configuration, or null if not found
     */
    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }
    
    /**
     * Loads a configuration file
     * 
     * @param name The name of the configuration file
     * @return The loaded configuration
     */
    public FileConfiguration loadConfig(String name) {
        try {
            File configFile = new File(plugin.getDataFolder(), name);
            
            // Log the file path for debugging
            plugin.getLogger().info("Attempting to load config file: " + configFile.getAbsolutePath());
            
            // Create parent directories if they don't exist
            if (!configFile.getParentFile().exists()) {
                plugin.getLogger().info("Creating parent directories for: " + name);
                configFile.getParentFile().mkdirs();
            }
            
            // Create the file if it doesn't exist
            if (!configFile.exists()) {
                plugin.getLogger().info("Config file doesn't exist, creating: " + name);
                try {
                    // Save default config from resources
                    InputStream defaultConfigStream = plugin.getResource(name);
                    if (defaultConfigStream != null) {
                        plugin.getLogger().info("Found default resource for: " + name);
                        plugin.saveResource(name, false);
                        plugin.getLogger().info("Saved default resource for: " + name);
                    } else {
                        plugin.getLogger().warning("No default resource found for: " + name);
                        configFile.createNewFile();
                        plugin.getLogger().info("Created empty file for: " + name);
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to create configuration file: " + name);
                    e.printStackTrace();
                    return null;
                }
            }
            
            // Load the configuration
            plugin.getLogger().info("Loading configuration from file: " + name);
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            
            // Check if config is valid
            if (config == null) {
                plugin.getLogger().severe("Failed to load configuration from file: " + name);
                return null;
            }
            
            // Store the configuration and file
            configs.put(name, config);
            configFiles.put(name, configFile);
            
            plugin.getLogger().info("Successfully loaded configuration: " + name);
            return config;
        } catch (Exception e) {
            plugin.getLogger().severe("Unexpected error loading configuration: " + name);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Reloads a configuration file
     * 
     * @param name The name of the configuration file
     * @return The reloaded configuration
     */
    public FileConfiguration reloadConfig(String name) {
        // Remove the configuration from the cache
        configs.remove(name);
        
        // Load the configuration again
        return loadConfig(name);
    }
    
    /**
     * Saves a configuration file
     * 
     * @param name The name of the configuration file
     */
    public void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        File configFile = configFiles.get(name);
        
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                LoggerUtil.severe("Failed to save configuration file: " + name, e);
            }
        }
    }
    
    /**
     * Creates a module configuration file
     * 
     * @param module The module
     * @param name The name of the configuration file
     * @return The created configuration
     */
    public FileConfiguration createModuleConfiguration(PluginModule module, String name) {
        String modulePath = "modules/" + module.getName().toLowerCase() + "/" + name;
        File configFile = new File(plugin.getDataFolder(), modulePath);
        
        // Create parent directories if they don't exist
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        
        // Create the file if it doesn't exist
        if (!configFile.exists()) {
            try {
                // Save default config from resources
                InputStream defaultConfigStream = plugin.getResource(modulePath);
                if (defaultConfigStream != null) {
                    plugin.saveResource(modulePath, false);
                } else {
                    configFile.createNewFile();
                }
            } catch (IOException e) {
                LoggerUtil.severe("Failed to create module configuration file: " + modulePath, e);
                return null;
            }
        }
        
        // Load the configuration
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        // Store the configuration and file
        configs.put(modulePath, config);
        configFiles.put(modulePath, configFile);
        
        return config;
    }
    
    /**
     * Reloads all configuration files
     */
    public void reloadAllConfigs() {
        for (String name : configs.keySet()) {
            reloadConfig(name);
        }
    }
}