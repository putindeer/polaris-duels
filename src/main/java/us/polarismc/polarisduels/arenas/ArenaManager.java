package us.polarismc.polarisduels.arenas;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.dao.ArenaDAO;
import us.polarismc.polarisduels.arenas.dao.ArenaGsonImpl;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;

import java.util.List;
import java.util.Optional;

@Getter
public class ArenaManager {
    private final Main plugin;
    public List<ArenaEntity> arenas;
    public ArenaDAO arenaFile;
    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        arenaFile = new ArenaGsonImpl(plugin);
        arenas = arenaFile.loadArenas();
    }
    public void setWaitingState(ArenaEntity arena) {
        ItemStack[] kit = new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD) };
        WaitingArenaState waitingState = new WaitingArenaState(arena, kit, 2, 2);
        arena.setArenaState(waitingState);
    }
    public Optional<ArenaEntity> findOpenArena(){
        return getArenas().stream().filter(arena -> arena.getArenaState() instanceof InactiveArenaState).findAny();
    }
}
