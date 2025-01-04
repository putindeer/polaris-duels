package us.polarismc.polarisduels.database;

import us.polarismc.polarisduels.Main;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private final Database database;

    public DatabaseInitializer(Database database) {
        this.database = database;
    }

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

