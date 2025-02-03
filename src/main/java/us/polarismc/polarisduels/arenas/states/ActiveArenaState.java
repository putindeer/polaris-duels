package us.polarismc.polarisduels.arenas.states;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
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
            savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
            DuelTeam TeamBlue;
            DuelTeam TeamRed;
            plugin.utils.message(player, Sound.BLOCK_ANCIENT_DEBRIS_BREAK, "&cThe Match has started!");

            winsPlayer.put(player.getUniqueId(), 0);

            plugin.getLogger().info("jugador: " + Objects.requireNonNull(Bukkit.getPlayer(player.getUniqueId())).getName() + ", rondas: " + ActiveArenaState.winsPlayer.get(player.getUniqueId()));

            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
            duelsPlayer.setDuel(true);
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

    private void restoreInventory(Player p) {
        plugin.getLogger().info("[DEBUG] Restoring inventory for: " + p.getName());
        ItemStack[] items = savedInventories.get(p.getUniqueId());
        if (items == null) {
            plugin.getLogger().info("[DEBUG] No saved inventory found for: " + p.getName());
            return;
        }

        plugin.getLogger().info("[DEBUG] Items found: " + Arrays.toString(items));
        p.getInventory().clear();
        p.getInventory().setContents(items);
        plugin.getLogger().info("[DEBUG] Inventory restored for: " + p.getName());
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        // Verifica si la entidad es un jugador
        if (!(event.getEntity() instanceof Player player)) return;
        // Mensaje de depuración: jugador dañado
        plugin.getLogger().info("[DEBUG] Player " + player.getName() + " took damage: " + event.getFinalDamage());
        plugin.getLogger().info("[DEBUG] hasPlayer(" + player.getName() + ") returned: " + arena.hasPlayer(player));
        if (!arena.hasPlayer(player)) return;

        event.setCancelled(false);

        // Si el daño final es mayor o igual a la salud del jugador
        if (event.getFinalDamage() >= player.getHealth()) {
            plugin.getLogger().info("[DEBUG] Player " + player.getName() + " would die from this damage.");
            event.setCancelled(true);

            // Cambia el estado del jugador
            alivePlayers.remove(player.getUniqueId());
            plugin.utils.setMaxHealth(player);
            player.setGameMode(GameMode.SPECTATOR);
            plugin.getLogger().info("[DEBUG] Player " + player.getName() + " set to spectator mode and removed from alivePlayers.");

            // Comprueba si queda un solo jugador con vida
            if (alivePlayers.size() <= 1) {
                plugin.getLogger().info("[DEBUG] Remaining alive players: " + alivePlayers.size());

                if (alivePlayers.size() == 1) {
                    UUID winnerUUID = alivePlayers.getFirst();
                    plugin.getLogger().info("[DEBUG] Round winner UUID: " + winnerUUID);

                    winsPlayer.put(winnerUUID, winsPlayer.get(winnerUUID) + 1);
                    plugin.getLogger().info("[DEBUG] Updated wins for player " + winnerUUID + ": " + winsPlayer.get(winnerUUID));

                    if (arena.getRounds() == winsPlayer.get(winnerUUID)) {
                        Player winner = Bukkit.getPlayer(winnerUUID);
                        assert winner != null;
                        plugin.getLogger().info("[DEBUG] Game winner: " + winner.getName());
                        Win(winner);
                    } else {
                        UUID roundWinnerUUID = alivePlayers.getFirst();
                        Player roundWinner = Bukkit.getPlayer(roundWinnerUUID);
                        assert roundWinner != null;
                        plugin.getLogger().info("[DEBUG] Preparing for next round. Round winner: " + roundWinner.getName());

                        nextRound(roundWinner);
                        return;
                    }
                } else {
                    plugin.getLogger().info("[DEBUG] No alive players remaining. Game over.");
                    plugin.utils.message(arena.getPlayerList(), "&cNo alive players... Game over.");
                }

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getLogger().info("[DEBUG] Resetting players and rolling back the arena.");
                    for (Player p : arena.getPlayerList()) {
                        plugin.utils.setMaxHealth(p);
                        plugin.getArenaManager().getRollBackManager().restore(p, plugin);
                        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(p);

                        if (duelsPlayer.getTeam() != null) {
                            plugin.getLogger().info("[DEBUG] Deleting team for player " + p.getName());
                            duelsPlayer.deleteTeam(duelsPlayer.getTeam());
                            duelsPlayer.removeTeam();
                        }
                        duelsPlayer.setDuel(false);
                        arena.removePlayer(p, plugin);
                        winsPlayer.remove(p.getUniqueId());
                        plugin.getLogger().info("[DEBUG] Removed player " + p.getName() + " from winsPlayer.");
                    }

                    plugin.getArenaManager().setInactiveState(arena);
                    plugin.getLogger().info("[DEBUG] Arena set to inactive state.");
                }, 20 * 5);
            } else {
                plugin.utils.message(arena.getPlayerList(), "&c" + player.getName() + " died.");
                plugin.getLogger().info("[DEBUG] Player " + player.getName() + " died. Remaining players: " + alivePlayers.size());
            }
        }
    }


    private void Win (Player winner){
        if (winner == null || !winner.isOnline()){
            plugin.utils.message(arena.getPlayerList(), "&cWinner could not be found... Game Over");

        } else {
            plugin.utils.message(arena.getPlayerList(), "&a" + winner.getName() + " has won!");
            int score1 = winsPlayer.get(arena.getPlayers().get(0));
            int score2 = winsPlayer.get(arena.getPlayers().get(1));
            for (Player player : arena.getPlayerList()) {
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
            if (player == roundWinner) {
                player.showTitle(Title.title(plugin.utils.chat("&aYou won."), plugin.utils.chat("&7Score: &c" + score1 + " &7- &9" + score2)));
            } else {
                player.showTitle(Title.title(plugin.utils.chat("&cYou lost."), plugin.utils.chat("&7Score: &c" + score1 + " &7- &9" + score2)));
                alivePlayers.add(player.getUniqueId());
            }
        }

        plugin.getLogger().info("[DEBUG] Starting next round");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            int lastSpawnId = 0;
            for (Player player : arena.getPlayerList()) {
                plugin.getLogger().info("[DEBUG] Restoring inventory for: " + player.getName());
                player.setGameMode(GameMode.SURVIVAL);
                restoreInventory(player);
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

    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        if (!arena.hasPlayer(p)) return;
        arena.removePlayer(p, plugin);

        alivePlayers.remove(p.getUniqueId());
        winsPlayer.remove(p.getUniqueId());

        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                UUID winnerUUID = alivePlayers.getFirst();
                Player winner = Bukkit.getPlayer(winnerUUID);
                Win(winner);
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
                        duelsPlayer.removeTeam();
                    }
                    winsPlayer.remove(player.getUniqueId());
                }

                plugin.getArenaManager().setInactiveState(arena);
            }, 20 * 5);
        } else {
            plugin.utils.message(arena.getPlayerList(), "&c" + event.getPlayer().getName() + " quit.");
        }

    }
}
