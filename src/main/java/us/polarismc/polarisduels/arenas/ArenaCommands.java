package us.polarismc.polarisduels.arenas;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.setup.GridPos;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaSize;
import us.polarismc.polarisduels.arenas.setup.ArenaSetupSession;
import us.polarismc.polarisduels.game.states.InactiveArenaState;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all arena-related commands and setup process for the PolarisDuels plugin.
 * This class manages the creation, deletion, and cloning of arenas through in-game commands.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Interactive arena setup with visual feedback</li>
 *   <li>WorldEdit integration for arena schematic handling</li>
 *   <li>Tab completion for commands</li>
 *   <li>Quadrant-based arena placement system</li>
 *   <li>Interactive chat-based configuration</li>
 * </ul>
 * 
 * <p>This class implements {@link Listener}, {@link CommandExecutor}, and {@link TabCompleter}
 * to handle both commands and related events.</p>
 */
public class ArenaCommands implements Listener, CommandExecutor, TabCompleter {
    private final Main plugin;
    
    /** Active arena setup sessions mapped by player UUID */
    private final Map<UUID, ArenaSetupSession> setupSessions = new HashMap<>();

    /**
     * Constructs a new ArenaCommands instance and registers command handlers.
     * 
     * @param plugin The main plugin instance
     */
    public ArenaCommands(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("arena")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("arena")).setTabCompleter(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the /arena command execution.
     * 
     * @param sender The command sender (player or console)
     * @param cmd The command that was executed
     * @param s The command alias used
     * @param args Command arguments
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!cmd.getName().equals("arena")) return false;

        if (args.length < 1) {
            plugin.utils.message(sender, "<red>Arena usage -> /arena [setup/delete/clone]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup" -> handleSetupCommand(sender);
            case "delete" -> handleDeleteCommand(sender, args);
            case "clone" -> handleCloneCommand(sender, args);
            default -> plugin.utils.message(sender, "<red>Arena usage -> /arena [setup/delete/clone]");
        }
        return true;
    }

    /**
     * Handles the /arena setup command.
     * Initiates the arena setup process by finding an available quadrant and teleporting the player there.
     * 
     * @param sender The command sender (must be a player)
     */
    private void handleSetupCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>You must be a player to use this command.");
            return;
        }
        
        // Find next available quadrant
        Optional<Location> quadrantCenter = findNextAvailableQuadrantCenter(player.getWorld());
        if (quadrantCenter.isEmpty()) {
            plugin.utils.message(player, "<red>No available quadrants found in this world!");
            return;
        }
        
        // Teleport player to the center of the quadrant at y=100
        Location teleportLoc = quadrantCenter.get();
        player.teleport(teleportLoc);
        
        // Place glass block at player's feet
        Location blockLoc = teleportLoc.clone().subtract(0, 1, 0);
        blockLoc.getBlock().setType(org.bukkit.Material.GLASS);
        
        plugin.utils.message(player, "<green>You've been teleported to an available quadrant at " + 
            String.format("(%d, %d, %d)", 
                (int) teleportLoc.getX(),
                (int) teleportLoc.getY(),
                (int) teleportLoc.getZ()));
                
        startArenaSetup(player);
    }

    /**
     * Finds the center location of the next available quadrant in the specified world.
     * Quadrants are 1000x1000 block areas arranged in a grid pattern.
     * 
     * @param world The world to search for available quadrants
     * @return An Optional containing the center location (y=100) of the next available quadrant,
     *         or empty if no quadrants are available
     * @see GridPos
     */
    public Optional<Location> findNextAvailableQuadrantCenter(World world) {
        return plugin.getArenaManager().getNextAvailableQuadrant(world).map(quadrant -> {
            double centerX = quadrant.getCenterWorldX() + 0.5;
            double centerZ = quadrant.getCenterWorldZ() + 0.5;
            return new Location(world, centerX, 100.0, centerZ);
        });
    }

    /**
     * Handles the /arena delete command to remove an existing arena.
     * Validates the command usage and checks if the specified arena exists.
     * 
     * @param sender The command sender
     * @param args The command arguments, where args[1] should be the arena name
     */
    private void handleDeleteCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.utils.message(sender, "<red>Arena usage -> /arena delete <arena name>");
            return;
        }

        String arenaName = args[1];
        Optional<ArenaEntity> arenaToDelete = plugin.getArenaManager().arenas.stream()
                .filter(arena -> arena.getName().equalsIgnoreCase(arenaName))
                .findFirst();

        if (arenaToDelete.isPresent()) {
            deleteArena(arenaToDelete.get());
            plugin.utils.message(sender, "<#10e8f3>Arena " + arenaName + " deleted");
        } else {
            plugin.utils.message(sender, "<red>Arena with the name <dark_red>" + arenaName + " <red>does not exist");
        }
    }

    /**
     * Handles the /arena clone command to create multiple copies of an existing arena.
     * Validates the command usage, checks if the source arena exists, and initiates the cloning process.
     * 
     * @param sender The command sender
     * @param args The command arguments, where args[1] is the source arena name and args[2] is the number of copies
     */
    private void handleCloneCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.utils.message(sender, "<red>Use: /arena clone <arena> <amount>");
            return;
        }

        String arenaName = args[1];
        int copies;

        try {
            copies = Integer.parseInt(args[2]);
            if (copies <= 0) {
                plugin.utils.message(sender, "<red>Amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            plugin.utils.message(sender, "<red>Amount must be a number.");
            return;
        }

        boolean success = cloneArena(arenaName, copies);
        if (success) {
            plugin.utils.message(sender, "<#10e8f3>Cloned arena '" + arenaName + "' cloned " + copies + " times.");
        } else {
            plugin.utils.message(sender, "<red>Failed to start cloning arenas. Check if the arena exists.");
        }
    }

    /**
     * Deletes an arena from the system.
     * Removes the arena from the arena manager and performs any necessary cleanup.
     * 
     * @param arena The arena entity to delete
     */
    private void deleteArena(ArenaEntity arena) {
        plugin.getArenaManager().arenas.remove(arena);
        plugin.getArenaManager().arenaFile.deleteArena(arena);
    }

    /**
     * Initiates the interactive arena setup process for a player.
     * Creates a new setup session and guides the player through the configuration steps.
     * 
     * @param player The player starting the arena setup
     */
    private void startArenaSetup(Player player) {
        if (setupSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.utils.chat("<red>You're already in setup mode!"));
            return;
        }

        ArenaEntity arena = new ArenaEntity();
        ArenaSetupSession session = new ArenaSetupSession(player, arena);
        setupSessions.put(player.getUniqueId(), session);

        plugin.utils.message(player, "<#10e8f3>Step 1: Please enter the name of the arena");
    }

    /**
     * Handles player chat events during arena setup.
     * Processes messages as input for the current setup step.
     * 
     * @param e The async chat event
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        ArenaSetupSession session = setupSessions.get(player.getUniqueId());

        if (session == null) return;

        e.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());

        handleSetupStep(player, session, message);
    }

    /**
     * Processes a single step in the arena setup process based on the current step number.
     * Routes the message to the appropriate handler method for the current step.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message The player's input message for this step
     */
    private void handleSetupStep(Player player, ArenaSetupSession session, String message) {
        switch (session.getStep()) {
            case 1 -> handleStep1(player, session, message);
            case 2 -> handleStep2(player, session, message);
            case 3 -> handleStep3(player, session, message);
            case 4 -> handleStep4(player, session, message);
            case 5 -> handleStep5(player, session, message);
            case 6 -> handleStep6(player, session, message);
            case 7 -> handleStep7(player, session, message);
            case 8 -> handleStep8(player, session, message);
            case 9 -> handleStep9(player, session, message);
            case 10 -> handleStep10(player, session, message);
        }
    }

    /**
     * Handles step 1 of arena setup: Setting the internal arena name.
     * This name is used for internal reference and commands.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message The player's input for the arena name (lowercase, no spaces)
     */
    private void handleStep1(Player player, ArenaSetupSession session, String message) {
        session.getArena().setName(message.toLowerCase());
        session.setStep(2);
        plugin.utils.message(player, "<#10e8f3>Step 2: Please enter the Display Name of the arena");
    }

    /**
     * Handles step 2 of arena setup: Setting the arena display name.
     * This is the name shown to players in menus and messages.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message The display name for the arena (can include color codes)
     */
    private void handleStep2(Player player, ArenaSetupSession session, String message) {
        session.getArena().setDisplayName(message);
        session.getArena().setWorld(player.getWorld());
        session.setStep(3);
        plugin.utils.message(player, "<#10e8f3>Step 3: Please stand on spawn point 1 and say 'loc1' to save");
    }

    /**
     * Handles step 3 of arena setup: Setting the first player's spawn point.
     * Player should stand at the desired spawn location and type 'loc1'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'loc1' to confirm the current location
     */
    private void handleStep3(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("loc1")) {
            session.getArena().setSpawnOne(player.getLocation());
            session.setStep(4);
            plugin.utils.message(player, "<#10e8f3>Step 4: Please stand on spawn point 2 and say 'loc2' to save");
        } else {
            plugin.utils.message(player, "<red>Please say 'loc1' to save spawn point 1");
        }
    }

    /**
     * Handles step 4 of arena setup: Setting the second player's spawn point.
     * Player should stand at the desired spawn location and type 'loc2'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'loc2' to confirm the current location
     */
    private void handleStep4(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("loc2")) {
            session.getArena().setSpawnTwo(player.getLocation());
            session.setStep(5);
            plugin.utils.message(player, "<#10e8f3>Step 5: Please stand on the first corner and say 'corner1' to save");
        } else {
            plugin.utils.message(player, "<red>Please say 'loc2' to save spawn point 2");
        }
    }

    /**
     * Handles step 5 of arena setup: Setting the first corner of the arena boundary.
     * Player should stand at one corner of the arena and type 'corner1'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'corner1' to confirm the current location
     */
    private void handleStep5(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("corner1")) {
            session.getArena().setCornerOne(player.getLocation());
            session.setStep(6);
            plugin.utils.message(player, "<#10e8f3>Step 6: Please stand on the second corner and say 'corner2' to save");
        } else {
            plugin.utils.message(player, "<red>Please say 'corner1' to save the first corner");
        }
    }

    /**
     * Handles step 6 of arena setup: Setting the second corner of the arena boundary.
     * Player should stand at the opposite corner of the arena and type 'corner2'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'corner2' to confirm the current location
     */
    private void handleStep6(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("corner2")) {
            session.getArena().setCornerTwo(player.getLocation());
            session.setStep(7);
            plugin.utils.message(player, "<#10e8f3>Step 7: Please stand on the first playable corner and say 'playablecorner1' to save");
        } else {
            plugin.utils.message(player, "<red>Please say 'corner2' to save the second corner");
        }
    }

    /**
     * Handles step 7 of arena setup: Setting the first corner of the playable area.
     * Player should stand at one corner of the playable area and type 'playablecorner1'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'playablecorner1' to confirm the current location
     */
    private void handleStep7(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("playablecorner1")) {
            session.getArena().setPlayableCornerOne(player.getLocation());
            session.setStep(8);
            plugin.utils.message(player, "<#10e8f3>Step 8: Please stand on the second playable corner and say 'playablecorner2' to save");
        } else {
            plugin.utils.message(player, "<red>Please say 'playablecorner1' to save the first playable corner");
        }
    }

    /**
     * Handles step 8 of arena setup: Setting the second corner of the playable area.
     * Player should stand at the opposite corner of the playable area and type 'playablecorner2'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'playablecorner2' to confirm the current location
     */
    private void handleStep8(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("playablecorner2")) {
            session.getArena().setPlayableCornerTwo(player.getLocation());
            session.setStep(9);
            plugin.utils.message(player, "<#10e8f3>Step 9: Please place an item in your hand for the Arena Logo and say 'go'");
        } else {
            plugin.utils.message(player, "<red>Please say 'playablecorner2' to save the second playable corner");
        }
    }

    /**
     * Handles step 9 of arena setup: Setting the arena logo.
     * Player should hold the item they want to use as the arena's logo in their main hand and type 'go'.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'go' with an item in hand to set as the logo
     */
    private void handleStep9(Player player, ArenaSetupSession session, String message) {
        if (message.equalsIgnoreCase("go") && !player.getInventory().getItemInMainHand().getType().isAir()) {
            session.getArena().setBlockLogo(player.getInventory().getItemInMainHand());
            session.setStep(10);
            plugin.utils.message(player, "<#10e8f3>Step 10: Please enter the arena size (small, medium, large)");
        } else {
            plugin.utils.message(player, "<red>Please hold an item in your hand and say 'go'");
        }
    }

    /**
     * Handles step 10 of arena setup: Setting the arena size.
     * Player should enter 'small', 'medium', or 'large' to set the arena size.
     * 
     * @param player The player in setup mode
     * @param session The active setup session
     * @param message Should be 'small', 'medium', or 'large' to set the size
     */
    private void handleStep10(Player player, ArenaSetupSession session, String message) {
        ArenaSize size = switch (message.toLowerCase()) {
            case "small" -> ArenaSize.SMALL;
            case "medium" -> ArenaSize.MEDIUM;
            case "large" -> ArenaSize.LARGE;
            default -> null;
        };

        if (size == null) {
            plugin.utils.message(player, "<red>Invalid size! Please enter 'small', 'medium', or 'large'");
            return;
        }

        session.getArena().setArenaSize(size);
        session.setStep(11);
        plugin.utils.message(player, "<#10e8f3>Arena size set to: " + size);
        plugin.utils.message(player, "<#10e8f3>Step 11: Please select the center of the arena by right-clicking");
    }

    /**
     * Handles the final step of arena setup: Setting the arena center.
     * Player should right-click a block to set the center of the arena.
     * 
     * @param e The player interact event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ArenaSetupSession session = setupSessions.get(player.getUniqueId());

        if (session == null || session.getStep() != 11 || e.getClickedBlock() == null) return;

        finishArenaSetup(player, session, e.getClickedBlock().getLocation());
    }

    /**
     * Completes the arena setup process and saves the new arena.
     * 
     * @param player The player who completed the setup
     * @param session The setup session containing the new arena
     * @param center The calculated center location of the arena
     */
    private void finishArenaSetup(Player player, ArenaSetupSession session, Location center) {
        session.getArena().setCenter(center);
        session.getArena().setQuadrant(plugin.getArenaManager().createGridPos(center));
        session.getArena().setArenaState(new InactiveArenaState());

        plugin.getArenaManager().arenas.add(session.getArena());
        plugin.utils.message(player, "<#FFC300>Arena setup completed!");

        plugin.getArenaManager().arenaFile.saveArenas(plugin.getArenaManager().arenas);

        setupSessions.remove(player.getUniqueId());
    }

    /**
     * Creates multiple clones of an existing arena.
     * 
     * @param name The name of the source arena to clone
     * @param times The number of copies to create
     * @return true if cloning was successful, false otherwise
     */
    private boolean cloneArena(String name, int times) {
        Optional<ArenaEntity> originalArenaOpt = plugin.getArenaManager().getArenas().stream()
                .filter(arena -> arena.getName().equalsIgnoreCase(name))
                .findFirst();

        if (originalArenaOpt.isEmpty()) {
            plugin.utils.warning("Failed to find arena with name: " + name);
            return false;
        }

        ArenaEntity originalArena = originalArenaOpt.get();

        // Create the cloned arena entities first to reserve quadrants
        List<ArenaEntity> clonedArenas = createClonedArenaEntities(originalArena, times);
        if (clonedArenas == null || clonedArenas.size() != times) {
            plugin.utils.warning("Failed to create " + times + " cloned arena entities. Created: " +
                    (clonedArenas != null ? clonedArenas.size() : 0));
            // Clean up any partially created arenas
            if (clonedArenas != null) {
                plugin.getArenaManager().getArenas().removeAll(clonedArenas);
            }
            return false;
        }

        try {
            // Copy the structure for each new arena
            if (!copyArenaStructure(originalArena, clonedArenas)) {
                plugin.utils.warning("Failed to copy arena structure");
                // Clean up the created arenas
                plugin.getArenaManager().getArenas().removeAll(clonedArenas);
                return false;
            }

            // Save all arenas
            plugin.getArenaManager().arenaFile.saveArenas(plugin.getArenaManager().getArenas());
            plugin.utils.log("Successfully cloned " + times + " arenas from " + name);
            return true;
        } catch (Exception e) {
            plugin.utils.warning("Error during arena cloning: " + e.getMessage());
            plugin.utils.warning(e.getStackTrace());
            plugin.getArenaManager().getArenas().removeAll(clonedArenas);
            return false;
        }
    }

    /**
     * Copies the physical structure of an arena to cloned arenas using FastAsyncWorldEdit.
     * 
     * @param originalArena The source arena to copy from
     * @param clonedArenas List of arenas to copy the structure to
     */
    private boolean copyArenaStructure(ArenaEntity originalArena, List<ArenaEntity> clonedArenas) {
        try {
            // Get the original corners
            Location cornerOne = originalArena.getCornerOne();
            Location cornerTwo = originalArena.getCornerTwo();

            if (cornerOne == null || cornerTwo == null) {
                plugin.utils.severe("Cannot copy arena structure: corners are not set");
                return false;
            }

            // Create the region to copy (using the original corners)
            BlockVector3 pos1 = BlockVector3.at(
                    Math.min(cornerOne.getBlockX(), cornerTwo.getBlockX()),
                    Math.min(cornerOne.getBlockY(), cornerTwo.getBlockY()),
                    Math.min(cornerOne.getBlockZ(), cornerTwo.getBlockZ())
            );

            BlockVector3 pos2 = BlockVector3.at(
                    Math.max(cornerOne.getBlockX(), cornerTwo.getBlockX()),
                    Math.max(cornerOne.getBlockY(), cornerTwo.getBlockY()),
                    Math.max(cornerOne.getBlockZ(), cornerTwo.getBlockZ())
            );

            // Create a region that includes both corners
            CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(originalArena.getWorld()), pos1, pos2);

            // Calculate the offset from the original center to the minimum corner
            Location originalCenter = originalArena.getCenter();
            double centerToMinX = pos1.x() - originalCenter.getX();
            double centerToMinZ = pos1.z() - originalCenter.getZ();

            // Create an async clipboard to store the copied region
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(originalArena.getWorld()))) {
                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

                    // Copy the region to the clipboard
                    ForwardExtentCopy copy = new ForwardExtentCopy(
                            editSession, region, clipboard, region.getMinimumPoint()
                    );
                    copy.setCopyingBiomes(true);
                    Operations.complete(copy);

                    for (ArenaEntity clonedArena : clonedArenas) {
                        if (clonedArena.getQuadrant() == null) {
                            plugin.utils.warning("Skipping arena " + clonedArena.getName() + " - no quadrant assigned");
                            continue;
                        }

                        // Calculate the target position based on the quadrant center
                        int targetX = (int) (clonedArena.getQuadrant().getCenterWorldX() + centerToMinX);
                        int targetZ = (int) (clonedArena.getQuadrant().getCenterWorldZ() + centerToMinZ);
                        BlockVector3 target = BlockVector3.at(targetX, pos1.y(), targetZ);

                        plugin.utils.log(String.format("Pasting arena %s at %d, %d, %d (quadrant %s)",
                                clonedArena.getName(), targetX, pos1.y(), targetZ, clonedArena.getQuadrantString()));

                        // Perform the paste operation
                        Operation paste = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(target)
                                .build();
                        Operations.complete(paste);
                    }
                    plugin.utils.log("<#FFC300>Successfully pasted all the arenas to " + clonedArenas.size() + " locations");
                }
            });
            return !clonedArenas.isEmpty();
        } catch (Exception e) {
            plugin.utils.warning("Error cloning arena structure: " + e.getMessage());
            plugin.utils.warning(e.getStackTrace());
            return false;
        }
    }

    /**
     * Creates the specified number of cloned arena entities based on an original arena.
     * 
     * @param original The source arena to clone
     * @param times The number of copies to create
     * @return List of newly created arena entities
     */
    private List<ArenaEntity> createClonedArenaEntities(ArenaEntity original, int times) {
        List<ArenaEntity> newArenas = new ArrayList<>();

        try {
            for (int i = 0; i < times; i++) {
                Optional<GridPos> quadrantOpt = plugin.getArenaManager().getNextAvailableQuadrant(original.getWorld());
                if (quadrantOpt.isEmpty()) {
                    plugin.utils.warning("No available quadrants for arena cloning");
                    return newArenas;
                }
                GridPos quadrant = quadrantOpt.get();
                String newName = generateUniqueName(original.getName());
                ArenaEntity cloned = cloneArenaProperties(newName, original, quadrant);
                plugin.getArenaManager().getArenas().add(cloned);
                newArenas.add(cloned);
            }
            return newArenas;

        } catch (Exception e) {
            plugin.utils.warning("Error creating cloned arena entities: " + e.getMessage());
            plugin.utils.warning(e.getStackTrace());
            plugin.getArenaManager().getArenas().removeAll(newArenas);
            return null;
        }
    }

    /**
     * Generates a unique name for a cloned arena by appending a number if needed.
     * 
     * @param baseName The base name to make unique
     * @return A unique name that doesn't conflict with existing arenas
     */
    private String generateUniqueName(String baseName) {
        Set<String> existingNames = plugin.getArenaManager().arenas.stream()
                .map(ArenaEntity::getName)
                .collect(Collectors.toSet());

        int counter = 2;
        String newName;
        do {
            newName = baseName + counter;
            counter++;
        } while (existingNames.contains(newName));

        return newName;
    }

    /**
     * Creates a new arena with properties cloned from an existing arena.
     * 
     * @param newName The name for the new arena
     * @param original The source arena to copy properties from
     * @param targetQuadrant The grid position for the new arena
     * @return The newly created arena entity
     */
    private ArenaEntity cloneArenaProperties(String newName, ArenaEntity original, GridPos targetQuadrant) {
        ArenaEntity cloned = new ArenaEntity();
        cloned.setName(newName);
        cloned.setDisplayName(original.getDisplayName());
        cloned.setWorld(original.getWorld());
        cloned.setBlockLogo(original.getBlockLogo());
        cloned.setArenaSize(original.getArenaSize());
        cloned.setArenaState(new InactiveArenaState());

        // Get the center of the original arena
        Location originalCenter = original.getCenter();

        // Calculate the center of the target quadrant (x500, z500 in the new quadrant)
        double targetCenterX = targetQuadrant.getCenterWorldX();
        double targetCenterZ = targetQuadrant.getCenterWorldZ();

        // Calculate the offset from original center to each point
        double centerX = originalCenter.getX();
        double centerZ = originalCenter.getZ();

        // Set all positions using the new relative calculations
        cloned.setCenter(calculateNewPosition(originalCenter, centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setSpawnOne(calculateNewPosition(original.getSpawnOne(), centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setSpawnTwo(calculateNewPosition(original.getSpawnTwo(), centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setCornerOne(calculateNewPosition(original.getCornerOne(), centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setCornerTwo(calculateNewPosition(original.getCornerTwo(), centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setPlayableCornerOne(calculateNewPosition(original.getPlayableCornerOne(), centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setPlayableCornerTwo(calculateNewPosition(original.getPlayableCornerTwo(), centerX, centerZ, targetCenterX, targetCenterZ));
        cloned.setQuadrant(targetQuadrant);

        return cloned;
    }

    /**
     * Calculates the new position for a location when cloning an arena.
     * 
     * @param originalLoc The original location in the source arena
     * @param centerX The X coordinate of the source arena's center
     * @param centerZ The Z coordinate of the source arena's center
     * @param targetCenterX The X coordinate of the target arena's center
     * @param targetCenterZ The Z coordinate of the target arena's center
     * @return The new location adjusted for the target arena
     */
    private Location calculateNewPosition(Location originalLoc, double centerX, double centerZ, double targetCenterX, double targetCenterZ) {
        // Calculate offset from original center
        double offsetX = originalLoc.getX() - centerX;
        double offsetZ = originalLoc.getZ() - centerZ;

        // Apply offset to target center
        double newX = targetCenterX + offsetX;
        double newZ = targetCenterZ + offsetZ;

        // Create new location with same yaw/pitch
        return new Location(
                originalLoc.getWorld(),
                newX,
                originalLoc.getY(),
                newZ,
                originalLoc.getYaw(),
                originalLoc.getPitch()
        );
    }

    /**
     * Provides tab completion for the /arena command.
     * 
     * @param sender The command sender
     * @param command The command being executed
     * @param alias The alias used
     * @param args The command arguments
     * @return List of possible completions for the current argument
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String @NotNull [] args) {
        if (!command.getName().equalsIgnoreCase("arena")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("setup", "delete", "clone"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("clone"))) {
            completions.addAll(plugin.getArenaManager().getArenas().stream()
                    .map(ArenaEntity::getName)
                    .toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("clone")) {
            completions.addAll(Arrays.asList("1", "3", "5", "10", "25", "50"));
        }

        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).sorted().collect(Collectors.toList());
    }
}