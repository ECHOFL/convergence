package abc.fliqq.convergence.modules.prison.custompickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.TextColor;

@Getter
@Setter
@AllArgsConstructor
public class CustomEnchant {
    private final String id;
    private final String name;
    private final int maxLvl;
    private final int cost;
    private final int costMultiplier;
    private final String description;
    private final TextColor color;
}
