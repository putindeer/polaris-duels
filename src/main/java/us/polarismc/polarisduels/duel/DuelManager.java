package us.polarismc.polarisduels.duel;

import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DuelManager {
    private final Main plugin;
    public DuelManager(Main plugin) {
        this.plugin = plugin;
    }
    private List<Duel> duelList = new ArrayList<>();

    public void sendRequest(Player player, Player otherPlayer, DuelType duelType){
        if (duelFor(player).isPresent()) {
            plugin.utils.message(player, "&cYou can't enter a duel while you're in one.");
            return;
        }
        if (duelFor(otherPlayer).isPresent()) {
            plugin.utils.message(player, "&cYou can't enter a duel while you're in one.");
            return;
        }

    }
    public Optional<Duel> duelFor(Player player){
        return duelList.stream().filter(duel -> {
            return duel.isPlayer(player);
        }).findAny();
    }
}
