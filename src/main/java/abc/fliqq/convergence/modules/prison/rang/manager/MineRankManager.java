
package abc.fliqq.convergence.modules.prison.rang.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import abc.fliqq.convergence.modules.prison.PrisonModule;
import abc.fliqq.convergence.modules.prison.mine.Mine;
import abc.fliqq.convergence.modules.prison.rang.MineRanks;

public class MineRankManager {
    private final PrisonModule prisonModule;
    private List<MineRanks> mineRanks;

    public MineRankManager(PrisonModule prisonModule) {
        this.prisonModule = prisonModule;
        this.mineRanks = new ArrayList<>();
        loadRanks();
    }

    private void loadRanks() {
        Map<Mine, Integer> mineWithPrices = prisonModule.getMineManager().getMineWithPrice();
        for (Mine mine : mineWithPrices.keySet()) {
            int price = mineWithPrices.get(mine);
            mineRanks.add(new MineRanks(mine.getId(), price));
        }
    }

    // Getter pour récupérer la liste des MineRanks
    public List<MineRanks> getMineRanks() {
        return mineRanks;
    }
    
    // Optionnel : Méthode pour rafraîchir la liste des rangs si la configuration change
    public void reloadRanks() {
        mineRanks.clear();
        loadRanks();
    }

        /**
     * Récupère le MineRanks correspondant à l'identifiant fourni.
     *
     * @param id L'identifiant de la mine.
     * @return L'instance MineRanks correspondante, ou null si non trouvée.
     */
    public MineRanks getMineRankFromId(String id) {
        for (MineRanks mineRank : mineRanks) {
            if (mineRank.getName().equalsIgnoreCase(id)) {
                return mineRank;
            }
        }
        return null;
    }
}
