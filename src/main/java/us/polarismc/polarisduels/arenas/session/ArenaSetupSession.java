package us.polarismc.polarisduels.arenas.session;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

@Setter
@Getter
public class ArenaSetupSession {
    private Player player;
    private ArenaEntity arena;
    private int step;

    public ArenaSetupSession(Player player, ArenaEntity arena) {
        this.player = player;
        this.arena = arena;
        this.step = 1;
    }
}