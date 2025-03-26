package abc.fliqq.convergence.modules.prison.connection;

import abc.fliqq.convergence.Convergence;
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
    private final FileConfiguration enchantConfig;
    private final FileConfiguration prisonConfig;
    /**
     * Construit le service en utilisant le DatabaseConnector et la configuration du module Prison.
     *
     * @param databaseConnector Instance centrale pour la connexion à la BDD.
     * @param prisonConfig      La configuration propre au module Prison (chargée depuis prison/config.yml).
     */
    public PlayerEnchantDataService(Convergence plugin, DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
        enchantConfig = plugin.getConfigManager().getConfig("modules/prison/enchants.yml");
        ConfigurationSection enchantsSection = enchantConfig.getConfigurationSection("enchant-definitions");
        prisonConfig = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        ConfigurationSection enchantsDbSection = prisonConfig.getConfigurationSection("enchants");
        enchantTable = enchantsDbSection.getString("table");

        // Vérification de l'existence de la table et des colonnes
        try {
            verifyAndUpdateTable(enchantsSection.getKeys(false));
        } catch (SQLException e) {
            e.printStackTrace();
        }
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



/**
 * Vérifie si la table des niveaux d'enchantements existe et
 * s'assure qu'elle possède une colonne pour chaque enchantement défini dans enchants.yml.
 * 
 * - Si la table n'existe pas, elle est créée avec la colonne 'player_id' (clé primaire)
 *   et une colonne INT DEFAULT 0 pour chaque clé d'enchantement.
 * - Si la table existe déjà, les colonnes manquantes sont ajoutées.
 *
 * IMPORTANT : L'ensemble enchantKeys doit contenir uniquement les clés provenant de la section
 * "enchant-definitions" du fichier enchants.yml (ex : efficiency, fortune, explosion, etc.).
 *
 * @param enchantKeys Ensemble des clés d'enchantements.
 * @throws SQLException En cas d'erreur lors des opérations SQL.
 */
public void verifyAndUpdateTable(Set<String> enchantKeys) throws SQLException {
    try (Connection connection = databaseConnector.getConnection()) {
        // Vérifier si la table existe déjà
        boolean tableExists = false;
        var metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(null, null, enchantTable, new String[]{"TABLE"})) {
            if (tables.next()) {
                tableExists = true;
            }
        }

        if (!tableExists) {
            // La table n'existe pas, on la crée avec 'player_id' et une colonne pour chaque enchantement.
            StringBuilder createQuery = new StringBuilder("CREATE TABLE " + enchantTable + " (player_id VARCHAR(36) PRIMARY KEY");
            for (String key : enchantKeys) {
                // Encadrer le nom de la colonne avec des backticks
                createQuery.append(", `").append(key).append("` INT DEFAULT 0");
            }
            createQuery.append(")");
            try (PreparedStatement stmt = connection.prepareStatement(createQuery.toString())) {
                stmt.executeUpdate();
            }
        } else {
            // La table existe : vérifier et ajouter les colonnes manquantes.
            Map<String, Boolean> existingColumns = new HashMap<>();
            try (ResultSet columns = metaData.getColumns(null, null, enchantTable, null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    existingColumns.put(columnName.toLowerCase(), true);
                }
            }

            // Pour chaque clé d'enchantement, on ajoute la colonne si elle n'existe pas
            for (String key : enchantKeys) {
                if (!existingColumns.containsKey(key.toLowerCase())) {
                    String alterQuery = "ALTER TABLE " + enchantTable + " ADD COLUMN `" + key + "` INT DEFAULT 0";
                    try (PreparedStatement stmt = connection.prepareStatement(alterQuery)) {
                        stmt.executeUpdate();
                    }
                }
            }
        }
    }
}



}