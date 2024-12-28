package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

public class StartingArenaState implements ArenaState {
    @Override
    public void onEnable(ArenaEntity arena) {
        Main.pl.getLogger().info("StartingArenaState enabled");
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        Main.pl.getLogger().info("StartingArenaState disabled");
    }
}
