package us.polarismc.polarisduels.game.states;

import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaState;

/**
 * Represents the inactive state of an arena.
 * 
 * <p>In this state, the arena is not in use and is available for new matches.
 * This is the default state for arenas when no players are present and no
 * matches are in progress.</p>
 */

public class InactiveArenaState implements ArenaState {
    
    /**
     * Logs a message when an arena enters the idle state.
     *
     * @throws IllegalArgumentException if arena is null
     */
    @Override
    public void onEnable() {
        Main.pl.getLogger().info("InactiveArenaState enabled");
    }

    /**
     * Logs a message when an arena exits the idle state.
     * This method is called when the arena transitions to a different state.
     */
    @Override
    public void onDisable(ArenaState state) {
        Main.pl.getLogger().info("InactiveArenaState disabled");
    }
}
