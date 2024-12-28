package us.polarismc.polarisduels.arenas.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.arenas.states.ArenaState;

@Data
@NoArgsConstructor
public class ArenaEntity {

    private String displayName;
    private String name;
    private Location spawnOne;
    private Location spawnTwo;
    private Location center;
    private boolean beingUsed;
    private ItemStack blockLogo;
    private ArenaState arenaState;

    public void setArenaState(ArenaState state) {
        this.arenaState.onDisable(this);
        this.arenaState = state;
        this.arenaState.onEnable(this);
    }
}
