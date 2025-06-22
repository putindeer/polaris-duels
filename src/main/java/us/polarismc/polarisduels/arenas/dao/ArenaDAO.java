package us.polarismc.polarisduels.arenas.dao;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

import java.util.List;

/**
 * Data Access Object interface for arena-related operations.
 * Defines methods for loading, saving, and managing arena data persistence.
 */
public interface ArenaDAO {
    /**
     * Loads all arena worlds required for the plugin to function.
     * This method should be called during plugin initialization.
     */
    void loadArenaWorlds();

    /**
     * Saves a list of arena entities to persistent storage.
     *
     * @param arenas The list of ArenaEntity objects to save
     */
    void saveArenas(List<ArenaEntity> arenas);

    /**
     * Converts a Bukkit Location to a JsonObject for serialization.
     *
     * @param location The Bukkit Location to convert
     * @return A JsonObject containing the location data
     */
    JsonObject locationToJson(Location location);

    /**
     * Loads all arenas from persistent storage.
     *
     * @return A list of loaded ArenaEntity objects
     */
    List<ArenaEntity> loadArenas();

    /**
     * Converts a JsonObject back to a Bukkit Location.
     *
     * @param locationJson The JsonObject containing location data
     * @return A Bukkit Location object
     */
    Location jsonToLocation(JsonObject locationJson);

    /**
     * Deletes an arena from persistent storage.
     *
     * @param arena The ArenaEntity to delete
     */
    void deleteArena(ArenaEntity arena);
}
