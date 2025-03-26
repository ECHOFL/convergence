package abc.fliqq.convergence.modules.prison.mine;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Classe abstraite représentant une mine avec les données communes
 * à tous les types de mines (rank, prestige, mine personnelle, etc.).
 */
public abstract class AbstractMine {

    protected final String id;
    protected String name;
    protected int resetInterval;
    protected Location pos1;
    protected Location pos2;
    protected final Map<Material, Double> composition = new HashMap<>();

    public AbstractMine(String id, String name, int resetInterval) {
        this.id = id;
        this.name = name;
        this.resetInterval = resetInterval;
    }
    
    /** 
     * Constructeur complet avec région.
     */
    public AbstractMine(String id, String name, int resetInterval, Location pos1, Location pos2) {
        this(id, name, resetInterval);
        this.pos1 = pos1;
        this.pos2 = pos2;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getResetInterval() {
        return resetInterval;
    }
    
    public void setResetInterval(int resetInterval) {
        this.resetInterval = resetInterval;
    }
    
    public Location getPos1() {
        return pos1;
    }
    
    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }
    
    public Location getPos2() {
        return pos2;
    }
    
    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }
    
    public Map<Material, Double> getComposition() {
        return composition;
    }
    
    public void addMaterial(Material material, double percentage) {
        composition.put(material, percentage);
    }
    
    public void removeMaterial(Material material) {
        composition.remove(material);
    }
    
    /**
     * Méthode abstraite pour rafraîchir ou réinitialiser la mine.
     * La logique sera implémentée différemment selon le type de mine.
     */
    public abstract void resetMine();
    
    @Override
    public String toString() {
        return "AbstractMine [id=" + id + ", name=" + name + ", resetInterval=" + resetInterval 
                + ", pos1=" + pos1 + ", pos2=" + pos2 + ", composition=" + composition + "]";
    }
}