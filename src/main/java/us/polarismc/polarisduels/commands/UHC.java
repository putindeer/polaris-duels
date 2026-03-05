package us.polarismc.polarisduels.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

public class UHC implements CommandExecutor {
    private final Main plugin;

    public UHC(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("uhc")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>Only players can use this command.");
            return true;
        }
        plugin.utils.message(sender, "<green>Transferring to UHC... <gray>(This command will only transfer you if you are on 1.20.5+)");
        player.transfer("polarismc.us", 25565);
        return true;
    }
}
