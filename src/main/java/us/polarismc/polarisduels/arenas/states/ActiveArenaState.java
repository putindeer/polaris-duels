package us.polarismc.polarisduels.arenas.states;

import io.papermc.paper.registry.keys.SoundEventKeys;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaAttribute;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.duel.DuelTeam;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the active state of an arena during a duel match.
 * Handles match flow, player interactions, and game mechanics.
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Match progression and round management</li>
 *   <li>Player combat and damage handling</li>
 *   <li>Arena state tracking and reset logic</li>
 *   <li>Team scoring and victory conditions</li>
 *   <li>Special kit attributes and game rules</li>
 * </ul>
 * 
 * <p>Implements {@link Listener} to handle Bukkit events during matches.</p>
 */

public class ActiveArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    private ArenaEntity arena;

    //region ArenaState Implementation
    /**
     * Activates the arena state and starts the duel.
     * 
     * @param arena the arena being activated
     * @throws IllegalArgumentException if arena is null
     */
    @Override
    public void onEnable(ArenaEntity arena) {
        if (arena == null) {
            throw new IllegalArgumentException("Arena cannot be null");
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.arena = arena;
        startDuel();
        Main.pl.getLogger().info("ActiveArenaState enabled for arena " + arena.getName());
    }

    /**
     * Cleans up resources when the arena state is disabled.
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Main.pl.getLogger().info("ActiveArenaState disabled");
    }
    //endregion

    //region Match Flow
    private int healthTaskId = -1;
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
    /**
     * Initializes and starts a new duel match.
     * Sets up teams, teleports players, and starts the match countdown.
     */
    private void startDuel() {
        List<Player> playerList = arena.getPlayerList();

        if (arena.getPlayersNeeded() == 2) {
            String name1 = Objects.requireNonNull(Bukkit.getPlayer(arena.getPlayers().get(0))).getName();
            String name2 = Objects.requireNonNull(Bukkit.getPlayer(arena.getPlayers().get(1))).getName();
            for (Player player : playerList) {
                player.showTitle(Title.title(
                        plugin.utils.chat("&b&lGO!"),
                        plugin.utils.chat("&c" + name1 + " &7vs &9" + name2)
                ));
            }
        } else {
            for (Player player : playerList) {
                player.showTitle(Title.title(
                        plugin.utils.chat("&b&lGO!"),
                        plugin.utils.chat("&cRED &7vs &9BLUE")
                ));
            }
        }

        List<DuelsPlayer> teamRedPlayers = new ArrayList<>();
        List<DuelsPlayer> teamBluePlayers = new ArrayList<>();

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            player.setInvulnerable(false);
            player.setSaturation(5.0f);
            savedInventories.put(player.getUniqueId(), plugin.getKitManager().loadKit(player.getUniqueId(), arena.getKit()));
            plugin.utils.message(player, Sound.sound(SoundEventKeys.BLOCK_ANCIENT_DEBRIS_BREAK, Sound.Source.MASTER, 10f, 1f), "&cThe Match has started!");
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
            duelsPlayer.setStartingDuel(false);
            duelsPlayer.setDuel(true);

            if (duelsPlayer.getTeam() != null) {
                if (duelsPlayer.getTeam().getColor() == NamedTextColor.RED) {
                    teamRedPlayers.add(duelsPlayer);
                } else {
                    teamBluePlayers.add(duelsPlayer);
                }
            } else {
                if (i < (playerList.size() / 2)) {
                    teamRedPlayers.add(duelsPlayer);
                } else {
                    teamBluePlayers.add(duelsPlayer);
                }
            }
        }

        DuelTeam redTeam = (teamRedPlayers.isEmpty())
                ? new DuelTeam( scoreboard, NamedTextColor.RED, "RED")
                : new DuelTeam( scoreboard, teamRedPlayers, NamedTextColor.RED, "RED");
        winsTeam.put(redTeam, 0);

        DuelTeam blueTeam = (teamBluePlayers.isEmpty())
                ? new DuelTeam( scoreboard, NamedTextColor.BLUE, "BLUE")
                : new DuelTeam( scoreboard, teamBluePlayers, NamedTextColor.BLUE, "BLUE");
        winsTeam.put(blueTeam, 0);

        for (Player player : playerList) {
            if (redTeam.hasPlayer(player)) {
                player.teleport(arena.getSpawnOne());
            } else {
                player.teleport(arena.getSpawnTwo());
            }
        }

        if (arena.getRounds() != 1) {
            plugin.utils.message(playerList, "&7The first to win &b" + arena.getRounds() + " &7rounds get the victory!");
        }

        if (arena.getKit().hasAttribute(ArenaAttribute.HEALTH_INDICATOR)) {
            addHealthIndicator();
        }
    }

    /**
     * Adds a health indicator below player names.
     * Shows the player's current health as a percentage.
     */
    private void addHealthIndicator() {
        if (scoreboard.getObjective("HealthNamePL") == null) {
            scoreboard.registerNewObjective("HealthNamePL", Criteria.DUMMY, plugin.utils.chat("&c❤")).setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        healthTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                Objective objective = scoreboard.getObjective("HealthNamePL");
                Score score = Objects.requireNonNull(objective).getScore(player.getName());
                double totalhealth = player.getHealth() + player.getAbsorptionAmount();
                score.setScore((int) Math.floor((totalhealth / 20) * 100));
            }
        },0,5);
    }

    /**
     * Stops and cleans up the health indicator task.
     */
    private void stopHealthIndicator() {
        if (healthTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(healthTaskId);
            healthTaskId = -1;
        }
    }
    //endregion

    //region [Player Death System]
    /**
     * Handles damage events to detect player deaths.
     * 
     * @param event The damage event
     */
    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!arena.hasPlayer(player)) return;
        if (event.getFinalDamage() < player.getHealth()) return;
        if (hasTotem(player)) return;

        event.setCancelled(true);

        player.setGameMode(GameMode.SPECTATOR);

        checkForWinner(player);
    }

    /**
     * Checks if a team has won the round after a player dies.
     * 
     * @param player The player who died
     */
    private void checkForWinner(Player player) {
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
        DuelTeam team = duelsPlayer.getTeam();
        if (team != null) {
            team.getAlivePlayers().remove(player.getUniqueId());
            if (!team.getAlivePlayers().isEmpty()) {
                plugin.utils.message(arena.getPlayerList(), "&c" + player.getName() + " died.");
                dropItems(player);
                return;
            }
        }

        DuelTeam winningTeam = arena.getPlayerList().stream().map(p -> plugin.getPlayerManager().getDuelsPlayer(p)).map(DuelsPlayer::getTeam)
                .filter(t -> !t.getAlivePlayers().isEmpty()).findFirst().orElse(null);

        if (winningTeam == null) {
            plugin.utils.message(arena.getPlayerList(), "&cNo alive players... Game over.");
            plugin.utils.delay(20 * 5, this::resetArena);
            return;
        }

        winsTeam.compute(winningTeam, (t, wins) -> wins == null ? 1 : wins + 1);

        if (arena.getRounds() == winsTeam.get(winningTeam) || !multipleConnectedTeams()) {
            Win(winningTeam);
        } else {
            nextRound(winningTeam);
        }
    }

    /**
     * Checks if there are multiple teams with online players remaining.
     * 
     * @return true if multiple teams have online players, false otherwise
     */
    private boolean multipleConnectedTeams() {
        Set<DuelTeam> onlineTeams = arena.getPlayerList().stream()
                .map(p -> plugin.getPlayerManager().getDuelsPlayer(p).getTeam())
                .filter(t -> t != null && t.getMembers().stream().anyMatch(p -> p != null && p.isOnline()))
                .collect(Collectors.toSet());
        plugin.getLogger().info("" + onlineTeams.size());
        return onlineTeams.size() > 1;
    }

    /**
     * Drops a player's inventory items at their location.
     * 
     * @param player The player whose items to drop
     */
    private void dropItems(Player player) {
        Location location = player.getLocation();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                player.getWorld().dropItemNaturally(location, item);
            }
        }

        player.getInventory().clear();
    }

    /**
     * Checks if the player is holding a totem of undying in either hand.
     * 
     * @param player The player to check
     * @return true if the player is holding a totem, false otherwise
     */
    private boolean hasTotem(Player player) {
        return player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING || player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING;
    }
    //endregion

    //region Round Management
    /**
     * Handles the transition to the next round after a team wins.
     * Announces the winner, updates scores, and prepares for the next round.
     * 
     * @param team The team that won the current round
     */
    private void nextRound(DuelTeam team) {
        if (arena.getPlayersNeeded() == 2) {
            Player winner = Bukkit.getPlayer(team.getMembers().getFirst().getUuid());
            if (winner != null) {
                plugin.utils.message(arena.getPlayerList(), "&a" + winner.getName() + " has won this round! &7Next Round starting in &c5s");
            }
        } else {
            plugin.utils.message(arena.getPlayerList(), "&a" + team.getTeamName() + " has won this round! &7Next Round starting in &c5s");
        }

        Pair<Integer, Integer> scores = getScores();

        plugin.utils.message(arena.getPlayerList(), "&7Score: &c" + scores.left() + " &7- &9" + scores.right());

        for (Player player : arena.getPlayerList()) {
            player.setInvulnerable(true);
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
            duelsPlayer.setOnHold(true);
            if (team.hasPlayer(player)) {
                player.showTitle(Title.title(plugin.utils.chat("&aYou won."), plugin.utils.chat("&7Score: &c" + scores.left() + " &7- &9" + scores.right())));
            } else {
                player.showTitle(Title.title(plugin.utils.chat("&cYou lost."), plugin.utils.chat("&7Score: &c" + scores.left() + " &7- &9" + scores.right())));
            }
        }

        resetRound();
    }

    /**
     * Resets the arena state for a new round.
     * Restores the arena blocks and entities, then respawns all players.
     * Players are teleported to their respective spawn points with full health and inventory.
     */
    private void resetRound() {
        plugin.utils.delay(20 * 5, () -> {
            if (!arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_REGENERATION)) {
                resetArenaBlocks();
                resetArenaEntities();
            }

            DuelTeam redTeam = null;
            DuelTeam blueTeam = null;

            for (Player player : arena.getPlayerList()) {
                DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
                if (duelsPlayer.getTeam() != null) {
                    if (redTeam == null) {
                        redTeam = duelsPlayer.getTeam();
                    } else if (blueTeam == null && duelsPlayer.getTeam() != redTeam) {
                        blueTeam = duelsPlayer.getTeam();
                    }
                }
            }

            for (Player player : arena.getPlayerList()) {
                player.setGameMode(GameMode.SURVIVAL);
                restorePlayer(player);
                DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
                DuelTeam team = duelsPlayer.getTeam();
                team.getAlivePlayers().add(player.getUniqueId());
                duelsPlayer.setOnHold(false);
                player.showTitle(Title.title(plugin.utils.chat("&b&lGO!"), Component.empty()));
                if (team == redTeam) {
                    player.teleport(arena.getSpawnOne());
                } else {
                    player.teleport(arena.getSpawnTwo());
                }
            }
            plugin.utils.message(arena.getPlayerList(), "&aNew Round has started.");
        });
    }

    /**
     * Fully restores a player's state between rounds.
     * Resets inventory, health, food level, potion effects, and other combat-related states.
     * 
     * @param p The player to restore
     */
    private void restorePlayer(Player p) {
        ItemStack[] items = savedInventories.get(p.getUniqueId());
        p.getInventory().clear();
        p.getInventory().setContents(items);
        plugin.utils.setMaxHealth(p);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        p.setLevel(0);
        p.setExp(0.0f);
        p.setFireTicks(0);
        p.setArrowsInBody(0);
        p.setItemOnCursor(new ItemStack(Material.AIR));
        p.setInvulnerable(false);
        removeFluidBoost(p);
    }
    //endregion

    //region Win System
    /**
     * Handles the win condition when a team wins the match.
     * Announces the winner, shows victory/defeat messages, and resets the arena.
     * 
     * @param team The team that won the match
     */
    private void Win(DuelTeam team) {
        Pair<Integer, Integer> scores = getScores();

        if (arena.getPlayersNeeded() == 2) {
            Player winner = Bukkit.getPlayer(team.getMembers().getFirst().getUuid());
            if (winner != null) {
                plugin.utils.message(arena.getPlayerList(), "<green>" + winner.getName() + " has won!");
            }
        } else {
            plugin.utils.message(arena.getPlayerList(), "<green>" + team.getTeamName() + " has won!");
        }

        for (Player player : arena.getPlayerList()) {
            player.setInvulnerable(true);
            if (team.hasPlayer(player)) {
                player.showTitle(Title.title(plugin.utils.chat("&cYou won."),
                        plugin.utils.chat("&7Score: &c" + scores.left() + " &7- &9" + scores.right())));
            } else {
                player.showTitle(Title.title(plugin.utils.chat("&cYou lost."),
                        plugin.utils.chat("&7Score: &c" + scores.left() + " &7- &9" + scores.right())));
            }
        }
        plugin.utils.delay(20 * 5, this::resetArena);
    }


    /**
     * Resets the arena to its initial state after a match.
     * Restores players, clears teams, and prepares the arena for the next match.
     */
    private void resetArena() {
        for (Player p : arena.getPlayerList()) {
            plugin.utils.setMaxHealth(p);
            plugin.getArenaManager().getRollBackManager().restore(p);
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            if (duelsPlayer.getTeam() != null) {
                duelsPlayer.getTeam().deleteTeam();
            }
            duelsPlayer.setDuel(false);
            p.setInvulnerable(false);
            removeFluidBoost(p);
            arena.removePlayer(p, plugin);
        }
        if (arena.getKit().hasAttribute(ArenaAttribute.HEALTH_INDICATOR)) {
            stopHealthIndicator();
        }

        winsTeam.clear();
        resetArenaBlocks();
        resetArenaEntities();
        plugin.getArenaManager().setInactiveState(arena);
    }
    //endregion

    //region Arena Management
    private final HashMap<DuelTeam, Integer> winsTeam = new HashMap<>();
    /**
     * Handles player disconnection during a match.
     * Removes the player from the arena and checks for a winner.
     * 
     * @param event The player quit event
     */
    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        if (p.isInvulnerable()) p.setInvulnerable(false);

        bucketHoldingPlayers.remove(p);

        checkForWinner(p);
        arena.removePlayer(p, plugin);
    }

    /**
     * Gets the current scores for both teams.
     * 
     * @return A Pair containing the red team's score (left) and blue team's score (right)
     */
    private Pair<Integer, Integer> getScores() {
        int redScore = winsTeam.entrySet().stream()
                .filter(e -> e.getKey().getColor() == NamedTextColor.RED)
                .mapToInt(Map.Entry::getValue)
                .findFirst().orElse(0);
        int blueScore = winsTeam.entrySet().stream()
                .filter(e -> e.getKey().getColor() == NamedTextColor.BLUE)
                .mapToInt(Map.Entry::getValue)
                .findFirst().orElse(0);
        return Pair.of(redScore, blueScore);
    }

    /**
     * Prevents players from leaving the arena boundaries during a match.
     * Teleports them back if they try to escape the arena.
     * 
     * @param event The player move event
     */
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        if (plugin.utils.isInside(event.getTo(), arena.getPlayableCornerOne(), arena.getPlayableCornerTwo())) return;

        event.setCancelled(true);
        p.teleport(event.getFrom());
        p.sendActionBar(plugin.utils.chat("&cThis is the limit of the arena. You can't go further."));
    }

    /**
     * Gets the number of wins for a team by their color.
     * 
     * @param color The team color to check
     * @return The number of wins for the specified team
     */
    public int getWinsByColor(NamedTextColor color) {
        for (Map.Entry<DuelTeam, Integer> entry : winsTeam.entrySet()) {
            if (entry.getKey().getColor() == color) {
                return entry.getValue();
            }
        }
        return 0;
    }
    //endregion

    //region Fluid Mechanics
    /**
     * Tracks players holding buckets for fluid interaction buffs.
     * Used to apply extended interaction range for fluid mechanics.
     */
    private final Set<Player> bucketHoldingPlayers = new HashSet<>();

    /**
     * Handles bucket holding state changes for players.
     * Adjusts interaction range when holding a bucket to prevent ghosting issues in versions before 1.20.5.
     * Restores normal range when not holding a bucket.
     * 
     * @param event The item held event
     */
    @EventHandler
    public void onBucketHeld(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = p.getInventory().getItem(event.getNewSlot());
        boolean wasBucket = oldItem != null && isBucket(oldItem.getType());
        boolean isNowBucket = newItem != null && isBucket(newItem.getType());

        if (wasBucket && isNowBucket) return;
        if (wasBucket) removeFluidBoost(p);
        if (isNowBucket) applyFluidBoost(p);
    }

    /**
     * Handles bucket movement in the inventory.
     * Adjusts interaction range when a bucket is moved to/from the hotbar to prevent ghosting issues.
     * 
     * @param event The inventory click event
     */
    @EventHandler
    public void onBucketClick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        if (!arena.hasPlayer(p)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        // revisar esto, no sé que tal funciona si le das click a un
        // cubo en el inventario mientras tienes uno en la mano
        // o si funciona siquiera por ese 'else return;'
        if (item.getType() == Material.AIR && isBucket(p.getInventory().getItemInMainHand().getType())) {
            applyFluidBoost(p);
        } else return;

        if (isBucket(item.getType())) {
            removeFluidBoost(p);
        }
    }

    /**
     * Checks if a material is a bucket type used for fluid mechanics.
     * 
     * @param material The material to check
     * @return true if the material is a fluid bucket, false otherwise
     */
    private boolean isBucket(Material material) {
        return switch (material) {
            case WATER_BUCKET, COD_BUCKET, SALMON_BUCKET, TROPICAL_FISH_BUCKET, PUFFERFISH_BUCKET, AXOLOTL_BUCKET,
                 TADPOLE_BUCKET, LAVA_BUCKET -> true;
            default -> false;
        };
    }

    /**
     * Applies an interaction range boost to a player holding a bucket.
     * This helps prevent ghosting issues with fluid mechanics.
     * 
     * @param player The player to apply the boost to
     */
    private void applyFluidBoost(Player player) {
        if (!bucketHoldingPlayers.contains(player)) {
            AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
            assert attribute != null;
            double newValue = attribute.getBaseValue() + 0.5;
            attribute.setBaseValue(newValue);
            bucketHoldingPlayers.add(player);
        }
    }

    /**
     * Removes the interaction range boost from a player no longer holding a bucket.
     * 
     * @param player The player to remove the boost from
     */
    private void removeFluidBoost(Player player) {
        if (bucketHoldingPlayers.contains(player)) {
            AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
            assert attribute != null;
            double newValue = attribute.getBaseValue() - 0.5;
            attribute.setBaseValue(newValue);
            bucketHoldingPlayers.remove(player);
        }
    }
    //endregion

    //region Arena Tracking
    /**
     * Tracks all blocks that have been modified during the match.
     * Maps block locations to their original material for restoration.
     */
    private final Map<Location, Material> modifiedBlocks = new HashMap<>();

    /**
     * Tracks all blocks that have been placed by players during the match.
     * Used to determine which blocks can be broken when NO_ARENA_DESTRUCTION is enabled.
     */
    private final Set<Location> placedBlocks = new HashSet<>();
    /**
     * Handles block break events in the arena.
     * Tracks broken blocks to restore them later.
     * 
     * @param event The block break event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
        modifiedBlocks.putIfAbsent(loc, block.getType());
    }

    /**
     * Handles block place events in the arena.
     * Tracks placed blocks for arena restoration and enforces arena destruction rules.
     * 
     * @param event The block place event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
        modifiedBlocks.putIfAbsent(loc, Material.AIR);
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_DESTRUCTION)) {
            placedBlocks.add(event.getBlock().getLocation());
        }
    }

    /**
     * Handles bucket empty events in the arena.
     * Tracks fluid placement for arena restoration.
     * 
     * @param event The bucket empty event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;

        if (block.getBlockData() instanceof Waterlogged waterlogged) {
            if (!waterlogged.isWaterlogged()) {
                modifiedBlocks.putIfAbsent(loc, block.getType());
            }
        } else modifiedBlocks.putIfAbsent(loc, Material.AIR);
    }

    /**
     * Handles fluid flow events in the arena.
     * Tracks fluid spread for arena restoration.
     * 
     * @param event The block from-to event
     */
    @EventHandler(ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        Block block = event.getBlock();
        if (block.isLiquid()) {
            Location loc = event.getToBlock().getLocation();
            if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
            modifiedBlocks.putIfAbsent(loc, Material.AIR);
        }
    }

    /**
     * Handles block formation events from lava-water interactions.
     * Tracks generated blocks like cobblestone for arena restoration.
     * 
     * @param event The block form event
     */
    @EventHandler(ignoreCancelled = true)
    public void onLavaCast(BlockFormEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;

        modifiedBlocks.putIfAbsent(loc, Material.AIR);
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_DESTRUCTION)) {
            placedBlocks.add(loc);
        }
    }

    /**
     * Handles block burn events in the arena.
     * Tracks blocks destroyed by fire for arena restoration.
     * 
     * @param event The block burn event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;

        modifiedBlocks.putIfAbsent(loc, block.getType());
    }

    /**
     * Tracks grass blocks turning into dirt in the arena.
     * @param event The block fade event
     */
    @EventHandler(ignoreCancelled = true)
    public void onGrassFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (block.getType() == Material.GRASS_BLOCK && event.getNewState().getType() == Material.DIRT) {
            if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
            modifiedBlocks.putIfAbsent(loc, block.getType());
        }
    }

    /**
     * Handles block transformation events from tool interactions.
     * Tracks changes like grass to path or farmland for arena restoration.
     * 
     * @param event The player interact event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockTransform(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
        Material blockType = block.getType();

        if ((item.getType().toString().endsWith("_HOE") || item.getType().toString().endsWith("_SHOVEL")) && (blockType == Material.GRASS_BLOCK || blockType == Material.DIRT)) {
            modifiedBlocks.putIfAbsent(loc, blockType);
        }
    }

    /**
     * Handles entity explosion events in the arena.
     * Tracks blocks destroyed by explosions for arena restoration.
     * 
     * @param event The entity explode event
     */
    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!plugin.utils.isInside(event.getLocation(), arena.getCornerOne(), arena.getCornerTwo())) return;
        event.blockList().forEach(b -> modifiedBlocks.putIfAbsent(b.getLocation(), b.getType()));
    }

    /**
     * Restores all block changes made during the round.
     * If arena regeneration is disabled, restores after the entire duel.
     */
    public void resetArenaBlocks() {
        for (Map.Entry<Location, Material> entry : modifiedBlocks.entrySet()) {
            Location loc = entry.getKey();
            Material material = entry.getValue();
            loc.getBlock().setType(material);
        }
        modifiedBlocks.clear();
    }

    /**
     * Removes all entities from the arena after the round.
     * If arena regeneration is disabled, removes them after the entire duel.
     */
    public void resetArenaEntities() {
        arena.getWorld().getEntities().stream()
                .filter(entity -> !(entity instanceof Player))
                .filter(entity -> plugin.utils.isInside(entity.getLocation(), arena.getCornerOne(), arena.getCornerTwo()))
                .forEach(Entity::remove);
    }
    //endregion

    //region Arena Attributes
    /**
     * Enforces block breaking restrictions based on the current kit's attributes.
     * Prevents breaking blocks if disabled in the kit or if the block is part of the arena
     * and arena destruction is disabled. Shows appropriate feedback to the player.
     * 
     * @param event The block break event to handle
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_BLOCK_BREAK)) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cBreaking blocks is disabled in this kit!"));
        }
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_DESTRUCTION) && !placedBlocks.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cBreaking the arena is disabled in this kit!"));
        }
    }

    /**
     * Enforces block placement restrictions based on the current kit's attributes.
     * Prevents placing blocks if disabled in the kit. Shows feedback to the player
     * when placement is denied.
     * 
     * @param event The block place event to handle
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_BLOCK_PLACE)) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cPlacing blocks is disabled in this kit!"));
        }
    }

    /**
     * Handles health regeneration events.
     * Disables natural health regeneration if disabled in the kit.
     * 
     * @param event The entity regain health event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_NATURAL_REGEN) && 
            event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Applies damage modifiers to melee attacks based on kit attributes.
     * Currently supports increasing melee damage by 33% when the
     * ONE_THIRD_MORE_MELEE_DAMAGE attribute is present in the kit.
     * 
     * @param event The entity damage by entity event to process
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!arena.hasPlayer(victim) || !arena.hasPlayer(attacker)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.ONE_THIRD_MORE_MELEE_DAMAGE)) {
            double newDamage = event.getDamage() * 4 / 3;
            event.setDamage(newDamage);
        }
    }
    //endregion
}