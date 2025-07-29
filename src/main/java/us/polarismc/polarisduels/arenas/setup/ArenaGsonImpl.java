package us.polarismc.polarisduels.arenas.setup;

import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import us.polarismc.api.util.generator.VoidGenerator;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaEntity;
import us.polarismc.polarisduels.arenas.entity.ArenaSize;
import us.polarismc.polarisduels.game.states.InactiveArenaState;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ArenaDAO that uses JSON for data persistence.
 * Handles loading and saving arena data to/from a JSON file.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ArenaGsonImpl implements ArenaDAO {
    private final Main plugin;
    private final File file;

    /**
     * Creates a new ArenaGsonImpl instance and initializes the data file.
     *
     * @param plugin The main plugin instance
     */
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
     * Loads all arena worlds and initializes them with a void generator.
     * Skips worlds that are already loaded.
     */
    @Override
    public void loadArenaWorlds() {
        File[] dir = Bukkit.getWorldContainer().listFiles(File::isDirectory);
        if (dir == null) return;

        for (File folder : dir) {
            String name = folder.getName();

            if (Bukkit.getWorld(name) != null) continue;

            World world = new WorldCreator(name).generator(new VoidGenerator()).createWorld();
            assert world != null;
            world.setAutoSave(false);
        }
    }

    /**
     * Saves the list of arenas to a JSON file.
     * Converts each arena's properties to JSON format including locations and settings.
     *
     * @param arenas List of ArenaEntity objects to save
     */
    @Override
    public void saveArenas(List<ArenaEntity> arenas) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray arenasArray = new JsonArray();

        for (ArenaEntity arena : arenas) {
            JsonObject arenaJson = new JsonObject();

            arenaJson.addProperty("name", arena.getName());
            arenaJson.addProperty("displayName", arena.getDisplayName());
            arenaJson.addProperty("world", arena.getWorld().getName());
            arenaJson.add("spawnOne", locationToJson(arena.getSpawnOne()));
            arenaJson.add("spawnTwo", locationToJson(arena.getSpawnTwo()));
            arenaJson.add("center", locationToJson(arena.getCenter()));
            arenaJson.add("cornerOne", locationToJson(arena.getCornerOne()));
            arenaJson.add("cornerTwo", locationToJson(arena.getCornerTwo()));
            arenaJson.add("playableCornerOne", locationToJson(arena.getPlayableCornerOne()));
            arenaJson.add("playableCornerTwo", locationToJson(arena.getPlayableCornerTwo()));

            if (arena.getQuadrant() != null) {
                JsonObject quadrantJson = new JsonObject();
                quadrantJson.addProperty("x", arena.getQuadrant().x());
                quadrantJson.addProperty("z", arena.getQuadrant().z());
                arenaJson.add("quadrant", quadrantJson);
            }
            
            if (arena.getBlockLogo() != null) {
                arenaJson.add("blockLogo", itemStackToJson(arena.getBlockLogo()));
            }
            arenaJson.addProperty("size", arena.getArenaSize().name());


            arenasArray.add(arenaJson);
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(arenasArray, writer);
        } catch (IOException e) {
            plugin.utils.severe("Error saving arenas to file: " + e.getMessage());
            plugin.utils.severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.utils.severe(element.toString());
            }
        }
    }

    /**
     * Converts an ItemStack to a JSON object.
     *
     * @param item The ItemStack to convert
     * @return JsonObject containing the item's type and amount
     */
    private JsonObject itemStackToJson(ItemStack item) {
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty("type", item.getType().toString());
        itemJson.addProperty("amount", item.getAmount());
        return itemJson;
    }

    /**
     * Converts a Location to a JSON object.
     *
     * @param location The Location to convert
     * @return JsonObject containing the location's coordinates and world
     */
    @Override
    public JsonObject locationToJson(Location location) {
        JsonObject locationJson = new JsonObject();
        locationJson.addProperty("x", location.getX());
        locationJson.addProperty("y", location.getY());
        locationJson.addProperty("z", location.getZ());
        locationJson.addProperty("yaw", location.getYaw());
        locationJson.addProperty("pitch", location.getPitch());
        if (location.getWorld() != null) {
            locationJson.addProperty("world", location.getWorld().getName());
        }
        return locationJson;
    }

    /**
     * Loads all arenas from the JSON file.
     * Handles conversion from JSON to ArenaEntity objects.
     *
     * @return List of loaded ArenaEntity objects
     */
    @Override
    public List<ArenaEntity> loadArenas() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<ArenaEntity> arenas = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JsonArray arenasArray = gson.fromJson(reader, JsonArray.class);

            if (arenasArray == null) {
                plugin.utils.warning("El archivo JSON no contiene un arreglo de arenas válido.");
                return new ArrayList<>();
            }

            for (JsonElement element : arenasArray) {
                JsonObject arenaJson = element.getAsJsonObject();
                String name = arenaJson.get("name").getAsString();
                String displayName = arenaJson.get("displayName").getAsString();
                World world/* = Bukkit.getWorld(arenaJson.get("world").getAsString())*/;
                Location spawnOne = jsonToLocation(arenaJson.getAsJsonObject("spawnOne"));
                Location spawnTwo = jsonToLocation(arenaJson.getAsJsonObject("spawnTwo"));
                Location center = jsonToLocation(arenaJson.getAsJsonObject("center"));
                Location cornerOne = jsonToLocation(arenaJson.getAsJsonObject("cornerOne"));
                Location cornerTwo = jsonToLocation(arenaJson.getAsJsonObject("cornerTwo"));
                Location playableCornerOne = jsonToLocation(arenaJson.getAsJsonObject("playableCornerOne"));
                Location playableCornerTwo = jsonToLocation(arenaJson.getAsJsonObject("playableCornerTwo"));

                ArenaEntity arena = new ArenaEntity();
                arena.setName(name);
                arena.setDisplayName(displayName);
                if (arenaJson.has("world")) {
                    world = Bukkit.getWorld(arenaJson.get("world").getAsString());
                } else {
                    world = center != null ? center.getWorld() : null;
                }
                // Load quadrant information if available
                GridPos quadrant = null;
                if (arenaJson.has("quadrant")) {
                    JsonObject quadrantJson = arenaJson.getAsJsonObject("quadrant");
                    if (world != null) {
                        int x = quadrantJson.get("x").getAsInt();
                        int z = quadrantJson.get("z").getAsInt();
                        quadrant = new GridPos(world, x, z);
                    }
                }
                arena.setWorld(world);
                arena.setSpawnOne(spawnOne);
                arena.setSpawnTwo(spawnTwo);
                arena.setCenter(center);
                arena.setCornerOne(cornerOne);
                arena.setCornerTwo(cornerTwo);
                arena.setPlayableCornerOne(playableCornerOne);
                arena.setPlayableCornerTwo(playableCornerTwo);
                if (arenaJson.has("blockLogo")) {
                    arena.setBlockLogo(jsonToItemStack(arenaJson.getAsJsonObject("blockLogo")));
                }
                if (arenaJson.has("size")) {
                    try {
                        ArenaSize size = ArenaSize.valueOf(arenaJson.get("size").getAsString().toUpperCase());
                        arena.setArenaSize(size);
                    } catch (IllegalArgumentException e) {
                        plugin.utils.warning("Invalid arena size in JSON for arena: " + name + ". Defaulting to MEDIUM.");
                        arena.setArenaSize(ArenaSize.LARGE);
                    }
                } else arena.setArenaSize(ArenaSize.LARGE);
                
                // Set the quadrant if it was loaded
                if (quadrant != null) {
                    arena.setQuadrant(quadrant);
                } else {
                    // Calculate quadrant based on center if not explicitly set
                    try {
                        arena.updateQuadrant();
                    } catch (IllegalStateException e) {
                        plugin.utils.warning("Could not calculate quadrant for arena " + name + ": " + e.getMessage());
                    }
                }
                arena.setArenaState(new InactiveArenaState());
                arenas.add(arena);
            }
        } catch (IOException e) {
            plugin.utils.severe("Error reading arenas file: " + e.getMessage(),
                    "Stack trace: ");
            plugin.utils.severe(e.getStackTrace());
        }

        return arenas;
    }

    /**
     * Converts a JSON object to a Location.
     *
     * @param locationJson JsonObject containing location data
     * @return Location object created from the JSON data
     * @throws NullPointerException if required fields are missing
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
     * Converts a JSON object to an ItemStack.
     *
     * @param itemJson JsonObject containing item data
     * @return ItemStack created from the JSON data
     * @throws IllegalArgumentException if the material type is invalid
     */
    private ItemStack jsonToItemStack(JsonObject itemJson) {
        Material type = Material.valueOf(itemJson.get("type").getAsString());
        int amount = itemJson.get("amount").getAsInt();
        return new ItemStack(type, amount);
    }

    /**
     * Deletes an arena from the JSON storage.
     *
     * @param arena The ArenaEntity to delete
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
            plugin.utils.severe("Error reading arenas file while deleting arena: " + e.getMessage());
            plugin.utils.severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.utils.severe(element.toString());
            }
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(arenasArray, writer);
        } catch (IOException e) {
            plugin.utils.severe("Error writing arenas file after deleting arena: " + e.getMessage());
            plugin.utils.severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.utils.severe(element.toString());
            }
        }
    }
}
