package abc.fliqq.convergence.modules.prison.mine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import lombok.Getter;
import lombok.Setter;

public class Mine {
    public enum MineType {
        RANK,       // Standard rank mines (A-Z)
        PRESTIGE,   // Prestige-specific mines
        PERSONAL    // Player-owned personal mines
    }
    
    @Getter private final String id;
    @Getter @Setter private String name;
    @Getter private final MineType type;
    @Getter @Setter private int resetInterval;
    @Getter @Setter private Location pos1;
    @Getter @Setter private Location pos2;
    @Getter @Setter private UUID ownerUUID; // For personal mines
    
    // Composition of the mine (Material -> percentage)
    @Getter private final Map<Material, Double> composition = new HashMap<>();
    
    // For rank mines
    @Getter @Setter private String requiredRank;
    
    // For prestige mines
    @Getter @Setter private int requiredPrestige;
    
    // For personal mines
    @Getter @Setter private int size = 1; // Size level
    @Getter @Setter private int resetSpeed = 1; // Reset speed level
    @Getter @Setter private int compositionLevel = 1; // Composition quality level
    
    /**
     * Constructor for rank and prestige mines
     */
    public Mine(String id, String name, MineType type, int resetInterval) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.resetInterval = resetInterval;
    }
    
    /**
     * Constructor for personal mines
     */
    public Mine(String id, String name, UUID ownerUUID, int resetInterval) {
        this.id = id;
        this.name = name;
        this.type = MineType.PERSONAL;
        this.ownerUUID = ownerUUID;
        this.resetInterval = resetInterval;
    }
    
    /**
     * Full constructor with region
     */
    public Mine(String id, String name, MineType type, int resetInterval, Location pos1, Location pos2) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.resetInterval = resetInterval;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }
    
    /**
     * Checks if the mine has valid region coordinates
     * 
     * @return true if the mine has valid coordinates
     */
    public boolean hasValidRegion() {
        return pos1 != null && pos2 != null;
    }
    
    /**
     * Adds a material to the mine composition
     * 
     * @param material The material to add
     * @param percentage The percentage (0-100) of this material
     */
    public void addMaterial(Material material, double percentage) {
        composition.put(material, percentage);
    }
    
    /**
     * Removes a material from the mine composition
     * 
     * @param material The material to remove
     */
    public void removeMaterial(Material material) {
        composition.remove(material);
    }
    
    /**
     * Checks if this is a personal mine
     * 
     * @return true if this is a personal mine
     */
    public boolean isPersonalMine() {
        return type == MineType.PERSONAL;
    }
    
    /**
     * Checks if this is a rank mine
     * 
     * @return true if this is a rank mine
     */
    public boolean isRankMine() {
        return type == MineType.RANK;
    }
    
    /**
     * Checks if this is a prestige mine
     * 
     * @return true if this is a prestige mine
     */
    public boolean isPrestigeMine() {
        return type == MineType.PRESTIGE;
    }
    
    /**
     * Upgrades a personal mine's size
     * 
     * @return true if the upgrade was successful
     */
    public boolean upgradeSize() {
        if (!isPersonalMine()) return false;
        size++;
        return true;
    }
    
    /**
     * Upgrades a personal mine's reset speed
     * 
     * @return true if the upgrade was successful
     */
    public boolean upgradeResetSpeed() {
        if (!isPersonalMine()) return false;
        resetSpeed++;
        // Adjust reset interval based on speed level
        resetInterval = Math.max(60, 300 - ((resetSpeed - 1) * 30)); // Minimum 1 minute
        return true;
    }
    
    /**
     * Upgrades a personal mine's composition quality
     * 
     * @return true if the upgrade was successful
     */
    public boolean upgradeComposition() {
        if (!isPersonalMine()) return false;
        compositionLevel++;
        return true;
    }
}
