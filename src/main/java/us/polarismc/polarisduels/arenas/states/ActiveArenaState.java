package us.polarismc.polarisduels.arenas.states;

import net.kyori.adventure.text.Component;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaAttribute;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.duel.DuelTeam;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.*;

public class ActiveArenaState implements ArenaState, Listener {
    private List<UUID> alivePlayers;
    private final Main plugin = Main.getInstance();
    private ArenaEntity arena;
    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
    public static HashMap<UUID, Integer> winsPlayer = new HashMap<>();

    @Override
    public void onEnable(ArenaEntity arena) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Main.pl.getLogger().info("ActiveArenaState enabled");
        this.arena = arena;
        alivePlayers = new ArrayList<>(arena.getPlayers());
        int i = 0;
        for (Player player : arena.getPlayerList()) {
            savedInventories.put(player.getUniqueId(), plugin.getKitManager().loadKit(player.getUniqueId(), arena.getKit()));
            player.setSaturation(5.0f);
            DuelTeam TeamBlue;
            DuelTeam TeamRed;
            plugin.utils.message(player, Sound.BLOCK_ANCIENT_DEBRIS_BREAK, "&cThe Match has started!");

            winsPlayer.put(player.getUniqueId(), 0);

            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
            duelsPlayer.setStartingDuel(false);
            duelsPlayer.setDuel(true);

            if (duelsPlayer.getTeam() != null) {
                duelsPlayer.deleteTeam(duelsPlayer.getTeam());
            }
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(player.getName());
            if (team != null) {
                team.unregister();
            }

            if (arena.getPlayers().size() == 2){
                if (i == 0){
                    TeamBlue = new DuelTeam(duelsPlayer, ChatColor.RED, "RED");
                    i = 1;
                } else  {
                    TeamRed = new DuelTeam(duelsPlayer, ChatColor.BLUE, "BLUE");
                    i = 0;
                }

            }
            if (arena.getPlayersNeeded() == 2) {
                String playerOneName = Objects.requireNonNull(Bukkit.getPlayer(arena.getPlayers().get(0))).getName();
                String playerTwoName = Objects.requireNonNull(Bukkit.getPlayer(arena.getPlayers().get(1))).getName();
                player.showTitle(Title.title(plugin.utils.chat("&b&lGO!"), plugin.utils.chat("&c" + playerOneName + " &7vs &9" + playerTwoName)));
            } else {
                player.showTitle(Title.title(plugin.utils.chat("&b&lGO!"), plugin.utils.chat("&cRED &7vs &9Blue")));
            }


        }



        int lastSpawnId = 0;

        for (Player player : arena.getPlayerList()) {


            // teams

            if (lastSpawnId == 0){
                player.teleport(arena.getSpawnOne());
                lastSpawnId = 1;
            } else {
                player.teleport(arena.getSpawnTwo());
                lastSpawnId = 0;
            }




        }

        if (arena.getRounds() != 1) {
            plugin.utils.message(arena.getPlayerList(), "&7The first to win &b" + arena.getRounds() + " &7rounds get the victory!");
        }
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        HandlerList.unregisterAll(this);
        Main.pl.getLogger().info("ActiveArenaState disabled");
    }

    /**
     * Restaura al jugador completamente, su inventario, su vida, le quita los efectos, etc.
     * @param p El jugador al que se le restaurará
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
        p.setItemOnCursor(new ItemStack(Material.AIR));
        p.setInvulnerable(false);
        removeFluidBoost(p);
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!arena.hasPlayer(player)) return;

        event.setCancelled(false);

        if (event.getFinalDamage() >= player.getHealth()) {
            if (hasTotem(player)) return;

            event.setCancelled(true);

            // Cambia el estado del jugador
            alivePlayers.remove(player.getUniqueId());
            player.setGameMode(GameMode.SPECTATOR);

            // Comprueba si queda un solo jugador con vida
            if (alivePlayers.size() <= 1) {

                if (alivePlayers.size() == 1) {
                    UUID winnerUUID = alivePlayers.getFirst();

                    winsPlayer.put(winnerUUID, winsPlayer.get(winnerUUID) + 1);

                    if (arena.getRounds() == winsPlayer.get(winnerUUID)) {
                        Player winner = Bukkit.getPlayer(winnerUUID);
                        assert winner != null;
                        Win(winner);
                    } else {
                        UUID roundWinnerUUID = alivePlayers.getFirst();
                        Player roundWinner = Bukkit.getPlayer(roundWinnerUUID);
                        assert roundWinner != null;

                        nextRound(roundWinner);
                        return;
                    }
                } else {
                    plugin.utils.message(arena.getPlayerList(), "&cNo alive players... Game over.");
                }

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    for (Player p : arena.getPlayerList()) {
                        plugin.utils.setMaxHealth(p);
                        plugin.getArenaManager().getRollBackManager().restore(p, plugin);
                        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);

                        if (duelsPlayer.getTeam() != null) {
                            duelsPlayer.deleteTeam(duelsPlayer.getTeam());
                        }
                        duelsPlayer.setDuel(false);
                        p.setInvulnerable(false);
                        removeFluidBoost(p);
                        arena.removePlayer(p, plugin);
                        winsPlayer.remove(p.getUniqueId());
                    }

                    resetArenaBlocks();
                    resetArenaEntities();
                    plugin.getArenaManager().setInactiveState(arena);
                }, 20 * 5);
            } else {
                plugin.utils.message(arena.getPlayerList(), "&c" + player.getName() + " died.");
            }
        }
    }

    /**
     * Comprueba si el jugador está sosteniendo un totem
     * @param player El jugador a comprobar
     * @return Si el jugador sostiene un totem, devuelve 'true', si no, devuelve 'false'
     */
    private boolean hasTotem(Player player) {
        return player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING || player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING;
    }

    private void Win(Player winner) {
        if (winner == null || !winner.isOnline()) {
            plugin.utils.message(arena.getPlayerList(), "&cWinner could not be found... Game Over");
        } else {
            plugin.utils.message(arena.getPlayerList(), "&a" + winner.getName() + " has won!");
            int score1 = winsPlayer.get(arena.getPlayers().get(0));
            int score2 = winsPlayer.get(arena.getPlayers().get(1));
            for (Player player : arena.getPlayerList()) {
                player.setInvulnerable(true);
                if (player == winner){
                    player.showTitle(Title.title(plugin.utils.chat("&cYou won."), plugin.utils.chat("&7Score: &c" + score1 + " &7- &9" + score2)));
                } else {
                    player.showTitle(Title.title(plugin.utils.chat("&cYou lost."), plugin.utils.chat("&7Score: &c" + score1 + " &7- &9" + score2)));
                }
            }
        }
    }

    private void nextRound(Player roundWinner){
        plugin.utils.message(arena.getPlayerList(), "&a" + roundWinner.getName() + " has won this round! &7Next Round starting in &c5s");
        int score1 = winsPlayer.get(arena.getPlayers().get(0));
        int score2 = winsPlayer.get(arena.getPlayers().get(1));
        plugin.utils.message(arena.getPlayerList(), "&7Score: &c" + score1 + " &7- &9" + score2);

        for (Player player : arena.getPlayerList()) {
            player.setInvulnerable(true);
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
            duelsPlayer.setOnHold(true);
            if (player == roundWinner) {
                player.showTitle(Title.title(plugin.utils.chat("&aYou won."), plugin.utils.chat("&7Score: &c" + score1 + " &7- &9" + score2)));
            } else {
                player.showTitle(Title.title(plugin.utils.chat("&cYou lost."), plugin.utils.chat("&7Score: &c" + score1 + " &7- &9" + score2)));
                alivePlayers.add(player.getUniqueId());
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            int lastSpawnId = 0;
            if (!arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_REGENERATION)) {
                resetArenaBlocks();
                resetArenaEntities();
            }
            for (Player player : arena.getPlayerList()) {
                player.setGameMode(GameMode.SURVIVAL);
                restorePlayer(player);
                DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
                duelsPlayer.setOnHold(false);
                player.showTitle(Title.title(plugin.utils.chat("&b&lGO!"), Component.empty()));
                if (lastSpawnId == 0) {
                    player.teleport(arena.getSpawnOne());
                    lastSpawnId = 1;
                } else {
                    player.teleport(arena.getSpawnTwo());
                    lastSpawnId = 0;
                }
            }
            plugin.utils.message(arena.getPlayerList(), "&aNew Round has started.");
        }, 20 * 5);
    }

    ////////////////////////////////////
    // Inicio del buff de los fluidos //
    ////////////////////////////////////
    /**
     * Lista que guarda a los jugadores que tengan un cubo en la mano.
     */
    private final Set<Player> bucketHoldingPlayers = new HashSet<>();

    /**
     * Cuando el jugador tiene un cubo en la mano, aumenta su rango a 5.0 (pre-1.20.5)
     * para evitar problemas de ghosting con versiones antiguas.
     * De la misma manera, disminuye el rango si ya no lo tiene en la mano.
     * @param event El evento a registrar
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
     * Cuando el jugador da click a un cubo en el inventario y lo deja en la hotbar,
     * aumenta su rango a 5.0 (pre-1.20.5) para evitar problemas de ghosting con versiones antiguas.
     * De la misma manera, disminuye el rango si lo quita de la hotbar.
     * @param event El evento a registrar
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
     * Comprueba si el material es un cubo o no.
     * @param i El material a comprobar
     * @return Si el material es un cubo, devuelve 'true', si no lo es, devuelve 'false'.
     */
    private boolean isBucket(Material i) {
        return switch (i) {
            case WATER_BUCKET, COD_BUCKET, SALMON_BUCKET, TROPICAL_FISH_BUCKET, PUFFERFISH_BUCKET, AXOLOTL_BUCKET,
                 TADPOLE_BUCKET, LAVA_BUCKET -> true;
            default -> false;
        };
    }

    /**
     * Aumenta el rango del jugador para cuando tenga un cubo en la mano.
     * @param p El jugador al cual se le aumenta el rango
     */
    private void applyFluidBoost(Player p) {
        if (!bucketHoldingPlayers.contains(p)) {
            AttributeInstance attribute = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
            assert attribute != null;
            double newValue = attribute.getBaseValue() + 0.5;
            attribute.setBaseValue(newValue);
            bucketHoldingPlayers.add(p);
        }
    }

    /**
     * Aumenta el rango del jugador para cuando no tiene un cubo en la mano.
     * @param p El jugador al cual se le disminuye el rango
     */
    private void removeFluidBoost(Player p) {
        if (bucketHoldingPlayers.contains(p)) {
            AttributeInstance attribute = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
            assert attribute != null;
            double newValue = attribute.getBaseValue() - 0.5;
            attribute.setBaseValue(newValue);
            bucketHoldingPlayers.remove(p);
        }
    }
    /////////////////////////////////
    // Fin del buff de los fluidos //
    /////////////////////////////////

    ///////////////////////////////////////////////
    // Inicio del sistema de reparación de arena //
    ///////////////////////////////////////////////
    /**
     * Lista que guarda todos los bloques que se modifican desde el inicio del duel hasta el final de la ronda.
     * (O en caso de que la regeneración de la arena esté desactivada, hasta el final del duel)
     */
    private final Map<Location, Material> modifiedBlocks = new HashMap<>();

    /**
     * Guarda cuando se rompe un bloque en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        Block block = event.getBlock();
        Location loc = block.getLocation();
        modifiedBlocks.putIfAbsent(loc, block.getType());
    }

    /**
     * Guarda cuando se pone un bloque en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        Block block = event.getBlock();
        Location loc = block.getLocation();
        modifiedBlocks.putIfAbsent(loc, Material.AIR);
    }

    /**
     * Guarda cuando se pone un fluido en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;

        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (block.getBlockData() instanceof Waterlogged waterlogged) {
            if (!waterlogged.isWaterlogged()) {
                modifiedBlocks.putIfAbsent(loc, block.getType());
            }
        } else modifiedBlocks.putIfAbsent(loc, Material.AIR);
    }

    /**
     * Guarda cuando se expande un fluido en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onFluidFlow(BlockFromToEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
            Location loc = block.getLocation();
            if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
            modifiedBlocks.putIfAbsent(loc, Material.AIR);
        }
    }

    /**
     * Guarda cuando se crea cobblestone con agua y lava en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onLavaCast(BlockFormEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;

        modifiedBlocks.putIfAbsent(loc, Material.AIR);
    }

    /**
     * Guarda cuando un bloque de grass se transforma en uno de tierra en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onGrassFade(BlockFadeEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (block.getType() == Material.GRASS_BLOCK && event.getNewState().getType() == Material.DIRT) {
            if (!plugin.utils.isInside(loc, arena.getCornerOne(), arena.getCornerTwo())) return;
            modifiedBlocks.putIfAbsent(loc, block.getType());
        }
    }

    /**
     * Guarda cuando creas una farmland o un path con una azada o una pala en la arena.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onBlockTransform(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        Material blockType = block.getType();

        if ((item.getType().toString().endsWith("_HOE") || item.getType().toString().endsWith("_SHOVEL")) && (blockType == Material.GRASS_BLOCK || blockType == Material.DIRT)) {
            modifiedBlocks.putIfAbsent(loc, blockType);
        }
    }

    /**
     * Restaura todos los cambios hechos en la arena después de la ronda.
     * Si la regeneración de arena está desactivada, los restaura después del duel.
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
     * Borra todas las entidades en la arena después de que termine la ronda.
     * Si la regeneración de arena está desactivada, las borra después del duel.
     */
    public void resetArenaEntities() {
        arena.getCenter().getWorld().getEntities().stream()
                .filter(entity -> plugin.utils.isInside(entity.getLocation(), arena.getCornerOne(), arena.getCornerTwo()))
                .filter(entity -> !(entity instanceof Player))
                .forEach(Entity::remove);
    }
    ////////////////////////////////////////////
    // Fin del sistema de reparación de arena //
    ////////////////////////////////////////////

    //////////////////////////////////////
    // Sistema de atributos de la arena //
    //////////////////////////////////////
    /**
     * Si no se pueden romper bloques en el kit, cancela el evento.
     * @param event El evento a registrar
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_BLOCK_BREAK)) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cBreaking blocks is disabled in this kit!"));
        }
    }

    /**
     * Si no se pueden poner bloques en el kit, cancela el evento.
     * @param event El evento a registrar
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_BLOCK_PLACE)) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cPlacing blocks is disabled in this kit!"));
        }
    }

    /**
     * Si no se puede regenerar vida en el kit, cancela la regeneración.
     * @param event El evento a registrar
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_NATURAL_REGEN) && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }

    /**
     * Si no se puede craftear en el kit, cancela el resultado.
     * @param event El evento a registrar
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(PrepareItemCraftEvent event) {
        Player p = (Player) event.getView().getPlayer();
        if (!arena.hasPlayer(p)) return;

        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_CRAFTING)) {
            event.getInventory().setResult(null);
            p.sendActionBar(plugin.utils.chat("&cCrafting is disabled in this kit!"));
        }
    }

    /**
     * Si no se puede tener hambre en el kit, cancela la perdida de hambre.
     * Además, si no se puede perder toda la barra de comida en el kit y la barra de comida está en el limite permitido, cancela la perdida de hambre.
     * @param event El evento a registrar
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (!arena.hasPlayer(p)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.NO_HUNGER)) {
            event.setCancelled(true);
        }
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_COMPLETE_HUNGER_LOSS) && event.getFoodLevel() < 10) {
            event.setCancelled(true);
        }
    }

    /**
     * Si el daño melee está aumentado en el kit, lo aumenta a su respectivo porcentaje.
     * @param event El evento a registrar
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player v)) return;
        if (!(event.getDamager() instanceof Player a)) return;
        if (!arena.hasPlayer(v)) return;
        if (!arena.hasPlayer(a)) return;

        if (arena.getKit().hasAttribute(ArenaAttribute.ONE_THIRD_MORE_MELEE_DAMAGE)) {
            double newDamage = event.getDamage() * 4 / 3;
            event.setDamage(newDamage);
        }
    }
    //////////////////////////////////////////////
    // Fin del sistema de atributos de la arena //
    //////////////////////////////////////////////

    /**
     * Cuando un jugador sale de la arena, lo devuelve a la arena, para evitar problemas.
     * @param event El evento a registrar
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        if (plugin.utils.isInside(event.getTo(), arena.getCornerOne(), arena.getCornerTwo())) return;

        event.setCancelled(true);
        p.teleport(event.getFrom());
        p.sendActionBar(plugin.utils.chat("&cThis is the limit of the arena. You can't go further."));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        arena.removePlayer(p, plugin);
        if (p.isInvulnerable()) p.setInvulnerable(false);

        bucketHoldingPlayers.remove(p);

        alivePlayers.remove(p.getUniqueId());
        winsPlayer.remove(p.getUniqueId());

        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                UUID winnerUUID = alivePlayers.getFirst();
                Player winner = Bukkit.getPlayer(winnerUUID);
                assert winner != null;
                plugin.utils.message(arena.getPlayerList(), "&a" + winner.getName() + " has won!");
                winner.showTitle(Title.title(plugin.utils.chat("&cYou won."), Component.empty()));
            } else {
                plugin.utils.message(arena.getPlayerList(), "&cNo alive players... Game over.");
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Player player : arena.getPlayerList()) {
                    plugin.utils.setMaxHealth(player);
                    plugin.getArenaManager().getRollBackManager().restore(player, plugin);
                    DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
                    if (duelsPlayer.getTeam() != null) {
                        duelsPlayer.deleteTeam(duelsPlayer.getTeam());
                    }
                    winsPlayer.remove(player.getUniqueId());
                }

                resetArenaBlocks();
                resetArenaEntities();
                plugin.getArenaManager().setInactiveState(arena);
            }, 20 * 5);
        } else {
            plugin.utils.message(arena.getPlayerList(), "&c" + event.getPlayer().getName() + " quit.");
        }

    }
}
