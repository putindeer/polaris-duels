package us.polarismc.polarisduels.managers.party;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.game.KitType;

import java.util.UUID;

@Getter
public class PartyDuelRequest {
    private final Main plugin = Main.getInstance();
    private final Party targetParty;
    private final Party senderParty;
    private final KitType kit;
    private final int rounds;
    private final UUID senderId;
    private BukkitTask task;
    private final long EXPIRE_AFTER = 5 * 60 * 20;

    public PartyDuelRequest(Party targetParty, Party senderParty, KitType kit, int rounds) {
        this.targetParty = targetParty;
        this.senderParty = senderParty;
        this.kit = kit;
        this.rounds = rounds;
        this.senderId = senderParty.getLeaderId();

        this.send();
    }

    public void send() {
        targetParty.getRequests().add(this);
        senderParty.getRequests().add(this);

        sendDuelNotification();
        this.task = Bukkit.getScheduler().runTaskLater(plugin, this::expire, EXPIRE_AFTER);
    }

    public void sendDuelNotification() {
        String senderName = senderParty.getLeaderDuelsPlayer().getName();
        String accept = "<green>" + plugin.utils.clickableCommand("/party duel accept " + senderName) + "</green>";
        String decline = "<red>" + plugin.utils.clickableCommand("/party duel decline " + senderName) + "</red>";
        plugin.utils.message(targetParty.getOnlineMembers(),
                "<gold>Your party has received a duel request from " + senderName + "'s party!",
                "<gray>Kit: <white>" + kit.name() + " <gray>| Rounds: <white>" + rounds,
                "<gray>Use " + accept + " to accept or " + decline + " to decline.");

        plugin.utils.message(Bukkit.getPlayer(senderId),
                "<green>Duel request sent to " + targetParty.getLeaderDuelsPlayer().getName() + "'s party!");
    }

    public void accept() {
        String targetLeaderName = targetParty.getLeaderDuelsPlayer().getName();
        String senderLeaderName = senderParty.getLeaderDuelsPlayer().getName();

        plugin.utils.message(targetParty.getOnlineMembers(),
                "<green>Your party has accepted the duel request from " + senderLeaderName + "'s party!");
        plugin.utils.message(senderParty.getOnlineMembers(),
                "<green>" + targetLeaderName + "'s party has accepted your duel request!");

        plugin.getPartyManager().createPartyDuel(this);
        this.destroy();
    }

    public void decline() {
        String targetLeaderName = targetParty.getLeaderDuelsPlayer().getName();
        String senderLeaderName = senderParty.getLeaderDuelsPlayer().getName();

        plugin.utils.message(targetParty.getOnlineMembers(),
                "<red>Your party has declined the duel request from " + senderLeaderName + "'s party.");
        plugin.utils.message(senderParty.getOnlineMembers(),
                "<red>" + targetLeaderName + "'s party has declined your duel request.");

        this.destroy();
    }

    private void expire() {
        String targetLeaderName = targetParty.getLeaderDuelsPlayer().getName();
        String senderLeaderName = senderParty.getLeaderDuelsPlayer().getName();

        plugin.utils.message(targetParty.getOnlineMembers(),
                "<yellow>The duel request from " + senderLeaderName + "'s party has expired.");
        plugin.utils.message(senderParty.getOnlineMembers(),
                "<yellow>Your duel request to " + targetLeaderName + "'s party has expired.");

        this.destroy();
    }

    public void destroy() {
        if (!task.isCancelled()) {
            task.cancel();
        }
        targetParty.getRequests().remove(this);
        senderParty.getRequests().remove(this);
        task = null;
    }
}