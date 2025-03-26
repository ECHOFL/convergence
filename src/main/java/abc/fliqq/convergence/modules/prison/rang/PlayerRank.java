
package abc.fliqq.convergence.modules.prison.rang;

import java.util.UUID;

/**
 * Représente le rang d'un joueur avec un identifiant, un rang défini par MineRanks et un niveau de prestige.
 */
public class PlayerRank {
    private final UUID playerId;
    private MineRanks mineRank;
    private int prestigeLevel;

    /**
     * Constructeur de PlayerRank.
     *
     * @param playerId L'identifiant unique du joueur.
     * @param mineRank Le rang du joueur représenté par un objet MineRanks.
     * @param prestigeLevel Le niveau de prestige du joueur.
     */
    public PlayerRank(UUID playerId, MineRanks mineRank, int prestigeLevel) {
        this.playerId = playerId;
        this.mineRank = mineRank;
        this.prestigeLevel = prestigeLevel;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public MineRanks getMineRank() {
        return mineRank;
    }

    public void setMineRank(MineRanks mineRank) {
        this.mineRank = mineRank;
    }

    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    public void setPrestigeLevel(int prestigeLevel) {
        this.prestigeLevel = prestigeLevel;
    }
}
