package us.polarismc.polarisduels.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.duel.DuelTeam;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player in the duels system, maintaining their state and team information.
 * This class is used to track player-specific data and status throughout the dueling process.
 */
@Getter
@Setter
public class DuelsPlayer {
    private final Main plugin;
    /** The player's unique identifier */
    private final UUID uuid;
    /** The player's username */
    private final String name;
    /** The player's IP address */
    private final String ip;
    /** Whether the player is currently in a queue */
    private boolean queue = false;
    /** Whether the player's duel is about to start */
    private boolean startingDuel = false;
    /** Whether the player is currently in a duel */
    private boolean duel = false;
    /** Whether the player's actions are temporarily on hold */
    private boolean onHold = false;
    /** The team the player is currently on, if any */
    private DuelTeam team = null;
    /** Whether the player is in the process of disconnecting */
    private boolean disconnecting = false;

    /**
     * Creates a new DuelsPlayer instance with the specified UUID and name.
     * Automatically retrieves the player's IP address.
     *
     * @param uuid The player's UUID
     * @param name The player's username
     * @throws NullPointerException if the player's address cannot be determined
     */
    public DuelsPlayer(UUID uuid, String name) {
        this.plugin = Main.getInstance();

        this.uuid = uuid;
        this.name = name;
        this.ip = Objects.requireNonNull(getPlayer().getAddress()).getAddress().getHostAddress();
    }

    /**
     * Gets the Bukkit Player instance associated with this DuelsPlayer.
     *
     * @return The Player instance, or null if the player is offline
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Checks if the player is currently online and not disconnecting.
     *
     * @return true if the player is online and not disconnecting, false otherwise
     */
    public boolean isOnline() {
        Player p = Bukkit.getPlayer(uuid);
        if (disconnecting) return false;
        else return p != null;
    }
}

