package us.polarismc.polarisduels.managers.party.gui;

import fr.mrmicky.fastinv.InventoryScheme;
import fr.mrmicky.fastinv.PaginatedFastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.party.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PartyInfoGUI extends PaginatedFastInv {
    private final Main plugin;

    public PartyInfoGUI(Player player, Main plugin, int page) {
        super(owner -> Bukkit.createInventory(owner, 54, plugin.utils.chat("<#87CEEB>Party Info")));

        new InventoryScheme()
                .mask("000000000")
                .mask("000000000")
                .mask("0mmmmmmm0")
                .mask("0mmmmmmm0")
                .mask("0mmmmmmm0")
                .mask("000000000")
                .bindPagination('m').apply(this);

        this.plugin = plugin;
        Party party = plugin.getPartyManager().getParty(player);
        List<UUID> members = new ArrayList<>(party.getMembers());
        OfflinePlayer leader = party.getLeaderOfflinePlayer();
        boolean viewerIsLeader = player.getUniqueId().equals(leader.getUniqueId());

        for (int i = 0; i < 54; i++) {
            setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        setItem(2, plugin.utils.ib(Material.CLOCK)
                .name("<#87CEEB>Formation Time")
                .lore("Created <color:#F0E68C>" + plugin.utils.getTimeAgo(party.getCreatedAt()) + "</color:#F0EB68C>.")
                .build());

        setItem(4, plugin.utils.ib(Material.PLAYER_HEAD).profile(leader)
                .customName("<#FFD700>👑 " + leader.getName())
                .lore("<#F0E68C>Party Leader",
                        "",
                        "<#98FB98>Status: " + (leader.isOnline() ? "<#00FF00>Online" : "<#FF6B6B>Offline")
                ).build());

        setItem(6, plugin.utils.ib(Material.IRON_SWORD)
                .name("<#FFD700>Party vs Party Statistics")
                .lore("<#98FB98>Wins: <white>" + party.getWins(),
                        "<#FFB3BA>Losses: <white>" + party.getLosses(),
                        "<#87CEEB>Total Duels: <white>" + party.getTotalDuels()
                ).hideAttributeModifiers().build());

        setupNavigationButtons();

        members.stream().map(Bukkit::getOfflinePlayer)
                .sorted((p1, p2) -> Objects.requireNonNull(p1.getName()).compareToIgnoreCase(Objects.requireNonNull(p2.getName())))
                .forEach(member -> {
                    boolean isOnline = member.isOnline();
                    boolean isLeader = member.equals(leader);
                    String rank = "<#F0E68C>" + (isLeader ? "Party Leader" : "Party Member");

                    ItemStack memberItem = plugin.utils.ib(Material.PLAYER_HEAD)
                            .profile(member)
                            .customName((isOnline ? "<#00FF00>" : "<#FF6B6B>") + member.getName())
                            .lore(rank,
                                    "",
                                    "<#98FB98>Status: " + (isOnline ? "<#00FF00>Online" : "<#FF6B6B>Offline"),
                                    "<#98FB98>Joined: <white>" + plugin.utils.getTimeAgo(party.getMemberJoinTimes().get(member.getUniqueId()))
                            )
                            .addLoreIf(viewerIsLeader && !isLeader,
                                    "",
                                    "<#FFD700>Shift-Left Click: <#FFEB99>Promote to Leader",
                                    "<#FF6B6B>Shift-Right Click: <#FFB3BA>Kick from Party"
                            )
                            .build();

                    if (viewerIsLeader && !isLeader) {
                        addContent(memberItem, event -> {
                            if (event.isShiftClick()) {
                                if (event.isLeftClick()) {
                                    party.promoteLeader(member.getUniqueId());
                                    new PartyInfoGUI(player, plugin, currentPage());
                                } else if (event.isRightClick()) {
                                    plugin.getPartyManager().kickFromParty(member.getUniqueId());
                                    new PartyInfoGUI(player, plugin, currentPage());
                                }
                            }
                        });
                    } else {
                        addContent(memberItem);
                    }
                });

        if (viewerIsLeader) {
            setItem(49, plugin.utils.ib(Material.WRITABLE_BOOK)
                            .name("<#FFD700>Invite Players")
                            .lore("Click this to invite new players to your party.")
                            .build(), e -> new PartyInviteGUI(player, plugin));
        }

        addClickHandler(event -> event.setCancelled(true));
        openPage(page);
        open(player);
    }

    public PartyInfoGUI(Player player, Main plugin) {
        this(player, plugin, 1);
    }

    @Override
    protected void onPageChange(int page) {
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        if (!isLastPage() && lastPage() != 1) {
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