package us.polarismc.polarisduels.game.states;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaState;
import us.polarismc.polarisduels.game.GameType;
import us.polarismc.polarisduels.managers.hub.HubEvents;
import us.polarismc.polarisduels.game.events.GameAddPlayerEvent;
import us.polarismc.polarisduels.game.events.GameRemovePlayerEvent;
import us.polarismc.polarisduels.game.GameSession;

import java.util.List;

/**
 * Represents the waiting state of an arena where players are joining before a match starts.
 * 
 * <p>In this state, the arena is waiting for enough players to join before starting a match.
 * The state handles player management, including player quit events, and transitions to
 * the starting state once enough players have joined.</p>
 * 
 * <p>This class implements {@link Listener} to handle player-related events.</p>
 */

@Setter
@Getter
public class QueueArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    /** The arena this state belongs to */
    private ArenaEntity arena;
    private GameSession gameSession;

    /**
     * Creates a new QueueArenaState with the specified GameSession.
     */
    public QueueArenaState(ArenaEntity arena, GameSession session) {
        this.arena = arena;
        this.gameSession = session;
        arena.setGameSession(session);
        session.setArena(arena);
    }

    /**
     * Called when the arena enters the waiting state.
     * Registers event listeners and initializes the state.
     */
    @Override
    public void onEnable() {
        plugin.getLogger().info("QueueArenaState enabled");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when the arena exits the waiting state.
     * Unregisters event listeners and cleans up resources.
     */
    @Override
    public void onDisable(ArenaState state) {
        plugin.getLogger().info("QueueArenaState disabled");
        if (state instanceof StartingArenaState) {
            arena.getOnlinePlayers().stream().map(p -> plugin.getPlayerManager().getPlayer(p)).forEach(dp -> dp.setQueue(false));
        }
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void onAddPlayer(GameAddPlayerEvent event) {
        GameSession session = event.getSession();
        if (gameSession != session) return;

        Player player = event.getPlayer();
        plugin.getPlayerManager().getPlayer(player).setQueue(true);
        player.getInventory().setItem(8, HubEvents.LEAVE_QUEUE);

        ArenaEntity arena = session.getArena();
        List<Player> list = session.getPlayerList();
        int playersNeeded = session.getQueueType().getPlayersNeeded();

        if (session.getGameType() == GameType.DUEL_1V1) {
            if (list.size() == 1) {
                player.teleport(arena.getSpawnOne());
            } else {
                player.teleport(arena.getSpawnTwo());
            }
        } else {
            player.teleport(arena.getCenter().add(0.5, 1, 0.5));
        }

        plugin.utils.message(list, player.getName() + " joined <green>(" + list.size() + "/" + playersNeeded + ")");

        if (list.size() == playersNeeded) {
            arena.setArenaState(new StartingArenaState(arena, session));
        }
    }

    @EventHandler
    private void onRemovePlayer(GameRemovePlayerEvent event) {
        GameSession session = event.getSession();
        if (gameSession != session) return;
        Player player = event.getPlayer();
        plugin.getPlayerManager().getPlayer(player).setQueue(false);
        List<Player> list = session.getPlayerList();
        if (list.isEmpty()) {
            plugin.getArenaManager().setInactiveState(session.getArena());
        } else {
            plugin.utils.message(list, player.getName() + " quit &c(" + list.size() + "/" + session.getQueueType().getPlayersNeeded() + ")");
        }
    }
}
