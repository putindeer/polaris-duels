package us.polarismc.polarisduels.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

public class Spec implements CommandExecutor {
    private final Main plugin;

    public Spec(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("spec")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>Only players can use this command.");
            return true;
        }
        player.setGameMode(GameMode.SPECTATOR);
        return true;
    }
}
