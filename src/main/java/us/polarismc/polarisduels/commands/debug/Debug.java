package us.polarismc.polarisduels.commands.debug;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ArenaState;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.Objects;

public class Debug implements CommandExecutor {
    private final Main plugin;

    public Debug(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("debug")).setExecutor(this);
    }

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
            plugin.getLogger().info("In Queue: " + duelsPlayer.inQueue());
            plugin.getLogger().info("In Duel: " + duelsPlayer.inDuel());
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
        plugin.getLogger().info("Open Arena Found: " + plugin.getArenaManager().findOpenArena());
        ItemStack[] kit = new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD) };
        plugin.getLogger().info("Compatible Arena Found: " + plugin.getArenaManager().findCompatibleArena(kit, 2, 2));

        // RollBackManager status
        plugin.getLogger().info("Rollback Manager Active: " + (plugin.getArenaManager().getRollBackManager() != null));

        plugin.getLogger().info("===== End of Debug Info =====");
        return true;
    }

    private String getArenaStateName(ArenaState state) {
        if (state == null) {
            return "None";
        }
        return state.getClass().getSimpleName();
    }
}