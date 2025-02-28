package us.polarismc.polarisduels.arenas.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaSize;
import us.polarismc.polarisduels.arenas.session.ArenaSetupSession;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ArenaCommands implements Listener, CommandExecutor, TabCompleter {
    private final Main plugin;
    private final Map<UUID, ArenaSetupSession> setupSessions = new HashMap<>();

    public ArenaCommands(Main plugin){
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("arena")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("arena")).setTabCompleter(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
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

                Optional<ArenaEntity> deletedArena = plugin.getArenaManager().arenas.stream()
                        .filter(arena -> arena.getName().equalsIgnoreCase(arenaName))
                        .findFirst();

                if (deletedArena.isPresent()) {
                    plugin.getArenaManager().arenas.remove(deletedArena.get());
                    plugin.getArenaManager().arenaFile.deleteArena(deletedArena.get());
                    sender.sendMessage(plugin.utils.chat("&#10e8f3Arena " + arenaName + " deleted"));
                } else {
                    sender.sendMessage(plugin.utils.chat("&cArena with the name &4" + arenaName + " &cdoes not exists"));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("clone")) {
                if (args.length < 3) {
                    sender.sendMessage(plugin.utils.chat("&cUse: /arena clone <arena> <cantidad>"));
                    return true;
                }

                String arenaName = args[1];
                int copies;

                try {
                    copies = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.utils.chat("&cAmount must be a number."));
                    return true;
                }

                boolean success = cloneArena(arenaName, copies);
                if (success) {
                    sender.sendMessage(plugin.utils.chat("&#10e8f3Arena '" + arenaName + "' cloned " + copies + " times."));
                    sender.sendMessage(plugin.utils.chat("&#10e8f3Now, please restart the server to avoid performance issues or fatal errors."));
                } else {
                    sender.sendMessage(plugin.utils.chat("&cAn exception happened while cloning the arenas."));
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
    public void onPlayerChat(PlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!setupSessions.containsKey(p.getUniqueId())) return;

        ArenaSetupSession session = setupSessions.get(p.getUniqueId());
        e.setCancelled(true);

        String m = e.getMessage();
        switch (session.getStep()) {
            case 1 -> {
                session.getArena().setName(m.toLowerCase());
                session.setStep(2);
                plugin.utils.message(p, "&#10e8f3Step 2: Please enter the Display Name of the arena");
            }
            case 2 -> {
                session.getArena().setDisplayName(m);
                World world = plugin.utils.createVoidWorld(session.getArena().getName());
                p.teleport(new Location(world, 0.5, 65, 0.5));
                session.getArena().setWorld(world);
                session.setStep(3);
                plugin.utils.message(p, "&#10e8f3Step 3: Please stand on the spawn point 1 and say 'loc1' to save");
            }
            case 3 -> {
                if (m.equalsIgnoreCase("loc1")) {
                    session.getArena().setSpawnOne(p.getLocation());
                    session.setStep(4);
                    plugin.utils.message(p, "&#10e8f3Step 4: Please stand on the spawn point 2 and say 'loc2' to save");
                } else {
                    plugin.utils.message(p, "&cPlease say 'loc1' to save the spawn point 1");
                }
            }
            case 4 -> {
                if (m.equalsIgnoreCase("loc2")) {
                    session.getArena().setSpawnTwo(p.getLocation());
                    session.setStep(5);
                    plugin.utils.message(p, "&#10e8f3Step 5: Please stand on the first corner and say 'corner1' to save");
                } else {
                    plugin.utils.message(p, "&cPlease say 'loc2' to save the spawn point 2");
                }
            }
            case 5 -> {
                if (m.equalsIgnoreCase("corner1")) {
                    session.getArena().setCornerOne(p.getLocation());
                    session.setStep(6);
                    plugin.utils.message(p, "&#10e8f3Step 6: Please stand on the second corner and say 'corner2' to save");
                } else {
                    plugin.utils.message(p, "&cPlease say 'corner1' to save the first corner.");
                }
            }
            case 6 -> {
                if (m.equalsIgnoreCase("corner2")) {
                    session.getArena().setCornerTwo(p.getLocation());
                    session.setStep(7);
                    plugin.utils.message(p, "&#10e8f3Step 7: Please place an item in your hand for the Arena Logo and say 'go'");
                } else {
                    plugin.utils.message(p, "&cPlease say 'corner2' to save the second corner.");
                }
            }
            case 7 -> {
                if (m.equalsIgnoreCase("go") && !p.getInventory().getItemInMainHand().getType().isAir()) {
                    session.getArena().setBlockLogo(p.getInventory().getItemInMainHand());
                    session.setStep(8);
                    plugin.utils.message(p, "&#10e8f3Step 8: Please enter the arena size (small, medium, large)");
                } else {
                    plugin.utils.message(p, "&cThere's not any item in your hand");
                }
            }
            case 8 -> {
                switch (m.toLowerCase()) {
                    case "small" -> session.getArena().setArenaSize(ArenaSize.SMALL);
                    case "medium" -> session.getArena().setArenaSize(ArenaSize.MEDIUM);
                    case "large" -> session.getArena().setArenaSize(ArenaSize.LARGE);
                    default -> {
                        plugin.utils.message(p, "&cInvalid size! Please enter 'small', 'medium', or 'large'.");
                        return;
                    }
                }

                plugin.utils.message(p, "&#10e8f3Arena size set to: " + session.getArena().getArenaSize());
                session.setStep(9);
                plugin.utils.message(p, "&#10e8f3Step 9: Please select the center of the arena by right-clicking");
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
        if (session.getStep() == 8) {
            if (e.getClickedBlock() == null) return;
            session.getArena().setCenter(e.getClickedBlock().getLocation());
            session.setStep(9);
            session.getArena().setArenaState(new InactiveArenaState());
            plugin.getArenaManager().arenas.add(session.getArena());
            plugin.utils.message(p, "&#FFC300Arena setup completed!");
            Location loc = new Location(Bukkit.getWorld("lobby"), -0.5, 100, 0.5);
            p.teleport(loc);

            Bukkit.unloadWorld(session.getArena().getWorld(), true);

            plugin.getArenaManager().arenaFile.loadArenaWorlds();
            plugin.getArenaManager().arenaFile.saveArenas(plugin.getArenaManager().arenas);

            setupSessions.remove(p.getUniqueId());
        }
    }

    public boolean cloneArena(String originalName, int copies) {
        Optional<ArenaEntity> originalArenaOpt = plugin.getArenaManager().arenas.stream()
                .filter(arena -> arena.getName().equalsIgnoreCase(originalName))
                .findFirst();

        if (originalArenaOpt.isEmpty()) {
            return false;
        }

        ArenaEntity originalArena = originalArenaOpt.get();
        World source = originalArena.getWorld();
        if (source == null) return false;
        
        File oldFolder = new File(Bukkit.getWorldContainer(), originalArena.getName());
        if (!oldFolder.exists()) return false;

        int createdCount = 0;

        for (int i = 2; createdCount < copies; i++) {
            String newArenaName = originalArena.getName() + i;
            File newFolder = new File(Bukkit.getWorldContainer(), newArenaName);

            if (newFolder.exists()) continue;

            // Copia el mundo y lo carga
            copyWorld(oldFolder, newFolder);
            
            // Copia las propiedades de la arena y las guarda
            plugin.getArenaManager().arenaFile.loadArenaWorlds();
            ArenaEntity newArena = cloneArenaProperties(newArenaName, originalArena);
            plugin.getArenaManager().arenas.add(newArena);
            plugin.getArenaManager().arenaFile.saveArenas(plugin.getArenaManager().arenas);

            plugin.utils.broadcast("&#10e8f3Arena '" + newArena.getDisplayName() + "' ('" + newArenaName + "') created successfully! (" + (createdCount + 1) + "/" + copies + ")");

            createdCount++;
        }
        return true;
    }

    public void copyWorld(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
            if(!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists()) {
                        if (!target.mkdirs()) {
                            plugin.getLogger().severe("Error cloning folders. Source: " + source + " /-/ Target: " + target);
                        }
                    }
                    String[] files = source.list();
                    assert files != null;
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyWorld(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error cloning folders: " + e.getMessage());
        }
    }

    private static @NotNull ArenaEntity cloneArenaProperties(String newArenaName, ArenaEntity originalArena) {
        ArenaEntity newArena = new ArenaEntity();
        newArena.setName(newArenaName);
        newArena.setDisplayName(originalArena.getDisplayName());

        Location spawnOne = originalArena.getSpawnOne().clone();
        Location spawnTwo = originalArena.getSpawnTwo().clone();
        Location cornerOne = originalArena.getCornerOne().clone();
        Location cornerTwo = originalArena.getCornerTwo().clone();
        Location center = originalArena.getCenter().clone();
        
        World world = Bukkit.getWorld(newArenaName);

        spawnOne.setWorld(world); spawnTwo.setWorld(world); cornerOne.setWorld(world); cornerTwo.setWorld(world); center.setWorld(world);
        newArena.setSpawnOne(spawnOne); newArena.setSpawnTwo(spawnTwo); newArena.setCornerOne(cornerOne); newArena.setCornerTwo(cornerTwo); newArena.setCenter(center);

        newArena.setBlockLogo(originalArena.getBlockLogo());
        newArena.setArenaState(new InactiveArenaState());

        return newArena;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("arena")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("setup", "delete", "clone"));
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("clone")) {
                    completions.addAll(plugin.getArenaManager().getArenas().stream()
                            .map(ArenaEntity::getName)
                            .toList());
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("clone")) {
                completions.addAll(Arrays.asList("1", "3", "5", "10", "25", "50"));
            }
        }

        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).sorted().collect(Collectors.toList());
    }
}
