package us.polarismc.polarisduels.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.putindeer.api.util.builder.WorldBuilder;
import me.putindeer.api.util.generator.VoidGenerator;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

/**
 * A command that allows staff members to create void worlds with optimized game rules.
 * Void worlds are empty worlds that can be used for various purposes like minigames or events.
 * <p>
 * Permission: duels.admin
 * Usage: /createvoidworld <worldname>
 */
public class CreateVoidWorld implements CommandExecutor {
    /**
     * Reference to the main plugin instance
     */
    private final Main plugin;

    /**
     * Initializes the CreateVoidWorld command and registers it with the server.
     * 
     * @param plugin The main plugin instance
     */
    public CreateVoidWorld(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("createvoidworld"))
                .setExecutor(this);
    }

    /**
     * Executes the createvoidworld command with the provided arguments.
     *
     * @param sender The command sender (must be a player)
     * @param command The command being executed
     * @param label The alias of the command used
     * @param args The command arguments (world name)
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            plugin.utils.message(sender, "<red>Only players can use this command.");
            return true;
        }
        if (args.length != 1) {
            plugin.utils.message(sender, "<red>Usage: /createworld <name>");
            return true;
        }

        String name = args[0];
        if (Bukkit.getWorld(name) != null) {
            plugin.utils.message(sender, "<red>That world already exists.");
            return true;
        }

        World world = new WorldBuilder(name).generator(new VoidGenerator()).gamerule(GameRule.ANNOUNCE_ADVANCEMENTS, false).gamerule(GameRule.DO_DAYLIGHT_CYCLE, false).gamerule(GameRule.DO_MOB_SPAWNING, false).gamerule(GameRule.DO_PATROL_SPAWNING, false).gamerule(GameRule.DO_TRADER_SPAWNING, false).gamerule(GameRule.DO_WEATHER_CYCLE, false).gamerule(GameRule.SPAWN_CHUNK_RADIUS, 0).gamerule(GameRule.SPECTATORS_GENERATE_CHUNKS, false).autoSave(false).build();
        if (world != null) {
            plugin.utils.message(sender, "<green>World '" + name + "' created.");
        } else {
            plugin.utils.message(sender, "<red>Error creating world.");
        }
        return true;
    }
}