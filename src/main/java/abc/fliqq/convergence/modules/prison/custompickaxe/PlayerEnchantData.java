package abc.fliqq.convergence.modules.prison.custompickaxe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe encapsule un CustomEnchant (défini dans CustomEnchant.java)
 * et le niveau actuel du joueur pour cet enchant.
 */
@Getter
@Setter
@AllArgsConstructor
public class PlayerEnchantData {
    private CustomEnchant customEnchant;
    private int level;
}