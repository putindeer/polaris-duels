package us.polarismc.polarisduels.duel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.player.DuelsPlayer;
import us.polarismc.polarisduels.queue.KitType;

import java.util.*;

public class DuelManager {
    private final Main plugin;

    public DuelManager(Main plugin) {
        this.plugin = plugin;
    }

    private final Map<UUID, List<DuelRequest>> requests = new HashMap<>();

    public void sendRequest(Player requested, Player requestor, KitType kit, int rounds) {
        if (!canRequestorSendDuel(requested, requestor)) return;

        List<DuelRequest> playerRequests = requests.get(requested.getUniqueId());
        if (playerRequests != null && playerRequests.stream().anyMatch(duelRequest -> duelRequest.requestor().equals(requestor))) {
            plugin.utils.message(requestor, "&cYou already have a pending duel request to &b" + requested.getName() + "&c.");
            return;
        }

        requests.computeIfAbsent(requested.getUniqueId(), k -> new ArrayList<>())
                .add(new DuelRequest(requested, requestor, kit, rounds));

        //TODO - Hacer un método que funcione con TextComponent así no tengo que hacer este desastre para mandar un mensaje con click
        TextComponent message = Component.text("§b" + requestor.getName() + " has challenged you to a duel with " + kit + " for " + rounds + " rounds.\n");
        TextComponent accept = Component.text("§a/duel accept " + requestor.getName()).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + requestor.getName()));
        TextComponent deny = Component.text("§c/duel deny " + requestor.getName()).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/duel deny " + requestor.getName()));

        message = message.append(Component.text("§7Type "))
                .append(accept)
                .append(Component.text(" §7or "))
                .append(deny)
                .append(Component.text(" §7to respond."));

        plugin.utils.message(requestor, "&aYou have challenged " + requested.getName() + " to a duel with " + kit + " for " + rounds + " rounds.");
        requested.sendMessage(message);
    }

    public void acceptDuel(Player requested, Player requestor) {
        List<DuelRequest> playerRequests = requests.get(requested.getUniqueId());
        if (playerRequests == null || playerRequests.isEmpty()) {
            plugin.utils.message(requested, "&cYou have no pending duel requests.");
            return;
        }

        DuelRequest request = getRequest(requested, requestor);

        if (request == null) {
            plugin.utils.message(requested, "&cYou have no duel request from &b" + requestor.getName() + "&c.");
            return;
        }

        if (!canRequestedAcceptDuel(requested, requestor)) return;

        plugin.utils.message(requested, "&aYou accepted the duel request from: &b" + requestor.getName());
        plugin.utils.message(requestor, "&b" + requested.getName() + " &aaccepted your duel request!");
        playerRequests.remove(request);
        createDuel(request);
    }

    public void denyDuel(Player requested, Player requestor) {
        List<DuelRequest> playerRequests = requests.get(requested.getUniqueId());
        if (playerRequests == null || playerRequests.isEmpty()) {
            plugin.utils.message(requested, "&cYou have no pending duel requests.");
            return;
        }

        DuelRequest request = getRequest(requested, requestor);

        if (request == null) {
            plugin.utils.message(requested, "&cYou have no duel request from &b" + requestor.getName() + "&c.");
            return;
        }

        playerRequests.remove(request);

        plugin.utils.message(requested, "&cYou denied the duel request from: &b" + requestor.getName());
        plugin.utils.message(requestor, "&c" + requested.getName() + " denied your duel request.");
    }

    public void listRequests(Player player) {
        List<DuelRequest> playerRequests = requests.get(player.getUniqueId());

        if (playerRequests == null || playerRequests.isEmpty()) {
            plugin.utils.message(player, "&cYou have no pending duel requests.");
        } else {
            plugin.utils.message(player, "&cYou have the following pending duel requests:");

            for (DuelRequest duelRequest : playerRequests) {
                String requestorName = duelRequest.requestor().getName();
                String kitName = duelRequest.kit().toString();
                int rounds = duelRequest.rounds();

                plugin.utils.message(player, "&b" + requestorName
                        + " &7| &e" + kitName
                        + " &7| &6" + rounds + " rounds");
            }
        }
    }

    private void createDuel(DuelRequest request) {
        Optional<ArenaEntity> arena = plugin.getArenaManager().findOpenArena(request.kit(), 2, request.rounds());
        if (arena.isPresent()) {
            arena.get().addPlayer(request.requested(), plugin);
            arena.get().addPlayer(request.requestor(), plugin);
        } else {
            plugin.utils.message(request.requested(), "&cThere are no arenas open. Try again in a bit.");
            plugin.utils.message(request.requestor(), "&cThere are no arenas open. Try again in a bit.");
        }
    }

    private boolean canRequestorSendDuel(Player requested, Player requestor) {
        DuelsPlayer dpRequestor = plugin.getPlayerManager().getDuelsPlayer(requestor);
        if (dpRequestor.isQueue()) {
            plugin.utils.message(requestor, "&cYou can't send a duel request while you're in queue.");
            return false;
        }
        if (dpRequestor.isDuel()) {
            plugin.utils.message(requestor, "&cYou can't send a duel request while you're dueling.");
            return false;
        }
        DuelsPlayer dpRequested = plugin.getPlayerManager().getDuelsPlayer(requested);
        if (dpRequested.isQueue()) {
            plugin.utils.message(requestor, "&cYou can't send a duel request to someone who is in queue.");
            return false;
        }
        if (dpRequested.isDuel()) {
            plugin.utils.message(requestor, "&cYou can't send a duel request to someone who is dueling.");
            return false;
        }
        return true;
    }

    private boolean canRequestedAcceptDuel(Player requested, Player requestor) {
        DuelsPlayer dpRequested = plugin.getPlayerManager().getDuelsPlayer(requested);
        if (dpRequested.isQueue()) {
            plugin.utils.message(requested, "&cYou can't accept a duel request while you're in queue.");
            return false;
        }
        if (dpRequested.isDuel()) {
            plugin.utils.message(requested, "&cYou can't accept a duel request while you're already dueling.");
            return false;
        }
        DuelsPlayer dpRequestor = plugin.getPlayerManager().getDuelsPlayer(requestor);
        if (dpRequestor.isQueue()) {
            plugin.utils.message(requested, "&cYou can't accept a duel request from someone who is in queue.");
            return false;
        }
        if (dpRequestor.isDuel()) {
            plugin.utils.message(requested, "&cYou can't accept a duel request from someone who is currently dueling.");
            return false;
        }
        return true;
    }

    public DuelRequest getRequest(Player requested, Player requestor) {
        List<DuelRequest> playerRequests = requests.get(requested.getUniqueId());
        if (playerRequests != null) {
            return playerRequests.stream()
                    .filter(duelRequest -> duelRequest.requestor().equals(requestor))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
