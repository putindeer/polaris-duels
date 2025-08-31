package us.polarismc.polarisduels.commands.debug;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.party.Party;

import java.util.Objects;

public class AddAllToParty implements CommandExecutor {

    private final Main plugin;

    public AddAllToParty(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("addalltoparty")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!plugin.getPartyManager().hasParty(player)) {
            plugin.utils.message(player, "<red>You need to be in a party to use this command!");
            return true;
        }

        Party party = plugin.getPartyManager().getParty(player);

        if (!party.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can use this command!");
            return true;
        }

        int addedCount = 0;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!party.isMember(onlinePlayer) && !onlinePlayer.equals(player)) {
                if (plugin.getPartyManager().hasParty(onlinePlayer)) {
                    plugin.getPartyManager().leaveParty(onlinePlayer);
                }

                party.addMember(onlinePlayer);
                addedCount++;
            }
        }

        plugin.utils.message(player, "<green>Debug: Added " + addedCount + " players to your party!");

        return true;
    }
}