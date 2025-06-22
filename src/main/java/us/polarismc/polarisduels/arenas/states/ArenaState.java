package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

/**
 * Represents a state that an arena can be in.
 * 
 * <p>This interface defines the contract for all arena states in the state pattern
 * implementation. Each state handles specific behavior when the arena is in that state.</p>
 * 
 * <p>Implementations should be immutable and stateless, as they may be shared between
 * multiple arena instances.</p>
 */

public interface ArenaState {
    /**
     * Called when an arena enters this state.
     * 
     * @param arena The arena that is entering this state
     * @throws IllegalArgumentException if arena is null
     */
    void onEnable(ArenaEntity arena);
    
    /**
     * Called when an arena exits this state.
     * This method should clean up any resources or tasks associated with this state.
     */
    void onDisable();
}
