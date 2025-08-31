package us.polarismc.polarisduels.game;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import us.polarismc.polarisduels.Main;
import us.polarismc.polarisduels.arenas.entity.ArenaSize;

import java.util.EnumSet;
import java.util.List;

@Getter
public enum KitType {
    SMP(new ItemStack[] {
            Main.getInstance().utils.ib(Material.NETHERITE_SWORD).enchant(Enchantment.FIRE_ASPECT, 2).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_AXE).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            new ItemStack(Material.ENDER_PEARL, 16),
            Main.getInstance().utils.ib(Material.NETHERITE_SWORD).enchant(Enchantment.FIRE_ASPECT, 2).enchant(Enchantment.KNOCKBACK, 1).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            new ItemStack(Material.ENDER_PEARL, 16), new ItemStack(Material.ENDER_PEARL, 16),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            Main.getInstance().utils.ib(Material.NETHERITE_BOOTS).enchant(Enchantment.FEATHER_FALLING, 4).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_LEGGINGS).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_CHESTPLATE).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_HELMET).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.SHIELD).enchant(Enchantment.MENDING, 1).enchant(Enchantment.UNBREAKING, 3).build()
    }, EnumSet.of(GameAttribute.NO_BLOCK_PLACE, GameAttribute.NO_BLOCK_BREAK, GameAttribute.HEALTH_INDICATOR),
            1, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.ENDER_PEARL).hideGUI().build(), "<#ff00d1>SMP",
            List.of("Survival‑style PvP with maxed-out gear, pearls and totems.",
                    "Endurance-based PvP without arena destruction.")),

    AXE(new ItemStack[] {
            new ItemStack(Material.DIAMOND_AXE, 1),
            new ItemStack(Material.DIAMOND_SWORD, 1),
            new ItemStack(Material.CROSSBOW, 1),
            new ItemStack(Material.BOW, 1),
            new ItemStack(Material.ARROW, 6),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            new ItemStack(Material.DIAMOND_BOOTS, 1),
            new ItemStack(Material.DIAMOND_LEGGINGS, 1),
            new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
            new ItemStack(Material.DIAMOND_HELMET, 1),
            new ItemStack(Material.SHIELD, 1)
    }, EnumSet.of(GameAttribute.NO_BLOCK_PLACE, GameAttribute.NO_BLOCK_BREAK, GameAttribute.NO_NATURAL_REGEN, GameAttribute.NO_HUNGER, GameAttribute.HEALTH_INDICATOR),
            2, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.DIAMOND_AXE).hideGUI().build(), "<#9300ff>Axe",
            List.of("Diamond‑axe combat mixed with shield and crossbow tactics.",
                    "Low attack speed, high burst damage.")),

    NETHPOT(new ItemStack[] {
            Main.getInstance().utils.ib(Material.NETHERITE_SWORD).enchant(Enchantment.SHARPNESS, 5).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_BOOTS).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_LEGGINGS).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_CHESTPLATE).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_HELMET).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1)
    }, EnumSet.of(GameAttribute.NO_BLOCK_PLACE, GameAttribute.NO_BLOCK_BREAK, GameAttribute.HEALTH_INDICATOR),
            1, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.NETHERITE_HELMET).hideGUI().build(), "<#808080>NetheritePot",
            List.of("Full netherite armor with splash healing potions.",
                    "Leverage pot‑timing and strong‑hit combos to break defenses.")),

    UHC(new ItemStack[] {
            Main.getInstance().utils.ib(Material.DIAMOND_AXE).enchant(Enchantment.EFFICIENCY, 3).enchant(Enchantment.SHARPNESS, 1).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_SWORD).enchant(Enchantment.SHARPNESS, 4).build(),
            Main.getInstance().utils.ib(Material.CROSSBOW).enchant(Enchantment.PIERCING, 1).build(),
            Main.getInstance().utils.ib(Material.BOW).enchant(Enchantment.POWER, 1).build(),
            new ItemStack(Material.GOLDEN_APPLE, 13),
            new ItemStack(Material.COBWEB, 8),
            new ItemStack(Material.LAVA_BUCKET, 1),
            new ItemStack(Material.WATER_BUCKET, 1),
            new ItemStack(Material.COBBLESTONE, 64),
            null, null, null,
            Main.getInstance().utils.ib(Material.DIAMOND_PICKAXE).enchant(Enchantment.EFFICIENCY, 3).build(),
            new ItemStack(Material.WATER_BUCKET, 1),
            new ItemStack(Material.WATER_BUCKET, 1),
            new ItemStack(Material.SHIELD, 1),
            null, null, null, null, null, null,
            new ItemStack(Material.WATER_BUCKET, 1),
            new ItemStack(Material.LAVA_BUCKET, 1),
            null, null,
            new ItemStack(Material.OAK_PLANKS, 64),
            null, null,
            new ItemStack(Material.ARROW, 16),
            null, null, null, null, null,
            new ItemStack(Material.OAK_PLANKS, 64),
            Main.getInstance().utils.ib(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 2).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemStack(Material.SHIELD)
    }, EnumSet.of(GameAttribute.NO_CRAFTING, GameAttribute.NO_NATURAL_REGEN, GameAttribute.NO_HUNGER, GameAttribute.NO_ARENA_DESTRUCTION, GameAttribute.HEALTH_INDICATOR),
            1, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.GOLDEN_APPLE).hideGUI().build(), "<#d1bb00>UHC",
            List.of("No natural regeneration; heal only with golden apples.",
                    "Carry water, lava, blocks and cobwebs for tactical utility.")),

    DIAMONDPOT(new ItemStack[] {
            Main.getInstance().utils.ib(Material.DIAMOND_SWORD).enchant(Enchantment.SHARPNESS, 5).build(),
            Main.getInstance().utils.ib(Material.BOW).enchant(Enchantment.POWER, 5).enchant(Enchantment.PUNCH, 2).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_REGENERATION).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.ARROW, 24),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_REGENERATION).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_REGENERATION).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.COOKED_BEEF, 20)
    }, EnumSet.of(GameAttribute.NO_BLOCK_PLACE, GameAttribute.NO_BLOCK_BREAK, GameAttribute.ONE_THIRD_MORE_MELEE_DAMAGE, GameAttribute.HEALTH_INDICATOR),
            1, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.HEALING).hideGUI().build(), "<#ff0000>DiamondPot",
            List.of("Diamond armor with splash healing potions.",
                    "Classic potion PvP mode with 33% increased damage.")),

    SWORD(new ItemStack[] {
            Main.getInstance().utils.ib(Material.DIAMOND_SWORD).enchant(Enchantment.SWEEPING_EDGE, 3).build(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            Main.getInstance().utils.ib(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 3).build(),
            null
    }, EnumSet.of(GameAttribute.NO_BLOCK_PLACE, GameAttribute.NO_BLOCK_BREAK, GameAttribute.NO_COMPLETE_HUNGER_LOSS, GameAttribute.HEALTH_INDICATOR),
            2, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.DIAMOND_SWORD).hideGUI().build(), "<#00fff3>Sword",
            List.of("Classic sword PvP without shields.",
                    "Focuses on timing and spacing.")),

    MACE(new ItemStack[]{
            Main.getInstance().utils.ib(Material.SHIELD).enchant(Enchantment.UNBREAKING, 3).enchant(Enchantment.MENDING).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_SWORD).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            Main.getInstance().utils.ib(Material.NETHERITE_AXE).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.WIND_CHARGE, 64),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            new ItemStack(Material.ENDER_PEARL, 16),
            Main.getInstance().utils.ib(Material.MACE).enchant(Enchantment.DENSITY, 4).enchant(Enchantment.UNBREAKING, 3).enchant(Enchantment.WIND_BURST, 2).build(),
            Main.getInstance().utils.ib(Material.ELYTRA).enchant(Enchantment.UNBREAKING, 1).build(),
            Main.getInstance().utils.ib(Material.MACE).enchant(Enchantment.BREACH, 3).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.ENDER_PEARL, 16),
            new ItemStack(Material.WIND_CHARGE, 64),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemStack(Material.ENDER_PEARL, 16),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.LONG_LEAPING).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            new ItemStack(Material.TOTEM_OF_UNDYING),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 32),
            Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), Main.getInstance().utils.ib(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).enchant(Enchantment.MENDING).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).enchant(Enchantment.MENDING).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).enchant(Enchantment.MENDING).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).enchant(Enchantment.MENDING).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING)
    }, EnumSet.of(GameAttribute.NO_ARENA_LIMITS, GameAttribute.HEALTH_INDICATOR),
            1, ArenaSize.LARGE,
            Main.getInstance().utils.ib(Material.MACE).hideGUI().build(), "<#04cd14>Mace",
            List.of("PvP based on the mace as main weapon.",
                    "Combines aerial movement and burst damage with wind charges and elytra.",
                    "<red>Experimental kit from Enrico & Lurrn's TL.")),

    MARLOWUHC(new ItemStack[] {
            Main.getInstance().utils.ib(Material.DIAMOND_SWORD).enchant(Enchantment.SHARPNESS, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_AXE).enchant(Enchantment.EFFICIENCY, 3).build(),
            Main.getInstance().utils.marlowGHead(2),
            new ItemStack(Material.GOLDEN_APPLE, 8),
            new ItemStack(Material.WATER_BUCKET),
            new ItemStack(Material.LAVA_BUCKET),
            Main.getInstance().utils.ib(Material.CROSSBOW).enchant(Enchantment.PIERCING, 1).chargedProjectiles(new ItemStack(Material.ARROW)).build(),
            new ItemStack(Material.OAK_PLANKS, 64),
            new ItemStack(Material.COBWEB, 8),
            null, null, null,
            new ItemStack(Material.WATER_BUCKET),
            new ItemStack(Material.WATER_BUCKET),
            new ItemStack(Material.WATER_BUCKET),
            null, null,
            new ItemStack(Material.ARROW, 10),
            null, null, null, null,
            new ItemStack(Material.LAVA_BUCKET),
            null, null, null,
            Main.getInstance().utils.ib(Material.BOW).enchant(Enchantment.POWER, 1).build(),
            null, null, null, null,
            Main.getInstance().utils.ib(Material.DIAMOND_PICKAXE).enchant(Enchantment.EFFICIENCY, 3).build(),
            null, null, null,
            new ItemStack(Material.OAK_PLANKS, 64),
            Main.getInstance().utils.ib(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 3).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 2).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 2).build(),
            Main.getInstance().utils.ib(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemStack(Material.SHIELD)
    }, EnumSet.of(GameAttribute.NO_CRAFTING, GameAttribute.NO_NATURAL_REGEN, GameAttribute.NO_HUNGER, GameAttribute.NO_ARENA_DESTRUCTION, GameAttribute.HEALTH_INDICATOR),
            1, ArenaSize.LARGE,
            Main.getInstance().utils.goldenHeadTexture().hideGUI().build(), "<#d1bb00>Marlow UHC",
            List.of("Similar to UHC but with player heads for extra healing.",
                    "No natural regeneration; heal with golden apples and heads."));

    private final ItemStack[] defaultInv;
    private final EnumSet<GameAttribute> attributes;
    private final int defaultRounds;
    private final ArenaSize recommendedSize;
    private final ItemStack kitItem;
    private final String displayName;
    private final List<String> description;

    KitType(ItemStack[] inv, EnumSet<GameAttribute> attributes, int defaultRounds, ArenaSize recommendedSize,
            ItemStack kitItem, String displayName, List<String> description) {
        this.defaultInv = inv;
        this.attributes = attributes;
        this.defaultRounds = defaultRounds;
        this.recommendedSize = recommendedSize;
        this.kitItem = kitItem;
        this.displayName = displayName;
        this.description = description;
    }

    public boolean hasAttribute(GameAttribute attribute) {
        return attributes.contains(attribute);
    }
}