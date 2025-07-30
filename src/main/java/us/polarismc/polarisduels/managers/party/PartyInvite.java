package us.polarismc.polarisduels.managers.party;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

import java.util.UUID;

/**
 * Represents an invitation to join a party.
 * Handles the invitation lifecycle including expiration and acceptance.
 */
@Getter
public class PartyInvite {
    private final Main plugin = Main.getInstance();
    private final Party party;
    private final UUID senderId;
    private final UUID targetId;
    private BukkitTask task;
    private final long EXPIRE_AFTER = 5 * 60 * 20;

    /**
     * Creates a new party invitation.
     *
     * @param party The party you're being invited to
     * @param sender The player who sent the invite
     * @param target The player being invited
     */
    public PartyInvite(Party party, Player sender, Player target) {
        this.party = party;
        this.senderId = sender.getUniqueId();
        this.targetId = target.getUniqueId();
        invite();
    }

    public void invite() {
        if (pendingInvite()) return;
        DuelsPlayer target = plugin.getPlayerManager().getPlayer(targetId);
        target.getPartyInvites().add(this);
        party.getInvitations().add(this);
        sendInviteNotification();
        task = Bukkit.getScheduler().runTaskLater(plugin, this::expire, EXPIRE_AFTER);
    }

    public boolean pendingInvite() {
        DuelsPlayer target = plugin.getPlayerManager().getPlayer(targetId);
        Player sender = Bukkit.getPlayer(senderId);
        if (target.getPartyInvites().stream().anyMatch(invite -> invite.getParty().equals(this.getParty()))) {
            plugin.utils.message(sender, "<red>" + target.getName() + " already has a pending invite!");
            return true;
        } else return false;
    }

    public void sendInviteNotification() {
        Player target = Bukkit.getPlayer(targetId);
        Player sender = Bukkit.getPlayer(senderId);
        
        if (target != null && target.isOnline() && sender != null && sender.isOnline()) {
            String accept = "<green>" + plugin.utils.clickableCommand("/party accept " + sender.getName()) + "</green>";
            plugin.utils.message(target, "<green>You have been invited to join " + sender.getName() + "'s party!",
                    "<gray>Type " + accept + " to join.",
                    "<gray>This invite will expire in 5 minutes.");
            plugin.utils.message(sender, "<green>Invite sent to " + target.getName() + "!");
        }
    }

    public void accept() {
        Player target =  Bukkit.getPlayer(targetId);
        if (target == null) return;
        party.addMember(target);
        this.destroy();
    }

    public void decline() {
        Player target = Bukkit.getPlayer(targetId);
        assert target != null;
        plugin.utils.message(target, "<red>You have declined the party invitation.");
        Player leader = party.getLeaderPlayer();
        if (leader != null && leader.isOnline()) {
            plugin.utils.message(leader, "<red>" + target.getName() + " declined your party invitation.");
        }
        this.destroy();
    }

    public void expire() {
        DuelsPlayer target = plugin.getPlayerManager().getPlayer(targetId);
        DuelsPlayer sender = plugin.getPlayerManager().getPlayer(senderId);

        if (target.isOnline()) {
            plugin.utils.message(target.getPlayer(), "<red>The party invitation from " + sender.getName() + " has expired.");
        }
        if (sender.isOnline()) {
            plugin.utils.message(sender.getPlayer(), "<red>The party invitation you sent to " + target.getName() + " has expired.");
        }
        this.destroy();
    }

    public void destroy() {
        if (!task.isCancelled()) {
            task.cancel();
        }
        DuelsPlayer target = plugin.getPlayerManager().getPlayer(targetId);
        target.getPartyInvites().remove(this);
        party.getInvitations().remove(this);
        task = null;
    }
}
