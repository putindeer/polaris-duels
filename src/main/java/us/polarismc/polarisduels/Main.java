package us.polarismc.polarisduels;

import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.polarisduels.arenas.ArenaManager;
import us.polarismc.polarisduels.duel.DuelManager;
import us.polarismc.polarisduels.player.DuelsPlayer;
import us.polarismc.polarisduels.player.PlayerManager;
import us.polarismc.polarisduels.utils.StartThings;
import us.polarismc.polarisduels.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public static Main pl;
    @Getter
    private ArenaManager arenaManager;
    @Getter
    public DuelManager duelManager;
    @Getter
    public PlayerManager playerManager;
    public Map<UUID, FastBoard> boards = new HashMap<>();
    public Utils utils;

    @Override
    public void onEnable() {
        pl = this;
        utils = new Utils();
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        playerManager = new PlayerManager(this);
        new StartThings(this);
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            DuelsPlayer duelsPlayer = this.getPlayerManager().getDuelsPlayer(p);
            if (duelsPlayer.getTeam() != null) {
                duelsPlayer.deleteTeam(duelsPlayer.getTeam());
                duelsPlayer.removeTeam();
            }
        }
    }
    public static Main getInstance() {
        return pl;
    }
}
