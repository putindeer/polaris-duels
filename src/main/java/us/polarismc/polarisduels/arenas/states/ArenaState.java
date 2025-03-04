package us.polarismc.polarisduels.arenas.states;

import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

public interface ArenaState {
    void onEnable(ArenaEntity arena);
    void onDisable();
}
