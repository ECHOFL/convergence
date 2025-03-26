
package abc.fliqq.convergence.modules.prison.rang.connection;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.services.DatabaseConnector;
import abc.fliqq.convergence.modules.prison.rang.PlayerRank;
import abc.fliqq.convergence.modules.prison.rang.MineRanks;
import abc.fliqq.convergence.modules.prison.rang.manager.MineRankManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Service permettant de charger et sauvegarder les données de rang des joueurs.
 * Les informations sont stockées dans une table SQL (par exemple "PlayerRank")
 * avec deux colonnes pour le rank (représenté par l'identifiant d'une MineRanks) et le niveau de prestige, associées à player_id.
 */
public class PlayerRankDataService {

    private final DatabaseConnector databaseConnector;
    private final String tableName; // Par exemple "PlayerRank"
    private final MineRankManager mineRankManager;

    /**
     * Constructeur de PlayerRankDataService.
     *
     * @param plugin L'instance du plugin Convergence.
     * @param databaseConnector Le connecteur à la base de données.
     * @param mineRankManager Le manager permettant d'obtenir une instance de MineRanks à partir d'un identifiant.
     */
    public PlayerRankDataService(Convergence plugin, DatabaseConnector databaseConnector, MineRankManager mineRankManager) {
        this.databaseConnector = databaseConnector;
        this.mineRankManager = mineRankManager;
        FileConfiguration config = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        ConfigurationSection section = config.getConfigurationSection("ranks");
        this.tableName = section.getString("table");
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée la table PlayerRank si elle n'existe pas.
     * La table contient trois colonnes : player_id, rank et prestige_level.
     *
     * @throws SQLException en cas d'erreur SQL.
     */
    public void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "player_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "`rank` VARCHAR(50) NOT NULL, " +
                "prestige_level INT NOT NULL" +
                ")";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.execute();
        }
    }

    /**
     * Charge les données de rang d'un joueur depuis la base SQL.
     *
     * @param playerId L'identifiant du joueur.
     * @return L'objet PlayerRank, ou null s'il n'existe pas.
     * @throws SQLException en cas d'erreur SQL.
     */
    public PlayerRank loadPlayerRank(UUID playerId) throws SQLException {
        String query = "SELECT `rank`, prestige_level FROM " + tableName + " WHERE player_id = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String rankId = rs.getString("rank");
                    MineRanks mineRank = mineRankManager.getMineRankFromId(rankId);
                    int prestigeLevel = rs.getInt("prestige_level");
                    return new PlayerRank(playerId, mineRank, prestigeLevel);
                }
            }
        }
        return null;
    }

    /**
     * Sauvegarde ou met à jour les données de rang d'un joueur.
     * Utilise une opération UPSERT.
     *
     * @param playerRank Les données de rang du joueur.
     * @throws SQLException en cas d'erreur SQL.
     */
    public void saveOrUpdatePlayerRank(PlayerRank playerRank) throws SQLException {
        String query = "INSERT INTO " + tableName + " (player_id, `rank`, prestige_level) VALUES (?, ?, ?)" +
                       " ON DUPLICATE KEY UPDATE `rank` = ?, prestige_level = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerRank.getPlayerId().toString());
            stmt.setString(2, playerRank.getMineRank().getName());
            stmt.setInt(3, playerRank.getPrestigeLevel());
            stmt.setString(4, playerRank.getMineRank().getName());
            stmt.setInt(5, playerRank.getPrestigeLevel());
            stmt.executeUpdate();
        }
    }

    /**
     * Initialise la ligne de rang pour un joueur s'il n'existe pas déjà en base.
     * Utilise des valeurs par défaut pour le rang et le niveau de prestige.
     *
     * @param playerId L'identifiant du joueur.
     * @param defaultMineRank Le rang par défaut à appliquer.
     * @param defaultPrestigeLevel Le niveau de prestige par défaut.
     * @throws SQLException en cas d'erreur SQL.
     */
    public void initializePlayerRank(UUID playerId, MineRanks defaultMineRank, int defaultPrestigeLevel) throws SQLException {
        // Si la ligne n'existe pas pour ce joueur, on l'insère avec les valeurs par défaut.
        if (loadPlayerRank(playerId) == null) {
            PlayerRank defaultRank = new PlayerRank(playerId, defaultMineRank, defaultPrestigeLevel);
            saveOrUpdatePlayerRank(defaultRank);
        }
    }
}
