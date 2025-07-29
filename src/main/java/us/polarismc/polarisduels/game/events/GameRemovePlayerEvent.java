package us.polarismc.polarisduels.game.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.game.GameSession;

@Getter
public class GameRemovePlayerEvent extends GameEvent {
    private final Player player;

    public GameRemovePlayerEvent(GameSession gameSession, Player player) {
        super(gameSession);
        this.player = player;
    }
}
