package us.polarismc.polarisduels.arenas;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.setup.ArenaDAO;
import us.polarismc.polarisduels.arenas.setup.ArenaGsonImpl;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.game.states.InactiveArenaState;
import us.polarismc.polarisduels.arenas.setup.GridPos;
import us.polarismc.polarisduels.game.states.QueueArenaState;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.arenas.entity.ArenaSize;
import us.polarismc.polarisduels.game.states.StartingArenaState;
import us.polarismc.polarisduels.game.KitType;
import us.polarismc.polarisduels.managers.player.PlayerRollBackManager;
import us.polarismc.polarisduels.managers.queue.QueueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The ArenaManager class is responsible for managing all arena-related operations in the PolarisDuels plugin.
 * It handles arena creation, state management, player matching, and quadrant-based arena placement.
 *
 * <p>This class follows the singleton pattern and maintains the state of all arenas in the system.
 * It provides thread-safe methods for finding available arenas, managing arena states, and handling
 * arena-related operations.</p>
 */
@Getter
public class ArenaManager implements Listener {
    private final Main plugin;
    
    /** List of all registered arenas in the system */
    @Getter
    public final List<ArenaEntity> arenas;

    /** Data access object for arena persistence */
    public final ArenaDAO arenaFile;

    /** Manager for handling player state rollbacks after matches */
    @Getter
    private final PlayerRollBackManager rollBackManager;

    /**
     * Constructs a new ArenaManager instance.
     * 
     * @param plugin The main plugin instance
     * @throws IllegalStateException if there's an error loading arenas or arena worlds
     */
    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        arenaFile = new ArenaGsonImpl(plugin);
        arenaFile.loadArenaWorlds();
        arenas = arenaFile.loadArenas();
        this.rollBackManager = new PlayerRollBackManager(plugin);
        checkQuadrantConflicts();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles player quit events within the arena.
     * Removes the player from the arena.
     *
     * @param event The PlayerQuitEvent that was triggered
     */
    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        getPlayerArena(player).ifPresent(arena -> arena.removePlayer(player, plugin));
    }

    /**
     * Sets an arena to the inactive state, clearing all players and match data.
     * 
     * @param arena The arena to deactivate
     * @throws IllegalArgumentException if arena is null
     */
    public void setInactiveState(ArenaEntity arena) {
        if (arena == null) {
            throw new IllegalArgumentException("Arena cannot be null");
        }
        arena.getOnlinePlayers().forEach(rollBackManager::restore);
        arena.setGameSession(null);
        arena.setArenaState(new InactiveArenaState());
    }

    /**
     * Assigns an arena to the provided GameSession. Will attempt, in order:
     * 1) A Waiting arena already set up for the same kit/playersNeeded that is currently not full.
     * 2) An inactive arena (size filter if requestedSize present), which will be switched to WaitingArenaState.
     *
     * @param session GameSession describing the upcoming match
     * @return arena chosen or empty if none available
     */
    public Optional<ArenaEntity> assignArena(GameSession session) {
        if (session.getQueueType() != null) {
            return findQueuedArena(session);
        } else {
            return findInactiveArena(session);
        }
    }

    private Optional<ArenaEntity> findQueuedArena(GameSession session) {
        KitType kit = session.getKit();
        QueueType queueType = session.getQueueType();
        Optional<ArenaEntity> queuedArena = arenas.stream()
                .filter(a -> a.getArenaState() instanceof QueueArenaState)
                .filter(a -> a.getGameSession().getKit() == kit)
                .filter(a -> a.getGameSession().getQueueType() == queueType)
                .filter(a -> a.getGameSession().getPlayerList().size() + session.getPlayers().size() <= queueType.getPlayersNeeded())
                .findAny();
        if (queuedArena.isPresent()) {
            return queuedArena;
        } else {
            return findInactiveArena(session);
        }
    }

    private Optional<ArenaEntity> findInactiveArena(GameSession session) {
        List<ArenaEntity> shuffled = new ArrayList<>(arenas);
        Collections.shuffle(shuffled);
        ArenaSize requested = session.getRequestedSize();
        Optional<ArenaEntity> inactive = shuffled.stream()
                .filter(arena -> arena.getArenaState() instanceof InactiveArenaState)
                .filter(arena -> requested == null || arena.getArenaSize() == requested)
                .findFirst();
        inactive.ifPresent(arena -> {
            if (session.getQueueType() != null) {
                arena.setArenaState(new QueueArenaState(arena, session));
            } else {
                arena.setGameSession(session);
                session.setArena(arena);
                session.getPlayerList().forEach(arena::addPlayer);
                arena.setArenaState(new StartingArenaState(arena, session));
            }
        });
        return inactive;
    }

    /**
     * Finds the arena that contains the specified player.
     * 
     * @param player The player to search for
     * @return An Optional containing the player's arena if found, empty otherwise
     */
    public Optional<ArenaEntity> getPlayerArena(Player player) {
        return getArenas().stream().filter(arena -> arena.getGameSession() != null).filter(arena -> arena.getGameSession().isParticipant(player)).findAny();
    }
    
    /**
     * Finds the next available quadrant for a new arena in the specified world
     * @param world The world to find a quadrant in
     * @return Optional containing the next available GridPos, or empty if no quadrants are available
     */
    public Optional<GridPos> getNextAvailableQuadrant(World world) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        
        // Start with quadrant (0,0) and spiral outward until we find an available one
        int radius = 0;
        while (radius < 100) { // Prevent infinite loops with a reasonable limit
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        GridPos pos = new GridPos(world, x, z);
                        if (isQuadrantAvailable(pos)) {
                            return Optional.of(pos);
                        }
                    }
                }
            }
            radius++;
        }
        return Optional.empty();
    }
    
    /**
     * Checks if a quadrant is available (not occupied by any arena in the same world)
     * @param quadrant The quadrant to check
     * @return true if the quadrant is available, false otherwise
     */
    public boolean isQuadrantAvailable(GridPos quadrant) {
        if (quadrant == null) {
            throw new IllegalArgumentException("Quadrant cannot be null");
        }
        return getArenas().stream()
                .noneMatch(arena -> arena.getQuadrant() != null && 
                                  arena.getQuadrant().equals(quadrant));
    }
    
    /**
     * Checks for any quadrant conflicts between arenas
     * Logs warnings for any conflicts found
     */
    public void checkQuadrantConflicts() {
        List<ArenaEntity> arenaList = new ArrayList<>(getArenas());
        for (int i = 0; i < arenaList.size(); i++) {
            ArenaEntity a1 = arenaList.get(i);
            if (a1.getQuadrant() == null) continue;
            
            for (int j = i + 1; j < arenaList.size(); j++) {
                ArenaEntity a2 = arenaList.get(j);
                if (a1.hasQuadrantConflict(a2)) {
                    plugin.utils.warning(String.format(
                        "Quadrant conflict detected between arenas %s (world: %s) and %s (world: %s) at quadrant %s",
                        a1.getName(), a1.getWorld() != null ? a1.getWorld().getName() : "null",
                        a2.getName(), a2.getWorld() != null ? a2.getWorld().getName() : "null",
                        a1.getQuadrantString()
                    ));
                }
            }
        }
    }

    /**
     * Creates a GridPos from a location, calculating the appropriate quadrant
     * @param location The location to create a GridPos for
     * @return A new GridPos representing the quadrant containing the location
     * @throws IllegalArgumentException if location or its world is null
     */
    public GridPos createGridPos(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Location's world cannot be null");
        }
        return createGridPos(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    /**
     * Creates a GridPos from world coordinates, calculating the appropriate quadrant
     * @param world The world the coordinates are in
     * @param x The x coordinate in the world
     * @param z The z coordinate in the world
     * @return A new GridPos representing the quadrant containing the coordinates
     * @throws IllegalArgumentException if world is null
     */
    public GridPos createGridPos(World world, int x, int z) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        int quadrantX = (int) Math.floor(x / 1000.0);
        int quadrantZ = (int) Math.floor(z / 1000.0);
        return new GridPos(world, quadrantX, quadrantZ);
    }
}
