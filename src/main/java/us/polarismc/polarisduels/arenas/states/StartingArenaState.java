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

/**
 * Represents the starting state of an arena where the countdown to the match is in progress.
 * 
 * <p>In this state, players are prepared for the match by equipping them with the appropriate kit
 * and starting a countdown. The state transitions to the active state when the countdown completes.</p>
 * 
 * <p>This class implements {@link Listener} to handle player-related events during the countdown.</p>
 */

public class StartingArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    
    /** The countdown task that manages the pre-match countdown */
    @Getter
    private StartCountdownTask startCountdownTask;
    
    /** The arena this state belongs to */
    private ArenaEntity arena;
    /**
     * Called when the arena enters the starting state.
     * Sets up the countdown and prepares players for the match.
     * 
     * @param arena The arena that entered this state
     * @throws IllegalArgumentException if arena is null
     */
    @Override
    public void onEnable(ArenaEntity arena) {
        if (arena == null) {
            throw new IllegalArgumentException("Arena cannot be null");
        }
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
        plugin.getLogger().info("StartingArenaState enabled");
    }

    /**
     * Called when the arena exits the starting state.
     * Cleans up event listeners and resources.
     */
    @Override
    public void onDisable() {
        plugin.getLogger().info("StartingArenaState disabled");
        HandlerList.unregisterAll(this);
    }

    /**
     * Equips a player with the appropriate kit for the arena.
     * 
     * @param p The player to equip
     * @param arena The arena the player is in
     * @throws IllegalArgumentException if player or arena is null
     */
    public void setKit(Player p, ArenaEntity arena) {
        if (p == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (arena == null) {
            throw new IllegalArgumentException("Arena cannot be null");
        }
        ItemStack[] items = plugin.getKitManager().loadKit(p.getUniqueId(), arena.getKit());
        p.getInventory().clear();
        p.getInventory().setContents(items);
    }

    /**
     * Handles player quit events while the arena is in starting state.
     * Removes the player from the arena if they were in this arena.
     * 
     * @param event The PlayerQuitEvent that was triggered
     */
    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if (!arena.hasPlayer(event.getPlayer())) return;
        arena.removePlayer(event.getPlayer(), Main.pl);
    }
}
