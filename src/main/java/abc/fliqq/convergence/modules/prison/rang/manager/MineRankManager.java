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
    
    /**
     * Retourne le rang "suivant" à partir du rang actuel.
     * Si le rang actuel est le dernier de la liste, retourne null.
     *
     * @param currentRank Le rang actuel du joueur.
     * @return Le rang suivant, ou null si le joueur est déjà au rang maximum.
     */
    public MineRanks getNextRank(MineRanks currentRank) {
        int index = mineRanks.indexOf(currentRank);
        if(index == -1 || index == mineRanks.size() - 1) {
            return null;
        }
        return mineRanks.get(index + 1);
    }
}