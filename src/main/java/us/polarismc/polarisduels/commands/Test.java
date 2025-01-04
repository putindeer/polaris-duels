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

public class Test implements CommandExecutor {
    private final Main plugin;
    public Test(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("test")).setExecutor(this);
    }

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
