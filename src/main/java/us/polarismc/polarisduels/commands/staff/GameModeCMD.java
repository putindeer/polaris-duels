package us.polarismc.polarisduels.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GameModeCMD implements TabExecutor {
    private final Main plugin;
    public GameModeCMD(Main plugin) {
        this.plugin = plugin;

        Objects.requireNonNull(plugin.getCommand("gmc")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("gms")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("gmsp")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("gma")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("gm")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player p = (Player) sender;
        if (!sender.hasPermission("uhc.gamemode")) {
            plugin.utils.message(sender, "&cYou don't have permission to execute this command");
            return true;
        }

        String commandName = command.getName().toLowerCase();

        if (args.length == 0 && commandName.equalsIgnoreCase("gamemode")) {
            plugin.utils.message(sender, "&cUsage: /gamemode <survival/creative/adventure/spectator> [player]");
            return true;
        }

        if (commandName.equals("gms") || commandName.equals("gmc") || commandName.equals("gma") || commandName.equals("gmsp")) {
            GameMode newGM;
            switch (commandName) {
                case "gms":
                    newGM = GameMode.SURVIVAL;
                    break;
                case "gmc":
                    newGM = GameMode.CREATIVE;
                    break;
                case "gma":
                    newGM = GameMode.ADVENTURE;
                    break;
                case "gmsp":
                    newGM = GameMode.SPECTATOR;
                    break;
                default:
                    return true;
            }
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    plugin.utils.message(p, "&cThe specified player is offline.");
                    return true;
                }
                changeGameMode(target, p, newGM);
            }
            changeGameMode(p, null, newGM);
            return true;
        }


        GameMode newGM;
        switch (args[0].toLowerCase()) {
            case "s", "survival", "0":
                newGM = GameMode.SURVIVAL;
                break;
            case "c", "creative", "1":
                newGM = GameMode.CREATIVE;
                break;
            case "a", "adv", "adventure", "2":
                newGM = GameMode.ADVENTURE;
                break;
            case "sp", "spec", "spectator", "3":
                newGM = GameMode.SPECTATOR;
                break;
            default:
                plugin.utils.message(p, "&cInvalid game mode specified.");
                return true;
        }

        if (args.length == 1) {
            changeGameMode(p, null, newGM);
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.utils.message(sender, "&cThe specified player is offline.");
                return true;
            }
            changeGameMode(target, p, newGM);
        }
        return true;
    }

    private void changeGameMode(Player p, Player p2, GameMode newGM) {
        GameMode oldGM = p.getGameMode();
        if (oldGM == newGM) return;
        if (p == p2) p2 = null;
        p.setGameMode(newGM);
        if (p2 == null) {
            plugin.utils.message(p, "&7You changed your gamemode from &2" + oldGM.name() + " &7to &2" + newGM.name() + "&7.");
            Bukkit.getOnlinePlayers().stream()
                    .filter(a -> a.hasPermission("duels.admin") && a != p)
                    .forEach(a -> plugin.utils.message(a, "&f" + p.getName() + " &7changed his gamemode from &2" + oldGM.name() + " &7to &2" + newGM.name() + "&7."));
        } else {
            Player sender = p2;
            plugin.utils.message(p, "&f" + sender.getName() + " &7changed your gamemode from &2" + oldGM.name() + " &7to &2" + newGM.name() + "&7.");
            plugin.utils.message(sender, "&7You changed &f" + p.getName() + "&7's gamemode from &2" + oldGM.name() + " &7to &2" + newGM.name() + "&7.");
            Bukkit.getOnlinePlayers().stream()
                    .filter(a -> a.hasPermission("duels.admin") && a != p && a != sender)
                    .forEach(a -> plugin.utils.message(a, "&f" + sender.getName() + " &7changed &f" + p.getName() + "&7's gamemode from &2" + oldGM.name() + " &7to &2" + newGM.name() + "&7."));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String commandName = command.getName().toLowerCase();
            if (commandName.equals("gm")) {
                completions.addAll(Arrays.asList("survival", "creative", "adventure", "spectator"));
            }
            if (commandName.equals("gms") || commandName.equals("gmc") || commandName.equals("gma") || commandName.equals("gmsp")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }
        } else if (args.length == 2 && command.getName().equalsIgnoreCase("gm")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }
        completions.removeIf(s -> !s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }
}
