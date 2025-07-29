package us.polarismc.polarisduels.managers.tab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.game.events.GameAddPlayerEvent;
import us.polarismc.polarisduels.game.events.GameRemovePlayerEvent;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

import java.util.List;

public class TabManager implements Listener {
    private final Main plugin;
    public TabManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Updates the TAB list for the specified player to only show players from the provided list.
     * This is typically used to show only players in the same arena.
     *
     * @param player The player whose TAB list will be updated
     * @param list The list of players that should be visible to the player
     */
    public void setTabList(Player player, List<Player> list) {
        Bukkit.getOnlinePlayers().stream().filter(p -> !list.contains(p)).forEach(player::unlistPlayer);
    }

    /**
     * Refreshes the TAB list for the specified player to include newly added players from the list.
     * This is used to dynamically update the TAB list when new players should be visible.
     *
     * @param player The player whose TAB list will be updated
     * @param list The list of players that should be visible to the player
     */
    public void refreshTabList(Player player, List<Player> list) {
        list.stream().filter(p -> !player.isListed(p)).forEach(player::listPlayer);
    }

    public void refreshTabList(List<Player> list) {
        list.forEach(p -> refreshTabList(p, list));
    }

    /**
     * Resets the TAB list for the specified player to show all online players.
     * This is typically used when a player leaves an arena or similar restricted area.
     *
     * @param player The player whose TAB list will be reset
     */
    public void resetTabList(Player player) {
        Bukkit.getOnlinePlayers().forEach(player::listPlayer);
    }

    /**
     * Checks if the TAB list should be filtered for the specified player.
     * This is used to determine if the player is in a state where their TAB list should be restricted.
     *
     * @param duelsPlayer The player to check
     * @return true if the TAB list should be filtered, false otherwise
     */
    private boolean shouldFilterTab(DuelsPlayer duelsPlayer) {
        return duelsPlayer.isDuel()
                || duelsPlayer.isQueue()
                || duelsPlayer.isStartingDuel();
    }

    /**
     * Checks if the TAB list should be filtered for the specified player.
     * This is used to determine if the player is in a state where their TAB list should be restricted.
     *
     * @param player The Bukkit player to check
     * @return true if the TAB list should be filtered, false otherwise
     */
    private boolean shouldFilterTab(Player player) {
        return shouldFilterTab(plugin.getPlayerManager().getPlayer(player));
    }

    /**
     * Handles player join events to maintain proper TAB list filtering.
     * When a player joins the server, this method hides the new player from the TAB list
     * of all existing players who should have filtered TAB lists (players in duels, queue, or starting duels).
     * This ensures that players in combat situations don't see irrelevant players in their TAB list.
     *
     * @param event The PlayerJoinEvent containing information about the joining player
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getOnlinePlayers().stream().filter(this::shouldFilterTab).forEach(p -> p.unlistPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onAddPlayer(GameAddPlayerEvent event) {
        List<Player> list = event.getSession().getArena().getOnlinePlayers();
        setTabList(event.getPlayer(), list);
        refreshTabList(list);
    }

    @EventHandler
    public void onRemovePlayer(GameRemovePlayerEvent event) {
        resetTabList(event.getPlayer());
        refreshTabList(event.getSession().getArena().getOnlinePlayers());
    }
}
