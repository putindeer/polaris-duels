package us.polarismc.polarisduels.arenas;

/**
 * The ArenaManager class is responsible for managing all arena-related operations in the PolarisDuels plugin.
 * It handles arena creation, state management, player matching, and quadrant-based arena placement.
 * 
 * <p>This class follows the singleton pattern and maintains the state of all arenas in the system.
 * It provides thread-safe methods for finding available arenas, managing arena states, and handling
 * arena-related operations.</p>
 */

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import us.polarismc.api.util.generator.VoidGenerator;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.dao.ArenaDAO;
import us.polarismc.polarisduels.arenas.dao.ArenaGsonImpl;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;
import us.polarismc.polarisduels.arenas.commands.GridPos;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;
import us.polarismc.polarisduels.queue.KitType;
import us.polarismc.polarisduels.player.PlayerRollBackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public class ArenaManager {
    private final Main plugin;
    
    /** List of all registered arenas in the system */
    public final List<ArenaEntity> arenas;

    /** Data access object for arena persistence */
    public final ArenaDAO arenaFile;

    /** Manager for handling player state rollbacks after matches */
    @Getter
    private final PlayerRollBackManager rollBackManager;
    
    /**
     * Gets the list of all arenas.
     * 
     * @return An unmodifiable list of all arenas
     */
    public List<ArenaEntity> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

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
        arena.getPlayers().clear();
        arena.setKit(null);
        arena.setRounds(0);
        arena.setPlayersNeeded(0);
        arena.setArenaState(new InactiveArenaState());
    }

/**
     * Finds an open (inactive) arena and prepares it for a new match.
     * 
     * @param kit The kit type for the match
     * @param playersNeeded Number of players needed for the match
     * @param rounds Number of rounds for the match
     * @return An Optional containing the prepared arena if found, empty otherwise
     * @throws IllegalArgumentException if kit is null or playersNeeded/rounds are non-positive
     */
    public Optional<ArenaEntity> findOpenArena(KitType kit, int playersNeeded, int rounds) {
        if (kit == null) {
            throw new IllegalArgumentException("Kit cannot be null");
        }
        if (playersNeeded <= 0) {
            throw new IllegalArgumentException("Players needed must be positive");
        }
        if (rounds <= 0) {
            throw new IllegalArgumentException("Rounds must be positive");
        }
        List<ArenaEntity> arenaList = new ArrayList<>(getArenas());
        Collections.shuffle(arenaList);
        Optional<ArenaEntity> arena = arenaList.stream().filter(a -> a.getArenaState() instanceof InactiveArenaState).findAny();
        arena.ifPresent(arenaEntity -> arenaEntity.setArenaState(new WaitingArenaState(arenaEntity, kit, playersNeeded, rounds)));
        return arena;
    }

/**
     * Finds a compatible arena for the specified match parameters.
     * First tries to find a waiting arena with matching parameters, then falls back to an open arena.
     * 
     * @param kit The kit type for the match
     * @param playersNeeded Number of players needed for the match
     * @param rounds Number of rounds for the match
     * @return An Optional containing a compatible arena if found, empty otherwise
     * @throws IllegalArgumentException if kit is null or playersNeeded/rounds are non-positive
     */
    public Optional<ArenaEntity> findCompatibleArena(KitType kit, int playersNeeded, int rounds) {
        if (kit == null) {
            throw new IllegalArgumentException("Kit cannot be null");
        }
        if (playersNeeded <= 0) {
            throw new IllegalArgumentException("Players needed must be positive");
        }
        if (rounds <= 0) {
            throw new IllegalArgumentException("Rounds must be positive");
        }
        Optional<ArenaEntity> compatibleArena = getArenas().stream()
                .filter(arena -> {
                    if (arena.getArenaState() instanceof WaitingArenaState state) {
                        return state.getPlayersNeeded() == playersNeeded
                                && state.getRounds() == rounds
                                && state.getKit() == kit;
                    }
                    return false;
                })
                .findAny();

        return compatibleArena.isPresent() ? compatibleArena : findOpenArena(kit, playersNeeded, rounds);
    }

/**
     * Finds the arena that contains the specified player.
     * 
     * @param player The player to search for
     * @return An Optional containing the player's arena if found, empty otherwise
     * @throws IllegalArgumentException if player is null
     */
    public Optional<ArenaEntity> findPlayerArena(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return getArenas().stream().filter(arena -> arena.getPlayers().contains(player.getUniqueId())).findAny();
    }

/**
     * DEBUG METHOD: Finds any inactive arena.
     * This is primarily used for testing and debugging purposes.
     * 
     * @return An Optional containing an inactive arena if found, empty otherwise
     */
    public Optional<ArenaEntity> findInactiveArena() {
        return getArenas().stream().filter(a -> a.getArenaState() instanceof InactiveArenaState).findAny();
    }

/**
     * Finds a compatible arena without falling back to opening a new one.
     * Unlike findCompatibleArena, this will not create a new waiting arena if none is found.
     * 
     * @param kit The kit type for the match
     * @param playersNeeded Number of players needed for the match
     * @param rounds Number of rounds for the match
     * @return An Optional containing a compatible arena if found, empty otherwise
     * @throws IllegalArgumentException if kit is null or playersNeeded/rounds are non-positive
     */
    public Optional<ArenaEntity> findCompatibleArenaNoMethod(KitType kit, int playersNeeded, int rounds) {
        if (kit == null) {
            throw new IllegalArgumentException("Kit cannot be null");
        }
        if (playersNeeded <= 0) {
            throw new IllegalArgumentException("Players needed must be positive");
        }
        if (rounds <= 0) {
            throw new IllegalArgumentException("Rounds must be positive");
        }
        return getArenas().stream()
                .filter(arena -> {
                    if (arena.getArenaState() instanceof WaitingArenaState state) {
                        return state.getPlayersNeeded() == playersNeeded
                                && state.getRounds() == rounds
                                && state.getKit() == kit;
                    }
                    return false;
                })
                .findAny();
    }
    
    /**
     * Finds the next available quadrant for a new arena in the specified world
     * @param world The world to find a quadrant in
     * @return Optional containing the next available GridPos, or empty if no quadrants are available
     */
    public Optional<GridPos> findNextAvailableQuadrant(World world) {
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
