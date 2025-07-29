package us.polarismc.polarisduels.arenas.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import us.polarismc.polarisduels.arenas.setup.GridPos;
import lombok.NoArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.game.events.GameAddPlayerEvent;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.game.events.GameRemovePlayerEvent;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a dueling arena in the PolarisDuels plugin.
 * This class manages the state, players, and physical properties of an arena.
 * Each arena can be in different states (waiting, starting, active, etc.) and handles
 * player management, teleportation, and match progression.
 * 
 * <p>The arena is divided into quadrants for organization, and each arena maintains
 * its own set of spawn points, boundaries, and player list.</p>
 */

@Data
@NoArgsConstructor
public class ArenaEntity {
    /** User-friendly display name of the arena */
    private String displayName;
    
    /** Internal identifier for the arena */
    private String name;
    
    /** The world where this arena is located */
    private World world;
    
    /** First spawn point for team one */
    private Location spawnOne;
    
    /** Second spawn point for team two */
    private Location spawnTwo;
    
    /** First corner of the playable area boundary */
    private Location playableCornerOne;
    
    /** Second corner of the playable area boundary */
    private Location playableCornerTwo;
    
    /** First corner of the arena's physical boundary */
    private Location cornerOne;
    
    /** Second corner of the arena's physical boundary */
    private Location cornerTwo;
    
    /** Center point of the arena */
    private Location center;
    
    /** ItemStack used as an icon/logo for the arena */
    private ItemStack blockLogo;
    
    /** Size classification of the arena */
    private ArenaSize arenaSize;

    /**
     * The quadrant position of this arena in the world grid.
     * Used for organizing arenas in the world and preventing overlaps.
     */
    @Getter @Setter
    private GridPos quadrant; // Quadrant position (e.g., 0,0 or 1,0)

    /**
     * Calculates the quadrant based on the arena's center position and world
     * @return GridPos representing the quadrant coordinates in the arena's world
     * @throws IllegalStateException if center or world is not set
     */
    public GridPos calculateQuadrant() {
        if (center == null) {
            throw new IllegalStateException("Cannot calculate quadrant: Arena center is not set");
        }
        if (world == null) {
            throw new IllegalStateException("Cannot calculate quadrant: Arena world is not set");
        }
        int quadX = (int) Math.floor(center.getX() / 1000);
        int quadZ = (int) Math.floor(center.getZ() / 1000);
        return new GridPos(world, quadX, quadZ);
    }

    /**
     * Updates the quadrant based on the current center position
     */
    public void updateQuadrant() {
        this.quadrant = calculateQuadrant();
    }

    /**
     * Gets the quadrant position as a string in format "world,x,z"
     * @return String representation of the quadrant including world
     */
    public String getQuadrantString() {
        return quadrant != null ? 
            String.format("%s,%d,%d", quadrant.world().getName(), quadrant.x(), quadrant.z()) :
            "Not set";
    }

    /**
     * Checks if this arena has a quadrant conflict with another arena
     * Two arenas conflict if they are in the same world and have the same quadrant coordinates
     * @param other The other arena to check against
     * @return true if there is a quadrant conflict, false otherwise
     */
    public boolean hasQuadrantConflict(ArenaEntity other) {
        if (this.quadrant == null || other.quadrant == null) {
            return false;
        }
        return this.quadrant.equals(other.quadrant);
    }

    /** Current state of the arena (waiting, active, etc.) */
    @Getter
    private ArenaState arenaState;

    /** Current GameSession running in this arena */
    @Getter @Setter
    private GameSession gameSession;

    /**
     * Changes the current state of the arena and triggers appropriate state lifecycle methods.
     * 
     * @param state The new state to transition to
     * @throws IllegalArgumentException if state is null
     */
    public void setArenaState(ArenaState state) {
        if (state == null) {
            throw new IllegalArgumentException("ArenaState cannot be null");
        }
        if (this.arenaState != null) {
            this.arenaState.onDisable(state);
        }
        this.arenaState = state;
        this.arenaState.onEnable();
    }

    /**
     * Adds a player to this arena and handles the necessary setup.
     * This includes teleportation, inventory setup, and match start conditions.
     * 
     * @param player The player to add to the arena
     */
    public void addPlayer(Player player) {
        gameSession.getPlayers().add(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        Bukkit.getPluginManager().callEvent(new GameAddPlayerEvent(gameSession, player));
    }

    /**
     * Removes a player from this arena and handles cleanup.
     * This includes restoring their inventory, updating the match state, and notifying other players.
     * 
     * @param player The player to remove from the arena
     * @param plugin The main plugin instance
     * @throws IllegalArgumentException if player or plugin is null
     */
    public void removePlayer(Player player, Main plugin) {
        gameSession.getPlayers().remove(player.getUniqueId());
        Bukkit.getPluginManager().callEvent(new GameRemovePlayerEvent(gameSession, player));

        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        if (duelsPlayer.getTeam() != null) {
            duelsPlayer.getTeam().removePlayer(duelsPlayer);
        }

        if (player.isOnline()) {
            plugin.getArenaManager().getRollBackManager().restore(player);
        }
    }

    /**
     * Gets a list of online players currently in this arena.
     *
     * @return A list of online Player objects in this arena
     */
    public List<Player> getOnlinePlayers() {
        return gameSession.getPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the specified player is in this arena.
     * 
     * @param player The player to check
     * @return true if the player is in this arena, false otherwise
     */
    public boolean hasPlayer(Player player) {
        return hasPlayer(player.getUniqueId());
    }

    /**
     * Checks if a player with the specified UUID is in this arena.
     * 
     * @param uuid The UUID of the player to check
     * @return true if a player with this UUID is in the arena, false otherwise
     */
    public boolean hasPlayer(UUID uuid) {
        return gameSession.getPlayers().contains(uuid);
    }
}
