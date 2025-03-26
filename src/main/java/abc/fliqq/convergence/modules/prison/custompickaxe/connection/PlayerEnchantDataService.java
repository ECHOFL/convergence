package abc.fliqq.convergence.modules.prison.custompickaxe.connection;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.services.DatabaseConnector;
import abc.fliqq.convergence.modules.prison.custompickaxe.CustomEnchant;
import abc.fliqq.convergence.modules.prison.custompickaxe.PlayerEnchantData;

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
 * Service spécifique pour gérer les enchantements des joueurs dans le module Prison.
 * Le nom de la table est défini dans prison/config.yml (section enchants.db).
 *
 * Ce refactoring permet d'utiliser une Map associant chaque enchant (clé String)
 * à une instance PlayerEnchantData (contenant le CustomEnchant et le niveau).
 */
public class PlayerEnchantDataService {

    private final DatabaseConnector databaseConnector;
    private final String enchantTable;
    private final FileConfiguration enchantConfig;
    private final FileConfiguration prisonConfig;
    // Ensemble des clés d'enchantements tel que défini dans enchants.yml (section enchant-definitions)
    private final Set<String> enchantKeys;

    /**
     * Construit le service en utilisant le DatabaseConnector et la configuration du module Prison.
     *
     * @param plugin            Instance du plugin Convergence.
     * @param databaseConnector Instance centrale pour la connexion à la BDD.
     */
    public PlayerEnchantDataService(Convergence plugin, DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
        enchantConfig = plugin.getConfigManager().getConfig("modules/prison/enchants.yml");
        ConfigurationSection enchantsSection = enchantConfig.getConfigurationSection("enchant-definitions");
        this.enchantKeys = enchantsSection.getKeys(false);
        prisonConfig = plugin.getConfigManager().getConfig("modules/prison/config.yml");
        ConfigurationSection enchantsDbSection = prisonConfig.getConfigurationSection("enchants");
        enchantTable = enchantsDbSection.getString("table");

        // Vérification de l'existence de la table et des colonnes
        try {
            verifyAndUpdateTable(enchantKeys);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge les données d'enchantements d'un joueur.
     * Pour chaque clé d'enchantement (définie dans la configuration),
     * récupère son niveau via le ResultSet et crée une instance PlayerEnchantData.
     *
     * @param playerUuid   identifiant du joueur.
     * @param customEnchants Map associant la clé d'enchant à l'instance CustomEnchant (fournie par EnchantsManager).
     * @return Une Map associant chaque clé d'enchant à son PlayerEnchantData.
     * @throws SQLException en cas d'erreur SQL.
     */
    public Map<String, PlayerEnchantData> loadPlayerEnchantData(UUID playerUuid, Map<String, CustomEnchant> customEnchants) throws SQLException {
        String query = "SELECT * FROM " + enchantTable + " WHERE player_id = ?";
        Map<String, PlayerEnchantData> playerEnchants = new HashMap<>();

        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (String key : enchantKeys) {
                        int level = rs.getInt(key);
                        // Récupérer le CustomEnchant associé à cette clé depuis le Map customEnchants.
                        CustomEnchant ce = customEnchants.get(key);
                        // Créer et stocker l'instance de PlayerEnchantData.
                        playerEnchants.put(key, new PlayerEnchantData(ce, level));
                    }
                }
            }
        }
        return playerEnchants;
    }

    /**
     * Sauvegarde ou met à jour en BDD les données d'enchantements d'un joueur.
     * Construit dynamiquement la requête UPDATE pour mettre à jour les colonnes associées.
     *
     * @param playerUuid  identifiant du joueur.
     * @param enchantData Map associant chaque clé d'enchant à son PlayerEnchantData.
     * @throws SQLException en cas d'erreur SQL.
     */
    public void saveOrUpdatePlayerEnchantData(UUID playerUuid, Map<String, PlayerEnchantData> enchantData) throws SQLException {
        // Construction dynamique de la requête UPDATE.
        StringBuilder queryBuilder = new StringBuilder("UPDATE " + enchantTable + " SET ");
        int size = enchantData.size();
        int count = 0;
        for (String key : enchantData.keySet()) {
            queryBuilder.append("`").append(key).append("` = ?");
            if (++count < size) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(" WHERE player_id = ?");

        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            for (String key : enchantData.keySet()) {
                stmt.setInt(paramIndex++, enchantData.get(key).getLevel());
            }
            stmt.setString(paramIndex, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Vérifie si la table des enchantements existe et
     * s'assure qu'elle possède une colonne pour chaque enchantement défini dans enchants.yml.
     *
     * @param enchantKeys Ensemble des clés d'enchantements.
     * @throws SQLException en cas d'erreur lors des opérations SQL.
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

    /**
     * Vérifie si une ligne existe pour le joueur dans la table des enchantements.
     * Si non, insère une nouvelle ligne avec toutes les colonnes d'enchant initialisées à 0.
     *
     * @param playerId    identifiant du joueur.
     * @param enchantKeys ensemble des clés d'enchantements disponibles.
     * @throws SQLException en cas d'erreur d'accès à la BDD.
     */
    public void initializePlayerEnchant(String playerId, Set<String> enchantKeys) throws SQLException {
        String querySelect = "SELECT player_id FROM " + enchantTable + " WHERE player_id = ?";
        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(querySelect)) {
            selectStmt.setString(1, playerId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        StringBuilder columns = new StringBuilder("player_id");
        StringBuilder placeholders = new StringBuilder("?");
        for (String key : enchantKeys) {
            columns.append(", `").append(key).append("`");
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