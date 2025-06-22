package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

/**
 * Represents the inactive state of an arena.
 * 
 * <p>In this state, the arena is not in use and is available for new matches.
 * This is the default state for arenas when no players are present and no
 * matches are in progress.</p>
 */

public class InactiveArenaState implements ArenaState {
    
    /**
     * Logs a message when an arena enters the inactive state.
     * 
     * @param arena The arena that became inactive
     * @throws IllegalArgumentException if arena is null
     */
    @Override
    public void onEnable(ArenaEntity arena) {
        Main.pl.getLogger().info("InactiveArenaState enabled");
    }

    /**
     * Logs a message when an arena exits the inactive state.
     * This method is called when the arena transitions to a different state.
     */
    @Override
    public void onDisable() {
        Main.pl.getLogger().info("InactiveArenaState disabled");
    }
}
