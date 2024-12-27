package us.polarismc.polarisduels.utils;

import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.commands.ArenaCommands;
import us.polarismc.polarisduels.commands.Links;
import us.polarismc.polarisduels.commands.Msg;
import us.polarismc.polarisduels.commands.staff.Broadcast;
import us.polarismc.polarisduels.commands.staff.GameModeCMD;
import us.polarismc.polarisduels.events.HubEvents;

import java.text.DecimalFormat;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class StartThings {
    private final Main plugin;
    public StartThings(Main plugin) {
        this.plugin = plugin;
        onEnable();
    }

    public void onEnable() {
        registerListeners();
        registerCommands();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        // Fast Board
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (FastBoard board : plugin.boards.values()) {
                updateBoard(board);
            }
        }, 0, 20);
        plugin.getLogger().info("PolarisDuels is ON.");

        // TAB
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(p -> p.sendPlayerListHeaderAndFooter(
                Component.text(plugin.utils.chat("&9&lPolaris Duels")),
                Component.text(plugin.utils.chat("&7Ping: &9") + p.getPing() + " &8| &7Tps: " + new DecimalFormat("##").format(plugin.getServer().getTPS()[0]))
        )),0, 100);
    }

    public void registerCommands() {
        // User commands (polarisduels.commands)
        new Msg(plugin);
        new Links(plugin);
        // Admin commands (polarisduels.admin)
        new Broadcast(plugin);
        new GameModeCMD(plugin);
        // Specific commands
        new ArenaCommands(plugin);
    }

    public void registerListeners() {
        new HubEvents(plugin);
    }

    private void updateBoard(FastBoard board) {
        board.updateLines(
                "",
                "Players: " + plugin.getServer().getOnlinePlayers().size(),
                "",
                "polarismc.us",
                ""
        );
    }
}
