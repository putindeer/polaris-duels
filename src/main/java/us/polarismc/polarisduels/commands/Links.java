package us.polarismc.polarisduels.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

public class Links implements CommandExecutor {
    private final Main plugin;

    public Links(Main plugin) {
        this.plugin = plugin;

        Objects.requireNonNull(plugin.getCommand("links")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] strings) {
        Player p = (Player) sender;
        p.sendMessage(plugin.utils.chat("&9Discord: discord.polarismc.us"));
        p.sendMessage(plugin.utils.chat("&bStore: store.polarismc.us"));
        p.sendMessage(plugin.utils.chat("&aApplys: applys.polarismc.us"));
        return true;
    }
}