package us.polarismc.polarisduels.events;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.queue.QueueGUI;
import us.polarismc.polarisduels.player.DuelsPlayer;
import us.polarismc.polarisduels.utils.ItemBuilder;

import java.util.Objects;
import java.util.Optional;

public class HubEvents implements Listener {
    private final Main plugin;
    private static final String JOIN_1V1_QUEUE = "&c1v1 Queue";
    private static final String JOIN_2v2_QUEUE = "&c2v2 Queue";
    private static final String JOIN_3v3_QUEUE = "&c3v3 Queue";
    private static final String CREATE_PARTY = "&aCreate Party";
    private static final String KIT_EDITOR = "&9Kit Editor";
    public static final String LEAVE_QUEUE = "&cLeave Queue";

    public HubEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        event.joinMessage(plugin.utils.chat("&8(&a+&8) " + p.getName()));

        plugin.getPlayerManager().playerJoin(p);

        FastBoard board = new FastBoard(p);
        board.updateTitle("Polaris Duels");
        plugin.boards.put(p.getUniqueId(), board);

        Location loc = new Location(Bukkit.getWorld("lobby"), 0, 100, 0);
        p.teleport(loc);
        p.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        p.setHealth(20);
        plugin.utils.setMaxHealth(p);
        p.setFoodLevel(20);
        p.setLevel(0);
        p.setExp(0.0f);
        giveJoinItems(p);
    }
    public static void giveJoinItems(Player p) {
        Inventory inv = p.getInventory();
        inv.clear();

        inv.addItem(new ItemBuilder(Material.NAME_TAG).name(JOIN_1V1_QUEUE).lore("&7Use this item to enter to the 1v1 Queue").build());
        inv.addItem(new ItemBuilder(Material.NAME_TAG).name(JOIN_2v2_QUEUE).lore("&7Use this item to enter to the 2v2 Queue").build());
        inv.addItem(new ItemBuilder(Material.NAME_TAG).name(JOIN_3v3_QUEUE).lore("&7Use this item to enter to the 3v3 Queue").build());

        inv.setItem(7, new ItemBuilder(Material.ENDER_PEARL).name(CREATE_PARTY).lore("&7Use this item to create a party!").build());
        inv.setItem(8, new ItemBuilder(Material.DIAMOND_AXE).name(KIT_EDITOR).lore("&7Use this item to edit your kit.").build());
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.quitMessage(plugin.utils.chat("&8(&c-&8) " + p.getName()));
        FastBoard board = plugin.boards.remove(p.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    @EventHandler
    public void BreakBlock(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
           return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
        if (!duelsPlayer.isDuel()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void PlaceBlock(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
        if (!duelsPlayer.isDuel()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            if (!duelsPlayer.isDuel()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
        if (!duelsPlayer.isDuel()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            if (!duelsPlayer.isDuel()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
        if (!duelsPlayer.isDuel()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            if (!duelsPlayer.isDuel()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBowOrCrossbowShot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
            if (!duelsPlayer.isDuel()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void EntityInteractEvent(PlayerInteractAtEntityEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            if (e.getRightClicked().getType() == EntityType.PLAYER) {
                Player p = e.getPlayer();
                String t = e.getRightClicked().getName();
                p.chat("/examine " + t);
            }
        }
    }

    @EventHandler
    public void teleportingNether(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
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

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getWorld().getName().equals("lobby") && e.getPlayer().getLocation().getBlockY() <= -64)
            e.getPlayer().teleport(new Location(Bukkit.getWorld("lobby"), 0, 100, 0));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player p = e.getPlayer();

            ItemStack item = p.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();

            if (p.getGameMode() == GameMode.SPECTATOR) return;

            switch (item.getType()) {
                case BARRIER -> {
                    if (Objects.equals(meta.displayName(), plugin.utils.chat(LEAVE_QUEUE))) {
                        e.setCancelled(true);
                        Optional<ArenaEntity> arena = plugin.getArenaManager().findPlayerArena(p);
                        if (arena.isPresent()){
                            arena.get().removePlayer(p, plugin);
                            plugin.utils.message(p, "&cYou left the queue");
                        } else plugin.utils.message(p, "&cYou don't seem to be in any arena, try re-joining.");
                    }
                }
                case NAME_TAG -> {
                    if (Objects.equals(meta.displayName(), plugin.utils.chat(JOIN_1V1_QUEUE))) {
                        new QueueGUI(p, 1, plugin);
                    }
                    if (Objects.equals(meta.displayName(), plugin.utils.chat(JOIN_2v2_QUEUE))) {
                        new QueueGUI(p, 2, plugin);
                    }
                    if (Objects.equals(meta.displayName(), plugin.utils.chat(JOIN_3v3_QUEUE))) {
                        new QueueGUI(p, 3, plugin);
                    }
                }
                default -> {
                    if (p.getGameMode().equals(GameMode.CREATIVE)) {
                        return;
                    }
                    DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);
                    if (!duelsPlayer.isDuel()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
