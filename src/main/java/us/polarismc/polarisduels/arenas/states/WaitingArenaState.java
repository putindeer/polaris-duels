package us.polarismc.polarisduels.arenas.states;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;

@Setter
@Getter
public class WaitingArenaState implements ArenaState {
    private final ArenaEntity arena;
    private final ItemStack[] kit;
    private final int playersNeeded;
    private final int rounds;

    public WaitingArenaState(ArenaEntity arena, ItemStack[] kit, int playersNeeded, int rounds){
        this.arena = arena;
        this.kit = kit;
        this.playersNeeded = playersNeeded;
        this.rounds = rounds;
    }

    @Override
    public void onEnable(ArenaEntity arena) {
        Main.pl.getLogger().info("WaitingArenaState enabled");
    }

    @Override
    public void onDisable(ArenaEntity arena) {
        Main.pl.getLogger().info("WaitingArenaState disabled");
    }

    public void createWaitingArena(ArenaEntity arena, ItemStack[] kit, int playersNeeded, int rounds){
        ArenaEntity a;
    }

}
