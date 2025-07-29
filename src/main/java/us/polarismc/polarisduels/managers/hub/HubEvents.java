package us.polarismc.polarisduels.managers.hub;

import fr.mrmicky.fastboard.adventure.FastBoard;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import java.util.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.managers.queue.QueueGUI;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;
import us.polarismc.polarisduels.managers.queue.QueueType;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Handles all hub-related events and player interactions in the lobby.
 * Manages player join/quit, inventory interactions, and various game restrictions.
 */
@SuppressWarnings("unstable")
public class HubEvents implements Listener {
    
    // Item display names
    private static ItemStack JOIN_1V1_QUEUE;
    private static ItemStack JOIN_2V2_QUEUE;
    private static ItemStack JOIN_3V3_QUEUE;
    private static ItemStack CREATE_PARTY;
    public static ItemStack LEAVE_QUEUE;
    
    // Constants
    private static final String LOBBY_WORLD = "lobby";
    private static final Location LOBBY_SPAWN = new Location(null, 0.5, 100, 0.5, 0, 0);
    
    private final Main plugin;
    
    /**
     * Initializes the HubEvents listener.
     *
     * @param plugin The main plugin instance
     */

    public HubEvents(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        createQueueItems();
    }

    private void createQueueItems() {
        JOIN_1V1_QUEUE = plugin.utils.ib(Material.NAME_TAG).name("<red>1v1 Queue").lore("Use this item to enter the 1v1 Queue").build();
        JOIN_2V2_QUEUE = plugin.utils.ib(Material.NAME_TAG).name("<red>2v2 Queue").lore("Use this item to enter the 2v2 Queue").build();
        JOIN_3V3_QUEUE = plugin.utils.ib(Material.NAME_TAG).name("<red>3v3 Queue").lore("Use this item to enter the 3v3 Queue").build();
        CREATE_PARTY = plugin.utils.ib(Material.ENDER_PEARL).name("<green>Create Party").lore("Use this item to create a party!").build();
        LEAVE_QUEUE = plugin.utils.ib(Material.BARRIER).name("<red>Leave Queue").build();
    }

    /**
     * Handles player join event.
     * Sets up the player's state, scoreboard, and inventory when they join the server.
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(plugin.utils.chat("&8(&a+&8) " + player.getName()));

        // Initialize player data
        plugin.getPlayerManager().playerJoin(player);
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        
        // Remove from any existing team
        if (duelsPlayer.getTeam() != null) {
            duelsPlayer.getTeam().removePlayer(duelsPlayer);
        }

        // Setup scoreboard
        setupScoreboard(player);
        
        // Teleport to lobby and reset player state
        teleportToLobby(player);
        resetPlayerState(player);
        
        // Give lobby items
        giveLobbyItems(player);
    }
    
    /**
     * Sets up the scoreboard for a player.
     *
     * @param player The player to set up the scoreboard for
     */
    private void setupScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(plugin.utils.chat("&9&lPolaris Duels"));
        plugin.boards.put(player.getUniqueId(), board);
        
        String tps = new DecimalFormat("##").format(plugin.getServer().getTPS()[0]);
        String footer = String.format("&7Ping: &9%d &8| &7Tps: &9%s", player.getPing(), tps);
        
        player.sendPlayerListHeaderAndFooter(
            plugin.utils.chat("&9&lPolaris Duels"),
            plugin.utils.chat(footer)
        );
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    /**
     * Teleports a player to the lobby spawn point.
     *
     * @param player The player to teleport
     */
    private void teleportToLobby(Player player) {
        World lobbyWorld = Bukkit.getWorld(LOBBY_WORLD);
        if (lobbyWorld == null) return;
        
        Location spawn = LOBBY_SPAWN.clone();
        spawn.setWorld(lobbyWorld);
        player.teleport(spawn);
    }
    
    /**
     * Resets a player's game state to default lobby state.
     *
     * @param player The player to reset
     */
    private void resetPlayerState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        
        // Clear all active potion effects
        player.getActivePotionEffects().stream()
            .map(PotionEffect::getType)
            .forEach(player::removePotionEffect);
        
        // Reset health and food
        player.setHealth(20);
        plugin.utils.setMaxHealth(player);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        
        // Reset XP
        player.setLevel(0);
        player.setExp(0.0f);
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).setBaseValue(4.5);
        
        // Reset player movement speed to default (0.2 is the default walking speed in Minecraft)
        // Using setWalkSpeed which is more reliable across different Bukkit/Spigot versions
        player.setWalkSpeed(0.2f);
        
        // Reset other player attributes
        player.setFlySpeed(0.1f); // Default fly speed
        player.setAllowFlight(false); // Disable flight by default
    }
    
    /**
     * Gives the default lobby items to a player.
     * 
     * @param player The player to give items to
     */
    public static void giveLobbyItems(Player player) {
        Inventory inventory = player.getInventory();
        inventory.clear();

        inventory.setItem(0, JOIN_1V1_QUEUE);
        inventory.setItem(1, JOIN_2V2_QUEUE);
        inventory.setItem(2, JOIN_3V3_QUEUE);
        inventory.setItem(8, CREATE_PARTY);
    }

    /**
     * Handles player quit event.
     * Cleans up player data and removes their scoreboard.
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(plugin.utils.chat("&8(&c-&8) " + player.getName()));
        
        // Clean up scoreboard
        FastBoard board = plugin.boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    /**
     * Handles block break events.
     * Prevents block breaking in non-creative mode when not in a duel.
     *
     * @param event The BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles block placement events.
     * Prevents block placement in non-creative mode when not in a duel.
     *
     * @param e The BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
        if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles food level change events.
     * Prevents hunger changes when not in a duel.
     *
     * @param e The FoodLevelChangeEvent
     */
    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
            if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Handles item dropping events.
     * Prevents items from being drop when not in a duel.
     *
     * @param e The PlayerDropItemEvent
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(e.getPlayer());
        if (!duelsPlayer.isDuel() || duelsPlayer.isStartingDuel() || duelsPlayer.isOnHold()) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles inventory click events.
     * Prevents interacting with non-player inventories when not in a duel.
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (p.getGameMode().equals(GameMode.CREATIVE)) return;

        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
        if (duelsPlayer.isDuel() && !duelsPlayer.isOnHold()) return;
        if (event.getClickedInventory() == null) return;

        if (!duelsPlayer.isStartingDuel()) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles item consumption events.
     * Prevents item consumption in non-creative mode when not in a duel.
     *
     * @param e The PlayerItemConsumeEvent
     */
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
        if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles entity damage events.
     * Prevents damage when not in a duel.
     *
     * @param e The EntityDamageEvent
     */
    @EventHandler
    public void onDamageReceive(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
            if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Handles shield disabling mechanics when hit by an axe.
     * Plays shield break sound if the player blocks with a shield and is hit by an axe.
     *
     * @param event The EntityDamageByEntityEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void shieldDisable(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
            Player p = (Player) event.getEntity();
            Player a = (Player) event.getDamager();
            if (a.isInvulnerable()) a.setInvulnerable(false);
            if (a.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                if (p.isBlocking()) {
                    double h1 = p.getHealth() + p.getAbsorptionAmount();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            double h2 = p.getHealth() + p.getAbsorptionAmount();
                            if (h1 == h2) {
                                a.playSound(p.getLocation(), Sound.ITEM_SHIELD_BREAK, 10, 0.5F);
                            }
                        }
                    }.runTaskLater(plugin, 1L);
                }
            }
        }
    }

    /**
     * Handles player movement events.
     * Prevents falling into the void in the lobby by teleporting players back to spawn.
     *
     * @param e The PlayerMoveEvent
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getWorld().getName().equals("lobby") && e.getPlayer().getLocation().getBlockY() <= -64) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(e.getPlayer());
            if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
                e.getPlayer().teleport(new Location(Bukkit.getWorld("lobby"), 0, 100, 0));
            }
        }
    }

    /**
     * Marks a player as disconnecting when they quit.
     * This is used to handle cleanup operations.
     *
     * @param e The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void setDisconnecting(PlayerQuitEvent e) {
        plugin.getPlayerManager().getPlayer(e.getPlayer()).setDisconnecting(true);
    }

    /**
     * Cleans up the disconnecting state when a player quits.
     * This ensures proper state management when players leave the server.
     *
     * @param e The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeDisconnecting(PlayerQuitEvent e) {
        plugin.getPlayerManager().getPlayer(e.getPlayer()).setDisconnecting(false);
    }

    /**
     * Handles player interaction events.
     * Manages interactions with queue items and prevents other interactions when not in a duel.
     *
     * @param e The PlayerInteractEvent
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player p = e.getPlayer();
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(p);
            if (duelsPlayer.isDuel() && !duelsPlayer.isOnHold()) return;

            ItemStack item = p.getInventory().getItemInMainHand();
            if (p.getGameMode() == GameMode.SPECTATOR) return;

            switch (item.getType()) {
                case BARRIER -> {
                    if (Objects.equals(item, LEAVE_QUEUE)) {
                        e.setCancelled(true);
                        Optional<ArenaEntity> arena = plugin.getArenaManager().getPlayerArena(p);
                        if (arena.isPresent()){
                            arena.get().removePlayer(p, plugin);
                            plugin.utils.message(p, "<red>You left the queue");
                        } else plugin.utils.message(p, "<red>You don't seem to be in any arena, try re-joining.");
                    }
                }
                case NAME_TAG -> {
                    e.setCancelled(true);
                    if (Objects.equals(item, JOIN_1V1_QUEUE)) {
                        if (duelsPlayer.hasParty()) {
                            plugin.utils.message(p, "<red>You cannot join a queue while in a party!");
                            return;
                        }
                        new QueueGUI(p, QueueType.UNRANKED_1V1, plugin);
                    }
                    if (Objects.equals(item, JOIN_2V2_QUEUE)) {
                        if (duelsPlayer.hasParty()) {
                            plugin.utils.message(p, "<red>You cannot join a queue while in a party!");
                            return;
                        }
                        new QueueGUI(p,  QueueType.UNRANKED_2V2, plugin);
                    }
                    if (Objects.equals(item, JOIN_3V3_QUEUE)) {
                        if (duelsPlayer.hasParty()) {
                            plugin.utils.message(p, "<red>You cannot join a queue while in a party!");
                            return;
                        }
                        new QueueGUI(p,  QueueType.UNRANKED_3V3, plugin);
                    }
                }
                case ENDER_PEARL -> {
                    e.setCancelled(true);
                    if (Objects.equals(item, CREATE_PARTY)) {
                        plugin.getPartyManager().createParty(p);
                    }
                }
                default -> {
                    if (p.getGameMode().equals(GameMode.CREATIVE)) {
                        return;
                    }
                    e.setCancelled(true);
                }
            }
        }
    }
}
