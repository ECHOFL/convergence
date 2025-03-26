package abc.fliqq.convergence.modules.prison.rang;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MineRanks {
    private final String name;
    private final int cost;

    public MineRanks(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }


}
