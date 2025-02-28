package us.polarismc.polarisduels.arenas;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.dao.ArenaDAO;
import us.polarismc.polarisduels.arenas.dao.ArenaGsonImpl;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;
import us.polarismc.polarisduels.arenas.states.WaitingArenaState;
import us.polarismc.polarisduels.queue.KitType;
import us.polarismc.polarisduels.player.PlayerRollBackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public class ArenaManager {
    private final Main plugin;
    public final List<ArenaEntity> arenas;
    public final ArenaDAO arenaFile;
    @Getter
    private final PlayerRollBackManager rollBackManager;
    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        arenaFile = new ArenaGsonImpl(plugin);
        arenaFile.loadArenaWorlds();
        arenas = arenaFile.loadArenas();
        this.rollBackManager = new PlayerRollBackManager();
    }

    public void setInactiveState(ArenaEntity arena) {
        arena.getPlayers().clear();
        arena.setKit(null);
        arena.setRounds(0);
        arena.setPlayersNeeded(0);
        arena.setArenaState(new InactiveArenaState());
    }

    public Optional<ArenaEntity> findOpenArena(KitType kit, int playersNeeded, int rounds){
        List<ArenaEntity> arenaList = new ArrayList<>(getArenas());
        Collections.shuffle(arenaList);
        Optional<ArenaEntity> arena = arenaList.stream().filter(a -> a.getArenaState() instanceof InactiveArenaState).findAny();
        arena.ifPresent(arenaEntity -> arenaEntity.setArenaState(new WaitingArenaState(arenaEntity, kit, playersNeeded, rounds)));
        return arena;
    }

    public Optional<ArenaEntity> findCompatibleArena(KitType kit, int playersNeeded, int rounds) {
        Optional<ArenaEntity> compatibleArena = getArenas().stream()
                .filter(arena -> {
                    if (arena.getArenaState() instanceof WaitingArenaState state) {
                        return state.getPlayersNeeded() == playersNeeded
                                && state.getRounds() == rounds
                                && state.getKit() == kit;
                    }
                    return false;
                })
                .findAny();

        return compatibleArena.isPresent() ? compatibleArena : findOpenArena(kit, playersNeeded, rounds);
    }

    public Optional<ArenaEntity> findPlayerArena(Player player){
        return getArenas().stream().filter(arena -> arena.getPlayers().contains(player.getUniqueId())).findAny();
    }

    // DEBUG
    public Optional<ArenaEntity> findInactiveArena(){
        return getArenas().stream().filter(a -> a.getArenaState() instanceof InactiveArenaState).findAny();
    }

    public Optional<ArenaEntity> findCompatibleArenaNoMethod(KitType kit, int playersNeeded, int rounds) {
        return getArenas().stream()
                .filter(arena -> {
                    if (arena.getArenaState() instanceof WaitingArenaState state) {
                        return state.getPlayersNeeded() == playersNeeded
                                && state.getRounds() == rounds
                                && state.getKit() == kit;
                    }
                    return false;
                })
                .findAny();
    }
}
