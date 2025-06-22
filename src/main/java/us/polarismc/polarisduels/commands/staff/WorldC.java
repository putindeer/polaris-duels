package us.polarismc.polarisduels.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A command that allows staff members to teleport between different worlds.
 * Provides tab completion for available worlds.
 * 
 * Permission: duels.admin
 * Usage: /world <worldname>
 */
public class WorldC implements TabExecutor {
    /** Reference to the main plugin instance */
    private final Main plugin;
    
    /**
     * Initializes the World command and registers it with the server.
     * 
     * @param plugin The main plugin instance
     */
    public WorldC(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("world")).setExecutor(this);
    }

    /**
     * Executes the world command with the provided arguments.
     *
     * @param sender The command sender (must be a player)
     * @param command The command being executed
     * @param label The alias of the command used
     * @param args The command arguments (world name)
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "&cOnly players can use this command.");
            return true;
        }
        if (args.length != 1) {
            plugin.utils.message(sender, "&cUsage: /world <name>");
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            plugin.utils.message(sender, "&cThat world does not exist.");
            return true;
        }

        player.teleport(world.getSpawnLocation());
        plugin.utils.message(sender, "&aTeleported to " + args[0] + ".");
        return true;
    }

    /**
     * Provides tab completion for world names.
     *
     * @param sender The command sender
     * @param command The command being executed
     * @param alias The alias of the command used
     * @param args The command arguments
     * @return A list of matching world names
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            Bukkit.getWorlds().stream().map(World::getName).forEach(completions::add);
        }
        completions.removeIf(s -> !s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }
}
