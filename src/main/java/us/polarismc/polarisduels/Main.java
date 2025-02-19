package us.polarismc.polarisduels;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.polarisduels.arenas.ArenaManager;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ActiveArenaState;
import us.polarismc.polarisduels.duel.DuelManager;
import us.polarismc.polarisduels.database.Database;
import us.polarismc.polarisduels.database.DatabaseInitializer;
import us.polarismc.polarisduels.database.KitManager;
import us.polarismc.polarisduels.player.DuelsPlayer;
import us.polarismc.polarisduels.player.PlayerManager;
import us.polarismc.polarisduels.utils.StartThings;
import us.polarismc.polarisduels.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public static Main pl;
    public Utils utils;
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
    public Map<UUID, FastBoard> boards = new HashMap<>();

    @Override
    public void onEnable() {
        pl = this;
        utils = new Utils(this);
        new StartThings(this);
        initializeDatabase();
        initializeManagers();
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            DuelsPlayer duelsPlayer = this.getPlayerManager().getDuelsPlayer(p);
            if (duelsPlayer.getTeam() != null) {
                duelsPlayer.deleteTeam(duelsPlayer.getTeam());
            }
        }
        for (ArenaEntity arena : arenaManager.getArenas()) {
            if (arena.getArenaState() instanceof ActiveArenaState state) {
                state.resetArenaEntities();
                state.resetArenaBlocks();
            }
        }
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
    }

    public static Main getInstance() {
        return pl;
    }
}
