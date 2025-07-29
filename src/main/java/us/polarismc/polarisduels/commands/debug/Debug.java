package us.polarismc.polarisduels.commands.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaState;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.managers.duel.DuelTeam;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * A debugging command that provides detailed information about the plugin's state.
 * This command is used for troubleshooting and monitoring the plugin's internal state.
 * <p>
 * Permission: duels.admin
 * Usage: /debug
 */
public class Debug implements CommandExecutor {
    /**
     * Reference to the main plugin instance
     */
    private final Main plugin;

    /**
     * Initializes the Debug command and registers it with the server.
     *
     * @param plugin The main plugin instance
     */
    public Debug(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("debug")).setExecutor(this);
    }

    /**
     * Executes the debug command, collecting and displaying system information.
     *
     * @param sender The command sender
     * @param command The command being executed
     * @param label The alias of the command used
     * @param args The command arguments (not used)
     * @return true if the command was handled successfully
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        plugin.utils.log("===== PolarisDuels Debug Information =====");

        // General plugin information
        plugin.utils.log("Total Arenas Configured: " + plugin.getArenaManager().getArenas().size());

        // Arena details
        for (ArenaEntity arena : plugin.getArenaManager().getArenas()) {
            plugin.utils.log("--- Arena: " + arena.getName() + " ---");
            plugin.utils.log("Display Name: " + arena.getDisplayName());
            plugin.utils.log("State: " + getArenaStateName(arena.getArenaState()));

            // GameSession details if available
            GameSession session = arena.getGameSession();
            if (session != null) {
                plugin.utils.log("=== GameSession Details ===");
                plugin.utils.log("Session ID: " + session.getSessionId());
                plugin.utils.log("Created At: " + DateTimeFormatter.ISO_INSTANT.format(session.getCreatedAt()));
                plugin.utils.log("Kit: " + session.getKit().name());
                plugin.utils.log("Game Type: " + session.getGameType().name());
                plugin.utils.log("Rounds: " + session.getRounds());
                plugin.utils.log("Player List: " + arena.getOnlinePlayers());
                plugin.utils.log("Seconds Until Start: " + session.getSecondsUntilStart());
                plugin.utils.log("Players: " + arena.getGameSession().getPlayerList());
                plugin.utils.log("Rounds: " + arena.getGameSession().getRounds());
                plugin.utils.log("Requested Size: " + (session.getRequestedSize() != null ? session.getRequestedSize().name() : "None"));
                if (session.getQueueType() != null) {
                    plugin.utils.log("Queue Type: " + session.getQueueType().name());
                    plugin.utils.log("Players Needed: " + arena.getGameSession().getQueueType().getPlayersNeeded());
                }

                // Team information
                plugin.utils.log("Teams (" + session.getTeams().size() + "):");
                for (DuelTeam team : session.getTeams()) {
                    plugin.utils.log("  - Team: " + team.getDisplayName());
                    plugin.utils.log("    Color: " + (team.getColor() != null ? team.getColor().toString() : "None"));
                    plugin.utils.log("    Members: " + team.getMembers().size());
                    plugin.utils.log("    Alive Players: " + team.getAlivePlayers().size());
                    plugin.utils.log("    Online Players: " + team.getOnlinePlayers().size());
                    plugin.utils.log("    Wins: " + session.getWins(team));
                }

                // Participants vs Spectators
                plugin.utils.log("Participants: " + session.getPlayers().size());
                plugin.utils.log("Spectators: " + session.getSpectators().size());
                plugin.utils.log("Parties Involved: " + session.getPartiesInvolved().size());
                plugin.utils.log("Alive Teams: " + session.getAliveTeams().size());
            } else {
                plugin.utils.log("No GameSession attached to this arena");
            }

            plugin.utils.log("Spawn One: " + arena.getSpawnOne());
            plugin.utils.log("Spawn Two: " + arena.getSpawnTwo());
        }

        // Player manager details
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(onlinePlayer);
            plugin.utils.log("--- Player: " + onlinePlayer.getName() + " ---");
            plugin.utils.log("In Queue: " + duelsPlayer.isQueue());
            plugin.utils.log("In Duel: " + duelsPlayer.isDuel());
            plugin.utils.log("Starting Duel: " + duelsPlayer.isStartingDuel());
            plugin.utils.log("On Hold: " + duelsPlayer.isOnHold());
            plugin.utils.log("Team: " + duelsPlayer.getTeam());

            // Find the arena the player is in
            Optional<ArenaEntity> arena = plugin.getArenaManager().getPlayerArena(onlinePlayer);

            if (arena.isPresent()) {
                ArenaEntity playerArena = arena.get();
                plugin.utils.log("Current Arena: " + playerArena.getName());
                plugin.utils.log("Arena State: " + getArenaStateName(playerArena.getArenaState()));

                // Check if player is participant or spectator in GameSession
                GameSession session = playerArena.getGameSession();
                if (session != null) {
                    boolean isParticipant = session.isParticipant(onlinePlayer.getUniqueId());
                    boolean isSpectator = session.getSpectators().contains(onlinePlayer.getUniqueId());
                    plugin.utils.log("Session Role: " + (isParticipant ? "Participant" : isSpectator ? "Spectator" : "Unknown"));

                    // Find player's team in session
                    DuelTeam playerTeam = session.getTeams().stream()
                            .filter(team -> team.hasPlayer(onlinePlayer))
                            .findFirst()
                            .orElse(null);

                    if (playerTeam != null) {
                        plugin.utils.log("Session Team: " + playerTeam.getDisplayName());
                        plugin.utils.log("Team Alive: " + playerTeam.isAlive(onlinePlayer));
                    }
                }
            } else {
                plugin.utils.log("Current Arena: None");
            }
        }

        // RollBackManager status
        plugin.utils.log("Rollback Manager Active: " + (plugin.getArenaManager().getRollBackManager() != null));

        // GameSession statistics
        plugin.utils.log("--- GameSession Statistics ---");
        long activeSessionsCount = plugin.getArenaManager().getArenas().stream()
                .filter(arena -> arena.getGameSession() != null)
                .count();
        plugin.utils.log("Active GameSessions: " + activeSessionsCount);

        // Count sessions by game type
        plugin.getArenaManager().getArenas().stream()
                .filter(arena -> arena.getGameSession() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        arena -> arena.getGameSession().getGameType(),
                        java.util.stream.Collectors.counting()
                ))
                .forEach((gameType, count) ->
                        plugin.utils.log("Sessions with " + gameType + ": " + count)
                );

        plugin.utils.log("===== End of Debug Info =====");
        return true;
    }

    /**
     * Gets a readable name for the given arena state.
     *
     * @param state The arena state to get the name of
     * @return A string representation of the arena state
     */
    private String getArenaStateName(ArenaState state) {
        if (state == null) {
            return "None";
        }
        return state.getClass().getSimpleName();
    }
}