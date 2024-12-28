package us.polarismc.polarisduels.arenas.dao;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

import java.util.List;

public interface ArenaDAO {
    void saveArenas(List<ArenaEntity> arenas);

    JsonObject locationToJson(Location location);

    List<ArenaEntity> loadArenas();

    Location jsonToLocation(JsonObject locationJson);

    void deleteArena(ArenaEntity arena);
}
