package abc.fliqq.convergence.modules.prison.rang.manager;

import abc.fliqq.convergence.modules.prison.rang.PlayerRank;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Ce manager gère le cache en mémoire des données PlayerRank.
 * Les données sont chargées lors de la connexion et sauvegardées au logout/shutdown.
 */
public class PlayerRankCacheManager {
    // Utilisation d'une ConcurrentHashMap pour un accès sûr en multithreading.
    private final ConcurrentMap<UUID, PlayerRank> playerRankCache = new ConcurrentHashMap<>();

    /**
     * Retourne la donnée PlayerRank pour l'identifiant fourni.
     *
     * @param playerId L'identifiant du joueur.
     * @return Le PlayerRank en cache, ou null s'il n'est pas présent.
     */
    public PlayerRank getPlayerRank(UUID playerId) {
        return playerRankCache.get(playerId);
    }

    /**
     * Ajoute ou met à jour le PlayerRank en cache pour le joueur.
     *
     * @param playerId L'identifiant du joueur.
     * @param playerRank Les données de rang à mettre en cache.
     */
    public void putPlayerRank(UUID playerId, PlayerRank playerRank) {
        playerRankCache.put(playerId, playerRank);
    }

    /**
     * Supprime la donnée du cache pour le joueur spécifié.
     *
     * @param playerId L'identifiant du joueur.
     */
    public void removePlayerRank(UUID playerId) {
        playerRankCache.remove(playerId);
    }

    /**
     * Retourne la map complète du cache. Cette méthode peut être utilisée lors du shutdown pour sauvegarder toutes les données.
     *
     * @return La map du cache playerRank.
     */
    public ConcurrentMap<UUID, PlayerRank> getCache() {
        return playerRankCache;
    }
}