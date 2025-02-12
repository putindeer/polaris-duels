package us.polarismc.polarisduels.arenas.dao;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.states.InactiveArenaState;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class ArenaGsonImpl implements ArenaDAO {
    private final Main plugin;
    private final File file;
    public ArenaGsonImpl(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Saves the arenas to the JSON
     *
     * @param arenas map of the arenas
     */
    @Override
    public void saveArenas(List<ArenaEntity> arenas) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray arenasArray = new JsonArray();

        for (ArenaEntity arena : arenas) {
            JsonObject arenaJson = new JsonObject();

            arenaJson.addProperty("name", arena.getName());
            arenaJson.addProperty("displayName", arena.getDisplayName());
            arenaJson.add("spawnOne", locationToJson(arena.getSpawnOne()));
            arenaJson.add("spawnTwo", locationToJson(arena.getSpawnTwo()));
            arenaJson.add("center", locationToJson(arena.getCenter()));
            arenaJson.add("cornerOne", locationToJson(arena.getCornerOne()));
            arenaJson.add("cornerTwo", locationToJson(arena.getCornerTwo()));
            if (arena.getBlockLogo() != null) {
                arenaJson.add("blockLogo", itemStackToJson(arena.getBlockLogo()));
            }

            arenasArray.add(arenaJson);
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(arenasArray, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving arenas to file: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.getLogger().severe(element.toString());
            }
        }
    }

    /**
     * Translates item stacks to json
     *
     * @param item the item
     * @return the json object
     */
    private JsonObject itemStackToJson(ItemStack item) {
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty("type", item.getType().toString());
        itemJson.addProperty("amount", item.getAmount());
        return itemJson;
    }

    /**
     * Translates Location to json
     *
     * @param location Location
     * @return the json object
     */
    @Override
    public JsonObject locationToJson(Location location) {
        JsonObject locationJson = new JsonObject();
        locationJson.addProperty("world", location.getWorld().getName());
        locationJson.addProperty("x", location.getX());
        locationJson.addProperty("y", location.getY());
        locationJson.addProperty("z", location.getZ());
        locationJson.addProperty("yaw", location.getYaw());
        locationJson.addProperty("pitch", location.getPitch());
        return locationJson;
    }

    /**
     * Load the arenas at starting the server
     *
     * @return the list of the arenas to work with
     */
    @Override
    public List<ArenaEntity> loadArenas() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<ArenaEntity> arenas = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JsonArray arenasArray = gson.fromJson(reader, JsonArray.class);

            if (arenasArray == null) {
                plugin.getLogger().warning("El archivo JSON no contiene un arreglo de arenas válido.");
                return new ArrayList<>();
            }

            for (JsonElement element : arenasArray) {
                JsonObject arenaJson = element.getAsJsonObject();
                String name = arenaJson.get("name").getAsString();
                String displayName = arenaJson.get("displayName").getAsString();
                Location spawnOne = jsonToLocation(arenaJson.getAsJsonObject("spawnOne"));
                Location spawnTwo = jsonToLocation(arenaJson.getAsJsonObject("spawnTwo"));
                Location center = jsonToLocation(arenaJson.getAsJsonObject("center"));
                Location cornerOne = jsonToLocation(arenaJson.getAsJsonObject("cornerOne"));
                Location cornerTwo = jsonToLocation(arenaJson.getAsJsonObject("cornerTwo"));


                ArenaEntity arena = new ArenaEntity();
                arena.setName(name);
                arena.setDisplayName(displayName);
                arena.setSpawnOne(spawnOne);
                arena.setSpawnTwo(spawnTwo);
                arena.setCenter(center);
                arena.setCornerOne(cornerOne);
                arena.setCornerTwo(cornerTwo);
                if (arenaJson.has("blockLogo")) {
                    arena.setBlockLogo(jsonToItemStack(arenaJson.getAsJsonObject("blockLogo")));
                }
                arena.setArenaState(new InactiveArenaState());
                arenas.add(arena);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error loading arenas from file: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.getLogger().severe(element.toString());
            }
        }

        return arenas;
    }

    /**
     * Translates the json object to Location
     *
     * @param locationJson json object
     * @return the location
     */
    @Override
    public Location jsonToLocation(JsonObject locationJson) {
        String worldName = locationJson.get("world").getAsString();
        double x = locationJson.get("x").getAsDouble();
        double y = locationJson.get("y").getAsDouble();
        double z = locationJson.get("z").getAsDouble();
        float yaw = locationJson.get("yaw").getAsFloat();
        float pitch = locationJson.get("pitch").getAsFloat();

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    /**
     * Translates the json object to ItemStack
     *
     * @param itemJson the JsonObject
     * @return the ItemStack
     */
    private ItemStack jsonToItemStack(JsonObject itemJson) {
        Material type = Material.valueOf(itemJson.get("type").getAsString());
        int amount = itemJson.get("amount").getAsInt();
        return new ItemStack(type, amount);
    }

    /**
     * Deletes the arenas when /arenai delete -name
     *
     * @param arena the arena to be deleted
     */
    public void deleteArena(ArenaEntity arena) {
        Gson gson = new Gson();
        JsonArray arenasArray = new JsonArray();

        try (FileReader reader = new FileReader(file)) {
            JsonArray currentArenas = gson.fromJson(reader, JsonArray.class);

            for (JsonElement element : currentArenas) {
                JsonObject arenaJson = element.getAsJsonObject();
                if (!arenaJson.get("name").getAsString().equals(arena.getName())) {
                    arenasArray.add(arenaJson);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading arenas file while deleting arena: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.getLogger().severe(element.toString());
            }
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(arenasArray, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Error writing arenas file after deleting arena: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.getLogger().severe(element.toString());
            }
        }
    }
}
