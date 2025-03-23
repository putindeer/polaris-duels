package us.polarismc.polarisduels.utils;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Component chat(String s) {
        return MiniMessage.miniMessage().deserialize(convert(s));
    }

    /**
     * Esto está aquí únicamente para compatibilidad con algunos métodos.
     */
    public Component chat(Component s) {
        return s;
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
     * Envía un mensaje versátil a un destinatario con múltiples opciones de configuración.<br><br>
     * Este método permite enviar mensajes con:<br><br>
     * - Prefijo opcional<br>
     * - Múltiples tipos de mensajes ({@link String} o {@link Component})<br>
     * - Reproducción opcional de sonido<br><br>
     *
     * Existen varios submetodos más simples en caso de que hayan argumentos innecesarios o se necesite enviar a varios jugadores:<br><br>
     * - {@code message(receivers, messages)}: Envía mensaje con prefijo por defecto<br>
     * - {@code message(receivers, sound, messages)}: Envía mensaje con sonido<br>
     * - {@code message(receivers, prefix,  messages)}: Envía mensaje con prefix<br><br>
     * También existe el método {@code broadcast(prefix, sound, messages)} que envia un mensaje a todos los jugadores, incluyendo la consola.
     * Este método también cuenta con los submetodos anteriores.
     *
     * @param receiver Destinatario del mensaje (puede ser un {@link Player} o un {@link CommandSender})
     * @param usePrefix Determina si se usa el prefix del plugin en el mensaje
     * @param sound Sonido opcional para reproducir al enviar el mensaje (solo se aplica a {@link Player})
     * @param messages Los mensajes ({@link String} o {@link Component}) que se tienen que enviar
     *
     */
    public void message(CommandSender receiver, boolean usePrefix, Sound sound, Component... messages) {
        for (Component component : messages) {
            Component prefixComponent = usePrefix ? prefix : Component.text("");
            receiver.sendMessage(prefixComponent.append(chat(component)));
        }

        if (sound != null && receiver instanceof Player player) {
            player.playSound(sound);
        }
    }

    //region [Submetodos de 'message']
    //region [Metodos de String]
    public void broadcast(String... messages) {
        broadcast(true, null, messages);
    }

    public void broadcast(Sound sound, String... messages) {
        broadcast(true, sound, messages);
    }

    public void broadcast(boolean prefix, Sound sound, String... messages) {
        message(Stream.concat(Bukkit.getOnlinePlayers().stream().map(p -> (CommandSender) p), Stream.of(Bukkit.getConsoleSender())).collect(Collectors.toList()), prefix, sound, messages);
    }

    public void message(CommandSender receiver, String... messages) {
        message(receiver, true, null, messages);
    }

    public void message(CommandSender receiver, Sound sound, String... messages) {
        message(receiver, true, sound, messages);
    }

    public void message(CommandSender receiver, boolean prefix, String... messages) {
        message(receiver, prefix, null, messages);
    }

    public void message(Collection<? extends CommandSender> receivers, String... messages) {
        message(receivers, true, null, messages);
    }

    public void message(Collection<? extends CommandSender> receivers, boolean prefix, String... messages) {
        message(receivers, prefix, null, messages);
    }

    public void message(Collection<? extends CommandSender> receivers, boolean prefix, Sound sound, String... messages) {
        receivers.forEach(receiver -> message(receiver, prefix, sound, Arrays.stream(messages).map(this::chat).toArray(Component[]::new)));
    }

    public void message(CommandSender receiver, boolean prefix, Sound sound, String... messages) {
        message(receiver, prefix, sound, Arrays.stream(messages).map(this::chat).toArray(Component[]::new));
    }
    //endregion
    //region [Métodos de Component]
    public void broadcast(Component... messages) {
        broadcast(true, null, messages);
    }

    public void broadcast(Sound sound, Component... messages) {
        broadcast(true, sound, messages);
    }

    public void broadcast(boolean prefix, Sound sound, Component... messages) {
        message(Stream.concat(Bukkit.getOnlinePlayers().stream().map(p -> (CommandSender) p), Stream.of(Bukkit.getConsoleSender())).collect(Collectors.toList()), prefix, sound, messages);
    }

    public void message(CommandSender receiver, Component... messages) {
        message(receiver, true, null, messages);
    }

    public void message(CommandSender receiver, Sound sound, Component... messages) {
        message(receiver, true, sound, messages);
    }

    public void message(Collection<? extends CommandSender> receivers, Component... messages) {
        message(receivers, true, null, messages);
    }

    public void message(Collection<? extends CommandSender> receivers, boolean prefix, Component... messages) {
        message(receivers, prefix, null, messages);
    }

    public void message(Collection<? extends CommandSender> receivers, boolean prefix, Sound sound, Component... messages) {
        receivers.forEach(receiver -> message(receiver, prefix, sound, messages));
    }

    public void message(CommandSender receiver, boolean prefix, Component... messages) {
        message(receiver, prefix, null, messages);
    }
    //endregion
    //endregion

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
            world.setAutoSave(false);

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
     * <br>
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
