package us.polarismc.polarisduels.utils;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.player.DuelsPlayer;

public class Scoreboards {
    private static final Main plugin = Main.getInstance();
    public static void updateBoard(FastBoard board) {
        Component headfooter = plugin.utils.chat("&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r");

        board.updateLines(
                headfooter,
                plugin.utils.chat("&oOnline &r&8» &b" + plugin.getServer().getOnlinePlayers().size()),
                plugin.utils.chat("&oPlaying &r&8» &b" + inDuel()),
                plugin.utils.chat("&oIn Queue &r&8» &b" + inQueue()),
                Component.empty(),
                plugin.utils.chat("&oPing &r&8» &b" + board.getPlayer().getPing()),
                headfooter,
                plugin.utils.chat("&9polarismc.us")
        );
    }

    public static Integer inQueue() {
        return (int) plugin.getPlayerManager().getPlayerList().stream()
                .filter(DuelsPlayer::isQueue)
                .count();
    }

    public static Integer inDuel() {
        return (int) plugin.getPlayerManager().getPlayerList().stream()
                .filter(DuelsPlayer::isDuel)
                .count();
    }
}
