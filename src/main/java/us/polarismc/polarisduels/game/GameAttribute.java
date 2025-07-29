package us.polarismc.polarisduels.game;

/**
 * Defines various attributes that can be applied to game sesssions to modify their behavior.
 * These attributes control game mechanics and player interactions within the arena.
 */
public enum GameAttribute {
    /** Prevents players from breaking blocks in the current session */
    NO_BLOCK_BREAK,
    
    /** Prevents destruction of the arena structure itself */
    NO_ARENA_DESTRUCTION,
    
    /** Prevents players from placing blocks in the current session */
    NO_BLOCK_PLACE,
    
    /** Disables natural health regeneration in the current session */
    NO_NATURAL_REGEN,
    
    /** Disables the crafting interface in the current session */
    NO_CRAFTING,
    
    /** Disables hunger mechanics in the current session */
    NO_HUNGER,
    
    /** Prevents complete loss of hunger points in the current session */
    NO_COMPLETE_HUNGER_LOSS,
    
    /** Disables arena regeneration (e.g., block regeneration) */
    NO_ARENA_REGENERATION,
    
    /** Enables health indicator display for players in the current session */
    HEALTH_INDICATOR,
    
    /** Increases melee damage by 33% in the current session */
    ONE_THIRD_MORE_MELEE_DAMAGE,

    /** Lets you abandon the arena playable limits */
    NO_ARENA_LIMITS
}

