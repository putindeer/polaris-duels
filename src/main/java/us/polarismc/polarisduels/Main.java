package us.polarismc.polarisduels;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.polarisduels.arenas.dao.ArenaDAO;
import us.polarismc.polarisduels.arenas.dao.ArenaGsonImpl;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.utils.StartThings;
import us.polarismc.polarisduels.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public final Map<UUID, FastBoard> boards = new HashMap<>();
    public static Main pl;
    public StartThings start;
    public Utils utils;

    public ArenaDAO arenaFile;
    public Map<String, ArenaEntity> arenas;

    @Override
    public void onEnable() {
        pl = this;

        arenaFile = new ArenaGsonImpl(this);
        arenas = arenaFile.loadArenas();

        start = new StartThings(this);
        utils = new Utils();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
