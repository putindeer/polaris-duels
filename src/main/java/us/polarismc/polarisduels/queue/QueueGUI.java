package us.polarismc.polarisduels.queue;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;
import us.polarismc.api.util.builder.ItemBuilder;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

import java.util.Optional;

public class QueueGUI extends FastInv {
    public QueueGUI(Player p, int teamsize, Main plugin) {
        super(owner -> Bukkit.createInventory(owner, 27, plugin.utils.chat("&cQueue")));

        int[] glass = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        setItems(glass, plugin.utils.ib(Material.BLUE_STAINED_GLASS_PANE).name("").hideTooltip().build());

        // SMP
        setItem(10, plugin.utils.ib(Material.ENDER_PEARL).name("&#ff00d1&lSMP").lore("&7Placeholder Lore for SMP").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.SMP));
        // AXE
        setItem(11, plugin.utils.ib(Material.DIAMOND_AXE).name("&#9300ff&lAxe").lore("&7Placeholder Lore for AXE").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.AXE));
        // NETHERITEPOT
        setItem(12, plugin.utils.ib(Material.NETHERITE_HELMET).name("&#808080&lNetheritePot").lore("&7Placeholder Lore for NetheritePot").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.NETHPOT));
        // UHC
        setItem(13, plugin.utils.ib(Material.GOLDEN_APPLE).name("&#d1bb00&lUHC").lore("&7Placeholder Lore for UHC").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.UHC));
        // DIAMONDPOT
        setItem(14, plugin.utils.ib(Material.SPLASH_POTION).potionType(PotionType.HEALING).customName("&#ff0000&lDiamondPot").lore("&7Placeholder Lore for DiamondPot").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.DIAMONDPOT));
        // SWORD
        setItem(15, plugin.utils.ib(Material.DIAMOND_SWORD).name("&#00fff3&lSword").lore("&7Placeholder Lore for Sword").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.SWORD));
        // CUSTOM
        setItem(16, plugin.utils.ib(Material.ENDER_EYE).name("&#04cd14&lCustom").lore("&cComing soon...").hideGUI().build(), e -> setQueue(p, teamsize, plugin, KitType.CUSTOM));

        addClickHandler(e -> e.setCancelled(true));
        open(p);
    }

    private void setQueue(Player p, int teamsize, Main plugin, KitType kit) {
        Optional<ArenaEntity> arena = plugin.getArenaManager().findCompatibleArena(kit, teamsize * 2, kit.getDefaultRounds());
        if (arena.isPresent()) {
            arena.get().addPlayer(p, plugin);
        } else plugin.utils.message(p, "&cThere are no arenas open. Try again in a bit.");
    }
}
