package us.polarismc.polarisduels.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

public class Broadcast implements CommandExecutor {
    private final Main plugin;
    public Broadcast(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("broadcast")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("duels.admin")){
            sender.sendMessage(plugin.utils.chat("&cYou dont have permission to execute this command"));
            return true;
        }
        if (args.length < 1){
            sender.sendMessage(plugin.utils.chat("&cUsage: /bc <text>"));
            return true;
        } else {
            StringBuilder message = new StringBuilder();
            for (String part : args) {
                if (!message.toString().isEmpty()) message.append(" ");
                message.append(part);
            }
            for (Player all : Bukkit.getOnlinePlayers()) {
                all.sendMessage(plugin.utils.chat(plugin.utils.prefix + message));
                all.playSound(all.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10, 1);
            }
        }
        return false;
    }
}