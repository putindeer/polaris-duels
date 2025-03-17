package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

public class InactiveArenaState implements ArenaState {
    @Override
    public void onEnable(ArenaEntity arena) {
        Main.pl.getLogger().info("InactiveArenaState enabled");
    }

    @Override
    public void onDisable() {
        Main.pl.getLogger().info("InactiveArenaState disabled");
    }
}
