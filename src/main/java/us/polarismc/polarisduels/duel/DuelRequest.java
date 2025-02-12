package us.polarismc.polarisduels.duel;

import org.bukkit.entity.Player;
import us.polarismc.polarisduels.queue.KitType;

public record DuelRequest(Player requested, Player requestor, KitType kit, int rounds) {}