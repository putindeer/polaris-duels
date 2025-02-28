package us.polarismc.polarisduels.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("unused")
public class Utils {
    private final Main plugin;

    public Utils(Main plugin) {
        this.plugin = plugin;
    }
    /**
     * Prefix del plugin
     */
    public final Component prefix = chat("&bDuels &7» &r");

    /**
     * Convierte un texto con códigos HEX a un 'Component'
     * @param s La 'String' que recibe
     * @return El texto convertido
     */
    public Component chat(String s){
        return MiniMessage.miniMessage().deserialize(convert(s));
    }
    public Component chat(Component s){
        return MiniMessage.miniMessage().deserialize(convert(PlainTextComponentSerializer.plainText().serialize(s)));
    }

    /**
     * Convierte códigos HEX a tags de MiniMessage
     * @param s La 'String' con códigos HEX
     * @return La 'String' con tags de MiniMessage
     */
    public String convert(String s) {
        s = s.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
        return s.replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>").replace("&3", "<dark_aqua>").replace("&4", "<dark_red>").replace("&5", "<dark_purple>").replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>").replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>").replace("&c", "<red>").replace("&d", "<light_purple>").replace("&e", "<yellow>").replace("&f", "<white>").replace("&n", "<underlined>").replace("&m", "<strikethrough>").replace("&k", "<obfuscated>").replace("&o", "<italic>").replace("&l", "<bold>").replace("&r", "<reset>");
    }

    /**
     * Envia un mensaje a todos los jugadores del servidor
     * @param c El texto, como 'String' o 'Component'
     */
    public void broadcast(String c) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }
    public void broadcast(Component c) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }

    /**
     * Envia un mensaje y un sonido a todos los jugadores del servidor
     * @param c El texto que quieres mostrar, como 'String' o 'Component'
     * @param s El sonido
     */
    public void broadcast(String c, Sound s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
            p.playSound(p.getLocation(), s, 10, 1);
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }
    public void broadcast(Component c, Sound s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
            p.playSound(p.getLocation(), s, 10, 1);
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }

    /**
     * Envia un mensaje a uno o más jugadores
     * @param ps Los jugadores que recibirán los textos
     * @param c Los textos, como 'String' o 'Component'
     */
    public void message(Collection<Player> ps, String... c) {
        for (Player p : ps) {
            for (String part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
        }
    }

    public void message(Collection<Player> ps, Component... c) {
        for (Player p : ps) {
            for (Component part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
        }
    }

    public void message(CommandSender p, String... c) {
        for (String part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
    }

    public void message(CommandSender p, Component... c) {
        for (Component part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
    }

    /**
     * Envia un mensaje y un sonido a uno o más jugadores
     * @param ps Los jugadores que recibirán los textos
     * @param s El sonido
     * @param c Los textos, como 'String' o 'Component'
     */
    public void message(Collection<Player> ps, Sound s, String... c) {
        for (Player p : ps) {
            for (String part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
            p.playSound(p.getLocation(), s, 10, 1);
        }
    }

    public void message(Collection<Player> ps, Sound s, Component... c) {
        for (Player p : ps) {
            for (Component part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
            p.playSound(p.getLocation(), s, 10, 1);
        }
    }

    public void message(CommandSender cs, Sound s, String... c) {
        Player p = (Player) cs;
        for (String part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
        p.playSound(p.getLocation(), s, 10, 1);
    }

    public void message(CommandSender cs, Sound s, Component... c) {
        Player p = (Player) cs;
        for (Component part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
        p.playSound(p.getLocation(), s, 10, 1);
    }

    /**
     * Restablece la vida del jugador al máximo
     * @param p El jugador
     */
    public void setMaxHealth(Player p) {
        p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
    }

    /**
     * Determina si una localización está dentro de un area determinada
     * @param loc Localización a determinar
     * @param cornerOne Primera esquina del area determinada
     * @param cornerTwo Segunda esquina del area determinada
     * @return Si la localización está dentro devuelve 'true', si está fuera, devuelve 'false'
     */
    public boolean isInside(Location loc, Location cornerOne, Location cornerTwo) {
        if (cornerOne == null || cornerTwo == null) return false;

        double minX = Math.min(cornerOne.getX(), cornerTwo.getX());
        double maxX = Math.max(cornerOne.getX(), cornerTwo.getX());
        double minY = Math.min(cornerOne.getY(), cornerTwo.getY());
        double maxY = Math.max(cornerOne.getY(), cornerTwo.getY());
        double minZ = Math.min(cornerOne.getZ(), cornerTwo.getZ());
        double maxZ = Math.max(cornerOne.getZ(), cornerTwo.getZ());

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    public boolean isInWorld(Location loc, World world) {
        return loc.getWorld().getName().equalsIgnoreCase(world.getName());
    }

    /**
     * Crea un mundo completamente vacio con un bloque de cristal en [0,64,0]
     * @param name El nombre del mundo
     * @return Devuelve el mundo creado
     */
    public World createVoidWorld(String name) {
        WorldCreator creator = new WorldCreator(name);
        creator.generator(new VoidGenerator());
        creator.createWorld();

        World world = Bukkit.getWorld(name);

        if (world != null) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
            world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);

            return world;
        } else {
            plugin.getLogger().severe("The world '" + name + "' was not loaded.");
            return null;
        }
    }

    /**
     * Ejecuta una tarea ({@link Runnable}) después de un tiempo especificado.
     * @param delay El tiempo de espera en ticks antes de ejecutar.
     * @param run La tarea a ejecutar, implementada como un {@code Runnable}.
     */
    public void delay(int delay, Runnable run) {
        Bukkit.getScheduler().runTaskLater(Main.pl, run,delay);
    }

    public void delay(Runnable run) {
        delay(1, run);
    }

    /**
     * Verifica si un inventario tiene espacio suficiente para un ItemStack específico,
     * considerando la posibilidad de apilarlo con stacks existentes en el inventario.
     * <p>
     * Es útil para determinar si, al agregar un ítem, este será almacenado correctamente
     * o si terminará en el cursor del jugador o eliminado por falta de espacio.
     *
     * @param inv  El inventario al que se quiere añadir el ítem.
     * @param item El ítem que se quiere añadir.
     * @return {@code true} si el ítem se puede almacenar completamente, {@code false} si no hay espacio suficiente.
     */
    public boolean canCompletelyStore(Inventory inv, ItemStack item) {
        int toStore = item.getAmount();

        for (ItemStack stack : inv.getContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                toStore -= item.getMaxStackSize();
            }
            else if (stack.isSimilar(item)) {
                int space = stack.getMaxStackSize() - stack.getAmount();
                toStore -= space;
            }

            if (toStore <= 0) {
                return true;
            }
        }
        return false;
    }
}
