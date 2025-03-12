package us.polarismc.polarisduels.duel;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class DuelTeam {
    private final Main plugin = Main.getInstance();
    private final List<DuelsPlayer> members = new ArrayList<>();
    private final List<UUID> alivePlayers = new ArrayList<>();
    private final Scoreboard scoreboard;
    private final Team team;
    private NamedTextColor color;
    private String teamName;

    public DuelTeam(Scoreboard scoreboard, NamedTextColor color, String teamName) {
        this.scoreboard = scoreboard;
        this.color = color;
        this.teamName = teamName;
        this.team = scoreboard.registerNewTeam(teamName);
        this.team.displayName(Component.text(teamName));
        this.team.color(color);
    }

    public DuelTeam(Scoreboard scoreboard, List<DuelsPlayer> players, NamedTextColor color, String teamName) {
        this(scoreboard, color, teamName);
        for (DuelsPlayer player : players) {
            addPlayer(player);
        }
    }

    public void addPlayer(DuelsPlayer p) {
        members.add(p);
        alivePlayers.add(p.getUuid());
        p.setTeam(this);
        Player player = plugin.getServer().getPlayer(p.getUuid());
        if (player != null) {
            team.addEntry(player.getName());
            player.setScoreboard(scoreboard);
        }
    }

    public void addPlayer(Player player) {
        addPlayer(plugin.getPlayerManager().getDuelsPlayer(player));
    }

    public boolean hasPlayer(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    public void removePlayer(DuelsPlayer p) {
        if (p.getTeam() == null) return;
        members.remove(p);
        alivePlayers.remove(p.getUuid());
        Player player = plugin.getServer().getPlayer(p.getUuid());
        if (player != null) {
            team.removeEntry(player.getName());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        p.setTeam(null);
    }

    public void removePlayer(Player player) {
        removePlayer(plugin.getPlayerManager().getDuelsPlayer(player));
    }

    public void deleteTeam() {
        for (DuelsPlayer dp : new ArrayList<>(this.members)) {
            dp.setTeam(null);
            Player player = plugin.getServer().getPlayer(dp.getUuid());
            if (player != null) {
                team.removeEntry(player.getName());
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        this.members.clear();
        this.alivePlayers.clear();
        this.color = null;
        this.teamName = null;
        this.team.unregister();
    }
}
