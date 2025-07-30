package us.polarismc.polarisduels.managers.queue;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.game.GameSession;
import us.polarismc.polarisduels.game.KitType;

import java.util.Optional;

public class QueueGUI extends FastInv {
    private final Main plugin;
    private final Player player;
    private final QueueType type;
    public QueueGUI(Player player, QueueType type, Main plugin) {
        super(owner -> Bukkit.createInventory(owner, 27, plugin.utils.chat("<red>Queue")));
        this.plugin = plugin;
        this.player = player;
        this.type = type;

        int[] glass = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        setItems(glass, plugin.utils.ib(Material.BLUE_STAINED_GLASS_PANE).name("").hideTooltip().build());

        // SMP
        setItem(10, plugin.utils.ib(Material.ENDER_PEARL).name("<#ff00d1><bold>SMP")
                        .lore("<gray>Survival‑style PvP with maxed-out gear, pearls and totems.",
                        "<gray>Endurance-based PvP without arena destruction.").hideGUI().build(),
                e -> setQueue(KitType.SMP));
        // AXE
        setItem(11, plugin.utils.ib(Material.DIAMOND_AXE).name("<#9300ff><bold>Axe")
                        .lore("<gray>Diamond‑axe combat mixed with shield and crossbow tactics.",
                                "<gray>Low attack speed, high burst damage.")
                        .hideGUI().build(),
                e -> setQueue(KitType.AXE));
        // NETHERITEPOT
        setItem(12, plugin.utils.ib(Material.NETHERITE_HELMET).name("<#808080><bold>NetheritePot")
                        .lore("<gray>Full netherite armor with splash healing potions.",
                                "<gray>Leverage pot‑timing and strong‑hit combos to break defenses.")
                        .hideGUI().build(),
                e -> setQueue(KitType.NETHPOT));
        // UHC
        setItem(13, plugin.utils.ib(Material.GOLDEN_APPLE).name("<#d1bb00><bold>UHC")
                        .lore("<gray>No natural regeneration; heal only with golden apples.",
                                "<gray>Carry water, lava, blocks and cobwebs for tactical utility.")
                        .hideGUI().build(),
                e -> setQueue(KitType.UHC));
        // DIAMONDPOT
        setItem(14, plugin.utils.ib(Material.SPLASH_POTION).potionType(PotionType.HEALING).customName("<#ff0000><bold>DiamondPot")
                        .lore("<gray>Diamond armor with splash healing potions.",
                                "<gray>Classic potion PvP mode with 33% increased damage.")
                        .hideGUI().build(),
                e -> setQueue(KitType.DIAMONDPOT));
        // SWORD
        setItem(15, plugin.utils.ib(Material.DIAMOND_SWORD).name("<#00fff3><bold>Sword")
                        .lore("<gray>Classic sword PvP without shields.",
                                "<gray>Focuses on timing and spacing.")
                        .hideGUI().build(),
                e -> setQueue(KitType.SWORD));
        // CUSTOM
        setItem(16, plugin.utils.ib(Material.MACE).name("<#04cd14><bold>Mace")
                        .lore("<gray>PvP based on the mace as main weapon.",
                                "<gray>Combines aerial movement and burst damage with wind charges and elytra.",
                                "<red>Experimental kit from Enrico & Lurrn's TL.").hideGUI().build(),
                e -> setQueue(KitType.MACE));

        addClickHandler(e -> e.setCancelled(true));
        open(player);
    }

    private void setQueue(KitType kit) {
        GameSession session = GameSession.builder().gameType(type.getGameType()).queueType(type).kit(kit).rounds(kit.getDefaultRounds()).build();
        Optional<ArenaEntity> arena = plugin.getArenaManager().assignArena(session);
        if (arena.isPresent()) {
            arena.get().addPlayer(player);
        } else plugin.utils.message(player, "<red>There are no arenas open. Try again in a bit.");
    }
}
