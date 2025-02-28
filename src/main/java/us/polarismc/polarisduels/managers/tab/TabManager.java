package us.polarismc.polarisduels.managers.tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.player.DuelsPlayer;

import java.util.List;
import java.util.stream.Collectors;

public class TabManager {
    private final Main plugin;

    public TabManager(Main plugin) {
        this.plugin = plugin;
        registerPacketListener();
    }

    private void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(
                        plugin,
                        ListenerPriority.NORMAL,
                        PacketType.Play.Server.PLAYER_INFO
                ) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player receiver = event.getPlayer();
                        DuelsPlayer duelsPlayer = TabManager.this.plugin.getPlayerManager().getDuelsPlayer(receiver);

                        if (duelsPlayer == null || !shouldFilterTab(duelsPlayer)) {
                            return;
                        }

                        filterTabByWorld(event, receiver);
                    }
                }
        );
    }

    private boolean shouldFilterTab(DuelsPlayer duelsPlayer) {
        return duelsPlayer.isDuel()
                || duelsPlayer.isQueue()
                || duelsPlayer.isStartingDuel();
    }

    private void filterTabByWorld(PacketEvent event, Player receiver) {
        PacketContainer originalPacket = event.getPacket();

        try {
            // Verificamos que existan los campos necesarios
            if (originalPacket.getPlayerInfoAction().size() == 0 ||
                    originalPacket.getPlayerInfoDataLists().size() == 0) {
                return;
            }

            // Leemos la acción del paquete, por ejemplo, ADD_PLAYER
            EnumWrappers.PlayerInfoAction action = originalPacket.getPlayerInfoAction().read(0);
            // Solo actuamos para la acción ADD_PLAYER (podrías agregar más si es necesario)
            if (action != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                return;
            }

            // Obtenemos la lista de jugadores
            List<PlayerInfoData> list = originalPacket.getPlayerInfoDataLists().read(0);
            // Filtramos para que queden solo los jugadores del mismo mundo del receptor
            List<PlayerInfoData> filteredList = list.stream()
                    .filter(data -> {
                        Player target = Bukkit.getPlayer(data.getProfile().getUUID());
                        return target != null && target.getWorld().equals(receiver.getWorld());
                    })
                    .collect(Collectors.toList());

            // Cancelamos el paquete original para evitar que se muestre la info sin filtrar
            event.setCancelled(true);

            // Si hay jugadores que mostrar, creamos y enviamos un nuevo paquete con la lista filtrada
            if (!filteredList.isEmpty()) {
                PacketContainer newPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
                newPacket.getPlayerInfoAction().write(0, action);
                newPacket.getPlayerInfoDataLists().write(0, filteredList);

                ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, newPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}