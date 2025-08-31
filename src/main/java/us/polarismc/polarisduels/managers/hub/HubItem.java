package us.polarismc.polarisduels.managers.hub;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;

/**
 * Enumeration of all available hub item types.
 * Each type represents a different functional item in the hub.
 */
@Getter
public enum HubItem {
    JOIN_1V1_QUEUE(Main.getInstance().utils.ib(Material.NAME_TAG).name("<red>1v1 Queue").lore("Use this item to enter the 1v1 Queue!").build()),
    JOIN_2V2_QUEUE(Main.getInstance().utils.ib(Material.NAME_TAG).name("<red>2v2 Queue").lore("Use this item to enter the 2v2 Queue!").build()),
    JOIN_3V3_QUEUE(Main.getInstance().utils.ib(Material.NAME_TAG).name("<red>3v3 Queue").lore("Use this item to enter the 3v3 Queue!").build()),
    LEAVE_QUEUE(Main.getInstance().utils.ib(Material.BARRIER).name("<red>Leave Queue").lore("Use this item to leave the queue!").build()),
    CREATE_PARTY(Main.getInstance().utils.ib(Material.ENDER_PEARL).name("<green>Create Party").lore("Use this item to create a party!").build()),
    LEAVE_PARTY(Main.getInstance().utils.ib(Material.RED_DYE).name("<red>Leave Party").lore("Use this item to leave your party!").build()),
    LEAVE_DISBAND_PARTY(Main.getInstance().utils.ib(Material.RED_DYE).name("<red>Leave or Disband Party").lore("Use this item to leave or disband your party!").build()),
    PARTY_INFO(Main.getInstance().utils.ib(Material.KNOWLEDGE_BOOK).name("<#87CEEB>Party Info").lore("Use this item to view your party members and statistics!").build()),
    PARTY_FFA(Main.getInstance().utils.ib(Material.CROSSBOW).name("<#FF6B6B>Party FFA").lore("Start a free-for-all battle with your party members!").build());

    private final ItemStack item;

    HubItem(ItemStack item) {
        this.item = item;
    }
}