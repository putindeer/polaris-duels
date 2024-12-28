package us.polarismc.polarisduels;

import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.polarisduels.arenas.ArenaManager;
import us.polarismc.polarisduels.utils.StartThings;
import us.polarismc.polarisduels.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public static Main pl;
    @Getter
    private ArenaManager arenaManager;
    public Map<UUID, FastBoard> boards = new HashMap<>();
    public Utils utils;

    @Override
    public void onEnable() {
        pl = this;
        utils = new Utils();
        arenaManager = new ArenaManager(this);

        new StartThings(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
