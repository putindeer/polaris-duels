package us.polarismc.polarisduels.managers.scoreboard;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.managers.player.DuelsPlayer;

/**
 * Handles the creation and updating of scoreboards for players.
 * This class provides utility methods for managing scoreboard content
 * and tracking player statistics like online count, players in queue, and dueling players.
 */
public class Scoreboards {
    private static final Main plugin = Main.getInstance();
    /**
     * Updates the content of a player's scoreboard with current server statistics.
     * Displays online player count, players in duels, players in queue, and ping.
     *
     * @param board The FastBoard instance to update
     */
    public static void updateBoard(FastBoard board) {
        Component headfooter = plugin.utils.chat("<gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset><gray><st> <reset>");

        board.updateLines(
                headfooter,
                plugin.utils.chat("<i>Online <reset><dark_gray>» <aqua>" + plugin.getServer().getOnlinePlayers().size()),
                plugin.utils.chat("<i>Playing <reset><dark_gray>» <aqua>" + duelingCount()),
                plugin.utils.chat("<i>In Queue <reset><dark_gray>» <aqua>" + queueCount()),
                Component.empty(),
                plugin.utils.chat("<i>Ping <reset><dark_gray>» <aqua>" + board.getPlayer().getPing()),
                headfooter,
                plugin.utils.chat("<blue>polarismc.us")
        );
    }

    /**
     * Counts the number of players currently in queue.
     *
     * @return The number of players in queue
     */
    public static Integer queueCount() {
        return (int) plugin.getPlayerManager().getPlayerList().stream()
                .filter(DuelsPlayer::isQueue)
                .count();
    }

    /**
     * Counts the number of players currently in a duel or starting a duel.
     *
     * @return The number of players currently dueling or about to duel
     */
    public static Integer duelingCount() {
        return (int) plugin.getPlayerManager().getPlayerList().stream()
                .filter(p -> p.isDuel() || p.isStartingDuel())
                .count();
    }
}
