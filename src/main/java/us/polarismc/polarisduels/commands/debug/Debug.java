package us.polarismc.polarisduels.commands.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ArenaState;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;

import java.util.Objects;

public class Debug implements CommandExecutor {
    private final Main plugin;
    public Debug(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("debug")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (plugin.getArenaManager().getArenas().isEmpty()) {
            plugin.getLogger().info("No arenas have been created yet.");
            return false;
        }

        plugin.getLogger().info("===== Arena Debug Information =====");

        for (ArenaEntity arena : plugin.getArenaManager().getArenas()) {
            String arenaInfo = "Arena Name: " + arena.getName() +
                    "\nDisplay Name: " + arena.getDisplayName() +
                    "\nState: " + getArenaStateName(arena.getArenaState()) +
                    "\nPlayers Needed: " + (arena.getArenaState() instanceof WaitingArenaState
                    ? ((WaitingArenaState) arena.getArenaState()).getPlayersNeeded() : "N/A") +
                    "\nRounds: " + (arena.getArenaState() instanceof WaitingArenaState
                    ? ((WaitingArenaState) arena.getArenaState()).getRounds() : "N/A");

            plugin.getLogger().info(arenaInfo);
        }

        plugin.getLogger().info("===== End of Debug Info =====");
        return true;
    }

    private String getArenaStateName(ArenaState state) {
        if (state instanceof InactiveArenaState) {
            return "Inactive";
        } else if (state instanceof WaitingArenaState) {
            return "Waiting";
        }
        return "Unknown";
    }
}
