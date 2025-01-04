package us.polarismc.polarisduels.queue;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.utils.ItemBuilder;

import java.util.Optional;

public class QueueGUI extends FastInv {
    public QueueGUI(Player p, int teamsize, Main plugin) {
        super(owner -> Bukkit.createInventory(owner, 27, plugin.utils.chat("&cQueue")));

        int[] glass = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        setItems(glass, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name("").hideAll().build());

        // SMP
        setItem(10, new ItemBuilder(Material.ENDER_PEARL).name("&#ff00d1&lSMP").lore("&7Placeholder Lore for SMP").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.SMP));
        // AXE
        setItem(11, new ItemBuilder(Material.DIAMOND_AXE).name("&#9300ff&lAxe").lore("&7Placeholder Lore for AXE").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.AXE));
        // NETHERITEPOT
        setItem(12, new ItemBuilder(Material.NETHERITE_HELMET).name("&#808080&lNetheritePot").lore("&7Placeholder Lore for NetheritePot").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.NETHPOT));
        // UHC
        setItem(13, new ItemBuilder(Material.GOLDEN_APPLE).name("&#d1bb00&lUHC").lore("&7Placeholder Lore for UHC").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.UHC));
        // DIAMONDPOT
        setItem(14, new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.HEALING).name("&#ff0000DiamondPot").lore("&7Placeholder Lore for DiamondPot").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.DIAMONDPOT));
        // SWORD
        setItem(15, new ItemBuilder(Material.DIAMOND_SWORD).name("&#00fff3&lSword").lore("&7Placeholder Lore for Sword").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.SWORD));
        // CUSTOM
        setItem(16, new ItemBuilder(Material.ENDER_EYE).name("&#04cd14&lCustom").lore("&cComing soon...").hideAll().build(), e -> setQueue(p, teamsize, plugin, KitType.CUSTOM));

        addClickHandler(e -> e.setCancelled(true));
        open(p);
    }

    private void setQueue(Player p, int teamsize, Main plugin, KitType kit) {
        Optional<ArenaEntity> arena = plugin.getArenaManager().findCompatibleArena(kit, teamsize * 2, 2);
        if (arena.isPresent()) {
            arena.get().addPlayer(p, plugin);
        } else plugin.utils.message(p, "&cThere are no arenas open. Try again in a bit.");
    }
}
