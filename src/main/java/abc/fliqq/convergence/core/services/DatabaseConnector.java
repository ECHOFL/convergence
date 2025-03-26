package abc.fliqq.convergence.core.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe centrale qui gère la connexion à la base de données via HikariCP.
 * Les paramètres (jdbc-url, username, password) sont définis dans la configuration globale
 * du plugin sous la section "database".
 */
public class DatabaseConnector {

    private final HikariDataSource dataSource;

    public DatabaseConnector(FileConfiguration config) {
        // Récupérer la section "database.mysql" dans la configuration globale du plugin
        ConfigurationSection dbSection = config.getConfigurationSection("database.mysql");

        String host = dbSection.getString("host");
        int port = dbSection.getInt("port");
        String database = dbSection.getString("database");
        String username = dbSection.getString("username");
        String password = dbSection.getString("password");
        boolean useSSL = dbSection.getBoolean("ssl");

        // Construire l'URL JDBC pour MySQL
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=%b", host, port, database, useSSL);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // Propriétés recommandées pour HikariCP
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(hikariConfig);
    }
    /**
     * Retourne une connexion depuis le pool.
     *
     * @return une connexion SQL.
     * @throws SQLException en cas d'erreur.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Ferme le pool de connexions.
     */
    public void shutdown() {
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }
}
