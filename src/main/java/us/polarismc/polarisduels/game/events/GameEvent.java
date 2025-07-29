package us.polarismc.polarisduels.game.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.polarismc.polarisduels.game.GameSession;

@Getter
@SuppressWarnings("unused")
public abstract class GameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final GameSession session;
    public GameEvent(GameSession session) {
        this.session = session;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
