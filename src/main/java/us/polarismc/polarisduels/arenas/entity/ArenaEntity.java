package us.polarismc.polarisduels.arenas.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import us.polarismc.polarisduels.arenas.commands.GridPos;
import lombok.NoArgsConstructor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.states.ArenaState;
import us.polarismc.polarisduels.arenas.states.StartingArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;
import us.polarismc.polarisduels.queue.KitType;
import us.polarismc.polarisduels.events.HubEvents;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.ArrayList;
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
    
    /** Current state of the arena (waiting, active, etc.) */
    private ArenaState arenaState;
    
    /** List of player UUIDs currently in this arena */
    private final List<UUID> players = new ArrayList<>();
    
    /** Type of kit used in this arena */
    private KitType kit;
    
    /** Number of players needed to start a match */
    private int playersNeeded;
    
    /** Number of rounds in the match */
    private int rounds;
    /** 
     * The quadrant position of this arena in the world grid.
     * Used for organizing arenas in the world and preventing overlaps.
     */
    @Setter
    @Getter
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
            this.arenaState.onDisable();
        }
        this.arenaState = state;
        this.arenaState.onEnable(this);
    }

    /**
     * Gets a list of online players currently in this arena.
     * 
     * @return A list of online Player objects in this arena
     */
    public List<Player> getPlayerList() {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //TODO - SISTEMA DE TEAMS

    /**
     * Adds a player to this arena and handles the necessary setup.
     * This includes teleportation, inventory setup, and match start conditions.
     * 
     * @param player The player to add to the arena
     * @param plugin The main plugin instance
     * @throws IllegalArgumentException if player or plugin is null
     */
    public void addPlayer(Player player, Main plugin) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        players.add(player.getUniqueId());
        plugin.getTabManager().setTabList(player, getPlayerList());
        getPlayerList().forEach(p -> plugin.getTabManager().refreshTabList(p, getPlayerList()));
        plugin.getPlayerManager().getDuelsPlayer(player).setQueue(true);
        plugin.utils.message(getPlayerList(), player.getName() + " joined &a(" + players.size() + "/" + playersNeeded + ")");
        player.setGameMode(GameMode.SURVIVAL);


        plugin.getArenaManager().getRollBackManager().save(player);

        player.getInventory().setItem(8,
                plugin.utils.ib(Material.BARRIER)
                        .name(HubEvents.LEAVE_QUEUE)
                        .build()
        );
        int halfSize = playersNeeded / 2;
        if (players.size() <= halfSize) {
            player.teleport(spawnOne);
        } else {
            player.teleport(spawnTwo);
        }

        if (players.size() == playersNeeded) {
            setArenaState(new StartingArenaState());
        }
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
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        players.remove(player.getUniqueId());
        plugin.getTabManager().resetTabList(player);
        getPlayerList().forEach(p -> plugin.getTabManager().setTabList(p, getPlayerList()));
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
        if (duelsPlayer.getTeam() != null) {
            duelsPlayer.getTeam().removePlayer(duelsPlayer);
        }
        if (duelsPlayer.isQueue()) {
            duelsPlayer.setQueue(false);
        }
        if (duelsPlayer.isDuel()){
            duelsPlayer.setDuel(false);
        }
        if (duelsPlayer.isStartingDuel()) {
            duelsPlayer.setStartingDuel(false);
        }
        if (duelsPlayer.isOnHold()) {
            duelsPlayer.setOnHold(false);
        }

        if (player.isOnline()) {
            plugin.getArenaManager().getRollBackManager().restore(player);
        }

        if (arenaState instanceof StartingArenaState) {
            setArenaState(new WaitingArenaState(this, kit, playersNeeded, rounds));
            for (Player p : getPlayerList()) {
                plugin.utils.message(getPlayerList(), player.getName() + " quit &c(" + players.size() + "/" + playersNeeded + ")");
                p.showTitle(Title.title(plugin.utils.chat("&cMatch Cancelled"), plugin.utils.chat("Someone left the queue.")));
                p.getInventory().clear();
                p.getInventory().addItem(
                        plugin.utils.ib(Material.BARRIER)
                                .name(HubEvents.LEAVE_QUEUE)
                                .build()
                );
            }
        }
        if (arenaState instanceof WaitingArenaState) {
            plugin.utils.message(getPlayerList(), player.getName() + " quit &c(" + players.size() + "/" + playersNeeded + ")");
            if (players.isEmpty()) {
                plugin.getArenaManager().setInactiveState(this);
            }
        }
    }

    /**
     * Checks if the specified player is in this arena.
     * 
     * @param player The player to check
     * @return true if the player is in this arena, false otherwise
     * @throws IllegalArgumentException if player is null
     */
    public boolean hasPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return hasPlayer(player.getUniqueId());
    }

    /**
     * Checks if a player with the specified UUID is in this arena.
     * 
     * @param uuid The UUID of the player to check
     * @return true if a player with this UUID is in the arena, false otherwise
     * @throws IllegalArgumentException if uuid is null
     */
    public boolean hasPlayer(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        return players.contains(uuid);
    }
}
