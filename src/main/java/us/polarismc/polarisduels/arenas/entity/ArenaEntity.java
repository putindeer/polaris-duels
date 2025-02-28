package us.polarismc.polarisduels.arenas.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.states.ArenaState;
import us.polarismc.polarisduels.arenas.states.StartingArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;
import us.polarismc.polarisduels.queue.KitType;
import us.polarismc.polarisduels.events.HubEvents;
import us.polarismc.polarisduels.player.DuelsPlayer;
import us.polarismc.polarisduels.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ArenaEntity {
    private String displayName;
    private String name;
    private World world;
    private Location spawnOne;
    private Location spawnTwo;
    private Location cornerOne;
    private Location cornerTwo;
    private Location center;
    private ItemStack blockLogo;
    private ArenaSize arenaSize;
    private ArenaState arenaState;
    private List<UUID> players = new ArrayList<>();
    private KitType kit;
    private int playersNeeded;
    private int rounds;

    public void setArenaState(ArenaState state) {
        if (this.arenaState != null) {
            this.arenaState.onDisable(this);
        }
        this.arenaState = state;
        this.arenaState.onEnable(this);
    }

    public List<Player> getPlayerList() {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //TODO - SISTEMA DE TEAMS

    public void addPlayer(Player player, Main plugin) {
        players.add(player.getUniqueId());
        plugin.getPlayerManager().getDuelsPlayer(player).setQueue(true);
        plugin.utils.message(getPlayerList(), player.getName() + " joined &a(" + players.size() + "/" + playersNeeded + ")");
        player.setGameMode(GameMode.SURVIVAL);


        plugin.getArenaManager().getRollBackManager().save(player);

        player.getInventory().setItem(8,
                new ItemBuilder(Material.BARRIER)
                        .name(HubEvents.LEAVE_QUEUE)
                        .build()
        );
        int halfSize = playersNeeded / 2;
        if (players.size() <= halfSize) {
            player.teleport(spawnOne);
        } else {
            player.teleport(spawnTwo);
        }

        if (players.size() == playersNeeded) {
            setArenaState(new StartingArenaState());
        }
    }

    public void removePlayer(Player player, Main plugin) {
        players.remove(player.getUniqueId());
        DuelsPlayer duelsPlayer = plugin.getPlayerManager().getDuelsPlayer(player);
        if (duelsPlayer.getTeam() != null) {
            duelsPlayer.getTeam().removePlayer(duelsPlayer);
        }
        if (duelsPlayer.isQueue()) {
            duelsPlayer.setQueue(false);
        }
        if (duelsPlayer.isDuel()){
            duelsPlayer.setDuel(false);
        }
        if (duelsPlayer.isStartingDuel()) {
            duelsPlayer.setStartingDuel(false);
        }
        if (duelsPlayer.isOnHold()) {
            duelsPlayer.setOnHold(false);
        }

        if (player.isOnline()) {
            plugin.getArenaManager().getRollBackManager().restore(player, null);
        }

        if (arenaState instanceof StartingArenaState) {
            setArenaState(new WaitingArenaState(this, kit, playersNeeded, rounds));
            for (Player p : getPlayerList()) {
                plugin.utils.message(getPlayerList(), player.getName() + " quit &c(" + players.size() + "/" + playersNeeded + ")");
                p.showTitle(Title.title(plugin.utils.chat("&cMatch Cancelled"), plugin.utils.chat("Someone left the queue.")));
                p.getInventory().clear();
                p.getInventory().addItem(
                        new ItemBuilder(Material.BARRIER)
                                .name(HubEvents.LEAVE_QUEUE)
                                .build()
                );
            }
        }
        if (arenaState instanceof WaitingArenaState) {
            if (players.isEmpty()) {
                plugin.getArenaManager().setInactiveState(this);
            }
        }
    }
    public boolean hasPlayer(Player player){
        return players.contains(player.getUniqueId());
    }
    public boolean hasPlayer(UUID uuid){
        return players.contains(uuid);
    }
}
