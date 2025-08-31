package us.polarismc.polarisduels.managers.party;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.hub.HubLayout;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a party of players that can participate in duels together.
 * Handles party membership, leadership, and party-wide operations.
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class Party {
    private final Main plugin = Main.getInstance();
    private final Set<UUID> members = new HashSet<>();
    private final Set<PartyInvite> invitations = new HashSet<>();
    private final Set<PartyDuelRequest> requests = new HashSet<>();
    private final PartyGameSettings gameSettings = new PartyGameSettings();
    private final long createdAt = System.currentTimeMillis();
    private final Map<UUID, Long> memberJoinTimes = new HashMap<>();
    private int wins = 0;
    private int losses = 0;
    private UUID leaderId;

    /**
     * Creates a new party with the specified leader.
     *
     * @param leader The UUID of the party leader
     */
    public Party(Player leader) {
        plugin.getPartyManager().getParties().add(this);
        createLeader(leader);
    }

    private void createLeader(Player leader) {
        leaderId = leader.getUniqueId();
        members.add(leaderId);
        memberJoinTimes.put(leader.getUniqueId(), System.currentTimeMillis());
        DuelsPlayer player = plugin.getPlayerManager().getPlayer(leader);
        player.setParty(this);
        player.setLayout(HubLayout.PARTY_LEADER);
        plugin.utils.message(leader, "<green>Party created! Use /party invite <player> to invite others.");
    }

    /**
     * Disbands the party and cleans up all references.
     */
    public void disband() {
        plugin.utils.message(getOnlineMembers(leaderId), "<red>The party has been disbanded!");
        Player leader = Bukkit.getPlayer(leaderId);
        if (leader != null && leader.isOnline() && members.contains(leaderId)) {
            plugin.utils.message(leader, "<green>Your party has been disbanded.");
        }

        plugin.getPartyManager().getParties().remove(this);
        members.stream().map(member -> plugin.getPlayerManager().getPlayer(member)).forEach(member -> {
            member.setParty(null);
            member.setLayout(HubLayout.DEFAULT);
        });
        members.clear();
        invitations.forEach(PartyInvite::destroy);
        requests.forEach(PartyDuelRequest::destroy);

        leaderId = null;
    }

    //region [Player methods]
    /**
     * Adds a player to the party.
     *
     * @param player The UUID of the player to add
     */
    public void addMember(Player player) {
        if (members.add(player.getUniqueId())) {
            DuelsPlayer duelsPlayer = plugin.getPlayerManager().getPlayer(player);
            duelsPlayer.setParty(this);
            duelsPlayer.setLayout(HubLayout.PARTY_MEMBER);
            plugin.utils.message(getOnlineMembers(player), "<green>" + player.getName() + " has joined the party!");
            plugin.utils.message(player, "<green>You have joined the party!");
            memberJoinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            plugin.utils.message(player, "<red>You are already in this party!");
        }
    }

    /**
     * Removes a player from the party. If the player is the leader,
     * leadership will be transferred to another random member.
     *
     * @param playerId The UUID of the player to remove
     */
    public void removeMember(UUID playerId, boolean kicked) {
        DuelsPlayer player = plugin.getPlayerManager().getPlayer(playerId);
        members.remove(playerId);
        memberJoinTimes.remove(playerId);
        player.setParty(null);
        player.setLayout(HubLayout.DEFAULT);

        String playerName = Bukkit.getOfflinePlayer(playerId).getName();

        if (members.isEmpty()) {
            disband();
        } else if (isLeader(playerId)) {
            UUID newLeaderId = members.iterator().next();
            leaderId = newLeaderId;
            String newLeaderName = Bukkit.getOfflinePlayer(newLeaderId).getName();

            plugin.utils.message(getOnlineMembers(newLeaderId), "<yellow>" + playerName + " has left the party. " + newLeaderName + " is now the party leader!");
            Player newLeader = Bukkit.getPlayer(newLeaderId);
            if (newLeader != null && newLeader.isOnline()) {
                plugin.utils.message(newLeader, "<yellow>" + playerName + " has left the party. You are now the party leader.");
                getLeaderDuelsPlayer().setLayout(HubLayout.PARTY_LEADER);
            }
        } else {
            if (kicked) {
                plugin.utils.message(getOnlineMembers(leaderId), "<red>" + playerName + " was kicked from the party!");
            } else {
                plugin.utils.message(getOnlineMembers(), "<red>" + playerName + " has left the party!");
            }
        }
    }

    //TODO - public parties

    /**
     * Promotes a member to be the new party leader.
     *
     * @param newLeaderId The UUID of the new leader
     */
    public void promoteLeader(UUID newLeaderId) {
        getLeaderDuelsPlayer().setLayout(HubLayout.PARTY_MEMBER);
        leaderId = newLeaderId;
        plugin.utils.message(getOnlineMembers(newLeaderId), "<gold>" + Bukkit.getOfflinePlayer(newLeaderId).getName() + " is now the party leader!");
        Player player = Bukkit.getPlayer(newLeaderId);
        if (player != null && player.isOnline()) {
            getLeaderDuelsPlayer().setLayout(HubLayout.PARTY_LEADER);
            plugin.utils.message(player, "<gold>You are now the party leader.");
        }
    }
    //endregion

    //region [Utility methods]
    /**
     * Gets the number of members in the party.
     *
     * @return The number of members
     */
    public int getSize() {
        return members.size();
    }
    
    /**
     * Checks if a player is a member of this party.
     *
     * @param player Player to check
     * @return true if the player is a member
     */
    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }
    
    /**
     * Checks if a player is a member of this party.
     *
     * @param playerId UUID of player to check
     * @return true if the player is a member
     */
    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    /**
     * Gets all online members of the party excluding specific players.
     *
     * @param excludeIds Array of UUIDs to exclude
     * @return List of online Player objects
     */
    public List<Player> getOnlineMembers(List<UUID> excludeIds) {
        return members.stream().filter(member -> !excludeIds.contains(member)).map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline).toList();
    }

    /**
     * Gets all online members of the party.
     *
     * @return List of online Player objects
     */
    public List<Player> getOnlineMembers() {
        return getOnlineMembers(Collections.emptyList());
    }

    public Set<UUID> getOnlineMembersIds() {
        return getOnlineMembers().stream().map(Player::getUniqueId).collect(Collectors.toSet());
    }

    /**
     * Gets all online members of the party excluding specific players.
     *
     * @param excludePlayers Array of Players to exclude
     * @return List of online Player objects
     */
    public List<Player> getOnlineMembers(Player... excludePlayers) {
        return getOnlineMembers(Arrays.stream(excludePlayers).filter(Objects::nonNull).map(Player::getUniqueId).toList());
    }

    public List<Player> getOnlineMembers(UUID... excludeIds) {
        return getOnlineMembers(Arrays.asList(excludeIds));
    }

    /**
     * Checks if a player is the leader of this party.
     *
     * @param player Player to check
     * @return true if the player is the leader
     */
    public boolean isLeader(Player player) {
        return isLeader(player.getUniqueId());
    }

    /**
     * Checks if a player is the leader of this party.
     *
     * @param playerId UUID of player to check
     * @return true if the player is the leader
     */
    public boolean isLeader(UUID playerId) {
        return leaderId.equals(playerId);
    }

    /**
     * Gets the leader as an online Player object.
     *
     * @return The leader as Player, or null if offline
     */
    @Nullable
    public Player getLeaderPlayer() {
        return Bukkit.getPlayer(leaderId);
    }

    public OfflinePlayer getLeaderOfflinePlayer() {
        return Bukkit.getOfflinePlayer(leaderId);
    }

    /**
     * Gets the DuelsPlayer instance for the party leader.
     *
     * @return The DuelsPlayer of the leader, or null if offline
     */
    public DuelsPlayer getLeaderDuelsPlayer() {
        return plugin.getPlayerManager().getPlayer(leaderId);
    }

    public int getTotalDuels() {
        return wins + losses;
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }
    //endregion
}
