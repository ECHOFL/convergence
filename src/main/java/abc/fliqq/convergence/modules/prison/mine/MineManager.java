package abc.fliqq.convergence.modules.prison.mine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.utils.LoggerUtil;
import abc.fliqq.convergence.modules.prison.PrisonModule;
import lombok.Getter;

public class MineManager {
    
    private final PrisonModule module;
    private final Convergence plugin;
    
    // Maps to store different types of mines
    @Getter private final Map<String, Mine> rankMines = new HashMap<>();
    // Map to track reset tasks
    private final Map<String, BukkitTask> resetTasks = new HashMap<>();

    @Getter
    private final Map<Mine, Integer> mineWithPrice = new HashMap<>();
    
    // Configuration values from the module config (not the mine config)
    private final int defaultResetInterval;
    private final boolean broadcastReset;
    private FileConfiguration mineConfig;
    
    /**
     * Constructor for the MineManager.
     * Loads general configuration values, then initializes the mine configuration file,
     * loads the mines, and starts the reset tasks.
     *
     * @param module The prison module instance.
     */
    public MineManager(PrisonModule module) {
        this.module = module;
        this.plugin = module.getPlugin();
        
        // Load configuration values from the module's config
        FileConfiguration config = module.getPrisonConfig();
        this.defaultResetInterval = config.getInt("mines.reset-interval", 300);
        this.broadcastReset = config.getBoolean("mines.broadcast-reset", true);
        
        // Initialize the mines configuration file and load mines
        initializeMineConfig();
        loadMines();
        startResetTasks();
    }
    
    /**
     * Initializes the mine configuration file.
     * If the file does not exist or is empty, it creates it and writes default values.
     */
    private void initializeMineConfig() {
        mineConfig = plugin.getConfigManager().getConfig("modules/prison/mines.yml");
        if (mineConfig == null || mineConfig.getKeys(false).isEmpty()) {
            // Create new configuration file for mines
            mineConfig = plugin.getConfigManager().createModuleConfiguration(module, "mines.yml");
            setupDefaultMines();
            plugin.getConfigManager().saveConfig("modules/prison/mines.yml");
        }
    }
    
    /**
     * Writes default mine data to the configuration if not already present.
     */
    private void setupDefaultMines() {
        // Si l'une des sections principales existe déjà, on considère que le fichier n'est pas vide.
        if (mineConfig.contains("rankMines") || mineConfig.contains("prestigeMines") || mineConfig.contains("personalMines")) {
            return;
        }
        
        // Création de la section pour les mines de rang
        ConfigurationSection rankMinesSection = mineConfig.createSection("rankMines");
        
        // Exemple : Création de la mine A
        ConfigurationSection mineA = rankMinesSection.createSection("A");
        mineA.set("name", "Mine A");
        mineA.set("requiredRank", "A");
        mineA.set("resetInterval", defaultResetInterval);
        mineA.set("price", 0);
        ConfigurationSection compositionA = mineA.createSection("composition");
        compositionA.set("STONE", 70.0);
        compositionA.set("COAL_ORE", 20.0);
        compositionA.set("IRON_ORE", 10.0);
        
        // Exemple : Création de la mine B
        ConfigurationSection mineB = rankMinesSection.createSection("B");
        mineB.set("name", "Mine B");
        mineB.set("requiredRank", "B");
        mineB.set("resetInterval", defaultResetInterval);
        mineB.set("price", 10000);
        ConfigurationSection compositionB = mineB.createSection("composition");
        compositionB.set("STONE", 60.0);
        compositionB.set("COAL_ORE", 20.0);
        compositionB.set("IRON_ORE", 15.0);
        compositionB.set("GOLD_ORE", 5.0);
        
        
    }
    
    /**
     * Loads all mines from the configuration file.
     */
    private void loadMines() {
        // Load rank mines
        ConfigurationSection rankMinesSection = mineConfig.getConfigurationSection("rankMines");
        if (rankMinesSection != null) {
            for (String key : rankMinesSection.getKeys(false)) {
                ConfigurationSection mineSection = rankMinesSection.getConfigurationSection(key);
                if (mineSection == null) continue;
    
                String name = mineSection.getString("name", key);
                String requiredRank = mineSection.getString("requiredRank", key);
                int resetInterval = mineSection.getInt("resetInterval", defaultResetInterval);
                int price = mineSection.getInt("price", 0);


    
                Mine mine = rankMines.get(key); // Vérifie si la mine existe déjà
                if (mine == null) {
                    mine = new Mine(key, name, resetInterval);
                    rankMines.put(key, mine);
                }
    
                // Met à jour les propriétés de la mine
                mine.setName(name);
                mine.setResetInterval(resetInterval);
                mine.setRequiredRank(requiredRank);
    
                if (mineSection.contains("pos1") && mineSection.contains("pos2")) {
                    mine.setPos1(deserializeLocation(mineSection.getConfigurationSection("pos1")));
                    mine.setPos2(deserializeLocation(mineSection.getConfigurationSection("pos2")));
                }
    
                ConfigurationSection compositionSection = mineSection.getConfigurationSection("composition");
                if (compositionSection != null) {
                    mine.clearComposition(); // Efface l'ancienne composition
                    for (String material : compositionSection.getKeys(false)) {
                        try {
                            Material mat = Material.valueOf(material);
                            double percentage = compositionSection.getDouble(material);
                            mine.addMaterial(mat, percentage);
                        } catch (IllegalArgumentException e) {
                            LoggerUtil.warning("Invalid material in mine composition: " + material);
                        }
                    }
                }

                mineWithPrice.put(mine, price);
    
                LoggerUtil.info("Loaded rank mine: " + name);
            }
        }
    }
    
    /**
     * Starts reset tasks for all mines.
     */
    private void startResetTasks() {
        for (Mine mine : rankMines.values()) {
            scheduleResetTask(mine);
        }
    }
    
    /**
     * Schedules a reset task for a mine.
     * 
     * @param mine The mine to schedule a reset for.
     */
    private void scheduleResetTask(Mine mine) {
        if (!mine.hasValidRegion()) return;
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                resetMine(mine);
            }
        }.runTaskTimer(plugin, mine.getResetInterval() * 20L, mine.getResetInterval() * 20L);
        resetTasks.put(mine.getId(), task);
    }
    
    /**
     * Resets a mine by regenerating its blocks.
     * 
     * @param mine The mine to reset.
     */
    public void resetMine(Mine mine) {
        if (!mine.hasValidRegion()) return;
        Location pos1 = mine.getPos1();
        Location pos2 = mine.getPos2();
        if (!pos1.getWorld().equals(pos2.getWorld())) return;
        World world = pos1.getWorld();
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        Collection<Player> playersInMine = world.getNearbyEntitiesByType(
                Player.class,
                pos1.toVector().add(pos2.toVector()).multiply(0.5).toLocation(world),
                (maxX - minX) / 2.0,
                (maxY - minY) / 2.0,
                (maxZ - minZ) / 2.0
        );
        
        Location safeLocation = world.getSpawnLocation(); // Remplacer par une localisation de sécurité définie
        for (Player player : playersInMine) {
            player.teleport(safeLocation);
            player.sendMessage("§eThe mine is resetting! You have been teleported to safety.");
        }
        
        Map<Material, Double> composition = mine.getComposition();
        if (composition.isEmpty()) {
            composition.put(Material.STONE, 100.0);
        }
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material material = getRandomMaterial(composition);
                    block.setType(material);
                }
            }
        }
        
        if (broadcastReset) {
            Bukkit.broadcastMessage("§a§l" + mine.getName() + " §ehas been reset!");
        }
        
        LoggerUtil.info("Reset mine: " + mine.getName());
    }
    
    /**
     * Gets a random material based on composition percentages.
     * 
     * @param composition Map of materials to percentages.
     * @return A randomly selected material.
     */
    private Material getRandomMaterial(Map<Material, Double> composition) {
        double random = Math.random() * 100;
        double cumulative = 0.0;
        for (Map.Entry<Material, Double> entry : composition.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }
        return Material.STONE;
    }
    
    public Mine createRankMine(String id, String name, String requiredRank, int resetInterval) {
        Mine mine = new Mine(id, name, resetInterval);
        mine.setRequiredRank(requiredRank);
        rankMines.put(id, mine);
        return mine;
    }

    
    public Mine getMine(String id) {
       return rankMines.get(id);
    }

    
    
    public Collection<Mine> getAllMines() {
        Collection<Mine> allMines = rankMines.values().stream().collect(Collectors.toList());
        return allMines;
    }
    
    public void saveMines() {
        mineConfig.set("rankMines", null);
        
        ConfigurationSection rankMinesSection = mineConfig.createSection("rankMines");
        for (Map.Entry<String, Mine> entry : rankMines.entrySet()) {
            String id = entry.getKey();
            Mine mine = entry.getValue();
            ConfigurationSection mineSection = rankMinesSection.createSection(id);
            saveMineToConfig(mine, mineSection);
        }
        
        
        plugin.getConfigManager().saveConfig("modules/prison/mines.yml");
    }
    
    private void saveMineToConfig(Mine mine, ConfigurationSection section) {
        section.set("name", mine.getName());
        section.set("resetInterval", mine.getResetInterval());
        section.set("requiredRank", mine.getRequiredRank());
    
        
        if (mine.hasValidRegion()) {
            section.createSection("pos1", serializeLocation(mine.getPos1()));
            section.createSection("pos2", serializeLocation(mine.getPos2()));
        }
        
        ConfigurationSection compositionSection = section.createSection("composition");
        for (Map.Entry<Material, Double> entry : mine.getComposition().entrySet()) {
            compositionSection.set(entry.getKey().name(), entry.getValue());
        }
    }
    
    private Map<String, Object> serializeLocation(Location location) {
        Map<String, Object> map = new HashMap<>();
        map.put("world", location.getWorld().getName());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        return map;
    }
    
    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) return null;
        String worldName = section.getString("world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        return new Location(world, x, y, z);
    }
    
    public void shutdown() {
        for (BukkitTask task : resetTasks.values()) {
            task.cancel();
        }
        resetTasks.clear();
        saveMines();
    }
    
    public void reload() {
        for (BukkitTask task : resetTasks.values()) {
            task.cancel();
        }
        resetTasks.clear();
        rankMines.clear();
        mineConfig = plugin.getConfigManager().getConfig("modules/prison/mines.yml");
        loadMines();
        startResetTasks();
    }
}
