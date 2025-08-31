package us.polarismc.polarisduels.managers.hub;

import fr.mrmicky.fastboard.adventure.FastBoard;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import us.polarismc.polarisduels.Main;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;

/**
 * Main hub manager that handles all hub-related functionality.
 * Manages items, scoreboards, teleportation, and player state in the lobby.
 */
public class HubManager {
    private final Main plugin;

    /**
     * Initializes the HubManager with all its functionality.
     *
     * @param plugin The main plugin instance
     */
    public HubManager(Main plugin) {
        this.plugin = plugin;
        new HubEvents(plugin, this);
    }

    /**
     * Handles a player joining the hub.
     * Sets up all necessary components for the player.
     *
     * @param player The player joining
     */
    public void handlePlayerJoin(Player player) {
        // Remove from any existing team
        var duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        if (duelsPlayer.getTeam() != null) {
            duelsPlayer.getTeam().removePlayer(duelsPlayer);
        }

        // Setup all hub components
        setupScoreboard(player);
        teleportToLobby(player);
        resetPlayerState(player);
        giveLobbyItems(player);
    }

    /**
     * Handles a player leaving the hub.
     * Cleans up all player-related data.
     *
     * @param player The player leaving
     */
    public void handlePlayerQuit(Player player) {
        removeScoreboard(player);
    }

    /**
     * Sets up the scoreboard for a player.
     *
     * @param player The player to set up the scoreboard for
     */
    public void setupScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(plugin.utils.chat("<blue><bold>Polaris Duels"));
        plugin.boards.put(player.getUniqueId(), board);

        String tps = new DecimalFormat("##").format(plugin.getServer().getTPS()[0]);
        String footer = String.format("<gray>Ping: <blue>%d <dark_gray>| <gray>Tps: <blue>%s", player.getPing(), tps);

        player.sendPlayerListHeaderAndFooter(
                plugin.utils.chat("<blue><bold>Polaris Duels"),
                plugin.utils.chat(footer)
        );
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Removes the scoreboard for a player.
     *
     * @param player The player to remove the scoreboard from
     */
    public void removeScoreboard(Player player) {
        FastBoard board = plugin.boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    /**
     * Teleports a player to the lobby spawn point.
     *
     * @param player The player to teleport
     */
    public void teleportToLobby(Player player) {
        World lobbyWorld = Bukkit.getWorld("lobby");
        if (lobbyWorld == null) return;

        Location spawn = new Location(null, 0.5, 100, 0.5, 0, 0);
        spawn.setWorld(lobbyWorld);
        player.teleport(spawn);
    }

    /**
     * Resets a player's game state to default lobby state.
     *
     * @param player The player to reset
     */
    public void resetPlayerState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);

        // Clear all active potion effects
        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);

        // Reset health and food
        player.setHealth(20);
        plugin.utils.setMaxHealth(player);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);

        // Reset XP
        player.setLevel(0);
        player.setExp(0.0f);
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).setBaseValue(4.5);

        // Reset player movement speed to default (0.2 is the default walking speed in Minecraft)
        // Using setWalkSpeed which is more reliable across different Bukkit/Spigot versions
        player.setWalkSpeed(0.2f);

        // Reset other player attributes
        player.setFlySpeed(0.1f); // Default fly speed
        player.setAllowFlight(false); // Disable flight by default
    }

    /**
     * Gives lobby items to a player using their current layout.
     *
     * @param player The player to give items to
     */
    public void giveLobbyItems(Player player) {
        var duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        HubLayout layout = duelsPlayer.getLayout();

        giveLobbyItems(player, layout);
    }

    /**
     * Gives lobby items to a player using a specific layout.
     *
     * @param player The player to give items to
     * @param layout The layout to use for item placement
     */
    public void giveLobbyItems(Player player, HubLayout layout) {
        Inventory inventory = player.getInventory();
        inventory.clear();

        for (Map.Entry<Integer, HubItem> entry : layout.getLayout().entrySet()) {
            ItemStack item = entry.getValue().getItem();
            inventory.setItem(entry.getKey(), item);
        }
    }

    /**
     * Gets the hub item type for a given ItemStack.
     *
     * @param item The ItemStack to identify
     * @return The HubItemType if found, null otherwise
     */
    public HubItem getItemType(ItemStack item) {
        for (HubItem itemType : HubItem.values()) {
            if (itemType.getItem().equals(item)) {
                return itemType;
            }
        }
        return null;
    }

}