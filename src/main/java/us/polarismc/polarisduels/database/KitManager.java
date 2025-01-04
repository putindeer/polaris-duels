package us.polarismc.polarisduels.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.queue.KitType;

import java.sql.ResultSet;
import java.util.UUID;

public class KitManager {
    private final Main plugin;
    private final Database database;

    public KitManager(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void saveKit(UUID uuid, KitType kitType, ItemStack[] inv) {
        byte[] serializedInv = ItemStack.serializeItemsAsBytes(inv);

        try (Connection connection = database.getConnection()) {
            String insertOrReplaceQuery = """
                INSERT OR REPLACE INTO player_kits (player_uuid, kit_type, kit_items)
                VALUES (?, ?, ?);
            """;
            try (PreparedStatement insertStatement = connection.prepareStatement(insertOrReplaceQuery)) {
                insertStatement.setString(1, uuid.toString());
                insertStatement.setString(2, kitType.name());
                insertStatement.setBytes(3, serializedInv);
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error when saving inventory with the UUID " + uuid.toString() + " and KitType " + kitType.name() + ": " + e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) {
                plugin.getLogger().warning(s.toString());
            }
        }
    }

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
            plugin.getLogger().severe("Error when loading inventory with the UUID " + uuid.toString() + " and KitType " + kitType.name() + ": " + e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) {
                plugin.getLogger().warning(s.toString());
            }
        }
        return kitType.getDefaultInv();
    }
}
