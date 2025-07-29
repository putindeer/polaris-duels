package us.polarismc.polarisduels.arenas.setup;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

/**
 * Represents an active arena setup session for a player.
 * Tracks the current step in the arena setup process and the arena being configured.
 */
@Setter
@Getter
public class ArenaSetupSession {
    /** The player who is setting up the arena */
    private Player player;
    
    /** The arena entity being configured */
    private ArenaEntity arena;
    
    /** The current step in the setup process (1-11) */
    private int step;

    /**
     * Creates a new arena setup session for a player.
     * Initializes the setup process at step 1.
     *
     * @param player The player starting the setup
     * @param arena The arena entity to configure
     */
    public ArenaSetupSession(Player player, ArenaEntity arena) {
        this.player = player;
        this.arena = arena;
        this.step = 1;
    }
}