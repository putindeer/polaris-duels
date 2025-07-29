package us.polarismc.polarisduels.game.states;

import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.util.Vector;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaState;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.game.GameType;
import us.polarismc.polarisduels.game.events.GameRemovePlayerEvent;
import us.polarismc.polarisduels.managers.duel.DuelTeam;
import us.polarismc.polarisduels.game.GameAttribute;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

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

public class PlayingArenaState implements ArenaState, Listener {
    private final Main plugin = Main.getInstance();
    private final ArenaEntity arena;
    private GameSession session;
    
    /**
     * Modern constructor: ActiveArenaState bound to a pre-configured GameSession.
     */
    public PlayingArenaState(GameSession session) {
        this.session = session;
        this.arena = session.getArena();
    }

    //region ArenaState Implementation
    /**
     * Activates the arena state and starts the duel.
     *
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (session == null) {
            this.session = arena.getGameSession();
        }
        startDuel();
        Main.pl.getLogger().info("ActiveArenaState enabled for arena " + arena.getName());
    }

    /**
     * Cleans up resources when the arena state is disabled.
     */
    @Override
    public void onDisable(ArenaState state) {
        HandlerList.unregisterAll(this);
        Main.pl.getLogger().info("ActiveArenaState disabled");
    }
    //endregion

    //region Match Flow
    private int healthTaskId = -1;
    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();

    /**
     * Initializes and starts a new duel match.
     * Sets up teams, teleports players, and sends the title of a starting match.
     */
    private void startDuel() {
        List<Player> playerList = arena.getOnlinePlayers();

        sendTitle();

        preparePlayers();

        setupTeams();

        teleportPlayers();

        if (session.getRounds() != 1) {
            plugin.utils.message(playerList, "&7The first to win &b" + session.getRounds() + " &7rounds get the victory!");
        }

        if (session.getKit().hasAttribute(GameAttribute.HEALTH_INDICATOR)) {
            addHealthIndicator();
        }
    }

    private void preparePlayers() {
        arena.getOnlinePlayers().forEach(player -> {
            player.setInvulnerable(false);
            player.setSaturation(5.0f);
            savedInventories.put(player.getUniqueId(), plugin.getKitManager().loadKit(player.getUniqueId(), session.getKit()));
            plugin.utils.message(player, Sound.sound(SoundEventKeys.BLOCK_ANCIENT_DEBRIS_BREAK, Sound.Source.MASTER, 10f, 1f), "&cThe Match has started!");
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
            duelsPlayer.setStartingDuel(false);
            duelsPlayer.setDuel(true);
        });
    }

    //region [Team Setup]

    private void setupTeams() {
        Set<DuelTeam> teams = session.getTeams();
        List<UUID> pool = new ArrayList<>(session.getPlayers());

        teams.forEach(team -> {
            if (team.getColor() == NamedTextColor.WHITE
                    && (session.getGameType() != GameType.PARTY_FFA && teams.size() > 2)) {
                team.setColor(session.findNextAvailableColor());
            }
            pool.removeAll(team.getMembers());
        });

        if (session.getQueueType() != null) {
            setupQueueTeams(pool);
        }
    }

    private void setupQueueTeams(List<UUID> pool) {
        Collections.shuffle(pool);
        Set<DuelTeam> teams = session.getTeams();
        Scoreboard board = session.getScoreboard();
        int teamSize = session.getQueueType().getTeamSize();

        for (DuelTeam team : teams) {
            while (team.getMembers().size() < teamSize && !pool.isEmpty()) {
                UUID next = pool.removeFirst();
                team.addPlayer(next);
            }
        }

        while (pool.size() >= teamSize) {
            Set<UUID> members = new HashSet<>(pool.subList(0, teamSize));
            pool.subList(0, teamSize).clear();

            NamedTextColor color = session.findNextAvailableColor();
            teams.add(new DuelTeam(board, members, color));
        }
    }

    private void sendTitle() {
        List<Player> list = arena.getOnlinePlayers();
        Set<DuelTeam> teams = session.getTeams();

        String subtitle;

        if (session.getGameType() == GameType.PARTY_FFA) {
            subtitle = "<yellow>Party FFA";
        } else if (session.getGameType() == GameType.DUEL_1V1 && list.size() >= 2) {
            String p1 = list.get(0).getName();
            String p2 = list.get(1).getName();
            subtitle = "<red>" + p1 + " <gray>vs</gray> <blue>" + p2;
        } else {
            subtitle = teams.stream().map(DuelTeam::getColoredName).collect(Collectors.joining(" <gray>vs</gray> "));
        }

        plugin.utils.title(list, "<aqua><bold>GO!", subtitle);
    }
    //endregion

    //region [Teleport Setup]
    public void teleportPlayers() {
        ArenaEntity arena = session.getArena();
        List<DuelTeam> teams = new ArrayList<>(session.getTeams());

        if (teams.size() == 2) {
            teleportTwoTeams(teams, arena);
        } else {
            teleportMultipleTeams(teams, arena);
        }
    }

    private void teleportTwoTeams(List<DuelTeam> teams, ArenaEntity arena) {
        DuelTeam teamOne = teams.get(0);
        DuelTeam teamTwo = teams.get(1);

        session.getPlayerList().forEach(player -> {
            if (teamOne.hasPlayer(player)) {
                player.teleport(arena.getSpawnOne());
            } else if (teamTwo.hasPlayer(player)) {
                player.teleport(arena.getSpawnTwo());
            }
        });
    }

    private void teleportMultipleTeams(List<DuelTeam> teams, ArenaEntity arena) {
        Location center = arena.getCenter();
        Location cornerOne = arena.getPlayableCornerOne();
        Location cornerTwo = arena.getPlayableCornerTwo();
        double safeRadius = plugin.utils.calculateSafeRadius(cornerOne, cornerTwo);

        List<Location> teamLocations = plugin.utils.getPositionsAroundCenter(teams.size(), center, safeRadius);

        for (int i = 0; i < teams.size(); i++) {
            DuelTeam team = teams.get(i);
            Location teamCenter = teamLocations.get(i);
            teamCenter.setDirection(center.toVector().subtract(teamCenter.toVector()));

            team.getOnlinePlayers().forEach(player -> player.teleport(teamCenter));
        }
    }
    //endregion

    /**
     * Adds a health indicator below player names.
     * Shows the player's current health as a percentage.
     */
    private void addHealthIndicator() {
        Scoreboard scoreboard = session.getScoreboard();
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

        checkMace(event);

        player.setGameMode(GameMode.SPECTATOR);

        checkForWinner(player);
    }

    private void checkMace(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent entityEvent) {
            if (entityEvent.getDamager() instanceof Player attacker) {
                if (attacker.getInventory().getItemInMainHand().getType() == Material.MACE) {
                    attacker.setFallDistance(0);
                }
            }
        }
    }

    private void checkForWinner(Player player) {
        handlePlayerDeath(player);

        if (shouldGameContinue()) return;

        DuelTeam winningTeam = determineRoundWinner();
        if (winningTeam == null) {
            handleNoWinners();
            return;
        }

        session.addWin(winningTeam);
        checkFinalWinConditions(winningTeam);
    }

    private void handlePlayerDeath(Player player) {
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        DuelTeam team = duelsPlayer.getTeam();

        plugin.utils.message(arena.getOnlinePlayers(), "&c" + player.getName() + " died.");
        if (team != null) {
            team.killPlayer(player);
            announceTeamElimination(team);
        }
        dropItems(player);
    }


    private void announceTeamElimination(DuelTeam team) {
        if (team.getAlivePlayers().isEmpty() && session.getTeams().size() > 2) {
            plugin.utils.message(arena.getOnlinePlayers(), "&cTeam " + team.getDisplayName() + " has been eliminated!");
        }
    }

    private boolean shouldGameContinue() {
        return session.getAliveTeams().size() > 1;
    }

    private DuelTeam determineRoundWinner() {
        Optional<DuelTeam> team = session.getAliveTeams().stream().findFirst();
        return team.orElse(null);
    }

    private void handleNoWinners() {
        plugin.utils.message(arena.getOnlinePlayers(), "&cNo alive players... Game over.");
        plugin.utils.delay(20 * 5, this::resetArena);
    }

    private void checkFinalWinConditions(DuelTeam team) {
        boolean hasEnoughWins = session.getWins(team) >= session.getRounds();
        boolean isLastTeamStanding = !multipleConnectedTeams();

        if (hasEnoughWins || isLastTeamStanding) {
            Win(team);
        } else {
            nextRound(team);
        }
    }

    private boolean multipleConnectedTeams() {
        Set<DuelTeam> onlineTeams = new HashSet<>();

        for (DuelTeam team : session.getTeams()) {
            if (!team.getOnlinePlayers().isEmpty()) {
                onlineTeams.add(team);
            }
        }

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
        String winnerName = (session.getGameType() == GameType.DUEL_1V1) ? getFirstPlayerName(team) : team.getColoredName();
        plugin.utils.message(arena.getOnlinePlayers(), "<green>" + winnerName + " has won this round! <gray>Next round starting in <red>5s");

        String scores = formatScores();
        plugin.utils.message(arena.getOnlinePlayers(), "<gray>Score: " + scores);

        arena.getOnlinePlayers().forEach(player -> {
            player.setInvulnerable(true);
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
            duelsPlayer.setOnHold(true);
            if (team.isAlive(player)) {
                plugin.utils.title(player, "<green>You won.", "<gray>Score: " + scores);
            } else {
                plugin.utils.title(player, "<red>You lost.", "<gray>Score: " + scores);
            }
        });

        resetRound();
    }

    /**
     * Resets the arena state for a new round.
     * Restores players, inventories and teleports them to their spawns after a short delay.
     * Also refreshes team alive lists and clears the on-hold status.
     */
    private void resetRound() {
        plugin.utils.delay(20 * 5, () -> {
            arena.getOnlinePlayers().forEach(this::preparePlayerForRound);
            teleportPlayers();
            plugin.utils.title(arena.getOnlinePlayers(), "<aqua><bold>GO!");
            plugin.utils.message(arena.getOnlinePlayers(), "&aNew Round has started.");
        });
    }

    private void preparePlayerForRound(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        plugin.utils.restorePlayer(player);
        ItemStack[] items = savedInventories.get(player.getUniqueId());
        player.getInventory().clear();
        player.getInventory().setContents(items);
        removeFluidBoost(player);
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        DuelTeam team = duelsPlayer.getTeam();
        team.getAliveMembers().add(player.getUniqueId());
        duelsPlayer.setOnHold(false);
    }

    private String getFirstPlayerName(DuelTeam team) {
        return team.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .findFirst()
                .map(Player::getName)
                .orElse("Unknown Player");
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
        String winnerName = (session.getGameType() == GameType.DUEL_1V1) ? getFirstPlayerName(team) : team.getColoredName();
        String scores = formatScores();
        
        plugin.utils.message(arena.getOnlinePlayers(), "<green>" + winnerName + " has won! <gray>(Final Score: " + scores + "<gray>)");

        arena.getOnlinePlayers().forEach(player -> {
            player.setInvulnerable(true);
            if (team.hasPlayer(player)) {
                plugin.utils.title(player, "<gradient:#EAFF23:#BBFF37>You won!</gradient>", "<gray>Score: " + scores);
            } else {
                plugin.utils.title(player, "<gradient:#B31D3C:#2E1BEE>You lost.</gradient>", "<gray>Score: " + scores);
            }
        });
        plugin.utils.delay(20 * 5, this::resetArena);
    }

    /**
     * Resets the arena to its initial state after a match.
     * Restores players, clears teams, and prepares the arena for the next match.
     */
    private void resetArena() {
        if (arena.getGameSession().getKit().hasAttribute(GameAttribute.HEALTH_INDICATOR)) {
            stopHealthIndicator();
        }

        for (Player p : arena.getOnlinePlayers()) {
            plugin.utils.setMaxHealth(p);
            plugin.getArenaManager().getRollBackManager().restore(p);
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
            if (duelsPlayer.getTeam() != null) {
                duelsPlayer.getTeam().deleteTeam();
            }
            duelsPlayer.setDuel(false);
            p.setInvulnerable(false);
            removeFluidBoost(p);
        }

        resetArenaBlocks();
        resetArenaEntities();
        plugin.getArenaManager().setInactiveState(arena);
    }
    //endregion

    //region Arena Management
    @EventHandler
    private void onRemovePlayer(GameRemovePlayerEvent event) {
        Player player = event.getPlayer();
        if (player.isInvulnerable()) player.setInvulnerable(false);
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);

        duelsPlayer.setDuel(false);
        duelsPlayer.setOnHold(false);

        bucketHoldingPlayers.remove(player);
        checkForWinner(player);
    }

    private String formatScores() {
        List<DuelTeam> sortedTeams = session.getTeams().stream()
                .sorted(Comparator.comparingInt((DuelTeam team) -> session.getWins(team)).reversed())
                .collect(Collectors.toList());

        if (sortedTeams.size() > 8) {
            sortedTeams = sortedTeams.subList(0, 8);
        }

        return sortedTeams.stream()
                .map(team -> team.getColorTag() + session.getWins(team))
                .collect(Collectors.joining(" <gray>-</gray> "));
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
        Location to = event.getTo();
        Location from = event.getFrom();

        if (session.getKit().hasAttribute(GameAttribute.NO_ARENA_LIMITS)) {
            if (plugin.utils.isInsideIgnoreY(to, arena.getCornerOne(), arena.getCornerTwo())) return;
        } else if (plugin.utils.isInside(to, arena.getPlayableCornerOne(), arena.getPlayableCornerTwo())) return;

        event.setCancelled(true);

        Vector back = from.toVector().subtract(to.toVector()).normalize().multiply(1.0);
        Location pushBack = from.clone().add(back);
        pushBack.setYaw(from.getYaw());
        pushBack.setPitch(from.getPitch());

        p.teleport(pushBack);
        p.sendActionBar(plugin.utils.chat("<red>This is the limit of the arena. You can't go further."));
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

        if (item.getType() == Material.AIR && isBucket(p.getInventory().getItemInMainHand().getType())) {
            applyFluidBoost(p);
        }

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
            double newValue = 5.0;//attribute.getBaseValue() + 0.5;
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
            double newValue = 4.5;//attribute.getBaseValue() - 0.5;
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
        if (session.getKit().hasAttribute(GameAttribute.NO_ARENA_DESTRUCTION)) {
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
        if (event.getNewState().getType() == Material.ICE) {
            event.setCancelled(true);
            return;
        }
        modifiedBlocks.putIfAbsent(loc, Material.AIR);
        if (session.getKit().hasAttribute(GameAttribute.NO_ARENA_DESTRUCTION)) {
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

        if (session.getKit().hasAttribute(GameAttribute.NO_BLOCK_BREAK)) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cBreaking blocks is disabled in this kit!"));
        }
        if (session.getKit().hasAttribute(GameAttribute.NO_ARENA_DESTRUCTION) && !placedBlocks.contains(event.getBlock().getLocation())) {
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

        if (session.getKit().hasAttribute(GameAttribute.NO_BLOCK_PLACE)) {
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

        if (session.getKit().hasAttribute(GameAttribute.NO_NATURAL_REGEN) && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
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

        if (session.getKit().hasAttribute(GameAttribute.ONE_THIRD_MORE_MELEE_DAMAGE)) {
            double newDamage = event.getDamage() * 4 / 3;
            event.setDamage(newDamage);
        }
    }
    //endregion
}