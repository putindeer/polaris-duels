package us.polarismc.polarisduels.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.game.KitType;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * Manages player kit data storage and retrieval in the database.
 * Handles saving and loading of kit inventories for different kit types.
 */
public class KitManager {
    /** The main plugin instance */
    private final Main plugin;
    
    /** The database instance for kit storage */
    private final Database database;

    /**
     * Creates a new KitManager with the specified plugin and database.
     *
     * @param plugin The main plugin instance
     * @param database The database to use for kit storage
     */
    public KitManager(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Saves a player's kit to the database.
     * If a kit already exists for this player and kit type, it will be replaced.
     *
     * @param player The player
     * @param kitType The type of kit to save
     * @param inv The inventory contents to save as a kit
     */
    public void saveKit(Player player, KitType kitType, ItemStack[] inv) {
        UUID uuid = player.getUniqueId();
        byte[] serializedInv = ItemStack.serializeItemsAsBytes(inv);

        try (Connection c = database.getConnection()) {
            String query = """
                INSERT OR REPLACE INTO player_kits (player_uuid, kit_type, kit_items)
                VALUES (?, ?, ?);
            """;
            try (PreparedStatement s = c.prepareStatement(query)) {
                s.setString(1, uuid.toString());
                s.setString(2, kitType.name());
                s.setBytes(3, serializedInv);
                s.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.utils.severe("Error when saving inventory with the UUID " + uuid + " and KitType " + kitType.name() + ": " + e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) {
                plugin.utils.warning(s.toString());
            }
        }
    }

    /**
     * Loads a player's kit from the database.
     * If no kit is found, returns the default kit for the specified type.
     *
     * @param uuid The UUID of the player
     * @param kitType The type of kit to load
     * @return The loaded kit inventory, or default kit if not found
     */
    public ItemStack[] loadKit(UUID uuid, KitType kitType) {
        try (Connection connection = database.getConnection()) {
            String query = "SELECT kit_items FROM player_kits WHERE player_uuid = ? AND kit_type = ?";
            try (PreparedStatement s = connection.prepareStatement(query)) {
                s.setString(1, uuid.toString());
                s.setString(2, kitType.name());

                ResultSet rs = s.executeQuery();
                if (rs.next()) {
                    byte[] serializedInventory = rs.getBytes("kit_items");
                    return ItemStack.deserializeItemsFromBytes(serializedInventory);
                }
            }
        } catch (SQLException e) {
            plugin.utils.severe("Error when loading inventory with the UUID " + uuid.toString() + " and KitType " + kitType.name() + ": " + e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) {
                plugin.utils.warning(s.toString());
            }
        }
        return kitType.getDefaultInv();
    }
}
