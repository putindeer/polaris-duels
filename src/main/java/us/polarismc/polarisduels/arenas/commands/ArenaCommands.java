package us.polarismc.polarisduels.arenas.commands;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.ArenaManager;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.session.ArenaSetupSession;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaCommands implements Listener, CommandExecutor, TabCompleter {
    private final Main plugin;
    private final Map<UUID, ArenaSetupSession> setupSessions = new HashMap<>();
    private final ArenaManager arenaManager;

    public ArenaCommands(Main plugin){
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("arena")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("arena")).setTabCompleter(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.arenaManager = plugin.getArenaManager();
    }

    /**
     * On Command "/arena [setup|delete|list]"
     * if "setup" a few steps needs to be completed to create an arena
     * if "delete" deletes an arena
     * if "list" shows a GUI with all the arenas
     *
     * @param sender Source of the command
     * @param cmd    Command which was executed
     * @param s      Alias of the command which was used
     * @param args   Passed command arguments
     * @return ^
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (cmd.getName().equals("arena")){
            if (args.length < 1){
                sender.sendMessage(plugin.utils.chat("&cArena usage -> /arena [setup/delete]"));
                return true;
            }

            if (args[0].equalsIgnoreCase("setup")) {
                startArenaSetup((Player) sender);
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    sender.sendMessage(plugin.utils.chat("&cArena usage -> /arena delete <arena name>"));
                    return true;
                }
                String arenaName = args[1];

                Optional<ArenaEntity> deletedArena = arenaManager.arenas.stream()
                        .filter(arena -> arena.getName().equalsIgnoreCase(arenaName))
                        .findFirst();

                if (deletedArena.isPresent()) {
                    arenaManager.arenas.remove(deletedArena.get());
                    arenaManager.arenaFile.deleteArena(deletedArena.get());
                    sender.sendMessage(plugin.utils.chat("&#10e8f3Arena " + arenaName + "deleted"));
                } else {
                    sender.sendMessage(plugin.utils.chat("&cArena with the name &4" + arenaName + " &cdoes not exists"));
                }
                return true;
            } else {
                sender.sendMessage(plugin.utils.chat("&cArena usage -> /arena [setup/delete]"));
                return true;
            }
        }
        return true;
    }

    /**
     * Starts the Arena setup opening a new Arena Setup Session
     *
     * @param sender player
     */
    private void startArenaSetup(Player sender) {
        if (setupSessions.containsKey(sender.getUniqueId())) {
            sender.sendMessage(plugin.utils.chat("&cYou're already in setup mode!"));
            return;
        }

        ArenaEntity arena = new ArenaEntity();
        ArenaSetupSession session = new ArenaSetupSession(sender, arena);
        setupSessions.put(sender.getUniqueId(), session);

        plugin.utils.message(sender, "&#10e8f3Step 1: Please enter the name of the arena");
    }

    /**
     * Does almost all the chat related arena setup steps
     *
     * @param e event
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!setupSessions.containsKey(p.getUniqueId())) return;

        ArenaSetupSession session = setupSessions.get(p.getUniqueId());
        e.setCancelled(true);

        Component message = e.message();
        String m = ((TextComponent) message).content();
        switch (session.getStep()) {
            case 1:
                session.getArena().setName(m);
                session.setStep(2);
                plugin.utils.message(p, "&#10e8f3Step 2: Please enter the Display Name of the arena");
                break;
            case 2:
                session.getArena().setDisplayName(m);
                session.setStep(3);
                plugin.utils.message(p, "&#10e8f3Step 3: Please stand on the spawn point 1 and say 'loc1' to save");
                break;
            case 3:
                if (message.equals(Component.text("loc1"))) {
                    session.getArena().setSpawnOne(p.getLocation());
                    session.setStep(4);
                    plugin.utils.message(p, "&#10e8f3Step 4: Please stand on the spawn point 2 and say 'loc2' to save");
                } else {
                    plugin.utils.message(p, "&cPlease say 'loc1' to save the spawn point 1");
                }
                break;
            case 4:
                if (message.equals(Component.text("loc2"))) {
                    session.getArena().setSpawnTwo(p.getLocation());
                    session.setStep(5);
                    plugin.utils.message(p, "&#10e8f3Step 5: Please place an item in your hand for the Arena Logo and say 'go'");
                } else {
                    plugin.utils.message(p, "&cPlease say 'loc2' to save the spawn point 2");
                }
                break;
            case 5:
                if (message.equals(Component.text("go")) && !p.getInventory().getItemInMainHand().getType().isAir()) {
                    session.getArena().setBlockLogo(p.getInventory().getItemInMainHand());
                    session.setStep(6);
                    plugin.utils.message(p, "&#10e8f3Step 6: Please select the center of the arena by right-clicking");
                } else {
                    plugin.utils.message(p, "&cThere's not any item in your hand");
                }
        }
    }

    /**
     * Does the final step, center arena step
     *
     * @param e event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!setupSessions.containsKey(p.getUniqueId())) return;

        ArenaSetupSession session = setupSessions.get(p.getUniqueId());
        if (session.getStep() == 6) {
            if (e.getClickedBlock() == null) return;
            session.getArena().setCenter(e.getClickedBlock().getLocation());
            session.setStep(6);
            session.getArena().setArenaState(new InactiveArenaState());
            arenaManager.arenas.add(session.getArena());
            plugin.utils.message(p, "&#FFC300Arena setup completed!");

            arenaManager.arenaFile.saveArenas(arenaManager.arenas);

            setupSessions.remove(p.getUniqueId());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("arena")) {
            if (args.length == 1) {
                return Arrays.asList("setup", "delete");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("delete") && !arenaManager.arenas.isEmpty()) {
                return arenaManager.arenas.stream()
                        .map(ArenaEntity::getName)
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
}
