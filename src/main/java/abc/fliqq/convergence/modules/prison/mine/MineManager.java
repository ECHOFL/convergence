package abc.fliqq.convergence.modules.prison.mine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import abc.fliqq.convergence.modules.prison.mine.Mine.MineType;
import lombok.Getter;

public class MineManager {
    
    private final PrisonModule module;
    private final Convergence plugin;
    
    // Maps to store different types of mines
    @Getter private final Map<String, Mine> rankMines = new HashMap<>();
    @Getter private final Map<String, Mine> prestigeMines = new HashMap<>();
    @Getter private final Map<UUID, Mine> personalMines = new HashMap<>();
    
    // Map to track reset tasks
    private final Map<String, BukkitTask> resetTasks = new HashMap<>();
    
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
        ConfigurationSection compositionA = mineA.createSection("composition");
        compositionA.set("STONE", 70.0);
        compositionA.set("COAL_ORE", 20.0);
        compositionA.set("IRON_ORE", 10.0);
        
        // Exemple : Création de la mine B
        ConfigurationSection mineB = rankMinesSection.createSection("B");
        mineB.set("name", "Mine B");
        mineB.set("requiredRank", "B");
        mineB.set("resetInterval", defaultResetInterval);
        ConfigurationSection compositionB = mineB.createSection("composition");
        compositionB.set("STONE", 60.0);
        compositionB.set("COAL_ORE", 20.0);
        compositionB.set("IRON_ORE", 15.0);
        compositionB.set("GOLD_ORE", 5.0);
        
        // Création des sections pour les mines de prestige et personnelles (initialement vides)
        mineConfig.createSection("prestigeMines");
        mineConfig.createSection("personalMines");
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
    
                Mine mine = rankMines.get(key); // Vérifie si la mine existe déjà
                if (mine == null) {
                    mine = new Mine(key, name, MineType.RANK, resetInterval);
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
    
                LoggerUtil.info("Loaded rank mine: " + name);
            }
        }
        
        // Load prestige mines
        ConfigurationSection prestigeMinesSection = mineConfig.getConfigurationSection("prestigeMines");
        if (prestigeMinesSection != null) {
            for (String key : prestigeMinesSection.getKeys(false)) {
                ConfigurationSection mineSection = prestigeMinesSection.getConfigurationSection(key);
                if (mineSection == null) continue;
                
                String name = mineSection.getString("name", "Prestige " + key);
                int requiredPrestige = mineSection.getInt("requiredPrestige", Integer.parseInt(key));
                int resetInterval = mineSection.getInt("resetInterval", defaultResetInterval);
                
                Mine mine = new Mine(key, name, MineType.PRESTIGE, resetInterval);
                mine.setRequiredPrestige(requiredPrestige);
                
                if (mineSection.contains("pos1") && mineSection.contains("pos2")) {
                    mine.setPos1(deserializeLocation(mineSection.getConfigurationSection("pos1")));
                    mine.setPos2(deserializeLocation(mineSection.getConfigurationSection("pos2")));
                }
                
                ConfigurationSection compositionSection = mineSection.getConfigurationSection("composition");
                if (compositionSection != null) {
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
                
                prestigeMines.put(key, mine);
                LoggerUtil.info("Loaded prestige mine: " + name);
            }
        }
        
        // Load personal mines
        ConfigurationSection personalMinesSection = mineConfig.getConfigurationSection("personalMines");
        if (personalMinesSection != null) {
            for (String key : personalMinesSection.getKeys(false)) {
                ConfigurationSection mineSection = personalMinesSection.getConfigurationSection(key);
                if (mineSection == null) continue;
                try {
                    UUID ownerUUID = UUID.fromString(key);
                    String name = mineSection.getString("name", "Personal Mine");
                    int resetInterval = mineSection.getInt("resetInterval", defaultResetInterval);
                    
                    Mine mine = new Mine(key, name, ownerUUID, resetInterval);
                    
                    if (mineSection.contains("pos1") && mineSection.contains("pos2")) {
                        mine.setPos1(deserializeLocation(mineSection.getConfigurationSection("pos1")));
                        mine.setPos2(deserializeLocation(mineSection.getConfigurationSection("pos2")));
                    }
                    
                    mine.setSize(mineSection.getInt("size", 1));
                    mine.setResetSpeed(mineSection.getInt("resetSpeed", 1));
                    mine.setCompositionLevel(mineSection.getInt("compositionLevel", 1));
                    
                    ConfigurationSection compositionSection = mineSection.getConfigurationSection("composition");
                    if (compositionSection != null) {
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
                    
                    personalMines.put(ownerUUID, mine);
                    LoggerUtil.info("Loaded personal mine for player: " + ownerUUID);
                } catch (IllegalArgumentException e) {
                    LoggerUtil.warning("Invalid UUID in personal mines: " + key);
                }
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
        for (Mine mine : prestigeMines.values()) {
            scheduleResetTask(mine);
        }
        for (Mine mine : personalMines.values()) {
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
        Mine mine = new Mine(id, name, MineType.RANK, resetInterval);
        mine.setRequiredRank(requiredRank);
        rankMines.put(id, mine);
        return mine;
    }
    
    public Mine createPrestigeMine(String id, String name, int requiredPrestige, int resetInterval) {
        Mine mine = new Mine(id, name, MineType.PRESTIGE, resetInterval);
        mine.setRequiredPrestige(requiredPrestige);
        prestigeMines.put(id, mine);
        return mine;
    }
    
    public Mine createPersonalMine(UUID ownerUUID, String name, int resetInterval) {
        Mine mine = new Mine(ownerUUID.toString(), name, ownerUUID, resetInterval);
        personalMines.put(ownerUUID, mine);
        return mine;
    }
    
    public Mine getMine(String id, MineType type) {
        switch (type) {
            case RANK:
                return rankMines.get(id);
            case PRESTIGE:
                return prestigeMines.get(id);
            case PERSONAL:
                try {
                    return personalMines.get(UUID.fromString(id));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    public Mine getPersonalMine(UUID ownerUUID) {
        return personalMines.get(ownerUUID);
    }
    
    public Collection<Mine> getMinesByType(MineType type) {
        switch (type) {
            case RANK:
                return rankMines.values();
            case PRESTIGE:
                return prestigeMines.values();
            case PERSONAL:
                return personalMines.values();
            default:
                return null;
        }
    }
    
    public Collection<Mine> getAllMines() {
        Collection<Mine> allMines = rankMines.values().stream().collect(Collectors.toList());
        allMines.addAll(prestigeMines.values());
        allMines.addAll(personalMines.values());
        return allMines;
    }
    
    public void saveMines() {
        mineConfig.set("rankMines", null);
        mineConfig.set("prestigeMines", null);
        mineConfig.set("personalMines", null);
        
        ConfigurationSection rankMinesSection = mineConfig.createSection("rankMines");
        ConfigurationSection prestigeMinesSection = mineConfig.createSection("prestigeMines");
        ConfigurationSection personalMinesSection = mineConfig.createSection("personalMines");
        
        for (Map.Entry<String, Mine> entry : rankMines.entrySet()) {
            String id = entry.getKey();
            Mine mine = entry.getValue();
            ConfigurationSection mineSection = rankMinesSection.createSection(id);
            saveMineToConfig(mine, mineSection);
        }
        
        for (Map.Entry<String, Mine> entry : prestigeMines.entrySet()) {
            String id = entry.getKey();
            Mine mine = entry.getValue();
            ConfigurationSection mineSection = prestigeMinesSection.createSection(id);
            saveMineToConfig(mine, mineSection);
        }
        
        for (Map.Entry<UUID, Mine> entry : personalMines.entrySet()) {
            UUID ownerUUID = entry.getKey();
            Mine mine = entry.getValue();
            ConfigurationSection mineSection = personalMinesSection.createSection(ownerUUID.toString());
            saveMineToConfig(mine, mineSection);
        }
        
        plugin.getConfigManager().saveConfig("modules/prison/mines.yml");
    }
    
    private void saveMineToConfig(Mine mine, ConfigurationSection section) {
        section.set("name", mine.getName());
        section.set("resetInterval", mine.getResetInterval());
        
        if (mine.isRankMine()) {
            section.set("requiredRank", mine.getRequiredRank());
        } else if (mine.isPrestigeMine()) {
            section.set("requiredPrestige", mine.getRequiredPrestige());
        } else if (mine.isPersonalMine()) {
            section.set("size", mine.getSize());
            section.set("resetSpeed", mine.getResetSpeed());
            section.set("compositionLevel", mine.getCompositionLevel());
        }
        
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
        prestigeMines.clear();
        personalMines.clear();
        mineConfig = plugin.getConfigManager().getConfig("modules/prison/mines.yml");
        loadMines();
        startResetTasks();
    }
}
