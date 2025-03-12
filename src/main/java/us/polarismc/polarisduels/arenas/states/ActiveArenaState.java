package us.polarismc.polarisduels.arenas.states;

import it.unimi.dsi.fastutil.Pair;
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
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaAttribute;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.duel.DuelTeam;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.*;

public class ActiveArenaState implements ArenaState, Listener {
    //region [Metodos al activar y desactivar la arena]
    private final Main plugin = Main.getInstance();
    private ArenaEntity arena;
    @Override
    public void onEnable(ArenaEntity arena) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.arena = arena;
        startDuel();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    //endregion

    //region [Sistema para iniciar el duel]
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
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
            plugin.utils.message(player, Sound.BLOCK_ANCIENT_DEBRIS_BREAK, "&cThe Match has started!");

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

    private int healthTaskId = -1;

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

    private void stopHealthIndicator() {
        if (healthTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(healthTaskId);
            healthTaskId = -1;
        }
    }
    //endregion

    //region [Sistema para detectar cuando un jugador muere]
    public final static HashMap<DuelTeam, Integer> winsTeam = new HashMap<>();
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

        winsTeam.put(winningTeam, winsTeam.getOrDefault(winningTeam, 0) + 1);

        if (arena.getRounds() == winsTeam.get(winningTeam)) {
            Win(winningTeam);
        } else {
            nextRound(winningTeam);
        }
    }

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
     * Comprueba si el jugador está sosteniendo un totem
     * @param player El jugador a comprobar
     * @return Si el jugador sostiene un totem, devuelve 'true', si no, devuelve 'false'
     */
    private boolean hasTotem(Player player) {
        return player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING || player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING;
    }
    //endregion

    //region [Sistema de pasar a la siguiente ronda]
    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
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
                duelsPlayer.getTeam().getAlivePlayers().add(player.getUniqueId());
            }
        }

        resetRound();
    }

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
                team.getAlivePlayers().remove(player.getUniqueId());
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
        p.setArrowsInBody(0);
        p.setItemOnCursor(new ItemStack(Material.AIR));
        p.setInvulnerable(false);
        removeFluidBoost(p);
    }
    //endregion

    //region [Sistema de ganar la partida]
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

    //region [Metodos de la arena]
    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        if (p.isInvulnerable()) p.setInvulnerable(false);

        bucketHoldingPlayers.remove(p);

        checkForWinner(p);
        arena.removePlayer(p, plugin);
    }

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
     * Cuando un jugador sale de la arena, lo devuelve a la arena, para evitar problemas.
     * @param event El evento a registrar
     */
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) return;
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        if (plugin.utils.isInside(event.getTo(), arena.getCornerOne(), arena.getCornerTwo())) return;

        event.setCancelled(true);
        p.teleport(event.getFrom());
        p.sendActionBar(plugin.utils.chat("&cThis is the limit of the arena. You can't go further."));
    }

    /**
     * Obtiene las wins del team al darle un color.
     * @param color Color del equipo
     * @return El número de wins
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

    //region [Buff de los fluidos]
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
    //endregion

    //region [Sistema de reparación de la arena]
    /**
     * Lista que guarda todos los bloques que se modifican desde el inicio del duel hasta el final de la ronda.
     * (O en caso de que la regeneración de la arena esté desactivada, hasta el final del duel)
     */
    private final Map<Location, Material> modifiedBlocks = new HashMap<>();
    private final Set<Location> placedBlocks = new HashSet<>();

    /**
     * Guarda cuando se rompe un bloque en la arena.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;
        modifiedBlocks.putIfAbsent(loc, block.getType());
    }

    /**
     * Guarda cuando se pone un bloque en la arena.
     * Si no se puede romper la arena en el kit, guarda el bloque que puso el jugador.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;
        modifiedBlocks.putIfAbsent(loc, Material.AIR);
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_DESTRUCTION)) {
            placedBlocks.add(event.getBlock().getLocation());
        }
    }

    /**
     * Guarda cuando se pone un fluido en la arena.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;

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
    @EventHandler(ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        Block block = event.getBlock();
        if (block.isLiquid()) {
            Location loc = event.getToBlock().getLocation();
            if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;
            modifiedBlocks.putIfAbsent(loc, Material.AIR);
        }
    }

    /**
     * Guarda cuando se crea cobblestone con agua y lava en la arena.
     * Si no se puede romper la arena en el kit, guarda los bloques que generó el lavacast.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onLavaCast(BlockFormEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;

        modifiedBlocks.putIfAbsent(loc, Material.AIR);
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_DESTRUCTION)) {
            placedBlocks.add(loc);
        }
    }

    /**
     * Guarda cuando un bloque se quema en la arena debido al fuego.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;

        modifiedBlocks.putIfAbsent(loc, block.getType());
    }

    /**
     * Guarda cuando un bloque de grass se transforma en uno de tierra en la arena.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onGrassFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (block.getType() == Material.GRASS_BLOCK && event.getNewState().getType() == Material.DIRT) {
            if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;
            modifiedBlocks.putIfAbsent(loc, block.getType());
        }
    }

    /**
     * Guarda cuando creas una farmland o un path con una azada o una pala en la arena.
     * @param event El evento a registrar
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockTransform(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        if (!plugin.utils.isInWorld(loc, arena.getWorld())) return;
        Material blockType = block.getType();

        if ((item.getType().toString().endsWith("_HOE") || item.getType().toString().endsWith("_SHOVEL")) && (blockType == Material.GRASS_BLOCK || blockType == Material.DIRT)) {
            modifiedBlocks.putIfAbsent(loc, blockType);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!plugin.utils.isInWorld(event.getLocation(), arena.getWorld())) return;
        event.blockList().forEach(b -> modifiedBlocks.putIfAbsent(b.getLocation(), b.getType()));
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
        arena.getWorld().getEntities().stream()
                .filter(entity -> !(entity instanceof Player))
                .forEach(Entity::remove);
    }
    //endregion

    //region [Sistema de atributos de la arena]
    /**
     * Si no se pueden romper bloques en el kit, cancela el evento.
     * Si no se puede romper la arena en el kit, y el bloque no fue puesto por un jugador, cancela el evento.
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
        if (arena.getKit().hasAttribute(ArenaAttribute.NO_ARENA_DESTRUCTION) && !placedBlocks.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            p.sendActionBar(plugin.utils.chat("&cBreaking the arena is disabled in this kit!"));
        }
    }

    /**
     * Si no se pueden poner bloques en el kit, cancela el evento.
     * @param event El evento a registrar
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
    //endregion
}