package abc.fliqq.convergence.modules.prison.mine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import lombok.Getter;
import lombok.Setter;

public class Mine {
    @Getter private final String id;
    @Getter @Setter private String name;
    @Getter @Setter private int resetInterval;
    @Getter @Setter private Location pos1;
    @Getter @Setter private Location pos2;
    
    // Composition of the mine (Material -> percentage)
    @Getter private final Map<Material, Double> composition = new HashMap<>();

    // For rank mines
    @Getter @Setter private String requiredRank;
    
    /**
     * Constructor for rank and prestige mines
     */
    public Mine(String id, String name, int resetInterval) {
        this.id = id;
        this.name = name;
        this.resetInterval = resetInterval;
    }
    
    
    /**
     * Full constructor with region
     */
    public Mine(String id, String name, int resetInterval, Location pos1, Location pos2) {
        this.id = id;
        this.name = name;
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

    public void clearComposition() {
        this.composition.clear();
    }
}
