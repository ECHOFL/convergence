package abc.fliqq.convergence.modules.prison.custompickaxe.manager;

import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.utils.ColorUtil;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import abc.fliqq.convergence.modules.prison.PrisonModule;
import abc.fliqq.convergence.modules.prison.custompickaxe.CustomEnchant;
import lombok.Getter;

public class EnchantsManager {
    private final Convergence plugin;
    private final PrisonModule module;
    @Getter
    private final Map<String, CustomEnchant> enchants = new HashMap<>();

    //Parameters from prison config ?

    private FileConfiguration enchantConfig;

    public EnchantsManager(PrisonModule module) {
        this.plugin = module.getPlugin();
        this.module = module;

        initializeEnchants();
        loadEnchants();
    }

    private void initializeEnchants(){
        enchantConfig = plugin.getConfigManager().getConfig("modules/prison/enchants.yml");
        if (enchantConfig == null || enchantConfig.getKeys(false).isEmpty()){
            //Create new enchants file
            enchantConfig = plugin.getConfigManager().createModuleConfiguration(module, "enchants.yml");

            //Setup default enchants if nothing in file
            setupDefaultEnchants();

            plugin.getConfigManager().saveConfig("modules/prison/enchants.yml");
            plugin.getConfigManager().loadConfig("modules/prison/enchants.yml");
            
        }
    }

    private void setupDefaultEnchants(){
        if(enchantConfig.contains("enchant-definitions")) return;

        ConfigurationSection enchantSection = enchantConfig.createSection("enchant-definitions");
        ConfigurationSection efficiencySection = enchantSection.createSection("efficiency");
        efficiencySection.set("name", "Efficiency");
        efficiencySection.set("max-level", 5);
        efficiencySection.set("cost", 1000);
        efficiencySection.set("cost-multiplier", 2);
        efficiencySection.set("description", "Increases the speed of breaking blocks");
        efficiencySection.set("color", "GREEN");

        ConfigurationSection fortuneSection = enchantSection.createSection("fortune");
        fortuneSection.set("name", "Fortune");
        fortuneSection.set("max-level", 3);
        fortuneSection.set("cost", 1500);
        fortuneSection.set("cost-multiplier", 2);
        fortuneSection.set("description", "Increases the chance of getting more drops");
        fortuneSection.set("color", "GOLD");   

    }   

    private void loadEnchants(){
        ConfigurationSection enchantSection = enchantConfig.getConfigurationSection("enchant-definitions");
        for (String key : enchantSection.getKeys(false)){
            ConfigurationSection section = enchantSection.getConfigurationSection(key);
            if(section == null) continue;

            String name = section.getString("name");
            int maxLevel = section.getInt("max-level");
            int cost = section.getInt("cost");
            int costMultiplier = section.getInt("cost-multiplier");
            String description = section.getString("description");
            TextColor color = ColorUtil.parseColor(section.getString("color"));

            enchants.putIfAbsent(key, new CustomEnchant(key, name, maxLevel, cost, costMultiplier, description, color));

            LoggerUtil.info("Loaded enchant: " + name);
        }
    }

}
