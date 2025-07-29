package us.polarismc.polarisduels.managers.duel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.game.KitType;

import java.util.UUID;

/**
 * Represents a duel request between two players.
 * This record is immutable and contains all necessary information to start a duel.
 *
 * @param requestedUUID The UUID of the player who received the duel request
 * @param requestorUUID The UUID of the player who sent the duel request
 * @param kit The type of kit to be used in the duel
 * @param rounds The number of rounds for the duel
 */
public record DuelRequest(UUID requestedUUID, UUID requestorUUID, KitType kit, int rounds) {
    public Player requested() {
        return Bukkit.getPlayer(requestedUUID);
    }

    public Player requestor() {
        return Bukkit.getPlayer(requestorUUID);
    }
}