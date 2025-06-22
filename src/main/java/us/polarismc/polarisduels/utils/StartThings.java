package us.polarismc.polarisduels.utils;

import fr.mrmicky.fastinv.FastInvManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.commands.ArenaCommands;
import us.polarismc.polarisduels.commands.Msg;
import us.polarismc.polarisduels.commands.Test;
import us.polarismc.polarisduels.commands.debug.Debug;
import us.polarismc.polarisduels.commands.staff.Broadcast;
import us.polarismc.polarisduels.commands.staff.CreateVoidWorld;
import us.polarismc.polarisduels.commands.staff.GameModeC;
import us.polarismc.polarisduels.commands.staff.WorldC;
import us.polarismc.polarisduels.duel.DuelCommand;
import us.polarismc.polarisduels.events.HubEvents;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Handles the initialization and setup of various plugin components.
 * This class is responsible for registering commands, listeners, and scheduling
 * recurring tasks when the plugin is enabled.
 */
public class StartThings {
    private final Main plugin;

    /**
     * Initializes a new StartThings instance and enables all components.
     *
     * @param plugin The main plugin instance
     */
    public StartThings(Main plugin) {
        this.plugin = plugin;
        onEnable();
    }

    /**
     * Enables all plugin components including commands, listeners, and scheduled tasks.
     * This method is called during plugin startup and performs the following actions:
     * - Creates the plugin data folder if it doesn't exist
     * - Registers all commands and event listeners
     * - Schedules scoreboard updates
     * - Sets up tab list headers and footers
     * - Registers the health display system
     */
    public void onEnable() {
        registerListeners();
        registerCommands();
        FastInvManager.register(plugin);
        if (!plugin.getDataFolder().exists()) {
            if (plugin.getDataFolder().mkdir()) {
                plugin.getLogger().info("Plugin directory created successfully.");
            } else {
                plugin.getLogger().warning("The plugin directory could not be created.");
            }
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

    /**
     * Registers all plugin commands with their respective executors.
     * This includes both user and admin commands, as well as debug and test commands.
     * Commands are registered with their appropriate permission nodes.
     */
    public void registerCommands() {
        // User commands (polarisduels.commands)
        new Msg(plugin);
        // Admin commands (polarisduels.admin)
        new Broadcast(plugin);
        new CreateVoidWorld(plugin);
        new GameModeC(plugin);
        new WorldC(plugin);
        // Specific commands
        new ArenaCommands(plugin);
        new DuelCommand(plugin);
        new Debug(plugin);
        new Test(plugin);
    }

    /**
     * Registers all event listeners used by the plugin.
     * Currently registers the HubEvents listener for handling player interactions.
     */
    public void registerListeners() {
        new HubEvents(plugin);
    }

    /**
     * Sets up and manages the health display system below player names.
     * Creates a scoreboard objective to show player health and schedules
     * regular updates to keep the display current.
     * 
     * TODO: Improve the health display system under the nickname
     */
    public void registerScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getObjective("HealthNamePL") == null) {
            scoreboard.registerNewObjective("HealthNamePL", Criteria.DUMMY, plugin.utils.chat("&c❤")).setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                Objective objective = scoreboard.getObjective("HealthNamePL");
                Score score = Objects.requireNonNull(objective).getScore(player.getName());
                double totalhealth = player.getHealth() + player.getAbsorptionAmount();
                score.setScore((int) Math.floor((totalhealth / 20) * 100));
            }
        },0,5);
    }
}
