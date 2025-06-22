package us.polarismc.polarisduels.database;

import us.polarismc.polarisduels.Main;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles the initialization and setup of the database schema.
 * Creates necessary tables if they don't exist when the plugin starts.
 */
public class DatabaseInitializer {
    /** The database instance to initialize */
    private final Database database;

    /**
     * Creates a new DatabaseInitializer for the specified database.
     *
     * @param database The database to initialize
     */
    public DatabaseInitializer(Database database) {
        this.database = database;
    }

    /**
     * Initializes the database by creating required tables if they don't exist.
     * Currently creates a 'player_kits' table for storing player kit data.
     * Logs any SQL errors that occur during initialization.
     */
    public void initialize() {
        try (Statement statement = database.getConnection().createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS player_kits (
                    player_uuid VARCHAR(36),
                    kit_type TEXT NOT NULL,
                    kit_items BLOB NOT NULL,
                    PRIMARY KEY (player_uuid, kit_type)
                );
                """);
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("There was an error trying to initialize the database: " + e.getMessage());
        }
    }
}

