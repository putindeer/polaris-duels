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

import java.util.*;

/**
 * Represents a team in a duel, managing team members, their states, and scoreboard integration.
 * This class handles team creation, player management, and visual representation in the scoreboard.
 */
@Getter
@SuppressWarnings("unused")
public class DuelTeam {
    private final Main plugin = Main.getInstance();
    
    /** List of all team members */
    private final List<DuelsPlayer> members = new ArrayList<>();
    
    /** Set of UUIDs of currently alive team members */
    private final Set<UUID> alivePlayers = new HashSet<>();
    
    /** The scoreboard this team is associated with */
    private final Scoreboard scoreboard;
    
    /** The Bukkit Team object for scoreboard management */
    private final Team team;
    
    /** The color of the team for display purposes */
    private NamedTextColor color;
    
    /** The display name of the team */
    private String teamName;

    /**
     * Creates a new DuelTeam with the specified parameters.
     *
     * @param scoreboard The scoreboard to register this team on
     * @param color The color for the team display
     * @param teamName The name of the team
     */
    public DuelTeam(Scoreboard scoreboard, NamedTextColor color, String teamName) {
        this.scoreboard = scoreboard;
        this.color = color;
        this.teamName = teamName;
        this.team = scoreboard.registerNewTeam(teamName);
        this.team.displayName(Component.text(teamName));
        this.team.color(color);
    }

    /**
     * Creates a new DuelTeam with initial players.
     *
     * @param scoreboard The scoreboard to register this team on
     * @param players The initial players to add to the team
     * @param color The color for the team display
     * @param teamName The name of the team
     */
    public DuelTeam(Scoreboard scoreboard, List<DuelsPlayer> players, NamedTextColor color, String teamName) {
        this(scoreboard, color, teamName);
        for (DuelsPlayer player : players) {
            addPlayer(player);
        }
    }

    /**
     * Adds a player to this team and updates their scoreboard.
     *
     * @param p The DuelsPlayer to add to the team
     */
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

    /**
     * Adds a player to this team by their Player instance.
     *
     * @param player The player to add to the team
     */
    public void addPlayer(Player player) {
        addPlayer(plugin.getPlayerManager().getDuelsPlayer(player));
    }

    /**
     * Checks if a player is currently alive in this team.
     *
     * @param player The player to check
     * @return true if the player is alive and in this team, false otherwise
     */
    public boolean hasPlayer(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    /**
     * Removes a player from this team and updates their scoreboard.
     *
     * @param p The DuelsPlayer to remove from the team
     */
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

    /**
     * Removes a player from this team by their Player instance.
     *
     * @param player The player to remove from the team
     */
    public void removePlayer(Player player) {
        removePlayer(plugin.getPlayerManager().getDuelsPlayer(player));
    }

    /**
     * Completely removes this team, cleaning up all player references and scoreboard entries.
     * This should be called when the team is no longer needed.
     */
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
