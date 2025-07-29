package us.polarismc.polarisduels.managers.party;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;
import us.polarismc.polarisduels.game.KitType;

import java.util.*;


/**
 * Handles all party-related commands.
 */
public class PartyCommands implements TabExecutor {
    private final Main plugin;
    private final PartyManager pm;
    //TODO - add tab executor

    public PartyCommands(Main plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.pm = partyManager;
        Objects.requireNonNull(plugin.getCommand("party")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player);
            case "disband" -> handleDisband(player);
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player, args);
            case "decline" -> handleDecline(player, args);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "promote" -> handlePromote(player, args);
            case "info" -> handleInfo(player);
            case "chat" -> handleChat(player, args);
            case "duel" -> handleDuel(player, args);
            case "requests" -> handleRequests(player);
            case "ffa" -> handleFFA(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleCreate(Player player) {
        pm.createParty(player);
    }

    private void handleDisband(Player player) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        Party party = pm.getParty(player);

        if (!pm.getParty(player).isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can disband the party!");
            return;
        }

        party.disband();
    }

    private void handleInvite(Player player, String[] args) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        if (args.length < 2) {
            plugin.utils.message(player, "<red>Usage: /party invite <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.utils.message(player, "<red>This player is offline.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.utils.message(player, "<red>You cannot invite yourself!");
            return;
        }

        pm.sendInvite(player, target);
    }

    private void handleAccept(Player player, String[] args) {
        if (pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You already are on a party!");
            return;
        }

        if (args.length < 2) {
            Set<PartyInvite> invites = plugin.getPlayerManager().getPlayer(player).getPartyInvites();
            if (invites.size() == 1) {
                invites.stream().findFirst().ifPresent(invite -> pm.acceptInvite(player, invite.getSenderId()));
            } else if (invites.isEmpty()) {
                plugin.utils.message(player, "<red>You don't have any pending invites!");
            } else {
                plugin.utils.message(player, "<red>You have more than one pending invite! Use /party accept <player> instead.");
            }
            return;
        }

        pm.acceptInvite(player, args[1]);
    }

    private void handleDecline(Player player, String[] args) {
        if (args.length < 2) {
            Set<PartyInvite> invites = plugin.getPlayerManager().getPlayer(player).getPartyInvites();
            if (invites.size() == 1) {
                invites.stream().findFirst().ifPresent(invite -> pm.declineInvite(player, invite.getSenderId()));
            } else if (invites.isEmpty()) {
                plugin.utils.message(player, "<red>You don't have any pending invites!");
            } else {
                plugin.utils.message(player, "<red>You have more than one pending invite! Use /party decline <player> instead.");
            }
            return;
        }

        pm.declineInvite(player, args[1]);
    }

    private void handleLeave(Player player) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player.getPlayer(), "<red>You are not in a party!");
            return;
        }
        pm.removeFromParty(player.getUniqueId());
        plugin.utils.message(player, "<green>You have left the party.");
    }

    private void handleKick(Player player, String[] args) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        if (args.length < 2) {
            plugin.utils.message(player, "<red>Usage: /party kick <player>");
            return;
        }

        Party party = pm.getParty(player);
        if (!party.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can kick players!");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.utils.message(player, "<red>You cannot kick yourself!");
            return;
        }

        if (!pm.hasParty(target) || !pm.areInTheSameParty(target, player)) {
            plugin.utils.message(player, "<red>That player is not in your party!");
            return;
        }

        pm.kickFromParty(target.getUniqueId());
        if (target.isOnline()) {
            plugin.utils.message(Bukkit.getPlayer(target.getUniqueId()), "<red>You have been kicked from the party!");
        }
        plugin.utils.message(player, "<green>You have kicked " + target.getName() + " from the party.");
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            plugin.utils.message(player, "<red>Usage: /party promote <player>");
            return;
        }

        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        Party party = pm.getParty(player);
        if (!party.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can promote members!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.utils.message(player, "<red>Player not found or is offline.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.utils.message(player, "<red>You are already the leader!");
            return;
        }

        if (!pm.hasParty(target) || !pm.areInTheSameParty(target, player)) {
            plugin.utils.message(player, "<red>That player is not in your party!");
            return;
        }

        party.promoteLeader(target.getUniqueId());
    }

    private void handleInfo(Player player) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        plugin.utils.message(player, "<gold><bold>Party Information:",
                "<gray>Members: <reset>" + pm.getParty(player.getUniqueId()).getSize(),
                "<gray>Leader: <reset>" + Bukkit.getOfflinePlayer(pm.getParty(player.getUniqueId()).getLeaderId()).getName());
    }

    private void handleChat(Player player, String[] args) {
        if (args.length < 2) {
            plugin.utils.message(player, "&cUsage: /party chat <message>");
            return;
        }

        if (!pm.hasParty(player.getUniqueId())) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        //TODO - rehacer esto a un sistema por channels

        Component builder = Component.empty();
        for (int i = 1; i < args.length; i++) {
            if (builder.equals(Component.empty())) builder = Component.text(args[i]);
            else builder = builder.append(Component.text(" ").append(Component.text(args[i])));
        }

        final Component message = builder;

        //TODO - check this, player's messages could have click events (dangerous)
        Component formattedMessage = plugin.utils.chat(String.format("<light_purple>[Party] " + player.getName() + ":</light_purple> <white>", player.getName()));

        pm.getParty(player).getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline)
                .forEach(p -> plugin.utils.message(p, false, formattedMessage.append(message)));
    }

    private void handleDuel(Player player, String[] args) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        if (args.length < 2) {
            plugin.utils.message(player, "<red>Usage: /party duel <send/accept/decline> [player/kit] [rounds]");
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "send" -> handleDuelSend(player, args);
            case "accept" -> handleDuelAccept(player, args);
            case "decline" -> handleDuelDecline(player, args);
            default -> plugin.utils.message(player, "<red>Usage: /party duel <send/accept/decline> [player/kit] [rounds]");
        }
    }

    private void handleDuelSend(Player player, String[] args) {
        if (args.length < 4) {
            plugin.utils.message(player, "<red>Usage: /party duel send <player> <kit> [rounds]");
            return;
        }

        Party senderParty = pm.getParty(player);
        if (!senderParty.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can send duel requests!");
            return;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            plugin.utils.message(player, "<red>Player not found or is offline.");
            return;
        }

        if (!pm.hasParty(target)) {
            plugin.utils.message(player, "<red>That player is not in a party!");
            return;
        }

        Party targetParty = pm.getParty(target);
        if (targetParty.equals(senderParty)) {
            plugin.utils.message(player, "<red>You cannot duel your own party!");
            return;
        }

        // Verificar si ya existe una request pendiente
        boolean hasExistingRequest = senderParty.getRequests().stream()
                .anyMatch(request -> request.getTargetParty().equals(targetParty));

        if (hasExistingRequest) {
            plugin.utils.message(player, "<red>You already have a pending duel request to that party!");
            return;
        }

        Optional<DuelsPlayer> inDuel = senderParty.getOnlineMembers().stream().map(dp -> plugin.getPlayerManager().getPlayer(dp))
                .filter(dp -> dp.isDuel() || dp.isQueue()).findFirst();

        if (inDuel.isPresent()) {
            plugin.utils.message(senderParty.getOnlineMembers(), "&cSomeone from your party is in a duel or queue. Please wait until they end their game before sending a party duel request.");
            return;
        }

        KitType kit;
        try {
            kit = KitType.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.utils.message(player, "<red>Invalid kit type! Available kits: " + String.join(", ",
                    Arrays.stream(KitType.values()).map(Enum::name).toArray(String[]::new)));
            return;
        }

        int rounds = kit.getDefaultRounds();
        if (args.length > 4) {
            try {
                rounds = Integer.parseInt(args[4]);
                if (rounds < 1 || rounds > 100) {
                    plugin.utils.message(player, "<red>Rounds must be between 1 and 9!");
                    return;
                }
            } catch (NumberFormatException e) {
                plugin.utils.message(player, "<red>Invalid rounds number!");
                return;
            }
        }

        new PartyDuelRequest(targetParty, senderParty, kit, rounds);
    }

    private void handleDuelAccept(Player player, String[] args) {
        if (args.length < 3) {
            plugin.utils.message(player, "<red>Usage: /party duel accept <player>");
            return;
        }

        Party party = pm.getParty(player);
        if (!party.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can accept duel requests!");
            return;
        }

        PartyDuelRequest request = checkDuelRequest(args, player, party);
        if (request == null) return;

        Optional<DuelsPlayer> inDuel = party.getOnlineMembers().stream().map(dp -> plugin.getPlayerManager().getPlayer(dp))
                .filter(dp -> dp.isDuel() || dp.isQueue()).findFirst();

        if (inDuel.isPresent()) {
            plugin.utils.message(party.getOnlineMembers(), "&cSomeone from your party is in a duel or queue. Please wait until they end their game before accepting a party duel request.");
            return;
        }

        request.accept();
    }

    private void handleDuelDecline(Player player, String[] args) {
        if (args.length < 3) {
            plugin.utils.message(player, "<red>Usage: /party duel decline <player>");
            return;
        }

        Party party = pm.getParty(player);
        if (!party.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can decline duel requests!");
            return;
        }

        PartyDuelRequest request = checkDuelRequest(args, player, party);
        if (request == null) return;

        request.decline();
    }

    private PartyDuelRequest checkDuelRequest(String[] args, Player player, Party party) {
        OfflinePlayer sender = Bukkit.getOfflinePlayer(args[2]);

        Optional<PartyDuelRequest> request = party.getRequests().stream()
                .filter(r -> r.getSenderId().equals(sender.getUniqueId()))
                .findFirst();

        if (request.isEmpty()) {
            plugin.utils.message(player, "<red>You don't have a duel request from that player!");
            return null;
        }

        return request.get();
    }

    private void handleRequests(Player player) {
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        Party party = pm.getParty(player);
        Set<PartyDuelRequest> requests = party.getRequests();

        if (requests.isEmpty()) {
            plugin.utils.message(player, "<yellow>Your party has no pending duel requests.");
            return;
        }

        plugin.utils.message(player, "<gold><bold>--- Pending Duel Requests ---");

        for (PartyDuelRequest request : requests) {
            String senderName = Bukkit.getOfflinePlayer(request.getSenderId()).getName();
            plugin.utils.message(player,
                    "<gray>• <white>" + senderName + " <gray>- Kit: <white>" + request.getKit().name() +
                            " <gray>- Rounds: <white>" + request.getRounds());
        }
    }

    private void handleFFA(Player player, String[] args) {
        if (args.length < 2) {
            plugin.utils.message(player, "<red>Usage: /party ffa <kit> [rounds]");
            return;
        }
        if (!pm.hasParty(player)) {
            plugin.utils.message(player, "<red>You are not in a party!");
            return;
        }

        Party party = pm.getParty(player);
        if (!party.isLeader(player)) {
            plugin.utils.message(player, "<red>Only the party leader can start a FFA duel!");
        }

        KitType kit;
        try {
            kit = KitType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.utils.message(player, "<red>Invalid kit type! Available kits: " + String.join(", ",
                    java.util.Arrays.stream(KitType.values()).map(Enum::name).toArray(String[]::new)));
            return;
        }

        int rounds = kit.getDefaultRounds();
        if (args.length > 2) {
            try {
                rounds = Integer.parseInt(args[2]);
                if (rounds < 1 || rounds > 100) {
                    plugin.utils.message(player, "<red>Rounds must be between 1 and 9!");
                    return;
                }
            } catch (NumberFormatException e) {
                plugin.utils.message(player, "<red>Invalid rounds number!");
                return;
            }
        }

        pm.createPartyFFA(party, kit, rounds);
    }

    private void sendHelp(Player player) {
        plugin.utils.message(player, false, "<green><bold>--- Party Commands ---",
                "<green>/party create <gray>- Create a new party",
                "<green>/party invite <player> <gray>- Invite a player to your party",
                "<green>/party accept <player> <gray>- Accept a party invite",
                "<green>/party decline <player> <gray>- Decline a party invite",
                "<green>/party leave <gray>- Leave your current party",
                "<green>/party kick <player> <gray>- Kick a player from your party",
                "<green>/party promote <player> <gray>- Promote a player to party leader",
                "<green>/party info <gray>- View party information",
                "<green>/party chat <message> <gray>- Send a message to your party",
                "<green>/party disband <gray>- Disband your party (leader only)",
                "<green>/party duel send <player> <kit> [rounds] <gray>- Send a duel request",
                "<green>/party duel accept <player> <gray>- Accept a duel request",
                "<green>/party duel decline <player> <gray>- Decline a duel request",
                "<green>/party requests <gray>- View pending duel requests",
                "<green>/party ffa <kit> [rounds] <gray>- Start a Party FFA");
    }

    private final List<String> kits = List.of("SMP", "AXE", "NETHPOT", "UHC", "DIAMONDPOT", "SWORD", "MACE", "MARLOWUHC");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player) || !command.getName().equalsIgnoreCase("party")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        DuelsPlayer dp = plugin.getPlayerManager().getPlayer(player);

        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                    "create", "invite", "accept", "decline", "leave",
                    "kick", "promote", "info", "chat", "disband",
                    "duel", "requests", "ffa"
            ));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite" -> Bukkit.getOnlinePlayers().stream().filter(p -> !pm.areInTheSameParty(p, player)).map(Player::getName)
                        .forEach(completions::add);
                case "accept", "decline" -> dp.getPartyInvites().stream().map(PartyInvite::getSenderId).map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName)
                        .forEach(completions::add);
                case "kick", "promote" -> dp.getParty().getMembers().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName)
                        .forEach(completions::add);
                case "duel" -> completions.addAll(Arrays.asList("send", "accept", "decline"));
                case "ffa" -> completions.addAll(kits);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("duel")) {
                switch (args[1].toLowerCase()) {
                    case "send" ->
                            Bukkit.getOnlinePlayers().stream().filter(pm::hasParty).filter(p -> !p.equals(player)).map(Player::getName)
                                    .forEach(completions::add);
                    case "accept", "decline" ->
                            dp.getParty().getRequests().stream().map(PartyDuelRequest::getSenderId).map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName)
                                    .forEach(completions::add);
                }
            } else if (args[0].equalsIgnoreCase("ffa")) {
                completions.addAll(List.of("1", "2", "3", "5", "10"));
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("duel") && args[1].equalsIgnoreCase("send")) {
            completions.addAll(kits);
        } else if (args.length == 5 && args[0].equalsIgnoreCase("duel") && args[1].equalsIgnoreCase("send")) {
            completions.addAll(List.of("1", "2", "3", "5", "10"));
        }

        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).sorted().toList();
    }
}