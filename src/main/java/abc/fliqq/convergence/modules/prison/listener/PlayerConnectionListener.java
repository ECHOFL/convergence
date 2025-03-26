package abc.fliqq.convergence.modules.prison.listener;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import abc.fliqq.convergence.modules.prison.PrisonModule;
import abc.fliqq.convergence.modules.prison.custompickaxe.PlayerEnchantData;
import abc.fliqq.convergence.modules.prison.custompickaxe.connection.PlayerEnchantDataService;
import abc.fliqq.convergence.modules.prison.custompickaxe.manager.EnchantsManager;
import abc.fliqq.convergence.modules.prison.custompickaxe.manager.PlayerEnchantsCacheManager;
import abc.fliqq.convergence.modules.prison.rang.PlayerRank;
import abc.fliqq.convergence.modules.prison.rang.connection.PlayerRankDataService;
import abc.fliqq.convergence.modules.prison.rang.manager.MineRankManager;
import abc.fliqq.convergence.modules.prison.rang.manager.PlayerRankCacheManager;

public class PlayerConnectionListener implements Listener {

    private final PlayerEnchantDataService playerEnchantDataService;
    private final PlayerRankDataService playerRankDataService;
    private final MineRankManager mineRankManager;
    private final EnchantsManager enchantsManager;
    private final PlayerEnchantsCacheManager playerEnchantsCacheManager;
    private final PlayerRankCacheManager playerRankCacheManager;
    private final PrisonModule prisonModule;

    public PlayerConnectionListener(PrisonModule module) {
        this.prisonModule = module;
        this.playerEnchantDataService = module.getPlayerEnchantDataService();
        this.playerRankDataService = module.getPlayerRankDataService();
        this.mineRankManager = module.getMineRankManager();
        this.enchantsManager = module.getEnchantsManager();
        this.playerEnchantsCacheManager = module.getPlayerEnchantsCacheManager();
        this.playerRankCacheManager = module.getPlayerRankCacheManager();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final UUID playerId = player.getUniqueId();

        // Chargement asynchrone des données des enchantements
        Bukkit.getScheduler().runTaskAsynchronously(prisonModule.getPlugin(), () -> {
            try {
                // Initialiser la ligne en BDD pour les enchantements en utilisant l'ensemble des clés provenant du EnchantsManager
                playerEnchantDataService.initializePlayerEnchant(playerId.toString(), enchantsManager.getEnchants().keySet());
                // Charger les données d'enchantements en passant la map des CustomEnchant disponibles
                Map<String, PlayerEnchantData> enchantData = playerEnchantDataService.loadPlayerEnchantData(playerId, enchantsManager.getEnchants());
                if (enchantData != null) {
                    playerEnchantsCacheManager.putPlayerEnchants(playerId, enchantData);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Chargement asynchrone des données de rang du joueur
        Bukkit.getScheduler().runTaskAsynchronously(prisonModule.getPlugin(), () -> {
            try {
                // Initialiser la ligne de rang avec des valeurs par défaut (ici, rang "a" et niveau 0)
                playerRankDataService.initializePlayerRank(playerId, mineRankManager.getMineRankFromId("a"), 0);
                // Charger le PlayerRank depuis la base de données
                PlayerRank rank = playerRankDataService.loadPlayerRank(playerId);
                if (rank != null) {
                    playerRankCacheManager.putPlayerRank(playerId, rank);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        final UUID playerId = player.getUniqueId();

        // Sauvegarde asynchrone des données d'enchantements lors du quitter
        Bukkit.getScheduler().runTaskAsynchronously(prisonModule.getPlugin(), () -> {
            Map<String, PlayerEnchantData> enchantData = playerEnchantsCacheManager.getPlayerEnchants(playerId);
            if (enchantData != null) {
                try {
                    playerEnchantDataService.saveOrUpdatePlayerEnchantData(playerId, enchantData);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    playerEnchantsCacheManager.removePlayerEnchants(playerId);
                }
            }
        });

        // Sauvegarde asynchrone des données de rang lors du quitter
        Bukkit.getScheduler().runTaskAsynchronously(prisonModule.getPlugin(), () -> {
            PlayerRank rank = playerRankCacheManager.getPlayerRank(playerId);
            if (rank != null) {
                try {
                    playerRankDataService.saveOrUpdatePlayerRank(rank);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    playerRankCacheManager.removePlayerRank(playerId);
                }
            }
        });
    }
}