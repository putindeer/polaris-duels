package us.polarismc.polarisduels.game;

import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaSize;
import us.polarismc.polarisduels.managers.duel.DuelTeam;
import us.polarismc.polarisduels.managers.party.Party;
import us.polarismc.polarisduels.managers.queue.QueueType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Flexible container holding every piece of information required to run a duel or any other arena based game-mode.
 * <p>
 * It is purposely generic so that all modes can reuse the same structure.
 * <p>
 * All ArenaStates should receive a pre-configured {@link GameSession} and read/write their data exclusively through it.
 */
@Getter
@Builder
public class GameSession {
    /** Unique identifier for this session (useful for debugging / DB storage) */
    @Builder.Default
    private final UUID sessionId = UUID.randomUUID();

    /** Creation timestamp */
    @Builder.Default
    private final Instant createdAt = Instant.now();

    /** Arena selected for the match*/
    @Setter
    private ArenaEntity arena;

    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    /** Kit that must be equipped when the match starts */
    private final @NonNull KitType kit;

    /** Number of rounds to be played (First to get to 'n' rounds) */
    private final int rounds;

    /** Mode describing how players are organised in teams */
    private final @NonNull GameType gameType;

    /** Optional size requested when searching for arenas */
    @Builder.Default
    private final ArenaSize requestedSize = ArenaSize.LARGE;

    /** The QueueType of the session (set to null if the session doesn't come from a queue) */
    @Builder.Default
    private final QueueType queueType = null;

    /** Remaining seconds until the match starts */
    @Builder.Default
    private final int secondsUntilStart = 10;

    /**
     * Teams map – key = team index (0..n-1), value = immutable list of player UUIDs belonging to that team.
     * For FFA the map will contain size()==totalPlayers and each list will have single element.
     */
    @Builder.Default
    private final Set<DuelTeam> teams = new HashSet<>();

    private final HashMap<DuelTeam, Integer> teamWins = new HashMap<>();

    /** Players UUIDs */
    @Builder.Default
    private final Set<UUID> players = new HashSet<>();

    /** Spectators UUIDs */
    @Builder.Default
    private final Set<UUID> spectators = new HashSet<>();

    /** Parties involved in the match (may be empty) */
    @Builder.Default
    private final Set<Party> partiesInvolved = new HashSet<>();

    /* --------------------------------------------------------------------- */
    /* Utility methods                                                       */
    /* --------------------------------------------------------------------- */

    public List<Player> getPlayerList() {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Quick check whether a player is a participant (not spectator). */
    public boolean isParticipant(UUID playerId) {
        return players.contains(playerId);
    }

    /** Quick check whether a player is a participant (not spectator). */
    public boolean isParticipant(OfflinePlayer player) {
        return players.contains(player.getUniqueId());
    }

    /** Adds a spectator if not already present. */
    public void addSpectator(UUID playerId) {
        spectators.add(playerId);
    }

    /** Removes a spectator. */
    public void removeSpectator(UUID playerId) {
        spectators.remove(playerId);
    }

    public void addWin(DuelTeam team) {
        teamWins.compute(team, (t, wins) -> wins == null ? 1 : wins + 1);
    }

    public int getWins(DuelTeam team) {
        return teamWins.getOrDefault(team, 0);
    }

    public NamedTextColor findNextAvailableColor() {
        List<NamedTextColor> colorOrder = Arrays.asList(
                NamedTextColor.RED, NamedTextColor.BLUE, NamedTextColor.GREEN,
                NamedTextColor.YELLOW, NamedTextColor.LIGHT_PURPLE, NamedTextColor.GOLD,
                NamedTextColor.AQUA, NamedTextColor.GRAY
        );

        Set<NamedTextColor> usedColors = this.getTeams().stream()
                .map(DuelTeam::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (NamedTextColor color : colorOrder) {
            if (!usedColors.contains(color)) {
                return color;
            }
        }

        Collections.shuffle(colorOrder);
        return colorOrder.getFirst();
    }

    public Set<DuelTeam> getAliveTeams() {
        return getTeams().stream().filter(team -> !team.getAlivePlayers().isEmpty()).collect(Collectors.toSet());
    }
}
