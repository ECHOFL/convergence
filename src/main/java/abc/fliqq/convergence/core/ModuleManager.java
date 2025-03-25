package abc.fliqq.convergence.core;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    
    private final List<PluginModule> modules = new ArrayList<>();
    
    public void registerModule(PluginModule module){
        modules.add(module);
        module.onEnable();
    }
    public void disableModules(){
        for(PluginModule module : modules){
            module.onDisable();
        }
    }
    public void reloadModules(){
        for(PluginModule module : modules){
            module.onReload();
        }
    }
    public List<PluginModule> getModules(){
        return modules;
    }
}
