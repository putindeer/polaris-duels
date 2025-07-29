package us.polarismc.polarisduels.managers.player;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.hub.HubEvents;

public class PlayerRollBackManager {
    private final Main plugin;

    public PlayerRollBackManager(Main plugin) {
        this.plugin = plugin;
    }

    public void restore(Player player) {
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        duelsPlayer.setDuel(false);
        plugin.tabManager.resetTabList(player);

        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setLevel(0);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.setSaturation(5.0f);
        player.setItemOnCursor(new ItemStack(Material.AIR));
        HubEvents.giveLobbyItems(player);

        World world = Bukkit.getWorld("lobby");
        Location loc = new Location(world, 0, 100, 0);
        player.teleport(loc);

        plugin.utils.delay(() -> player.setFireTicks(0));
    }
}






