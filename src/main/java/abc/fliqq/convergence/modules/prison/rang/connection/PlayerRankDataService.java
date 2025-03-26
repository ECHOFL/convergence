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
     * @param databaseConnector Le connecteur à la base de données.
     * @param tableName Le nom de la table.
     * @param mineRankManager Le manager permettant d'obtenir une instance de MineRanks à partir d'un identifiant.
     */
    public PlayerRankDataService(Convergence plugin, DatabaseConnector databaseConnector, MineRankManager mineRankManager) {
        this.databaseConnector = databaseConnector;
        FileConfiguration config = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        ConfigurationSection section = config.getConfigurationSection("ranks");
        this.tableName = section.getString("table");
        this.mineRankManager = mineRankManager;
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
                "rank VARCHAR(50) NOT NULL, " +
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
        String query = "SELECT rank, prestige_level FROM " + tableName + " WHERE player_id = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Lecture de l'identifiant du rang depuis la base SQL
                    String rankId = rs.getString("rank");
                    // Utilisation du MineRankManager pour obtenir le MineRanks correspondant
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
        String query = "INSERT INTO " + tableName + " (player_id, rank, prestige_level) VALUES (?, ?, ?)" +
                       " ON DUPLICATE KEY UPDATE rank = ?, prestige_level = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerRank.getPlayerId().toString());
            // Puisque MineRanks.getId() n'existe pas, on peut supposer que MineRanks redéfinit toString() pour retourner son identifiant.
            stmt.setString(2, playerRank.getMineRank().toString());
            stmt.setInt(3, playerRank.getPrestigeLevel());
            stmt.setString(4, playerRank.getMineRank().toString());
            stmt.setInt(5, playerRank.getPrestigeLevel());
            stmt.executeUpdate();
        }
    }
}
