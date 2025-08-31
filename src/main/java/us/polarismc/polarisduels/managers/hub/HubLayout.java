package us.polarismc.polarisduels.managers.hub;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines different layouts for hub items in player inventories.
 * Allows for customizable item placement and different hub configurations.
 */
public enum HubLayout {
    /**
     * Default layout - basic items for all players
     */
    DEFAULT {{
        layout.put(0, HubItem.JOIN_1V1_QUEUE);
        layout.put(1, HubItem.JOIN_2V2_QUEUE);
        layout.put(2, HubItem.JOIN_3V3_QUEUE);
        layout.put(8, HubItem.CREATE_PARTY);
    }},

    /**
     * Party member layout - shown when player is member of a party
     */
    PARTY_MEMBER {{
        layout.put(0, HubItem.PARTY_INFO);
        layout.put(8, HubItem.LEAVE_PARTY);
    }},

    /**
     * Party leader layout - shown when player is leader of a party
     */
    PARTY_LEADER {{
        layout.put(0, HubItem.PARTY_INFO);
        layout.put(1, HubItem.PARTY_FFA);
        layout.put(8, HubItem.LEAVE_DISBAND_PARTY);
    }};

    protected final Map<Integer, HubItem> layout;

    /**
     * Creates a new HubItemLayout.
     */
    HubLayout() {
        this.layout = new HashMap<>();
    }

    /**
     * Gets the layout mapping of slot positions to item types.
     *
     * @return A map of inventory slots to hub item types
     */
    public Map<Integer, HubItem> getLayout() {
        return new HashMap<>(layout);
    }
}