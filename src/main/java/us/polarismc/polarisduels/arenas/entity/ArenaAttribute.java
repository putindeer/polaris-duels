package us.polarismc.polarisduels.arenas.entity;

/**
 * Defines various attributes that can be applied to arenas to modify their behavior.
 * These attributes control game mechanics and player interactions within the arena.
 */
public enum ArenaAttribute {
    /** Prevents players from breaking blocks in the arena */
    NO_BLOCK_BREAK,
    
    /** Prevents destruction of the arena structure itself */
    NO_ARENA_DESTRUCTION,
    
    /** Prevents players from placing blocks in the arena */
    NO_BLOCK_PLACE,
    
    /** Disables natural health regeneration in the arena */
    NO_NATURAL_REGEN,
    
    /** Disables the crafting interface in the arena */
    NO_CRAFTING,
    
    /** Disables hunger mechanics in the arena */
    NO_HUNGER,
    
    /** Prevents complete loss of hunger points in the arena */
    NO_COMPLETE_HUNGER_LOSS,
    
    /** Disables arena regeneration (e.g., block regeneration) */
    NO_ARENA_REGENERATION,
    
    /** Enables health indicator display for players in the arena */
    HEALTH_INDICATOR,
    
    /** Increases melee damage by 33% in the arena */
    ONE_THIRD_MORE_MELEE_DAMAGE
}

