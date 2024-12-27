package us.polarismc.polarisduels.arenas.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

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
}
