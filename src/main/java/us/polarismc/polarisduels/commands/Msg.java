package us.polarismc.polarisduels.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.HashMap;
import java.util.Objects;

public class Msg implements CommandExecutor {
    private final Main plugin;
    public Msg(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("msg")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("reply")).setExecutor(this);
    }
    private final HashMap<CommandSender,CommandSender> reply = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                plugin.utils.message(sender, "Usage: &c/msg <player> <message>");
                return true;
            }
            if (sender instanceof Player) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    plugin.utils.message(sender, "&b" + args[0] + " &cis not connected");
                    return true;
                }
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]).append(" ");
                }
                String msg = message.toString().trim();
                plugin.utils.message(sender, "&8(&3You &7» &b" + target.getName() + "&8)&7: " + msg);
                plugin.utils.message(target, Sound.BLOCK_NOTE_BLOCK_BELL, "&8(&3" + sender.getName() + " &7» &bYou&8)&7: " + msg);
                reply.put(target,sender);
                reply.put(sender,target);
            }
        }
        if (cmd.getName().equalsIgnoreCase("reply")) {
            if (args.length == 0) {
                plugin.utils.message(sender, "Usage: &c/r <message>");
                return true;
            }
            if (sender instanceof Player) {
                if (!reply.containsKey(sender)) {
                    plugin.utils.message(sender, "You don't have anyone to reply to.");
                    return true;
                }
                CommandSender target = reply.get(sender);
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(arg).append(" ");
                }
                String msg = message.toString().trim();
                plugin.utils.message(sender, "&8(&3You &7» &b" + target.getName() + "&8)&7: " + msg);
                if (target instanceof Player) {
                    plugin.utils.message(target, Sound.BLOCK_NOTE_BLOCK_BELL, "&8(&3" + sender.getName() + " &7» &bYou&8)&7: " + msg);
                }
            }
        }
        return false;
    }
}