package us.polarismc.polarisduels.managers.party;

import lombok.Getter;
import lombok.Setter;
import us.polarismc.polarisduels.game.GameType;
import us.polarismc.polarisduels.game.KitType;

@Getter
@Setter
public class PartyGameSettings {
    private KitType kit;
    private GameType gameType = GameType.PARTY_FFA;
    private int rounds = 1;
    private int teamSize = 1;
    private int teamCount = 2;
}