package us.polarismc.polarisduels.arenas.tasks;

import lombok.AllArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.ActiveArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;

import java.util.HashMap;

/**
 * A countdown task that runs before a duel match starts.
 * Handles the pre-match countdown, player notifications, and kit saving.
 * 
 * <p>This task is responsible for:</p>
 * <ul>
 *   <li>Displaying countdown titles to players</li>
 *   <li>Playing countdown sounds</li>
 *   <li>Saving player kits when the countdown completes</li>
 *   <li>Transitioning the arena to the active state when ready</li>
 * </ul>
 * 
 * <p>The countdown can be cancelled if the arena state changes to {@link WaitingArenaState}
 * before completion.</p>
 */
@AllArgsConstructor
public class StartCountdownTask extends BukkitRunnable {
    private final Main plugin;
    
    /** The arena this countdown is for */
    private final ArenaEntity arena;
    
    /** Remaining seconds until the match starts */
    private int secondsUntilStart;
    /**
     * Executes the countdown logic on each tick.
     * Handles countdown completion, state transitions, and player notifications.
     */
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
            plugin.utils.message(player, Sound.sound(Key.key("block.note_block.bit"), Sound.Source.MASTER, 10f, 1f), "The game is starting in &b" + secondsUntilStart + "&f seconds.");
        }

        secondsUntilStart--;
    }

    /**
     * Saves the current inventory of all players as their kit for this arena.
     * Handles edge cases like items on cursor and full inventories.
     * Displays appropriate error messages if kit saving fails.
     */
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
