package us.polarismc.polarisduels.commands;

import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.HashMap;
import java.util.Objects;

/**
 * Handles private messaging between players with /msg and /reply commands.
 * Maintains a reply map to easily respond to the last person who messaged you.
 * <p>
 * Commands:
 * - /msg <player> <message> - Sends a private message to the specified player
 * - /reply <message> - Replies to the last player who messaged you
 * <p>
 * Permission: None (default to all players)
 */
public class Msg implements CommandExecutor {
    private final Main plugin;
    
    /** Tracks the last person each player messaged for reply functionality */
    private final HashMap<CommandSender, CommandSender> replyMap = new HashMap<>();
    
    /**
     * Initializes the Msg command and registers both /msg and /reply commands.
     * 
     * @param plugin The main plugin instance
     */
    public Msg(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("msg")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("reply")).setExecutor(this);
    }

    /**
     * Handles both /msg and /reply commands.
     *
     * @param sender The command sender
     * @param cmd The command being executed
     * @param label The alias of the command used
     * @param args The command arguments
     * @return true if the command was handled successfully
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (cmd.getName().equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                plugin.utils.message(sender, "Usage: &c/msg <player> <message>");
                return true;
            }
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

            plugin.utils.message(sender, false, "&8(&3You &7» &b" + target.getName() + "&8)&7: " + msg);
            plugin.utils.message(target, false, Sound.sound(SoundEventKeys.BLOCK_NOTE_BLOCK_BELL, Sound.Source.MASTER, 10, 1),
                    "&8(&3" + sender.getName() + " &7» &bYou&8)&7: " + msg);

            // Update reply map for both parties
            replyMap.put(target, sender);
            replyMap.put(sender, target);
        }
        if (cmd.getName().equalsIgnoreCase("reply")) {
            if (args.length == 0) {
                plugin.utils.message(sender, "Usage: &c/r <message>");
                return true;
            }
            if (!replyMap.containsKey(sender)) {
                plugin.utils.message(sender, "&cYou don't have anyone to reply to.");
                return true;
            }
            CommandSender target = replyMap.get(sender);
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            String msg = message.toString().trim();

            plugin.utils.message(sender, false, "&8(&3You &7» &b" + target.getName() + "&8)&7: " + msg);
            plugin.utils.message(target, false, Sound.sound(SoundEventKeys.BLOCK_NOTE_BLOCK_BELL, Sound.Source.MASTER, 10, 1),
                    "&8(&3" + sender.getName() + " &7» &bYou&8)&7: " + msg);
        }
        return false;
    }
}