package us.polarismc.polarisduels.arenas.dao;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

import java.util.Map;

public interface ArenaDAO {
    void saveArenas(Map<String, ArenaEntity> arenas);

    JsonObject locationToJson(Location location);

    Map<String, ArenaEntity> loadArenas();

    Location jsonToLocation(JsonObject locationJson);

    void deleteArena(String arenaName);
}
