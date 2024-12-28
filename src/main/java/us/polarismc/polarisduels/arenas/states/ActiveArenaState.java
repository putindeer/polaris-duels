package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

public class ActiveArenaState implements ArenaState {
    @Override
    public void onEnable(ArenaEntity arena) {
        Main.pl.getLogger().info("ActiveArenaState enabled");
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        Main.pl.getLogger().info("ActiveArenaState disabled");
    }
}
