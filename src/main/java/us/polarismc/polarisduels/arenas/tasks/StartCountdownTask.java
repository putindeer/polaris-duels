package us.polarismc.polarisduels.arenas.tasks;

import lombok.AllArgsConstructor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ActiveArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;

@AllArgsConstructor
public class StartCountdownTask extends BukkitRunnable {
    private final Main plugin;
    private final ArenaEntity arena;
    private int secondsUntilStart;
    @Override
    public void run() {
        if (secondsUntilStart <= 0) {
            if (arena.getArenaState() instanceof WaitingArenaState) {
                cancel();
                return;
            }
            saveKits();
            arena.setArenaState(new ActiveArenaState());
            cancel();
            return;
        }

        if (arena.getArenaState() instanceof WaitingArenaState) {
            cancel();
            return;
        }

        for (Player player : arena.getPlayerList()) {
            player.showTitle(Title.title(plugin.utils.chat("&b" + secondsUntilStart), plugin.utils.chat("&fOrganize your inventory!")));
            plugin.utils.message(player, Sound.BLOCK_NOTE_BLOCK_BIT, "The game is starting in &b" + secondsUntilStart + "&f seconds.");
        }

        secondsUntilStart--;
    }

    private void saveKits() {
        for (Player p : arena.getPlayerList()) {
            if (p.getItemOnCursor().getType() != Material.AIR) {
                if (p.getInventory().firstEmpty() == -1) {
                    p.setItemOnCursor(new ItemStack(Material.AIR));
                    plugin.utils.message(p, "&cYou were holding an item on your cursor while your inventory was full. Your kit could not be saved because of this. Please avoid doing this.");
                    continue;
                } else {
                    p.getInventory().addItem(p.getItemOnCursor());
                    p.setItemOnCursor(new ItemStack(Material.AIR));
                }
            }
            plugin.getKitManager().saveKit(p.getUniqueId(), arena.getKit(), p.getInventory().getContents());
        }
    }
}
