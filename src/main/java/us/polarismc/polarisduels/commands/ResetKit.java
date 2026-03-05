package us.polarismc.polarisduels.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.game.KitType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResetKit implements TabExecutor {
    private final Main plugin;

    public ResetKit(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("resetkit")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            plugin.utils.message(player, "<red>Usage: /resetkit <kit type>");
            return true;
        }

        KitType kitType;
        try {
            kitType = KitType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.utils.message(player, "<red>Invalid kit type.");
            return true;
        }

        plugin.kitManager.resetKit(player, kitType);
        plugin.utils.message(player, "<green>Your " + kitType.name() + " kit has been reset.");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Arrays.stream(KitType.values())
                    .map(kit -> kit.name().toLowerCase())
                    .filter(name -> name.startsWith(input))
                    .toList();
        }

        return Collections.emptyList();
    }
}
