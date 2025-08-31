package us.polarismc.polarisduels.managers.hub;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.managers.party.gui.PartyGameGUI;
import us.polarismc.polarisduels.managers.party.gui.PartyInfoGUI;
import us.polarismc.polarisduels.managers.party.gui.PartyLeaveDisbandGUI;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;
import us.polarismc.polarisduels.managers.queue.QueueGUI;
import us.polarismc.polarisduels.managers.queue.QueueType;

import java.util.Optional;

/**
 * Handles all hub-related events and player interactions in the lobby.
 * Manages player join/quit, inventory interactions, and various game restrictions.
 */
public class HubEvents implements Listener {

    private final Main plugin;
    private final HubManager hubManager;

    /**
     * Initializes the HubEventHandler.
     *
     * @param plugin The main plugin instance
     */
    public HubEvents(Main plugin, HubManager hubManager) {
        this.plugin = plugin;
        this.hubManager = hubManager;

        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        event.joinMessage(plugin.utils.chat("<dark_gray>(<green>+</green>) " + player.getName()));

        hubManager.handlePlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void firstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().playerJoin(player);
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
        event.quitMessage(plugin.utils.chat("<dark_gray>(<red>-<dark_gray>) " + player.getName()));

        hubManager.handlePlayerQuit(player);
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
     * @param event The BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles food level change events.
     * Prevents hunger changes when not in a duel.
     *
     * @param event The FoodLevelChangeEvent
     */
    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
            if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles item dropping events.
     * Prevents items from being dropped when not in a duel.
     *
     * @param event The PlayerDropItemEvent
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(event.getPlayer());
        if (!duelsPlayer.isDuel() || duelsPlayer.isStartingDuel() || duelsPlayer.isOnHold()) {
            event.setCancelled(true);
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
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
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
     * @param event The PlayerItemConsumeEvent
     */
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
        if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles entity damage events.
     * Prevents damage when not in a duel.
     *
     * @param event The EntityDamageEvent
     */
    @EventHandler
    public void onDamageReceive(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
            if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
                event.setCancelled(true);
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
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();
            if (attacker.isInvulnerable()) attacker.setInvulnerable(false);
            if (attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                if (victim.isBlocking()) {
                    double initialHealth = victim.getHealth() + victim.getAbsorptionAmount();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            double finalHealth = victim.getHealth() + victim.getAbsorptionAmount();
                            if (initialHealth == finalHealth) {
                                attacker.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BREAK, 10, 0.5F);
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
     * @param event The PlayerMoveEvent
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld().getName().equals("lobby") && event.getPlayer().getLocation().getBlockY() <= -64) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(event.getPlayer());
            if (!duelsPlayer.isDuel() || duelsPlayer.isOnHold()) {
                event.getPlayer().teleport(new Location(Bukkit.getWorld("lobby"), 0, 100, 0));
            }
        }
    }

    /**
     * Marks a player as disconnecting when they quit.
     * This is used to handle cleanup operations.
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void setDisconnecting(PlayerQuitEvent event) {
        plugin.getPlayerManager().getPlayer(event.getPlayer()).setDisconnecting(true);
    }

    /**
     * Cleans up the disconnecting state when a player quits.
     * This ensures proper state management when players leave the server.
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeDisconnecting(PlayerQuitEvent event) {
        plugin.getPlayerManager().getPlayer(event.getPlayer()).setDisconnecting(false);
    }

    /**
     * Handles player interaction events.
     * Manages interactions with queue items and prevents other interactions when not in a duel.
     *
     * @param event The PlayerInteractEvent
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
            if (duelsPlayer.isDuel() && !duelsPlayer.isOnHold()) return;

            ItemStack item = player.getInventory().getItemInMainHand();
            if (player.getGameMode() == GameMode.SPECTATOR) return;

            // Get the item type from the hub manager
            HubItem itemType = hubManager.getItemType(item);
            if (itemType == null) {
                // Cancel interaction for unknown items if not in creative mode
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    event.setCancelled(true);
                }
                return;
            }

            event.setCancelled(true);
            handleHubItemInteraction(player, itemType);
        }
    }

    /**
     * Handles interaction with hub items based on their type.
     *
     * @param player   The player who interacted
     * @param itemType The type of hub item that was interacted with
     */
    private void handleHubItemInteraction(Player player, HubItem itemType) {
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);

        switch (itemType) {
            case LEAVE_QUEUE -> {
                Optional<ArenaEntity> arena = plugin.getArenaManager().getPlayerArena(player);
                if (arena.isPresent()) {
                    arena.get().removePlayer(player, plugin);
                    plugin.utils.message(player, "<red>You left the queue");
                    hubManager.giveLobbyItems(player);
                } else {
                    plugin.utils.message(player, "<red>You don't seem to be in any arena, try re-joining.");
                }
            }

            case JOIN_1V1_QUEUE -> {
                if (duelsPlayer.hasParty()) {
                    plugin.utils.message(player, "<red>You cannot join a queue while in a party!");
                    return;
                }
                new QueueGUI(player, QueueType.UNRANKED_1V1, plugin);
            }

            case JOIN_2V2_QUEUE -> {
                if (duelsPlayer.hasParty()) {
                    plugin.utils.message(player, "<red>You cannot join a queue while in a party!");
                    return;
                }
                new QueueGUI(player, QueueType.UNRANKED_2V2, plugin);
            }

            case JOIN_3V3_QUEUE -> {
                if (duelsPlayer.hasParty()) {
                    plugin.utils.message(player, "<red>You cannot join a queue while in a party!");
                    return;
                }
                new QueueGUI(player, QueueType.UNRANKED_3V3, plugin);
            }

            case CREATE_PARTY -> plugin.getPartyManager().createParty(player);

            case LEAVE_PARTY -> plugin.getPartyManager().leaveParty(player);

            case LEAVE_DISBAND_PARTY -> new PartyLeaveDisbandGUI(player, plugin);

            case PARTY_INFO -> new PartyInfoGUI(player, plugin);

            case PARTY_FFA -> new PartyGameGUI(player, plugin);
        }
    }
}