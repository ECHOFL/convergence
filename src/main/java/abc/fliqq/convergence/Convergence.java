package abc.fliqq.convergence;

import org.bukkit.plugin.java.JavaPlugin;

import abc.fliqq.convergence.core.ConfigManager;
import abc.fliqq.convergence.core.ModuleManager;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import abc.fliqq.convergence.core.utils.MessageManager;
import lombok.Getter;

public class Convergence extends JavaPlugin{

    @Getter
    private static Convergence instance;
    @Getter
    private ModuleManager moduleManager;
    @Getter
    private ConfigManager configManager;
    @Getter
    private MessageManager messageManager;
    // private MessageManager

    @Override
    public void onEnable(){
        instance=this;

        // this.configManager=new ConfigManager(this);
        this.moduleManager=new ModuleManager();

        //load configurations
        configManager.loadConfigs();

        registerModules();

        LoggerUtil.info("Convergence has been enabled!");
    }

    @Override
    public void onDisable(){
        if(moduleManager!=null){
            moduleManager.disableModules();
        }
        LoggerUtil.info("Convergence has been disabled!");
    }
    private void registerModules(){
        // moduleManager.registerModule(new ExampleModule());
    }

    public void reload(){
        configManager.reloadConfigs();
        messageManager.reloadMessages();
        moduleManager.reloadModules();
        LoggerUtil.info("Convergence has been reloaded!");
    }
}