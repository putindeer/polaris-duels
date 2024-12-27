package us.polarismc.polarisduels.events;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import us.polarismc.polarisduels.Main;

import java.util.Objects;

public class HubEvents implements Listener {
    private final Main plugin;

    public HubEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        event.joinMessage(plugin.utils.chat("&8(&a+&8) " + p.getName()));

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
        Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
        p.setFoodLevel(20);
        p.setLevel(0);
        p.setExp(0.0f);
        Inventory inv = p.getInventory();
        inv.clear();
    }
    @EventHandler
    public void onLeft (PlayerQuitEvent e) {
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
        if (!p.getGameMode().equals(GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void PlaceBlock(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void hunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }
            e.setCancelled(true);
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
}
