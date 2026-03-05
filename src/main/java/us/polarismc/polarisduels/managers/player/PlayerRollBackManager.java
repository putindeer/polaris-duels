package us.polarismc.polarisduels.managers.player;

import org.bukkit.*;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;

public class PlayerRollBackManager {
    private final Main plugin;

    public PlayerRollBackManager(Main plugin) {
        this.plugin = plugin;
    }

    public void restore(Player player) {
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        duelsPlayer.setDuel(false);
        plugin.tabManager.resetTabList(player);

        plugin.hubManager.resetPlayerState(player);
        plugin.hubManager.giveLobbyItems(player);
        plugin.hubManager.teleportToLobby(player);

        plugin.utils.delay(() -> player.setFireTicks(0));
    }
}






