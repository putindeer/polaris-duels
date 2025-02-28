package us.polarismc.polarisduels.duel;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class DuelTeam {
    private final Main plugin = Main.getInstance();
    private Team team;
    private final List<DuelsPlayer> members = new ArrayList<>();
    private final List<UUID> alivePlayers = new ArrayList<>();
    private NamedTextColor color;
    private String teamName;

    public DuelTeam(NamedTextColor color, String teamName) {
        this.color = color;
        this.teamName = teamName;
        this.team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(teamName);
        this.team.displayName(Component.text(teamName));
        this.team.color(color);
    }

    public DuelTeam(List<DuelsPlayer> players, NamedTextColor color, String teamName) {
        this(color, teamName);
        for (DuelsPlayer player : players) {
            addPlayer(player);
        }
    }

    public void addPlayer(DuelsPlayer player) {
        members.add(player);
        player.setTeam(this);
        team.addEntry(player.getPlayer().getName());
        alivePlayers.add(player.getUuid());
    }

    public boolean hasPlayer(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    public void removePlayer(DuelsPlayer player) {
        members.remove(player);
        team.removeEntry(player.getPlayer().getName());
        alivePlayers.remove(player.getUuid());
    }

    public void deleteTeam() {
        if (this.team != null) {
            this.team.unregister();
            this.team = null;
        }
        for (DuelsPlayer dp : new ArrayList<>(this.members)) {
            dp.setTeam(null);
        }
        this.members.clear();
        this.alivePlayers.clear();
        this.color = null;
        this.teamName = null;
    }
}

