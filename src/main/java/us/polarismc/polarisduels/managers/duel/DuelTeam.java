package us.polarismc.polarisduels.managers.duel;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

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
    private final Set<UUID> members = new HashSet<>();
    
    /** Set of UUIDs of currently alive team members */
    private final Set<UUID> aliveMembers = new HashSet<>();
    
    /** The scoreboard this team is associated with */
    private final Scoreboard scoreboard;
    
    /** The Bukkit Team object for scoreboard management */
    private final Team team;
    
    /** The color of the team for display purposes */
    private NamedTextColor color;
    
    /** The display name of the team */
    private String displayName;

    /**
     * Creates a new DuelTeam with the specified parameters.
     *
     * @param scoreboard The scoreboard to register this team on
     * @param color The color for the team display
     * @param displayName The name of the team
     */
    public DuelTeam(Scoreboard scoreboard, NamedTextColor color, String displayName) {
        this.scoreboard = scoreboard;
        this.team = scoreboard.registerNewTeam(displayName);
        setColor(color);
        setDisplayName(displayName);
    }

    public DuelTeam(Scoreboard scoreboard, Set<UUID> players) {
        this(scoreboard, NamedTextColor.WHITE, Bukkit.getOfflinePlayer(players.iterator().next()).getName());
        players.forEach(this::addPlayer);
    }

    /**
     * Creates a new DuelTeam with initial players.
     *
     * @param scoreboard The scoreboard to register this team on
     * @param players The initial players to add to the team
     * @param color The color for the team display
     * @param displayName The name of the team
     */
    public DuelTeam(Scoreboard scoreboard, Set<UUID> players, NamedTextColor color, String displayName) {
        this(scoreboard, color, displayName);
        players.forEach(this::addPlayer);
    }

    /**
     * Creates a new DuelTeam with initial players.
     *
     * @param scoreboard The scoreboard to register this team on
     * @param players The initial players to add to the team
     * @param color The color for the team display
     */
    public DuelTeam(Scoreboard scoreboard, Set<UUID> players, NamedTextColor color) {
        this(scoreboard, color, color.toString().toUpperCase());
        players.forEach(this::addPlayer);
    }

    public void setColor(NamedTextColor color) {
        this.color = color;
        this.team.color(color);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.team.displayName(Component.text(displayName));
    }

    public String getColorTag() {
        return "<" + color.toString().toLowerCase() + ">";
    }

    public String getColoredName() {
        return getColorTag() + displayName;
    }

    /**
     * Adds a player to this team and updates their scoreboard.
     *
     * @param uuid The uuid of the player you need to add to the team
     */
    public void addPlayer(UUID uuid) {
        members.add(uuid);
        aliveMembers.add(uuid);
        plugin.getPlayerManager().getPlayer(uuid).setTeam(this);
        Player player = Bukkit.getPlayer(uuid);
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
        addPlayer(player.getUniqueId());
    }

    /**
     * Checks if a player is currently alive in this team.
     *
     * @param player The player to check
     * @return true if the player is alive and in this team, false otherwise
     */
    public boolean hasPlayer(Player player) {
        return members.contains(player.getUniqueId());
    }

    /**
     * Checks if a player is currently alive in this team.
     *
     * @param player The player to check
     * @return true if the player is alive and in this team, false otherwise
     */
    public boolean isAlive(Player player) {
        return aliveMembers.contains(player.getUniqueId());
    }

    public List<Player> getOnlinePlayers() {
        return getMembers().stream()
                .map(uuid -> plugin.getPlayerManager().getPlayer(uuid))
                .filter(DuelsPlayer::isOnline)
                .map(DuelsPlayer::getPlayer)
                .toList();
    }

    public List<Player> getAlivePlayers() {
        return getAliveMembers().stream()
                .map(uuid -> plugin.getPlayerManager().getPlayer(uuid))
                .filter(DuelsPlayer::isOnline)
                .map(DuelsPlayer::getPlayer)
                .toList();
    }

    public void killPlayer(Player player) {
        getAliveMembers().remove(player.getUniqueId());
    }

    public void killPlayer(UUID uuid) {
        getAliveMembers().remove(uuid);
    }

    /**
     * Removes a player from this team and updates their scoreboard.
     *
     * @param duelsPlayer The DuelsPlayer to remove from the team
     */
    public void removePlayer(DuelsPlayer duelsPlayer) {
        if (duelsPlayer.getTeam() == null) return;
        members.remove(duelsPlayer.getUuid());
        aliveMembers.remove(duelsPlayer.getUuid());
        Player player = duelsPlayer.getPlayer();
        if (player != null) {
            team.removeEntry(player.getName());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        duelsPlayer.setTeam(null);
    }

    /**
     * Removes a player from this team by their Player instance.
     *
     * @param player The player to remove from the team
     */
    public void removePlayer(Player player) {
        removePlayer(plugin.getPlayerManager().getPlayer(player));
    }

    /**
     * Completely removes this team, cleaning up all player references and scoreboard entries.
     * This should be called when the team is no longer needed.
     */
    public void deleteTeam() {
        for (DuelsPlayer dp : members.stream().map(plugin.getPlayerManager()::getPlayer).toList()) {
            dp.setTeam(null);
            Player player = dp.getPlayer();
            if (player != null) {
                team.removeEntry(player.getName());
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        this.members.clear();
        this.aliveMembers.clear();
        this.color = null;
        this.displayName = null;
        this.team.unregister();
    }
}
