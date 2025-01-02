package us.polarismc.polarisduels.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.events.HubEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRollBackManager {

    private final Map<UUID, Location> previousLocationMap = new HashMap<>();
    private final Map<UUID, GameMode> previousGameModeMap = new HashMap<>();
    private final Map<UUID, ItemStack[]> previousArmorContents = new HashMap<>();
    private final Map<UUID, Integer> previousHungerValue = new HashMap<>();
    private final Map<UUID, Integer> previousLevelMap = new HashMap<>();

    public void save(Player player){
        previousLocationMap.put(player.getUniqueId(), player.getLocation());
        previousGameModeMap.put(player.getUniqueId(), player.getGameMode());
        previousArmorContents.put(player.getUniqueId(), player.getInventory().getArmorContents());
        previousHungerValue.put(player.getUniqueId(), player.getFoodLevel());
        previousLevelMap.put(player.getUniqueId(), player.getLevel());
        player.getInventory().clear();
    }
    public void restore(Player player, JavaPlugin plugin){
        player.getInventory().clear();
        DuelsPlayer duelsPlayer = Main.pl.getPlayerManager().getDuelsPlayer(player);
        duelsPlayer.setDuel(false);

        ItemStack[] armorContents = previousArmorContents.get(player.getUniqueId());
        if (armorContents != null){
            player.getInventory().setContents(armorContents);
        }
        GameMode previousGameMode = previousGameModeMap.get(player.getUniqueId());
        if (previousGameMode != null){
            player.setGameMode(previousGameMode);
        }
        Location previousLocation = previousLocationMap.get(player.getUniqueId());
        if (previousLocation != null){
            World world = Bukkit.getWorld("lobby");
            Location loc = new Location(world, 0, 100, 0);
            player.teleport(loc);
        }
        player.setFoodLevel(previousHungerValue.getOrDefault(player.getUniqueId(), 20));
        player.setLevel(previousLevelMap.getOrDefault(player.getUniqueId(), 0));
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        previousHungerValue.remove(player.getUniqueId());
        previousLevelMap.remove(player.getUniqueId());
        previousLocationMap.remove(player.getUniqueId());
        previousGameModeMap.remove(player.getUniqueId());
        previousArmorContents.remove(player.getUniqueId());

        HubEvents.giveJoinItems(player);

        if (plugin == null) return;
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                player.setFireTicks(0);
            }
        }, 2);
    }





}






