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

    //TODO - not being able to do /msg while muted

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.utils.chat(plugin.utils.prefix + "Usage: &c/msg <player> <message>"));
                return true;
            }
            if (sender instanceof Player) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(plugin.utils.chat(plugin.utils.prefix + "&b" + args[0] + " &cno está conectado"));
                    return true;
                }
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]).append(" ");
                }
                String msg = message.toString().trim();
                sender.sendMessage(plugin.utils.chat("&8(&3Tú &7» &b" + target.getName() + "&8)&7: " + msg));
                target.sendMessage(plugin.utils.chat("&8(&3" + sender.getName() + " &7» &bTú&8)&7: " + msg));
                reply.put(target,sender);
                reply.put(sender,target);
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1,1);
            }
        }
        if (cmd.getName().equalsIgnoreCase("reply")) {
            if (args.length == 0) {
                sender.sendMessage(plugin.utils.chat(plugin.utils.prefix + "Usage: &c/r <message>"));
                return true;
            }
            if (sender instanceof Player) {
                if (!reply.containsKey(sender)) {
                    sender.sendMessage(plugin.utils.chat(plugin.utils.prefix + "No tienes a nadie para responder"));
                    return true;
                }
                CommandSender target = reply.get(sender);
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(arg).append(" ");
                }
                String msg = message.toString().trim();
                sender.sendMessage(plugin.utils.chat("&8(&3Tú &7» &b" + target.getName() + "&8)&7: " + msg));
                target.sendMessage(plugin.utils.chat("&8(&3" + sender.getName() + " &7» &bTú&8)&7: " + msg));
                if (target instanceof Player t) t.playSound(t.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1,1);
            }
        }
        return false;
    }
}