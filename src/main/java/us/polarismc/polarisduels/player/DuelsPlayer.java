package us.polarismc.polarisduels.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.duel.DuelTeam;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class DuelsPlayer {
    private final Main plugin;
    private final UUID uid;
    private final String name;
    private final String ip;
    private boolean queue = false;
    private boolean startingDuel = false;
    private boolean duel = false;
    private boolean onHold = false;
    private DuelTeam team = null;

    public DuelsPlayer(UUID uid, String name) {
        this.plugin = Main.getInstance();

        this.uid = uid;
        this.name = name;
        this.ip = Objects.requireNonNull(getPlayer().getAddress()).getAddress().getHostAddress();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uid);
    }

    public boolean isOnline() {
        Player p = Bukkit.getPlayer(uid);
        return p != null;
    }

    public String getIp() {
        if (isOnline()) return Objects.requireNonNull(getPlayer().getAddress()).getAddress().getHostAddress();
        return ip;
    }

    public void deleteTeam(DuelTeam team) {
        team.getTeam().unregister();
        team.getMembers().forEach(p -> p.setTeam(null));
        team.getTeams().remove(team);
        this.team = null;
    }
}
