package us.polarismc.polarisduels.arenas.tasks;

import lombok.AllArgsConstructor;
import net.kyori.adventure.title.Title;
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
            plugin.getKitManager().saveKit(p.getUniqueId(), arena.getKit(), p.getInventory().getContents());
        }
    }
}
