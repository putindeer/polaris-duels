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

import java.util.HashMap;

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
            cancel();
            saveKits();
            arena.setArenaState(new ActiveArenaState());
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
                ItemStack item = p.getItemOnCursor().clone();
                p.setItemOnCursor(new ItemStack(Material.AIR));
                HashMap<Integer, ItemStack> remaining = p.getInventory().addItem(item);
                if (!remaining.isEmpty()) {
                    p.setItemOnCursor(remaining.values().iterator().next());
                    plugin.utils.message(p, "&cYou were holding an item on your cursor while your inventory was full. Your kit could not be saved because of this. Please avoid doing this.");
                    continue;
                }
            }
            plugin.getKitManager().saveKit(p.getUniqueId(), arena.getKit(), p.getInventory().getContents());
        }
    }
}
