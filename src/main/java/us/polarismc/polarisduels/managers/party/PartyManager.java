package us.polarismc.polarisduels.managers.party;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.game.GameType;
import us.polarismc.polarisduels.managers.duel.DuelTeam;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;
import us.polarismc.polarisduels.managers.player.PlayerManager;
import us.polarismc.polarisduels.game.KitType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all parties and provides utility methods for party operations.
 */
@SuppressWarnings("unused")
public class PartyManager implements Listener {
    private final Main plugin;
    @Getter
    private final Set<Party> parties = ConcurrentHashMap.newKeySet();
    private final PlayerManager playerManager;

    public PartyManager(Main plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        new PartyCommands(plugin, this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates a new party with the specified leader.
     *
     * @param creator The player that is creating the party
     */
    public void createParty(Player creator) {
        if (hasParty(creator)) {
            plugin.utils.message(creator, "<red>You are already in a party!");
            return;
        }

        Party party = new Party(creator);
    }

    /**
     * Removes a player from their current party.
     *
     * @param playerId The UUID of the player to remove
     */
    public void removeFromParty(UUID playerId) {
        Party party = getParty(playerId);
        party.removeMember(playerId, false);
    }

    public void kickFromParty(UUID playerId) {
        Party party = getParty(playerId);
        party.removeMember(playerId, true);
    }

    /**
     * Gets the party a player is in.
     *
     * @param playerId The UUID of the player
     * @return The Party the player is in, or null if not in a party
     */
    public Party getParty(UUID playerId) {
        DuelsPlayer player = playerManager.getPlayer(playerId);
        return player != null ? player.getParty() : null;
    }

    public Party getParty(OfflinePlayer player) {
        return getParty(player.getUniqueId());
    }

    /**
     * Checks if a player is in a party.
     *
     * @param playerId The UUID of the player to check
     * @return true if the player is in a party, false otherwise
     */
    public boolean hasParty(UUID playerId) {
        DuelsPlayer player = playerManager.getPlayer(playerId);
        return player != null && player.getParty() != null;
    }

    public boolean hasParty(OfflinePlayer player) {
        return hasParty(player.getUniqueId());
    }

    public boolean areInTheSameParty(UUID player1Id, UUID player2Id) {
        Party party = getParty(player1Id);
        return party != null && party.equals(getParty(player2Id));
    }

    public boolean areInTheSameParty(OfflinePlayer player1, OfflinePlayer player2) {
        return areInTheSameParty(player1.getUniqueId(), player2.getUniqueId());
    }

    /**
     * Sends a party invite from one player to another.
     *
     * @param sender The player sending the invite
     * @param target The player receiving the invite
     */
    public void sendInvite(Player sender, Player target) {
        if (sender == null || target == null) {
            return;
        }

        Party party = getParty(sender);
        if (party == null || !party.isLeader(sender)) {
            plugin.utils.message(sender, "<red>Only the party leader can invite new members!");
            return;
        }

        new PartyInvite(party, sender, target);
    }

    /**
     * Accepts a pending party invite.
     *
     * @param player The player accepting the invite
     * @param inviterId The inviter's UUID
     */
    public void acceptInvite(Player player, UUID inviterId) {
        DuelsPlayer duelsPlayer = playerManager.getPlayer(player);

        Optional<PartyInvite> invite = duelsPlayer.getPartyInvites().stream().filter(i -> i.getSenderId().equals(inviterId)).findFirst();

        if (invite.isEmpty()) {
            plugin.utils.message(player, "<red>You don't have a party invite from that player!");
        } else {
            invite.get().accept();
        }
    }

    public void acceptInvite(Player player, String inviterName) {
        acceptInvite(player, Bukkit.getOfflinePlayer(inviterName).getUniqueId());
    }

    /**
     * Declines a pending party invite.
     *
     * @param player The player accepting the invite
     * @param inviterId The inviter's UUID
     */
    public void declineInvite(Player player, UUID inviterId) {
        DuelsPlayer duelsPlayer = playerManager.getPlayer(player);

        Optional<PartyInvite> invite = duelsPlayer.getPartyInvites().stream().filter(i -> i.getSenderId().equals(inviterId)).findFirst();

        if (invite.isEmpty()) {
            plugin.utils.message(player, "<red>You don't have a party invite from that player!");
        } else {
            invite.get().decline();
        }
    }

    public void declineInvite(Player player, String inviterName) {
        declineInvite(player, Bukkit.getOfflinePlayer(inviterName).getUniqueId());
    }

    /**
     * Gets all online members of a player's party.
     *
     * @param playerId The UUID of the player
     * @return List of online party members, or empty list if not in a party
     */
    public List<Player> getOnlinePartyMembers(UUID playerId) {
        Party party = getParty(playerId);
        return party != null ? party.getOnlineMembers() : Collections.emptyList();
    }

    /**
     * Creates a new duel by finding an available arena and adding both players.
     *
     * @param request The duel request containing all necessary duel parameters
     */
    public void createPartyDuel(PartyDuelRequest request) {
        List<Player> targetPlayers = request.getTargetParty().getOnlineMembers();
        List<Player> senderPlayers = request.getSenderParty().getOnlineMembers();
        List<Player> players = new ArrayList<>();
        players.addAll(targetPlayers);
        players.addAll(senderPlayers);

        Set<UUID> targetUUIDs = request.getTargetParty().getOnlineMembersIds();
        Set<UUID> senderUUIDs = request.getSenderParty().getOnlineMembersIds();
        Set<UUID> playerUUIDs = new HashSet<>();
        playerUUIDs.addAll(targetPlayers.stream().map(Player::getUniqueId).toList());
        playerUUIDs.addAll(senderPlayers.stream().map(Player::getUniqueId).toList());

        GameSession session = GameSession.builder().players(playerUUIDs).gameType(GameType.PARTY_VS_PARTY).rounds(request.getRounds()).kit(request.getKit()).build();
        DuelTeam one = new DuelTeam(session.getScoreboard(), targetUUIDs, session.findNextAvailableColor());
        session.getTeams().add(one);
        DuelTeam two = new DuelTeam(session.getScoreboard(), senderUUIDs, session.findNextAvailableColor());
        session.getTeams().add(two);

        Optional<ArenaEntity> arena = plugin.getArenaManager().assignArena(session);
        if (arena.isEmpty()) {
            plugin.utils.message(players, "&cThere are no arenas open. Try again in a bit.");
        }
    }

    public void createPartyFFA(Party party, KitType kit, int rounds) {
        Optional<DuelsPlayer> inDuel = party.getOnlineMembers().stream().map(player -> plugin.getPlayerManager().getPlayer(player))
                .filter(player -> player.isDuel() || player.isQueue()).findFirst();

        if (inDuel.isPresent()) {
            plugin.utils.message(party.getOnlineMembers(), "&cSomeone from your party is in a duel or queue. Please wait until they end their game before starting a party FFA.");
            return;
        }

        Set<UUID> playerList = party.getOnlineMembersIds();
        GameSession session = GameSession.builder().players(playerList).gameType(GameType.PARTY_FFA).rounds(rounds).kit(kit).build();

        Optional<ArenaEntity> arena = plugin.getArenaManager().assignArena(session);
        List<DuelTeam> teams = playerList.stream().map(uuid -> new DuelTeam(session.getScoreboard(), Collections.singleton(uuid))).toList();
        session.getTeams().addAll(teams);
        if (arena.isEmpty()) {
            plugin.utils.message(party.getOnlineMembers(), "&cThere are no arenas open. Try again in a bit.");
        }
    }

    private final Map<UUID, Integer> pendingKicks = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!hasParty(player)) return;
        Party party = getParty(player);

        scheduleAutoKick(player.getUniqueId(), party);

        plugin.utils.message(party.getOnlineMembers(), "<yellow>" + player.getName() + " disconnected. They will get automatically kicked in 5 minutes if they don't join back.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (pendingKicks.containsKey(playerId)) {
            int taskId = pendingKicks.remove(playerId);
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void scheduleAutoKick(UUID playerId, Party party) {
        plugin.utils.log("Scheduled kick for " + playerId + " in 5 minutes");
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!Bukkit.getOfflinePlayer(playerId).isOnline()) {
                if (hasParty(playerId)) {
                    kickFromParty(playerId);
                }
            }
            pendingKicks.remove(playerId);
        }, 20 * 60 * 5).getTaskId();

        pendingKicks.put(playerId, taskId);
    }
}
