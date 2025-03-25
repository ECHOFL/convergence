package abc.fliqq.convergence.core;

public abstract class PluginModule {
    
    public abstract String getName();
    public abstract void onEnable();
    public abstract void onDisable();
    public abstract void onReload();
}
