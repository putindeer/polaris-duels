package us.polarismc.polarisduels.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages the database connection pool using HikariCP.
 * Provides a thread-safe way to obtain database connections.
 */
@Getter
public class Database {
    /** The Hikari data source for connection pooling */
    private final HikariDataSource dataSource;

    /**
     * Creates a new Database instance with SQLite configuration.
     *
     * @param dataFolder The folder where the SQLite database file will be stored
     */
    public Database(File dataFolder) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Gets a connection from the connection pool.
     *
     * @return A database connection
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Closes the data source and releases all resources.
     * Should be called when the plugin is disabled.
     */
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}

