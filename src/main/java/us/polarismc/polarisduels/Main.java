package us.polarismc.polarisduels;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.api.util.PluginUtils;
import us.polarismc.polarisduels.arenas.ArenaManager;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ActiveArenaState;
import us.polarismc.polarisduels.duel.DuelManager;
import us.polarismc.polarisduels.database.Database;
import us.polarismc.polarisduels.database.DatabaseInitializer;
import us.polarismc.polarisduels.database.KitManager;
import us.polarismc.polarisduels.managers.tab.TabManager;
import us.polarismc.polarisduels.player.PlayerManager;
import us.polarismc.polarisduels.utils.StartThings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public static Main pl;
    public PluginUtils utils;
    public Database database;
    public DatabaseInitializer databaseInitializer;
    @Getter
    private ArenaManager arenaManager;
    @Getter
    public DuelManager duelManager;
    @Getter
    public PlayerManager playerManager;
    @Getter
    public KitManager kitManager;
    @Getter
    public TabManager tabManager;
    public final Map<UUID, FastBoard> boards = new HashMap<>();

    @Override
    public void onEnable() {
        pl = this;
        utils = new PluginUtils(this, "&bDuels &7» &r");
        new StartThings(this);
        initializeDatabase();
        initializeManagers();
    }

    @Override
    public void onDisable() {
        for (ArenaEntity arena : arenaManager.getArenas()) {
            if (arena.getArenaState() instanceof ActiveArenaState state) {
                state.resetArenaEntities();
                state.resetArenaBlocks();
            }
        }
        arenaManager.arenaFile.saveArenas(arenaManager.arenas);
        database.close();
    }

    private void initializeDatabase() {
        database = new Database(getDataFolder());
        databaseInitializer = new DatabaseInitializer(database);
        databaseInitializer.initialize();
    }

    private void initializeManagers() {
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        playerManager = new PlayerManager(this);
        kitManager = new KitManager(this, database);
        tabManager = new TabManager(this);
    }

    public static Main getInstance() {
        return pl;
    }
}
