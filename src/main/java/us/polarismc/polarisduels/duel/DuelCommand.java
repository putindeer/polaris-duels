package us.polarismc.polarisduels.duel;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.queue.KitType;

import java.util.*;

public class DuelCommand implements CommandExecutor, TabExecutor {
    private final Main plugin;

    public DuelCommand(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("duel")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            plugin.utils.message(sender, "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            plugin.utils.message(p, "&cUsage: /duel <player> <kit> <rounds> | /duel accept <player> | /duel deny <player> | /duel list");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> {
                if (args.length < 2) {
                    plugin.utils.message(p, "&cUsage: /duel accept <player>");
                    return true;
                }
                Player requestor = Bukkit.getPlayer(args[1]);
                if (requestor == null || !requestor.isOnline()) {
                    plugin.utils.message(p, "&cThe player you tried to accept a duel from is either offline or does not exist.");
                    return true;
                }
                if (sender.equals(requestor)) {
                    plugin.utils.message(p, "&cYou cannot accept a duel to yourself.");
                    return true;
                }
                plugin.getDuelManager().acceptDuel(p, requestor);
            }
            case "deny" -> {
                if (args.length < 2) {
                    plugin.utils.message(p, "&cUsage: /duel deny <player>");
                    return true;
                }
                Player requestor = Bukkit.getPlayer(args[1]);
                if (requestor == null || !requestor.isOnline()) {
                    plugin.utils.message(p, "&cThe player you tried to deny a duel from is either offline or does not exist.");
                    return true;
                }
                if (sender.equals(requestor)) {
                    plugin.utils.message(p, "&cYou cannot deny a duel to yourself.");
                    return true;
                }
                plugin.getDuelManager().denyDuel(p, requestor);
            }
            case "list" -> plugin.getDuelManager().listRequests(p);
            default -> sendDuelRequest(p, args);
        }
        return true;
    }


    private void sendDuelRequest(Player sender, String[] args) {
        if (args.length < 3) {
            plugin.utils.message(sender, "&cUsage: /duel <player> <kit> <rounds>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            plugin.utils.message(sender, "&cThat player is not online.");
            return;
        }

        if (sender.equals(target)) {
            plugin.utils.message(sender, "&cYou cannot duel yourself.");
            return;
        }

        KitType kit;
        try {
            kit = KitType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.utils.message(sender, "&cInvalid kit type. Available kits: SMP, AXE, NETHPOT, UHC, DIAMONDPOT, SWORD.");
            return;
        }

        int rounds;
        try {
            rounds = Integer.parseInt(args[2]);
            if (rounds < 1 || rounds > 100) {
                plugin.utils.message(sender, "&cRounds must be between 1 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            plugin.utils.message(sender, "&cInvalid number of rounds.");
            return;
        }

        plugin.getDuelManager().sendRequest(target, sender, kit, rounds);
    }

    private final List<String> kits = List.of("SMP", "AXE", "NETHPOT", "UHC", "DIAMONDPOT", "SWORD");
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();

        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> {
                completions.addAll(Arrays.asList("accept", "deny", "list"));
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .map(Player::getName)
                        .forEach(completions::add);
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "accept", "deny" -> completions.addAll(plugin.getDuelManager().getPlayerRequests(player));
                    case "list" -> {}
                    default -> completions.addAll(kits);
                }
            }
            case 3 -> {
                if (!Arrays.asList("accept", "deny", "list").contains(args[0].toLowerCase()))
                    completions.addAll(Arrays.asList("1", "3", "5", "10"));
            }
        }

        completions.removeIf(s -> s == null || !s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
        Collections.sort(completions);
        return completions;
    }
}
