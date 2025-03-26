package abc.fliqq.convergence.modules.prison.custompickaxe;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomPickaxe {
    private final UUID ownerUUID;
    private final Material pickaxeSkin;
    private final int level;
    private final Map<CustomEnchant, Integer> enchants;
    private final int blocksMined;
    private final int blocksBroken;
    private final int price;
    
}
