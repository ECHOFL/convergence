package abc.fliqq.convergence.modules.prison.custompickaxe.manager;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import abc.fliqq.convergence.modules.prison.custompickaxe.PlayerEnchantData;

/**
 * Ce manager gère le cache en mémoire des enchantements pour chaque joueur.
 * Pour chaque joueur identifié par son UUID, on stocke une map associant
 * l'identifiant de l'enchant (String) à une instance de PlayerEnchantData qui contient
 * le CustomEnchant (défini dans CustomEnchant.java) et le niveau actuel du joueur pour cet enchant.
 * Les données sont chargées lors du PlayerJoinEvent et sauvegardées à la déconnexion ou au shutdown/reload.
 */
public class PlayerEnchantsCacheManager {

    // Cache principal : pour chaque joueur, une map des enchantements.
    private final ConcurrentMap<UUID, ConcurrentMap<String, PlayerEnchantData>> playerEnchantsCache = new ConcurrentHashMap<>();

    /**
     * Retourne la map des enchantements pour un joueur donné.
     *
     * @param playerId L'identifiant du joueur.
     * @return La map associant l'identifiant d'enchant à PlayerEnchantData, ou null si non présente.
     */
    public Map<String, PlayerEnchantData> getPlayerEnchants(UUID playerId) {
        return playerEnchantsCache.get(playerId);
    }

    /**
     * Met en cache la map des enchantements pour un joueur.
     *
     * @param playerId L'identifiant du joueur.
     * @param enchantments La map associant l'identifiant d'enchant à PlayerEnchantData.
     */
    public void putPlayerEnchants(UUID playerId, Map<String, PlayerEnchantData> enchantments) {
        playerEnchantsCache.put(playerId, new ConcurrentHashMap<>(enchantments));
    }

    /**
     * Ajoute ou met à jour un enchant pour un joueur dans le cache.
     *
     * @param playerId L'identifiant du joueur.
     * @param enchantId L'identifiant de l'enchant.
     * @param data L'instance de PlayerEnchantData à associer (contenant le CustomEnchant et le niveau).
     */
    public void updatePlayerEnchant(UUID playerId, String enchantId, PlayerEnchantData data) {
        playerEnchantsCache
            .computeIfAbsent(playerId, key -> new ConcurrentHashMap<>())
            .put(enchantId, data);
    }

    /**
     * Supprime l'ensemble des enchantements en cache pour le joueur spécifié.
     *
     * @param playerId L'identifiant du joueur.
     */
    public void removePlayerEnchants(UUID playerId) {
        playerEnchantsCache.remove(playerId);
    }

    /**
     * Retourne l'intégralité du cache, utile lors du shutdown ou reload pour sauvegarder toutes les données.
     *
     * @return La map complète des données d'enchantements en cache.
     */
    public ConcurrentMap<UUID, ConcurrentMap<String, PlayerEnchantData>> getCache() {
        return playerEnchantsCache;
    }
}