package us.polarismc.polarisduels.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.duel.DuelTeam;

import java.util.UUID;

public class DuelsPlayer {

    private final Main plugin;
    @Getter
    private final UUID uid;
    @Getter
    private final String name;
    private final String ip;
    @Setter
    private boolean duel = false;
    @Setter
    private boolean queue = false;
    @Getter
    @Setter
    private DuelTeam team = null;

    public DuelsPlayer(UUID uid, String name) {
        this.plugin = Main.getInstance();

        this.uid = uid;
        this.name = name;
        this.ip = getPlayer().getAddress().getAddress().getHostAddress();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uid);
    }

    public boolean isOnline() {
        Player p = Bukkit.getPlayer(uid);
        return p != null;
    }

    public boolean inQueue() {
        return queue;
    }

    public boolean inDuel() {
        return duel;
    }

    public String getIp() {
        if (isOnline()) return getPlayer().getAddress().getAddress().getHostAddress();
        return ip;
    }


    public synchronized void removeTeam() {
        this.team = null;
    }

    public void deleteTeam(DuelTeam team) {
        team.getTeam().unregister();
        team.getMembers().forEach(p -> p.setTeam(null));
        team.getTeams().remove(team);
    }
}
