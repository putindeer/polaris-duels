package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

public class RegeneratingArenaState implements ArenaState {
    @Override
    public void onEnable(ArenaEntity arena) {
        Main.pl.getLogger().info("RegeneratingArenaState enabled");
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        Main.pl.getLogger().info("RegeneratingArenaState disabled");
    }
}
