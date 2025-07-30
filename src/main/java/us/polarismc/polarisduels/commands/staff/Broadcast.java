package us.polarismc.polarisduels.commands.staff;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

/**
 * A command that allows staff members to send formatted broadcast messages to all online players.
 * Includes a sound effect to draw attention to the broadcast.
 * 
 * Permission: duels.admin
 * Usage: /broadcast <message>
 */
public class Broadcast implements CommandExecutor {
    /**
     * Reference to the main plugin instance
     */
    private final Main plugin;

    /**
     * Initializes the Broadcast command and registers it with the server.
     * 
     * @param plugin The main plugin instance
     */
    public Broadcast(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("broadcast")).setExecutor(this);
    }

    /**
     * Executes the broadcast command with the provided arguments.
     *
     * @param sender The command sender
     * @param command The command being executed
     * @param label The alias of the command used
     * @param args The command arguments (message parts)
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("duels.admin")){
            plugin.utils.message(sender, "<red>You dont have permission to execute this command");
            return true;
        }
        if (args.length < 1){
            plugin.utils.message(sender, "<red>Usage: /bc <text>");
            return true;
        } else {
            Component msg = Component.empty();
            for (String part : args) {
                if (msg.equals(Component.empty())) msg = Component.text(part);
                else msg = msg.append(Component.text(" ").append(Component.text(part)));
            }
            plugin.utils.broadcast(Sound.sound(Key.key("block.note_block.bit"), Sound.Source.MASTER, 10f, 1f), msg);
        }
        return false;
    }
}