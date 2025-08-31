package us.polarismc.polarisduels.managers.party.gui;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;

public class PartyLeaveDisbandGUI extends FastInv {
    public PartyLeaveDisbandGUI(Player player, Main plugin) {
        super(owner -> Bukkit.createInventory(owner, 45, plugin.utils.chat("<#FF6B6B>Leave or Disband Party")));

        for (int i = 0; i < 45; i++) {
            setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        int[] leave = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
        setItems(leave, plugin.utils.ib(Material.PRISMARINE_SHARD)
                .name("<#6EC8C1>Leave Party")
                .lore("<#A8E6E0>Click this to leave this party.",
                        "<#FFE066>⚠ Leadership will be transferred to another member.").build(),
                event -> {
            plugin.getPartyManager().leaveParty(player);
            player.closeInventory();
        });

        int[] disband = new int[]{14, 15, 16, 23, 24, 25, 32, 33, 34};
        setItems(disband, plugin.utils.ib(Material.REDSTONE)
                .name("<#FF6678>Disband Party")
                .lore("<#FFB3BA>Click this to disband this party.",
                        "<#FFE066>⚠ This will remove all party members.").build(),
                event -> {
            plugin.getPartyManager().disbandParty(player);
            player.closeInventory();
        });

        addClickHandler(event -> event.setCancelled(true));
        open(player);
    }
}
