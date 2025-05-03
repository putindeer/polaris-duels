package us.polarismc.polarisduels.managers.tab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.List;

public class TabManager implements Listener {
    private final Main plugin;
    public TabManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Actualiza el TAB del jugador receptor para que solo vea a los jugadores de la lista (por ejemplo, los que están en la misma arena).
     * @param player El jugador al que se le actualiza la lista
     * @param list Los jugadores que deben mostrarse
     */
    public void setTabList(Player player, List<Player> list) {
        Bukkit.getOnlinePlayers().stream().filter(p -> !list.contains(p)).forEach(player::unlistPlayer);
    }

    /**
     * Actualiza el TAB del jugador receptor para que vea a jugadores que se han añadido a la lista.
     * @param player El jugador al que se le actualiza la lista
     * @param list Los jugadores que deben mostrarse
     */
    public void refreshTabList(Player player, List<Player> list) {
        list.stream().filter(p -> !player.isListed(p)).forEach(player::listPlayer);
    }

    /**
     * Reinicia el TAB del jugador receptor para que vea a todos los players.
     * @param player El jugador al que se le reinicia la lista
     */
    public void resetTabList(Player player) {
        Bukkit.getOnlinePlayers().forEach(player::listPlayer);
    }

    /**
     * Comprueba si debe filtrarle el TAB al jugador
     * @param duelsPlayer El jugador al que filtrarle el TAB
     * @return Si se debería filtrar o no
     */
    private boolean shouldFilterTab(DuelsPlayer duelsPlayer) {
        return duelsPlayer.isDuel()
                || duelsPlayer.isQueue()
                || duelsPlayer.isStartingDuel();
    }

    private boolean shouldFilterTab(Player player) {
        return shouldFilterTab(plugin.getPlayerManager().getDuelsPlayer(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getOnlinePlayers().stream().filter(this::shouldFilterTab).forEach(p -> p.unlistPlayer(event.getPlayer()));
    }
}
