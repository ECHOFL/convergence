package abc.fliqq.convergence.modules.prison.connection;

import abc.fliqq.convergence.core.services.DatabaseConnector;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service spécifique pour gérer les niveaux d'enchantements des joueurs dans le module Prison.
 * Le nom de la table est défini dans prison/config.yml (section enchants.db).
 */
public class PlayerEnchantDataService {

    private final DatabaseConnector databaseConnector;
    private final String enchantTable;

    /**
     * Construit le service en utilisant le DatabaseConnector et la configuration du module Prison.
     *
     * @param databaseConnector Instance centrale pour la connexion à la BDD.
     * @param prisonConfig      La configuration propre au module Prison (chargée depuis prison/config.yml).
     */
    public PlayerEnchantDataService(DatabaseConnector databaseConnector, FileConfiguration prisonConfig) {
        this.databaseConnector = databaseConnector;
        // Récupération de la section 'enchants.db' depuis prison/config.yml
        ConfigurationSection enchantsSection = prisonConfig.getConfigurationSection("enchants");
        this.enchantTable = enchantsSection.getString("table");
    }

    /**
     * Charge les niveaux d'enchantements d'un joueur.
     *
     * @param playerId identifiant du joueur.
     * @return une Map contenant les clés d'enchantement et leurs niveaux.
     * @throws SQLException en cas d'erreur SQL.
     */
    public Map<String, Integer> loadPlayerEnchantLevels(UUID playerUuid) throws SQLException {
        String query = "SELECT enchant_key, level FROM " + enchantTable + " WHERE player_id = ?";
        Map<String, Integer> enchantLevels = new HashMap<>();

        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String enchantKey = rs.getString("enchant_key");
                    int level = rs.getInt("level");
                    enchantLevels.put(enchantKey, level);
                }
            }
        }
        return enchantLevels;
    }

    /**
     * Sauvegarde ou met à jour le niveau d'un enchantement pour un joueur.
     * Utilise un UPSERT (INSERT ... ON DUPLICATE KEY UPDATE) pour MySQL.
     *
     * @param playerId   identifiant du joueur.
     * @param enchantKey clé de l'enchantement (en minuscule).
     * @param level      niveau de l'enchantement.
     * @throws SQLException en cas d'erreur SQL.
     */
    public void saveOrUpdatePlayerEnchantLevel(String playerId, String enchantKey, int level) throws SQLException {
        String query = "INSERT INTO " + enchantTable + " (player_id, enchant_key, level) VALUES (?, ?, ?)" +
                " ON DUPLICATE KEY UPDATE level = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerId);
            stmt.setString(2, enchantKey);
            stmt.setInt(3, level);
            stmt.setInt(4, level);
            stmt.executeUpdate();
        }
    }


    /**
     * Vérifie si une ligne existe pour le joueur dans la table des niveaux d'enchantements.
     * Si non, insère une nouvelle ligne avec toutes les colonnes d'enchant initialisées à 0.
     *
     * @param playerId    identifiant du joueur.
     * @param enchantKeys ensemble des clés d'enchantements disponibles (obtenu depuis EnchantsManager).
     * @throws SQLException en cas d'erreur d'accès à la BDD.
     */
    public void ensurePlayerEnchantRow(String playerId, Set<String> enchantKeys) throws SQLException {
        // Vérification de l'existence d'une ligne pour le joueur.
        String querySelect = "SELECT player_id FROM " + enchantTable + " WHERE player_id = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(querySelect)) {
            selectStmt.setString(1, playerId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    // Le joueur existe déjà, on ne fait rien.
                    return;
                }
            }
        }
    
        // Construction dynamique de la requête d'insertion.
        // On démarre avec la colonne player_id puis on ajoute chacune des clés avec la valeur 0.
        StringBuilder columns = new StringBuilder("player_id");
        StringBuilder placeholders = new StringBuilder("?");
    
        for (String key : enchantKeys) {
            columns.append(", ").append(key);
            placeholders.append(", 0");
        }
    
        String queryInsert = "INSERT INTO " + enchantTable + " (" + columns.toString() + ") VALUES (" + placeholders.toString() + ")";
    
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(queryInsert)) {
            insertStmt.setString(1, playerId);
            insertStmt.executeUpdate();
        }
    }

}