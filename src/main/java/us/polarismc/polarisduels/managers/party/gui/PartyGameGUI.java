package us.polarismc.polarisduels.managers.party.gui;

import fr.mrmicky.fastinv.FastInv;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.game.GameType;
import us.polarismc.polarisduels.managers.party.Party;
import us.polarismc.polarisduels.game.KitType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PartyGameGUI extends FastInv {
    private final Main plugin;
    private final Party party;
    private final Player player;
    private final KitType kit;
    private final GameType gameType;
    private final int rounds;
    private final int teamSize;
    private final int teamCount;

    public PartyGameGUI(Player player, Main plugin) {
        super(owner -> Bukkit.createInventory(owner, 45, plugin.utils.chat("<#FF6B6B>Party FFA Settings")));

        this.plugin = plugin;
        this.player = player;
        this.party = plugin.getPartyManager().getParty(player);

        this.kit = party.getGameSettings().getKit();
        this.gameType = party.getGameSettings().getGameType();
        this.rounds = party.getGameSettings().getRounds();
        this.teamSize = party.getGameSettings().getTeamSize();
        this.teamCount = party.getGameSettings().getTeamCount();

        openGameGUI();
        addClickHandler(event -> event.setCancelled(true));
        open(player);
    }

    private void openGameGUI() {
        for (int i = 0; i < 45; i++) {
            setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        setItem(10, getGameTypeItem(), getGameTypeClickHandler());

        ItemStack kitItem;

        //todo - test between simple (<gray>) or hex descriptions

        if (kit == null) {
            kitItem = plugin.utils.ib(Material.BARRIER).name("<#FF6B6B>No Kit Selected")
                    .lore("No combat kit selected",
                            "<#98FB98>Click to browse available kits.").build();
        } else {
            kitItem = plugin.utils.ib(kit.getKitItem()).name("<u>" + kit.getDisplayName())
                    .lore("Selected combat kit",
                            "<#98FB98>Click to browse other kits.").build();
        }
        setItem(13, kitItem, event -> openKitGUI());

        ItemStack roundsItem = plugin.utils.ib(Material.COMPASS)
                .name("<#87CEEB>Rounds")
                .lore("<dark_gray>┤ First to 1",
                        "<dark_gray>┤ First to 3",
                        "<dark_gray>┤ First to 5",
                        "<green>┤ First to 10 <red>←",
                        "",
                        "<yellow>Left-Click: <white>Switch rounds",
                        "<aqua>Right-Click: <white>Manual setup")
                .build();

        setItem(16, roundsItem, event -> openRoundsGUI());
        
        setItem(31, plugin.utils.ib(Material.DIAMOND_SWORD)
                        .name("<#00FF00>🚀 Start FFA Battle")
                        .lore("<#F0E68C>Begin the free-for-all duel",
                                "<#87CEEB>May the best fighter win!")
                        .build(),
                event -> {
                    if (kit == null) {
                        plugin.utils.message(player, "<red>Please select a kit before starting a duel.");
                    } else {
                        plugin.getPartyManager().createPartyFFA(party, kit, rounds);
                    }
                    player.closeInventory();
                });
    }

    private void openRoundsGUI() {
        FastInv gui = new FastInv(owner -> Bukkit.createInventory(owner, 27, plugin.utils.chat("<#87CEEB>Select Rounds")));

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        int[] roundOptions = {1, 3, 5, 7, 10};
        int[] slots = {10, 11, 12, 13, 14};

        for (int i = 0; i < roundOptions.length; i++) {
            int roundOption = roundOptions[i];
            Material material = rounds == roundOption ? Material.LIME_STAINED_GLASS_PANE : Material.WHITE_STAINED_GLASS_PANE;

            gui.setItem(slots[i], plugin.utils.ib(material)
                            .name("<#FFD700>" + roundOption + " Round" + (roundOption > 1 ? "s" : ""))
                            .lore("<#F0E68C>Set rounds to " + roundOption,
                                    rounds == roundOption ? "<#00FF00>Currently selected" : "<#98FB98>Click to select")
                            .build(),
                    event -> {
                        party.getGameSettings().setRounds(roundOption);
                        new PartyGameGUI(player, plugin);
                    });
        }

        gui.setItem(22, plugin.utils.ib(Material.BARRIER).name("<red>Go Back").lore("<gray>Click this to go back to the previous menu.").build(),
                event -> new PartyGameGUI(player, plugin));

        gui.addClickHandler(event -> event.setCancelled(true));
        gui.open(player);
    }

    private void openKitGUI() {
        FastInv gui = new FastInv(owner -> Bukkit.createInventory(owner, 54, plugin.utils.chat("<#FFD700>Select Kit")));

        for (int i = 0; i < 54; i++) {
            gui.setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        KitType[] kits = KitType.values();
        int[] slots = {10, 12, 14, 16, 28, 30, 32, 34};
        int slotIndex = 0;

        for (KitType kit : kits) {
            if (slotIndex >= slots.length) break;

            ItemStack item = plugin.utils.ib(kit.getKitItem()).glintIf(this.kit == kit).build();
            List<String> lore = new ArrayList<>();
            kit.getDescription().forEach(line -> lore.add("<#F0E68C>" + line));
            lore.add(this.kit == kit ? "<#00FF00>Currently selected" : "<#98FB98>Click to select");

            gui.setItem(slots[slotIndex], plugin.utils.ib(item)
                            .customName("<#FFD700>" + kit.getDisplayName())
                            .lore(lore)
                            .build(),
                    event -> {
                        party.getGameSettings().setKit(kit);
                        new PartyGameGUI(player, plugin);
                    });
            slotIndex++;
        }

        gui.setItem(49, plugin.utils.ib(Material.BARRIER).name("<red>Go Back").lore("<gray>Click this to go back to the previous menu.").build(),
                event -> new PartyGameGUI(player, plugin));

        gui.addClickHandler(event -> event.setCancelled(true));
        gui.open(player);
    }

    private void openGameTypeGUI() {
        FastInv gui = new FastInv(owner -> Bukkit.createInventory(owner, 27, plugin.utils.chat("<#FFD700>Select Game Type")));

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, plugin.utils.ib(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().build());
        }

        gui.setItem(11, plugin.utils.ib(Material.CROSSBOW)
                        .name("<#FF6B6B>Free For All")
                        .lore("Everyone fights for themselves",
                                "Last player standing wins",
                                gameType == GameType.PARTY_FFA ? "<#00FF00>Currently selected" : "Click to select")
                        .build(),
                e -> {
                    party.getGameSettings().setGameType(GameType.PARTY_FFA);
                    player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1.0f));
                    new PartyGameGUI(player, plugin);
                });

        gui.setItem(13, plugin.utils.ib(Material.SHEARS)
                        .name("<#87CEEB>Split Teams")
                        .lore("Divide party into equal teams",
                                "Balanced automatic assignment",
                                gameType == GameType.PARTY_SPLIT ? "<#00FF00>Currently selected" : "Click to select")
                        .build(),
                e -> {
                    party.getGameSettings().setGameType(GameType.PARTY_SPLIT);
                    player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1.0f));
                    new PartyGameGUI(player, plugin);
                });

        gui.setItem(15, plugin.utils.ib(Material.WHITE_BANNER)
                        .name("<#FFD700>Team Size")
                        .lore("Set team size and create multiple teams",
                                "Players divided by specified team size",
                                gameType == GameType.PARTY_TEAMS ? "<#00FF00>Currently selected" : "Click to select")
                        .build(),
                e -> {
                    party.getGameSettings().setGameType(GameType.PARTY_TEAMS);
                    player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1.0f));
                    new PartyGameGUI(player, plugin);
                });

        gui.setItem(22, plugin.utils.ib(Material.BARRIER).name("<red>Go Back").lore("<gray>Click this to go back to the previous menu.").build(),
                event -> new PartyGameGUI(player, plugin));

        gui.addClickHandler(event -> event.setCancelled(true));
        gui.open(player);
    }

    private ItemStack getGameTypeItem() {
        //todo - añadir que muestre el teamsize establecido en el contador del itme
        return switch (gameType) {
            case PARTY_FFA -> plugin.utils.ib(Material.CROSSBOW)
                    .name("<#FFD700>Game Type")
                    .lore("Current mode: Party FFA",
                            "Everyone fights for themselves",
                            "Click to change game mode")
                    .build();

            case PARTY_SPLIT -> plugin.utils.ib(Material.SHEARS)
                    .name("<#FFD700>Game Type")
                    .lore("Current mode: Party Split",
                            "Teams: " + teamCount + " (" + calculateSplitTeamSizes() + ")",
                            "Click to change | Shift+Click to adjust teams")
                    .build();

            case PARTY_TEAMS -> plugin.utils.ib(Material.WHITE_BANNER)
                    .name("<#FFD700>Game Type")
                    .lore("Current mode: Party Teams",
                            "Team size: " + teamSize + " (" + calculateTeamsCount() + " teams)",
                            "Click to change | Shift+Click to adjust size")
                    .build();

            default -> new ItemStack(Material.AIR);
        };
    }

    private String calculateSplitTeamSizes() {
        int players = party.getOnlineMembers().size();
        int baseSize = players / teamCount;
        int remainder = players % teamCount;
        if (remainder == 0) {
            return teamCount + "x" + baseSize;
        } else {
            return remainder + "x" + (baseSize + 1) + ", " + (teamCount - remainder) + "x" + baseSize;
        }
    }

    private int calculateTeamsCount() {
        return party.getOnlineMembers().size() / teamSize;
    }

    private Consumer<InventoryClickEvent> getGameTypeClickHandler() {
        //TODO - esto en realidad va en playingarenastate, pero necesito que cuando sean más de 8 teams se manejen los teams por números en vez de color.
        return e -> {
            if (e.isShiftClick()) {
                switch (gameType) {
                    case PARTY_SPLIT -> {
                        if (e.isLeftClick()) {
                            if (teamCount < party.getOnlineMembers().size()) {
                                party.getGameSettings().setTeamCount(teamCount + 1);
                                player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1.2f));
                            } else {
                                player.playSound(Sound.sound(SoundEventKeys.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 0.5f, 1.0f));
                                plugin.utils.message(player, "<red>Team count cannot exceed the number of players on your party.");
                            }
                        } else if (e.isRightClick()) {
                            if (teamCount > 2) {
                                party.getGameSettings().setTeamCount(teamCount - 1);
                                player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 0.8f));
                            } else {
                                player.playSound(Sound.sound(SoundEventKeys.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 0.5f, 1.0f));
                                plugin.utils.message(player, "<red>A minimum of 2 teams is required for split mode.");
                            }
                        }
                        new PartyGameGUI(player, plugin);
                    }
                    case PARTY_TEAMS -> {
                        if (e.isLeftClick()) {
                            if (teamSize < party.getOnlineMembers().size()) {
                                party.getGameSettings().setTeamSize(teamSize + 1);
                                player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1.2f));
                            } else {
                                player.playSound(Sound.sound(SoundEventKeys.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 0.5f, 1.0f));
                                plugin.utils.message(player, "<red>Team size cannot exceed the number of online players.");
                            }
                        } else if (e.isRightClick()) {
                            if (teamSize > 1) {
                                party.getGameSettings().setTeamSize(teamSize - 1);
                                player.playSound(Sound.sound(SoundEventKeys.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 0.8f));
                            } else {
                                player.playSound(Sound.sound(SoundEventKeys.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 0.5f, 1.0f));
                                plugin.utils.message(player, "<red>Team size must be at least 1 player.");
                            }
                        }
                        new PartyGameGUI(player, plugin);
                    }
                }
            } else {
                openGameTypeGUI();
            }
        };
    }
}