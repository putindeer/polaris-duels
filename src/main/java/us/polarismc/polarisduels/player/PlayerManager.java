package us.polarismc.polarisduels.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages all player-related operations and maintains a list of active DuelsPlayer instances.
 * This class handles player registration, retrieval, and management throughout the plugin.
 */
@SuppressWarnings("unused")
public class PlayerManager {
    /** List of all active DuelsPlayer instances */
    @Getter
    public final List<DuelsPlayer> playerList;

    /**
     * Initializes a new PlayerManager with the specified plugin instance.
     *
     * @param plugin The main plugin instance
     */
    public PlayerManager(Main plugin) {
        playerList = new ArrayList<>();
    }

    /**
     * Checks if a player exists in the manager.
     *
     * @param p The player to check
     * @return true if the player exists, false otherwise
     */
    public boolean doesPlayerExist(Player p) {
        return getDuelsPlayer(p.getUniqueId()) != null;
    }

    /**
     * Gets the DuelsPlayer instance for the specified Player.
     *
     * @param p The player to retrieve
     * @return The DuelsPlayer instance, or null if not found
     */
    public DuelsPlayer getDuelsPlayer(Player p) {
        return getDuelsPlayer(p.getUniqueId());
    }

    /**
     * Gets the DuelsPlayer instance for the specified player name.
     *
     * @param name The name of the player to retrieve
     * @return The DuelsPlayer instance, or null if not found
     */
    public DuelsPlayer getDuelsPlayer(String name) {
        for (DuelsPlayer p : playerList) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Gets the DuelsPlayer instance for the specified UUID.
     *
     * @param uid The UUID of the player to retrieve
     * @return The DuelsPlayer instance, or null if not found
     */
    public DuelsPlayer getDuelsPlayer(UUID uid) {
        for (DuelsPlayer p : playerList) {
            if (p.getUuid().equals(uid)) {
                return p;
            }
        }

        return null;
    }

    /**
     * Handles player join event by ensuring the player is registered.
     * Creates a new DuelsPlayer instance if one doesn't exist.
     *
     * @param player The player who joined
     */
    public void playerJoin(Player player) {
        if (getDuelsPlayer(player.getUniqueId()) == null) {
            newDuelsPlayer(player);
        }
    }

    /**
     * Creates a new DuelsPlayer instance for the specified player.
     *
     * @param p The player to create a DuelsPlayer for
     */
    public void newDuelsPlayer(Player p) {
        newDuelsPlayer(p.getUniqueId(), p.getName());
    }

    /**
     * Creates a new DuelsPlayer instance with the specified UUID and name.
     *
     * @param uid The UUID of the player
     * @param name The name of the player
     */
    public void newDuelsPlayer(UUID uid, String name) {
        DuelsPlayer newPlayer = new DuelsPlayer(uid,name);
        playerList.add(newPlayer);
    }
}
