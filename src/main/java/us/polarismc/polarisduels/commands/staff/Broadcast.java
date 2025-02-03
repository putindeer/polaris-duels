package us.polarismc.polarisduels.commands.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("duels.admin")){
            plugin.utils.message(sender, "&cYou dont have permission to execute this command");
            return true;
        }
        if (args.length < 1){
            plugin.utils.message(sender, "&cUsage: /bc <text>");
            return true;
        } else {
            Component msg = Component.empty();
            for (String part : args) {
                if (msg.equals(Component.empty())) msg = Component.text(part);
                else msg = msg.append(Component.text(" ").append(Component.text(part)));
            }
            plugin.utils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_BIT);
        }
        return false;
    }
}