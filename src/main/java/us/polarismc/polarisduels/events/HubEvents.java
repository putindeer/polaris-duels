package us.polarismc.polarisduels.events;

import fr.mrmicky.fastboard.adventure.FastBoard;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
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
import us.polarismc.polarisduels.queue.QueueGUI;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Handles all hub-related events and player interactions in the lobby.
 * Manages player join/quit, inventory interactions, and various game restrictions.
 */
@SuppressWarnings("unstable")
public class HubEvents implements Listener {
    
    // Item display names
    private static final String JOIN_1V1_QUEUE = "&c1v1 Queue";
    private static final String JOIN_2V2_QUEUE = "&c2v2 Queue";
    private static final String JOIN_3V3_QUEUE = "&c3v3 Queue";
    private static final String CREATE_PARTY = "&aCreate Party (NOT WORKING)";
    public static final String LEAVE_QUEUE = "&cLeave Queue";
    
    // Constants
    private static final String LOBBY_WORLD = "lobby";
    private static final Location LOBBY_SPAWN = new Location(null, -0.5, 100, 0.5, 0, 0);
    private static final int PARTY_ITEM_SLOT = 8;
    
    private final Main plugin;
    
    /**
     * Initializes the HubEvents listener.
     *
     * @param plugin The main plugin instance
     */

    public HubEvents(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
        
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

        // Add queue selection items
        addQueueItem(inventory, 0, JOIN_1V1_QUEUE, "1v1");
        addQueueItem(inventory, 1, JOIN_2V2_QUEUE, "2v2");
        addQueueItem(inventory, 2, JOIN_3V3_QUEUE, "3v3");

        // Add party creation item
        inventory.setItem(PARTY_ITEM_SLOT, Main.getInstance().utils.ib(Material.ENDER_PEARL)
            .name(CREATE_PARTY)
            .lore("&7Use this item to create a party!")
            .build());
    }
    
    /**
     * Helper method to add a queue selection item to a player's inventory.
     *
     * @param inventory The player's inventory
     * @param slot The slot to place the item in
     * @param displayName The display name of the item
     * @param queueType The type of queue (1v1, 2v2, 3v3)
     */
    private static void addQueueItem(Inventory inventory, int slot, String displayName, String queueType) {
        inventory.setItem(slot, Main.getInstance().utils.ib(Material.NAME_TAG)
            .name(displayName)
            .lore(String.format("&7Use this item to enter the %s Queue", queueType))
            .build());
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
        
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
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
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
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
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
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
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(e.getPlayer());
        if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
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
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
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
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
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
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
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
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(e.getPlayer());
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
        plugin.getPlayerManager().getDuelsPlayer(e.getPlayer()).setDisconnecting(true);
    }

    /**
     * Cleans up the disconnecting state when a player quits.
     * This ensures proper state management when players leave the server.
     *
     * @param e The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeDisconnecting(PlayerQuitEvent e) {
        plugin.getPlayerManager().getDuelsPlayer(e.getPlayer()).setDisconnecting(false);
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
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            if (duelsPlayer.isDuel() && !duelsPlayer.isOnHold()) return;

            ItemStack item = p.getInventory().getItemInMainHand();
            Component name = item.getData(DataComponentTypes.ITEM_NAME);
            if (p.getGameMode() == GameMode.SPECTATOR) return;

            switch (item.getType()) {
                case BARRIER -> {
                    if (Objects.equals(name, plugin.utils.chat(LEAVE_QUEUE))) {
                        e.setCancelled(true);
                        Optional<ArenaEntity> arena = plugin.getArenaManager().findPlayerArena(p);
                        if (arena.isPresent()){
                            arena.get().removePlayer(p, plugin);
                            plugin.utils.message(p, "&cYou left the queue");
                        } else plugin.utils.message(p, "&cYou don't seem to be in any arena, try re-joining.");
                    }
                }
                case NAME_TAG -> {
                    e.setCancelled(true);
                    if (Objects.equals(name, plugin.utils.chat(JOIN_1V1_QUEUE))) {
                        new QueueGUI(p, 1, plugin);
                    }
                    if (Objects.equals(name, plugin.utils.chat(JOIN_2V2_QUEUE))) {
                        new QueueGUI(p, 2, plugin);
                    }
                    if (Objects.equals(name, plugin.utils.chat(JOIN_3V3_QUEUE))) {
                        new QueueGUI(p, 3, plugin);
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
