package us.polarismc.polarisduels.utils;

import fr.mrmicky.fastboard.adventure.FastBoard;
import fr.mrmicky.fastinv.FastInvManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.commands.ArenaCommands;
import us.polarismc.polarisduels.commands.Links;
import us.polarismc.polarisduels.commands.Msg;
import us.polarismc.polarisduels.commands.Test;
import us.polarismc.polarisduels.commands.debug.Debug;
import us.polarismc.polarisduels.commands.staff.Broadcast;
import us.polarismc.polarisduels.commands.staff.GameModeCMD;
import us.polarismc.polarisduels.duel.DuelCommand;
import us.polarismc.polarisduels.events.HubEvents;

import java.text.DecimalFormat;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class StartThings {
    private final Main plugin;
    public StartThings(Main plugin) {
        this.plugin = plugin;
        onEnable();
    }

    public void onEnable() {
        registerListeners();
        registerCommands();
        FastInvManager.register(plugin);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        // Scoreboard
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> plugin.boards.values().forEach(Scoreboards::updateBoard), 0, 20);
        plugin.getLogger().info("PolarisDuels is ON.");

        // TAB
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(p -> p.sendPlayerListHeaderAndFooter(
                plugin.utils.chat("&9&lPolaris Duels"),
                plugin.utils.chat("&7Ping: &9" + p.getPing() + " &8| &7Tps: &9" + new DecimalFormat("##").format(plugin.getServer().getTPS()[0]))
        )),0, 100);

        //TODO - Mejorar el sistema de vida bajo el nick
        registerScoreboard();
    }

    public void registerCommands() {
        // User commands (polarisduels.commands)
        new Msg(plugin);
        new Links(plugin);
        // Admin commands (polarisduels.admin)
        new Broadcast(plugin);
        new GameModeCMD(plugin);
        // Specific commands
        new ArenaCommands(plugin);
        new DuelCommand(plugin);
        new Debug(plugin);
        new Test(plugin);
    }

    public void registerListeners() {
        new HubEvents(plugin);
    }

    public void registerScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getObjective("HealthNamePL") == null) {
            scoreboard.registerNewObjective("HealthNamePL", "Dummy").setDisplayName(ChatColor.RED + "❤");
            scoreboard.getObjective("HealthNamePL").setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                Objective objective = scoreboard.getObjective("HealthNamePL");
                Score score = objective.getScore(player.getName());
                double totalhealth = player.getHealth() + player.getAbsorptionAmount();
                score.setScore((int) Math.floor((totalhealth / 20) * 100));
            }
        },0,5);
    }
}
