package us.polarismc.polarisduels.arenas.states;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.tasks.StartCountdownTask;
import us.polarismc.polarisduels.player.DuelsPlayer;

public class StartingArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    @Getter
    private StartCountdownTask startCountdownTask;
    private ArenaEntity arena;
    @Override
    public void onEnable(ArenaEntity arena) {
        Bukkit.getPluginManager().registerEvents(this, Main.pl);
        this.arena = arena;
        for (Player p : arena.getPlayerList()) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            duelsPlayer.setQueue(false);
            duelsPlayer.setStartingDuel(true);
            setKit(p, arena);
        }
        this.startCountdownTask =  new StartCountdownTask(plugin, arena, 10);
        this.startCountdownTask.runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        HandlerList.unregisterAll(this);
    }

    public void setKit(Player p, ArenaEntity arena) {
        ItemStack[] items = plugin.getKitManager().loadKit(p.getUniqueId(), arena.getKit());
        p.getInventory().clear();
        p.getInventory().setContents(items);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        if (!arena.hasPlayer(event.getPlayer())) return;
        arena.removePlayer(event.getPlayer(), Main.pl);
    }
}
