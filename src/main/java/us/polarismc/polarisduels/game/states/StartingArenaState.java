package us.polarismc.polarisduels.game.states;

import io.papermc.paper.registry.keys.SoundEventKeys;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaState;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.game.GameType;
import us.polarismc.polarisduels.game.events.GameAddPlayerEvent;
import us.polarismc.polarisduels.game.events.GameRemovePlayerEvent;
import us.polarismc.polarisduels.managers.hub.HubEvents;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

import java.util.HashMap;
import java.util.List;

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
    /** The arena this state belongs to */
    private final ArenaEntity arena;
    /** The GameSession that calls this state */
    private final GameSession gameSession;

    /**
     * Creates a new StartingArenaState with the specified GameSession.
     *
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public StartingArenaState(ArenaEntity arena, GameSession session) {
        this.arena = arena;
        this.gameSession = session;
        arena.setGameSession(session);
        session.setArena(arena);
    }

    /**
     * Called when the arena enters the starting state.
     * Sets up the countdown and prepares players for the match.
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (gameSession.getQueueType() == null) {
            teleportPlayers(gameSession);
        }
        arena.getOnlinePlayers().stream().map(p -> plugin.getPlayerManager().getPlayer(p)).forEach(dp -> {
            dp.setStartingDuel(true);
            setKit(dp.getPlayer());
        });
        startCountdown();
        plugin.getLogger().info("StartingArenaState enabled");
    }

    /**
     * Called when the arena exits the starting state.
     * Cleans up event listeners and resources.
     */
    @Override
    public void onDisable(ArenaState newState) {
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
        }

        if (newState instanceof QueueArenaState) {
            arena.getOnlinePlayers().stream().map(p -> plugin.getPlayerManager().getPlayer(p)).forEach(dp -> dp.setQueue(true));
        }

        arena.getOnlinePlayers().stream().map(p -> plugin.getPlayerManager().getPlayer(p)).forEach(dp -> dp.setStartingDuel(false));
        plugin.getLogger().info("StartingArenaState disabled");
        HandlerList.unregisterAll(this);
    }

    /** The countdown task that manages the pre-match countdown */
    @Getter
    private BukkitTask countdownTask;

    /**
     * Starts the countdown for the match.
     * Handles countdown display, sounds, and transitions to active state.
     */
    private void startCountdown() {
        this.countdownTask = new BukkitRunnable() {
            int secondsUntilStart = gameSession.getSecondsUntilStart();
            @Override
            public void run() {
                //TODO - testear si estos códigos tachados son necesarios
                if (secondsUntilStart <= 0) {
                    /*if (arena.getArenaState() instanceof QueueArenaState) {
                        cancel();
                        return;
                    }*/
                    cancel();
                    saveKits();
                    arena.setArenaState(new PlayingArenaState(gameSession));
                    return;
                }

                /*if (arena.getArenaState() instanceof QueueArenaState) {
                    cancel();
                    return;
                }*/

                arena.getOnlinePlayers().forEach(player -> {
                    plugin.utils.title(player, "<aqua>" + secondsUntilStart, "<white>Organize your inventory!");
                    plugin.utils.message(player, SoundEventKeys.BLOCK_NOTE_BLOCK_BIT, "The game is starting in <aqua>" + secondsUntilStart + "</aqua> seconds.");
                });

                secondsUntilStart--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    /**
     * Cancels the countdown and transitions back to queue state.
     * Called when a player leaves during countdown.
     */
    private void cancelCountdown() {
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
        }
    }

    /**
     * Saves the current inventory of all players as their kit for this arena.
     * Handles edge cases like items on cursor and full inventories.
     * Displays appropriate error messages if kit saving fails.
     */
    private void saveKits() {
        arena.getOnlinePlayers().forEach(player -> {
            if (player.getItemOnCursor().getType() != Material.AIR) {
                ItemStack item = player.getItemOnCursor().clone();
                player.setItemOnCursor(new ItemStack(Material.AIR));
                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                if (!remaining.isEmpty()) {
                    player.setItemOnCursor(remaining.values().iterator().next());
                    plugin.utils.message(player, "&cYou were holding an item on your cursor while your inventory was full. Your kit could not be saved because of this. Please avoid doing this.");
                    return;
                }
            }
            plugin.getKitManager().saveKit(player, gameSession.getKit(), player.getInventory().getContents());
        });
    }

    /**
     * Equips a player with the appropriate kit for the arena.
     *
     * @param p The player to equip
     * @throws IllegalArgumentException if player or arena is null
     */
    private void setKit(Player p) {
        ItemStack[] items = plugin.getKitManager().loadKit(p.getUniqueId(), gameSession.getKit());
        p.getInventory().clear();
        p.getInventory().setContents(items);
    }

    private void teleportPlayers(GameSession session) {
        ArenaEntity arena = session.getArena();
        List<Player> list = session.getPlayerList();

        if (session.getGameType() == GameType.DUEL_1V1) {
            Player player1 = list.getFirst();
            Player player2 = list.getLast();
            player1.teleport(arena.getSpawnOne());
            player2.teleport(arena.getSpawnTwo());
        } else {
            list.forEach(player -> player.teleport(arena.getCenter().add(0.5, 1, 0.5)));
        }
    }

    /**
     * Handles player removal events during the starting state.
     * Cancels the countdown when a player is removed.
     *
     * @param event The DuelRemovePlayerEvent that was triggered
     */
    @EventHandler
    private void onRemovePlayer(GameRemovePlayerEvent event) {
        GameSession session = event.getSession();
        if (gameSession != session) return;

        DuelsPlayer player = plugin.getPlayerManager().getPlayer(event.getPlayer());
        player.setStartingDuel(false);

        cancelCountdown();
        if (session.getQueueType() != null) {
            setBackToQueue();
        } else {
            cancelGame();
        }
    }

    private void setBackToQueue() {
        arena.getOnlinePlayers().forEach(player -> {
            plugin.utils.title(player, "<red>Match Cancelled", "A player left the queue.");
            player.getInventory().clear();
            player.getInventory().addItem(HubEvents.LEAVE_QUEUE);
        });

        arena.setArenaState(new QueueArenaState(arena, gameSession));
    }

    private void cancelGame() {
        arena.getOnlinePlayers().forEach(player -> {
            plugin.utils.title(player, "<red>Match Cancelled", "A player left the duel.");
            player.getInventory().clear();
        });
        plugin.utils.delay(20 * 5, () -> plugin.getArenaManager().setInactiveState(arena));
    }
}