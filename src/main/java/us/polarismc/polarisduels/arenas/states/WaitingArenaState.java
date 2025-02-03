package us.polarismc.polarisduels.arenas.states;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.queue.KitType;

@Setter
@Getter
public class WaitingArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    private final ArenaEntity arena;
    private final KitType kit;
    private final int playersNeeded;
    private final int rounds;

    public WaitingArenaState(ArenaEntity arena, KitType kit, int playersNeeded, int rounds){
        this.arena = arena;
        this.kit = kit;
        this.playersNeeded = playersNeeded;
        this.rounds = rounds;
        arena.setKit(kit);
        arena.setPlayersNeeded(playersNeeded);
        arena.setRounds(rounds);
    }

    @Override
    public void onEnable(ArenaEntity arena) {
        Bukkit.getPluginManager().registerEvents(this, Main.pl);
        Main.pl.getLogger().info("WaitingArenaState enabled");
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        HandlerList.unregisterAll(this);
        Main.pl.getLogger().info("WaitingArenaState disabled");
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        if (!arena.hasPlayer(event.getPlayer())) return;
        arena.removePlayer(event.getPlayer(), Main.pl);
    }
}
