package us.polarismc.polarisduels.managers.queue;

import lombok.Getter;
import us.polarismc.polarisduels.game.GameType;

public enum QueueType {
    UNRANKED_1V1(2, 1, GameType.DUEL_1V1),
    UNRANKED_2V2(4, 2, GameType.DUEL_2V2),
    UNRANKED_3V3(6, 3, GameType.DUEL_3V3);
    @Getter
    private final int playersNeeded;
    @Getter
    private final int teamSize;
    @Getter
    private final GameType gameType;
    QueueType(int playersNeeded, int teamSize, GameType gameType) {
        this.playersNeeded = playersNeeded;
        this.teamSize = teamSize;
        this.gameType = gameType;
    }
}
