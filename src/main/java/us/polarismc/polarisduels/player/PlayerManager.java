package us.polarismc.polarisduels.player;

import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerManager {
    private final Main plugin;

    private final List<DuelsPlayer> players;

    public PlayerManager(Main plugin) {
        this.plugin = plugin;
        players = new ArrayList<>();
    }

    public boolean doesPlayerExist(Player p) {
        return getDuelsPlayer(p.getUniqueId()) != null;
    }

    public DuelsPlayer getDuelsPlayer(Player p) {
        return getDuelsPlayer(p.getUniqueId());
    }
    public DuelsPlayer getDuelsPlayer(String name) {
        for (DuelsPlayer p : getPlayerList()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }
    public DuelsPlayer getDuelsPlayer(UUID uid) {
        for (DuelsPlayer p : getPlayerList()) {
            if (p.getUid().equals(uid)) {
                return p;
            }
        }

        return null;
    }

    public void playerJoin(Player player) {
        if (getDuelsPlayer(player.getUniqueId()) == null) {
            newDuelsPlayer(player);
        }
    }

    public void newDuelsPlayer(Player p) {
        newDuelsPlayer(p.getUniqueId(), p.getName());
    }
    public void newDuelsPlayer(UUID uid, String name) {
        DuelsPlayer newPlayer = new DuelsPlayer(uid,name);
        plugin.getLogger().info("nuevo jugador ha sido registrado");
        getPlayerList().add(newPlayer);
    }

    public List<DuelsPlayer> getPlayerList() {
        return players;
    }
}
