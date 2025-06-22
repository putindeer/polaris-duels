package us.polarismc.polarisduels.commands.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ArenaState;
import us.polarismc.polarisduels.queue.KitType;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.Objects;

/**
 * A debugging command that provides detailed information about the plugin's state.
 * This command is used for troubleshooting and monitoring the plugin's internal state.
 * 
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
        plugin.getLogger().info("===== PolarisDuels Debug Information =====");

        // General plugin information
        plugin.getLogger().info("Total Arenas Configured: " + plugin.getArenaManager().getArenas().size());

        // Arena details
        for (ArenaEntity arena : plugin.getArenaManager().getArenas()) {
            plugin.getLogger().info("--- Arena: " + arena.getName() + " ---");
            plugin.getLogger().info("Display Name: " + arena.getDisplayName());
            plugin.getLogger().info("State: " + getArenaStateName(arena.getArenaState()));
            plugin.getLogger().info("Players: " + arena.getPlayers().size());
            plugin.getLogger().info("Players Needed: " + arena.getPlayersNeeded());
            plugin.getLogger().info("Rounds: " + arena.getRounds());

            // Method results
            plugin.getLogger().info("Player List: " + arena.getPlayerList());
            plugin.getLogger().info("Spawn One: " + arena.getSpawnOne());
            plugin.getLogger().info("Spawn Two: " + arena.getSpawnTwo());
        }

        // Player manager details
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(onlinePlayer);
            plugin.getLogger().info("--- Player: " + onlinePlayer.getName() + " ---");
            plugin.getLogger().info("In Queue: " + duelsPlayer.isQueue());
            plugin.getLogger().info("In Duel: " + duelsPlayer.isDuel());
            plugin.getLogger().info("Team: " + duelsPlayer.getTeam());

            // Find the arena the player is in
            ArenaEntity playerArena = plugin.getArenaManager().getArenas().stream()
                    .filter(arena -> arena.getPlayers().contains(onlinePlayer.getUniqueId()))
                    .findFirst()
                    .orElse(null);

            if (playerArena != null) {
                plugin.getLogger().info("Current Arena: " + playerArena.getName());
                plugin.getLogger().info("Arena State: " + getArenaStateName(playerArena.getArenaState()));
            } else {
                plugin.getLogger().info("Current Arena: None");
            }
        }

        // ArenaManager verifications
        plugin.getLogger().info("--- ArenaManager Checks ---");
        plugin.getArenaManager().findInactiveArena().ifPresentOrElse(
                arena -> plugin.getLogger().info("Open Arena Found: " + arena),
                () -> plugin.getLogger().info("No Inactive Arena was found.")
                );
        for (KitType kit : KitType.values()) {
            for (int playersNeeded : new int[]{2, 4, 6}) {
                plugin.getArenaManager().findCompatibleArenaNoMethod(kit, playersNeeded, 2)
                        .ifPresentOrElse(
                                arena -> plugin.getLogger().info("Compatible Arena Found for " + kit + " with " + playersNeeded + " players needed: " + arena),
                                () -> plugin.getLogger().info("No Compatible Arena Found for " + kit + " with " + playersNeeded + " players needed")
                        );
            }
        }

        // RollBackManager status
        plugin.getLogger().info("Rollback Manager Active: " + (plugin.getArenaManager().getRollBackManager() != null));

        plugin.getLogger().info("===== End of Debug Info =====");
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