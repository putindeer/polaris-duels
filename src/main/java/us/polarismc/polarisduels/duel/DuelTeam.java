package us.polarismc.polarisduels.duel;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelTeam {

    private final Main plugin = Main.getInstance();
    @Getter
    private Team team;
    @Getter
    private final List<DuelsPlayer> members;

    @Getter
    private List<DuelTeam> teams = new ArrayList<>();

    @Getter
    private List<UUID> playerList = new ArrayList<>();

    @Getter
    private List<UUID> alivePlayers = new ArrayList<>();

    public DuelTeam(DuelsPlayer p, ChatColor color, String teamName) {
        members = new ArrayList<>();
        members.add(p);
        p.setTeam(this);

        team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(p.getName());
        team.setDisplayName(teamName);
        team.setColor(color);
        team.addEntry(p.getName());

        this.getTeams().add(this);
    }


    public boolean isPlayer(Player player) {
        return playerList.contains(player.getUniqueId());
    }

}
