package us.polarismc.polarisduels.duel;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Duel {

    private DuelTeam teamOne;
    private DuelTeam teamTwo;

    private DuelType type;

    public void join(Player player){

    }
    public void quit(Player player){

    }
    public void start(){

    }

    public boolean isPlayer(Player player) {
        return teamOne.isPlayer(player) || teamTwo.isPlayer(player);
    }

}
