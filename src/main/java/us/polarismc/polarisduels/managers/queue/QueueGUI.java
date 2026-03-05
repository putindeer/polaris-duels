package us.polarismc.polarisduels.managers.queue;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

        setKitItem(9, KitType.SMP);
        setKitItem(10, KitType.AXE);
        setKitItem(11, KitType.NETHPOT);
        setKitItem(12, KitType.UHC);

        setKitItem(14, KitType.MARLOWUHC);
        setKitItem(15, KitType.DIAMONDPOT);
        setKitItem(16, KitType.SWORD);
        setKitItem(17, KitType.MACE);

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

    private void setKitItem(int slot, KitType kit) {
        setItem(slot,
                plugin.utils.ib(kit.getKitItem())
                        .name(kit.getDisplayName())
                        .lore(kit.getDescription())
                        .build(),
                e -> setQueue(kit)
        );
    }
}
