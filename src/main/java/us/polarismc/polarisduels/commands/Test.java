package us.polarismc.polarisduels.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.queue.KitType;

import java.util.Objects;

/**
 * A testing command used for development and debugging purposes.
 * This command cycles through all available kits and applies them to the player's inventory.
 * 
 * Permission: duels.admin
 * Usage: /test
 */
public class Test implements CommandExecutor {
    /** Reference to the main plugin instance */
    private final Main plugin;
    
    /**
     * Initializes the Test command and registers it with the server.
     * 
     * @param plugin The main plugin instance
     */
    public Test(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("test")).setExecutor(this);
    }

    /**
     * Executes the test command, cycling through all available kits.
     *
     * @param sender The command sender (must be a player)
     * @param command The command being executed
     * @param label The alias of the command used
     * @param args The command arguments (not used)
     * @return true if the command was handled successfully
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof Player p) {
            plugin.utils.message(p, "&cNow we wait...");

            KitType[] kits = KitType.values();
            int[] index = {0};

            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (index[0] < kits.length) {
                    KitType kit = kits[index[0]];
                    p.getInventory().clear();
                    p.sendMessage("Kit: " + kit.name());
                    p.getInventory().setContents(kit.getDefaultInv());

                    index[0]++;
                } else {
                    Bukkit.getScheduler().cancelTasks(plugin);
                }
            }, 0L, 100L);
        }

        return true;
    }
}
