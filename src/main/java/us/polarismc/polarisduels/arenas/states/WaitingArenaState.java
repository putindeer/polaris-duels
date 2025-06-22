package us.polarismc.polarisduels.arenas.states;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.queue.KitType;

/**
 * Represents the waiting state of an arena where players are joining before a match starts.
 * 
 * <p>In this state, the arena is waiting for enough players to join before starting a match.
 * The state handles player management, including player quit events, and transitions to
 * the starting state once enough players have joined.</p>
 * 
 * <p>This class implements {@link Listener} to handle player-related events.</p>
 */

@Setter
@Getter
public class WaitingArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    
    /** The arena this state belongs to */
    private final ArenaEntity arena;
    
    /** The kit type for the upcoming match */
    private final KitType kit;
    
    /** Number of players needed to start the match */
    private final int playersNeeded;
    
    /** Number of rounds in the upcoming match */
    private final int rounds;

    /**
     * Creates a new WaitingArenaState with the specified parameters.
     * 
     * @param arena The arena this state belongs to
     * @param kit The kit type for the upcoming match
     * @param playersNeeded Number of players needed to start the match
     * @param rounds Number of rounds in the upcoming match
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public WaitingArenaState(ArenaEntity arena, KitType kit, int playersNeeded, int rounds) {
        if (arena == null) {
            throw new IllegalArgumentException("Arena cannot be null");
        }
        if (kit == null) {
            throw new IllegalArgumentException("Kit cannot be null");
        }
        if (playersNeeded <= 0) {
            throw new IllegalArgumentException("Players needed must be positive");
        }
        if (rounds <= 0) {
            throw new IllegalArgumentException("Rounds must be positive");
        }
        this.arena = arena;
        this.kit = kit;
        this.playersNeeded = playersNeeded;
        this.rounds = rounds;
        arena.setKit(kit);
        arena.setPlayersNeeded(playersNeeded);
        arena.setRounds(rounds);
    }

    /**
     * Called when the arena enters the waiting state.
     * Registers event listeners and initializes the state.
     * 
     * @param arena The arena that entered this state
     * @throws IllegalArgumentException if arena is null
     */
    @Override
    public void onEnable(ArenaEntity arena) {
        if (arena == null) {
            throw new IllegalArgumentException("Arena cannot be null");
        }
        plugin.getLogger().info("WaitingArenaState enabled");
        Bukkit.getPluginManager().registerEvents(this, Main.pl);
    }

    /**
     * Called when the arena exits the waiting state.
     * Unregisters event listeners and cleans up resources.
     */
    @Override
    public void onDisable() {
        plugin.getLogger().info("WaitingArenaState disabled");
        HandlerList.unregisterAll(this);
    }

    /**
     * Handles player quit events while the arena is in waiting state.
     * Removes the player from the arena if they were in this arena.
     * 
     * @param event The PlayerQuitEvent that was triggered
     */
    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if (!arena.hasPlayer(event.getPlayer())) return;
        arena.removePlayer(event.getPlayer(), Main.pl);
    }
}
