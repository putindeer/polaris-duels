package us.polarismc.polarisduels.duel;

import org.bukkit.entity.Player;
import us.polarismc.polarisduels.queue.KitType;

/**
 * Represents a duel request between two players.
 * This record is immutable and contains all necessary information to start a duel.
 *
 * @param requested The player who received the duel request
 * @param requestor The player who sent the duel request
 * @param kit The type of kit to be used in the duel
 * @param rounds The number of rounds for the duel
 */
public record DuelRequest(Player requested, Player requestor, KitType kit, int rounds) {}