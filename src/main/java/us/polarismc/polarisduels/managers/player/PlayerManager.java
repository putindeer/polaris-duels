package us.polarismc.polarisduels.managers.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;

import java.util.*;

/**
 * Manages all player-related operations and maintains a list of active DuelsPlayer instances.
 * This class handles player registration, retrieval, and management throughout the plugin.
 */
@SuppressWarnings("unused")
public class PlayerManager {
    private final Main plugin;
    /** List of all active DuelsPlayer instances */
    @Getter
    public final Set<DuelsPlayer> playerList = new HashSet<>();

    /**
     * Initializes a new PlayerManager with the specified plugin instance.
     *
     * @param plugin The main plugin instance
     */
    public PlayerManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player exists in the manager.
     *
     * @param player The player to check
     * @return true if the player exists, false otherwise
     */
    public boolean doesPlayerExist(Player player) {
        return getPlayer(player.getUniqueId()) != null;
    }

    /**
     * Gets the DuelsPlayer instance for the specified Player.
     *
     * @param player The player to retrieve
     * @return The DuelsPlayer instance, or null if not found
     */
    public DuelsPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    /**
     * Gets the DuelsPlayer instance for the specified player name.
     *
     * @param name The name of the player to retrieve
     * @return The DuelsPlayer instance, or null if not found
     */
    public DuelsPlayer getPlayer(String name) {
        return playerList.stream().filter(dp -> dp.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Gets the DuelsPlayer instance for the specified UUID.
     *
     * @param uuid The UUID of the player to retrieve
     * @return The DuelsPlayer instance, or null if not found
     */
    public DuelsPlayer getPlayer(UUID uuid) {
        return playerList.stream().filter(dp -> dp.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Handles player join event by ensuring the player is registered.
     * Creates a new DuelsPlayer instance if one doesn't exist.
     *
     * @param player The player who joined
     */
    public void playerJoin(Player player) {
        if (getPlayer(player.getUniqueId()) == null) {
            newDuelsPlayer(player);
        }
    }

    /**
     * Creates a new DuelsPlayer instance for the specified player.
     *
     * @param plugin The player to create a DuelsPlayer for
     */
    public void newDuelsPlayer(Player plugin) {
        newDuelsPlayer(plugin.getUniqueId(), plugin.getName());
    }

    /**
     * Creates a new DuelsPlayer instance with the specified UUID and name.
     *
     * @param uuid The UUID of the player
     * @param name The name of the player
     */
    public void newDuelsPlayer(UUID uuid, String name) {
        DuelsPlayer newPlayer = new DuelsPlayer(uuid, name);
        playerList.add(newPlayer);
    }
}
