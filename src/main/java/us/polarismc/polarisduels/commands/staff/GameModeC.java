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
import java.util.List;
import java.util.Objects;

/**
 * Handles game mode related commands including /gm, /gms, /gmc, /gma, and /gmsp.
 * Allows staff members to change their own or other players' game modes.
 * <p>
 * Commands:
 * - /gm <mode> [player] - Changes game mode with full mode name or alias
 * - /gms [player] - Sets survival mode
 * - /gmc [player] - Sets creative mode
 * - /gma [player] - Sets adventure mode
 * - /gmsp [player] - Sets spectator mode
 * <p>
 * Permission: duels.admin
 */
public class GameModeC implements TabExecutor {
    /** Reference to the main plugin instance */
    private final Main plugin;
    
    /** List of all game mode command aliases */
    private static final List<String> gameModeCommands = List.of("gms", "gmc", "gma", "gmsp");
    
    /**
     * Initializes the game mode commands and registers them with the server.
     * 
     * @param plugin The main plugin instance
     */
    public GameModeC(Main plugin) {
        this.plugin = plugin;
        
        // Register all game mode commands
        gameModeCommands.forEach(cmd -> Objects.requireNonNull(plugin.getCommand(cmd)).setExecutor(this));
        Objects.requireNonNull(plugin.getCommand("gm")).setExecutor(this);
    }

    /**
     * Handles the execution of game mode commands.
     *
     * @param sender The command sender
     * @param command The command being executed
     * @param label The alias of the command used
     * @param args The command arguments
     * @return true if the command was handled successfully
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "&cOnly players can use this command.");
            return true;
        }
        
        if (!sender.hasPermission("duels.admin")) {
            plugin.utils.message(sender, "&cYou don't have permission to execute this command.");
            return true;
        }

        String commandName = command.getName().toLowerCase();
        
        // Handle /gamemode command without arguments
        if (commandName.equals("gm") && args.length == 0) {
            plugin.utils.message(sender, "&cUsage: /gamemode <survival/creative/adventure/spectator> [player]");
            return true;
        }

        try {
            // Handle shortcut commands (/gms, /gmc, etc.)
            if (gameModeCommands.contains(commandName)) {
                GameMode newMode = getGameModeFromCommand(commandName);
                if (args.length > 0) {
                    // Change another player's game mode
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        plugin.utils.message(sender, "&cThe specified player is not online.");
                        return true;
                    }
                    changeGameMode(target, player, newMode);
                } else {
                    // Change own game mode
                    changeGameMode(player, null, newMode);
                }
                return true;
            }

            // Handle /gamemode <mode> [player]
            if (commandName.equals("gm")) {
                GameMode newMode = parseGameMode(args[0]);
                if (newMode == null) {
                    plugin.utils.message(sender, "&cInvalid game mode. Use: survival, creative, adventure, or spectator");
                    return true;
                }

                if (args.length > 1) {
                    // Change another player's game mode
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        plugin.utils.message(sender, "&cThe specified player is not online.");
                        return true;
                    }
                    changeGameMode(target, player, newMode);
                } else {
                    // Change own game mode
                    changeGameMode(player, null, newMode);
                }
                return true;
            }
        } catch (Exception e) {
            plugin.utils.message(sender, "&cAn error occurred while processing the command.");
            plugin.getLogger().warning("Error in GameModeC command: " + e.getMessage());
        }
        
        return true;
    }

    /**
     * Changes a player's game mode and notifies relevant players.
     *
     * @param target The player whose game mode will be changed
     * @param sender The player who initiated the change (null if console or self)
     * @param newGameMode The new game mode to set
     */
    private void changeGameMode(Player target, Player sender, GameMode newGameMode) {
        GameMode oldGameMode = target.getGameMode();
        if (oldGameMode == newGameMode) {
            return;
        }
        
        // Prevent self-reference in notifications
        if (sender != null && sender.equals(target)) {
            sender = null;
        }
        
        target.setGameMode(newGameMode);
        
        // Send appropriate notifications
        if (sender == null) {
            // Console or self-change
            plugin.utils.message(target, String.format(
                "&7You changed your game mode from &2%s &7to &2%s&7.",
                formatGameModeName(oldGameMode), formatGameModeName(newGameMode)));
                
            // Notify staff
            notifyStaff(target, null, oldGameMode, newGameMode);
        } else {
            // Changed by another player
            plugin.utils.message(target, String.format(
                "&f%s &7changed your game mode from &2%s &7to &2%s&7.",
                sender.getName(), formatGameModeName(oldGameMode), formatGameModeName(newGameMode)));
                
            plugin.utils.message(sender, String.format(
                "&7You changed &f%s&7's game mode from &2%s &7to &2%s&7.",
                target.getName(), formatGameModeName(oldGameMode), formatGameModeName(newGameMode)));
                
            // Notify other staff
            notifyStaff(target, sender, oldGameMode, newGameMode);
        }
    }
    
    /**
     * Notifies staff members about game mode changes.
     *
     * @param target The player whose game mode was changed
     * @param sender The player who changed the game mode (null if console or self)
     * @param oldMode The previous game mode
     * @param newMode The new game mode
     */
    private void notifyStaff(Player target, Player sender, GameMode oldMode, GameMode newMode) {
        String message = sender == null 
            ? String.format("&f%s &7changed their game mode from &2%s &7to &2%s&7.",
                target.getName(), formatGameModeName(oldMode), formatGameModeName(newMode))
            : String.format("&f%s &7changed &f%s&7's game mode from &2%s &7to &2%s&7.",
                sender.getName(), target.getName(), formatGameModeName(oldMode), formatGameModeName(newMode));
        
        Bukkit.getOnlinePlayers().stream()
            .filter(staff -> staff.hasPermission("duels.admin"))
            .filter(staff -> !staff.equals(target) && (!staff.equals(sender)))
            .forEach(staff -> plugin.utils.message(staff, message));
    }
    
    /**
     * Parses a game mode from a string input.
     *
     * @param input The input string to parse
     * @return The corresponding GameMode, or null if invalid
     */
    private GameMode parseGameMode(String input) {
        if (input == null) return null;
        
        return switch (input.toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure", "adv" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator", "spec" -> GameMode.SPECTATOR;
            default -> null;
        };
    }
    
    /**
     * Gets the game mode corresponding to a shortcut command.
     *
     * @param command The command name (gms, gmc, gma, gmsp)
     * @return The corresponding GameMode
     */
    private GameMode getGameModeFromCommand(String command) {
        return switch (command.toLowerCase()) {
            case "gms" -> GameMode.SURVIVAL;
            case "gmc" -> GameMode.CREATIVE;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown game mode command: " + command);
        };
    }
    
    /**
     * Formats a game mode name for display.
     *
     * @param mode The game mode to format
     * @return A formatted string representation of the game mode
     */
    private String formatGameModeName(GameMode mode) {
        if (mode == null) return "Unknown";
        return mode.name().charAt(0) + mode.name().substring(1).toLowerCase();
    }

    /**
     * Provides tab completion for game mode commands.
     *
     * @param sender The command sender
     * @param command The command being executed
     * @param alias The alias of the command used
     * @param args The command arguments
     * @return A list of possible completions
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                     @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player) || !sender.hasPermission("duels.admin")) {
            return completions;
        }
        
        String commandName = command.getName().toLowerCase();
        
        // Tab complete game modes for /gm <mode>
        if (commandName.equals("gm") && args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> modes = List.of("survival", "creative", "adventure", "spectator");
            modes.stream()
                .filter(mode -> mode.startsWith(input))
                .forEach(completions::add);
        }
        // Tab complete player names for /gm <mode> <player> or /gmx <player>
        else if ((commandName.equals("gm") && args.length == 2) || 
                 (gameModeCommands.contains(commandName) && args.length == 1)) {
            String input = args[args.length - 1].toLowerCase();
            Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(completions::add);
        }
        
        return completions;
    }
}
