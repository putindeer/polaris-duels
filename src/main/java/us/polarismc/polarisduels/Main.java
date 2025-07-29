package us.polarismc.polarisduels;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.api.util.PluginUtils;
import us.polarismc.polarisduels.arenas.ArenaManager;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.game.states.PlayingArenaState;
import us.polarismc.polarisduels.managers.duel.DuelManager;
import us.polarismc.polarisduels.database.Database;
import us.polarismc.polarisduels.database.DatabaseInitializer;
import us.polarismc.polarisduels.database.KitManager;
import us.polarismc.polarisduels.managers.party.PartyManager;
import us.polarismc.polarisduels.managers.tab.TabManager;
import us.polarismc.polarisduels.managers.player.PlayerManager;
import us.polarismc.polarisduels.managers.startup.StartThings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main class for the PolarisDuels plugin.
 * This class serves as the entry point for the plugin and manages core functionality,
 * including plugin initialization, resource management, and access to various managers.
 */
public final class Main extends JavaPlugin {
    /** Singleton instance of the plugin */
    public static Main pl;
    
    /** Utility class for common plugin operations */
    public PluginUtils utils;
    
    /** Handles database connections and operations */
    public Database database;
    
    /** Manages database schema and initialization */
    public DatabaseInitializer databaseInitializer;
    
    /** Manages arena-related operations and state */
    @Getter
    private ArenaManager arenaManager;
    
    /** Handles duel logic and matchmaking */
    @Getter
    public DuelManager duelManager;
    
    /** Manages player data and state */
    @Getter
    public PlayerManager playerManager;
    
    /** Manages party system and operations */
    @Getter
    private PartyManager partyManager;
    
    /** Manages available kits and their configurations */
    @Getter
    public KitManager kitManager;
    
    /** Handles player tab list customization */
    @Getter
    public TabManager tabManager;
    
    /** Maps player UUIDs to their FastBoard instances for scoreboard management */
    public final Map<UUID, FastBoard> boards = new HashMap<>();

    /**
     * Called when the plugin is enabled.
     * Initializes all core components, managers, and scheduled tasks.
     */
    @Override
    public void onEnable() {
        pl = this;
        utils = new PluginUtils(this, "<blue><bold>Duels</bold></blue> <dark_gray>»</dark_gray> <reset>");
        new StartThings(this);
        initializeDatabase();
        initializeManagers();
    }

    /**
     * Called when the plugin is disabled.
     * Performs cleanup operations including saving arena states and closing database connections.
     */
    @Override
    public void onDisable() {
        // Save all arenas and reset active ones on shutdown
        for (ArenaEntity arena : arenaManager.getArenas()) {
            if (arena.getArenaState() instanceof PlayingArenaState state) {
                state.resetArenaEntities();
                state.resetArenaBlocks();
            }
        }
        // Save all arena data to file
        arenaManager.getArenaFile().saveArenas(arenaManager.getArenas());
        database.close();
    }

    /**
     * Initializes the database connection and sets up the database schema.
     * Creates necessary tables and ensures the database is ready for use.
     */
    private void initializeDatabase() {
        database = new Database(getDataFolder());
        databaseInitializer = new DatabaseInitializer(database);
        databaseInitializer.initialize();
    }

    /**
     * Initializes all plugin managers with their required dependencies.
     * This includes arena, duel, player, kit, and tab list managers.
     */
    private void initializeManagers() {
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        playerManager = new PlayerManager(this);
        partyManager = new PartyManager(this);
        kitManager = new KitManager(this, database);
        tabManager = new TabManager(this);
    }

    /**
     * Gets the singleton instance of the plugin.
     *
     * @return The Main plugin instance
     */
    public static Main getInstance() {
        return pl;
    }
}
