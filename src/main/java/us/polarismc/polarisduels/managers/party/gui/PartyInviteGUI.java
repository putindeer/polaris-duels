package us.polarismc.polarisduels.managers.party.gui;

import fr.mrmicky.fastinv.InventoryScheme;
import fr.mrmicky.fastinv.PaginatedFastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;

import java.util.List;
import java.util.stream.Collectors;

public class PartyInviteGUI extends PaginatedFastInv {
    private final Main plugin;
    private final Player player;

    public PartyInviteGUI(Player player, Main plugin, int page) {
        super(owner -> Bukkit.createInventory(owner, 54, plugin.utils.chat("<#FFD700>Party Invites")));

        new InventoryScheme()
                .mask("000000000")
                .mask("0mmmmmmm0")
                .mask("0mmmmmmm0")
                .mask("0mmmmmmm0")
                .mask("0mmmmmmm0")
                .mask("000000000")
                .bindPagination('m').apply(this);

        this.plugin = plugin;
        this.player = player;
        List<Player> availablePlayers = getAvailablePlayersForInvite(player);

        for (int i = 0; i < 54; i++) {
            setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        setItem(4, plugin.utils.ib(Material.PAPER)
                .name("<#FFD700>Send Party Invites")
                .lore("<#F0E68C>Select a player below to invite them to your party.",
                        "",
                        "<#87CEEB>Available Players: <white>" + availablePlayers.size())
                .build());

        for (Player availablePlayer : availablePlayers) {
            ItemStack head = plugin.utils.ib(Material.PLAYER_HEAD)
                    .customName("<#00FF00>" + availablePlayer.getName())
                    .lore("<#F0E68C>Click this to invite " + availablePlayer.getName() + " to your party.")
                    .profile(availablePlayer)
                    .build();

            addContent(head, event -> {
                plugin.getPartyManager().sendInvite(player, availablePlayer);
                new PartyInviteGUI(player, plugin, currentPage());
            });
        }

        setupNavigationButtons();

        setItem(49, plugin.utils.ib(Material.BARRIER).name("<red>Go Back").lore("<gray>Click this to go back to the previous menu.").build(),
                event -> new PartyInfoGUI(player, plugin));
        addClickHandler(event -> event.setCancelled(true));
        openPage(page);
        open(player);
    }

    public PartyInviteGUI(Player player, Main plugin) {
        this(player, plugin, 1);
    }

    private List<Player> getAvailablePlayersForInvite(Player inviter) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(onlinePlayer -> !plugin.getPartyManager().hasParty(onlinePlayer.getUniqueId()))
                .filter(onlinePlayer -> !onlinePlayer.equals(inviter))
                .filter(onlinePlayer -> plugin.getPlayerManager().getPlayer(onlinePlayer).getPartyInvites().stream()
                        .noneMatch(invite -> invite.getSenderId().equals(inviter.getUniqueId())))
                .collect(Collectors.toList());
    }

    @Override
    protected void onPageChange(int page) {
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        if (!isLastPage() && lastPage() != 1 && !getAvailablePlayersForInvite(player).isEmpty()) {
            setItem(53, plugin.utils.ib(Material.ARROW).name("<#00FF00>Next Page")
                    .lore("<#87CEEB>Click this to go to the next page.").build(), event -> openNext());
        } else {
            setItem(53, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        if (!isFirstPage()) {
            setItem(45, plugin.utils.ib(Material.ARROW).name("<#FF6B6B>Previous Page")
                    .lore("<#87CEEB>Click this to go to the previous page.").build(), event -> openPrevious());
        } else {
            setItem(45, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }
    }
}