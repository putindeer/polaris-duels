package us.polarismc.polarisduels.queue;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import us.polarismc.polarisduels.arenas.entity.ArenaAttribute;
import us.polarismc.polarisduels.utils.ItemBuilder;

import java.util.EnumSet;

@Getter
public enum KitType {
    SMP(new ItemStack[] {
            new ItemBuilder(Material.NETHERITE_SWORD).enchant(Enchantment.FIRE_ASPECT, 2).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_AXE).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            new ItemStack(Material.ENDER_PEARL, 16),
            new ItemBuilder(Material.NETHERITE_SWORD).enchant(Enchantment.FIRE_ASPECT, 2).enchant(Enchantment.KNOCKBACK, 1).enchant(Enchantment.SHARPNESS, 5).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            new ItemStack(Material.ENDER_PEARL, 16), new ItemStack(Material.ENDER_PEARL, 16),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            new ItemBuilder(Material.NETHERITE_BOOTS).enchant(Enchantment.FEATHER_FALLING, 4).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_LEGGINGS).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_CHESTPLATE).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_HELMET).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.SHIELD).enchant(Enchantment.MENDING, 1).enchant(Enchantment.UNBREAKING, 3).build()
    }, EnumSet.of(ArenaAttribute.NO_BLOCK_PLACE, ArenaAttribute.NO_BLOCK_BREAK)),

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
    }, EnumSet.of(ArenaAttribute.NO_BLOCK_PLACE, ArenaAttribute.NO_BLOCK_BREAK, ArenaAttribute.NO_NATURAL_REGEN, ArenaAttribute.NO_HUNGER)),

    NETHPOT(new ItemStack[] {
            new ItemBuilder(Material.NETHERITE_SWORD).enchant(Enchantment.SHARPNESS, 5).build(),
            new ItemStack(Material.GOLDEN_APPLE, 64),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_FIRE_RESISTANCE).build(),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.NETHERITE_BOOTS).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_LEGGINGS).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_CHESTPLATE).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.NETHERITE_HELMET).enchant(Enchantment.MENDING, 1).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1)
    }, EnumSet.of(ArenaAttribute.NO_BLOCK_PLACE, ArenaAttribute.NO_BLOCK_BREAK)),

    UHC(new ItemStack[] {
            new ItemBuilder(Material.DIAMOND_AXE).enchant(Enchantment.EFFICIENCY, 3).enchant(Enchantment.SHARPNESS, 1).build(),
            new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.SHARPNESS, 4).build(),
            new ItemBuilder(Material.CROSSBOW).enchant(Enchantment.PIERCING, 1).build(),
            new ItemBuilder(Material.BOW).enchant(Enchantment.POWER, 1).build(),
            new ItemStack(Material.GOLDEN_APPLE, 13),
            new ItemStack(Material.COBWEB, 8),
            new ItemStack(Material.LAVA_BUCKET, 1),
            new ItemStack(Material.WATER_BUCKET, 1),
            new ItemStack(Material.COBBLESTONE, 64),
            null, null, null,
            new ItemBuilder(Material.DIAMOND_PICKAXE).enchant(Enchantment.EFFICIENCY, 3).build(),
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
            new ItemBuilder(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemBuilder(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemBuilder(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 2).build(),
            new ItemBuilder(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemStack(Material.SHIELD)
    }, EnumSet.of(ArenaAttribute.NO_CRAFTING, ArenaAttribute.NO_NATURAL_REGEN, ArenaAttribute.NO_HUNGER)),

    DIAMONDPOT(new ItemStack[] {
            new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.SHARPNESS, 5).build(),
            new ItemBuilder(Material.BOW).enchant(Enchantment.POWER, 5).enchant(Enchantment.PUNCH, 2).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_REGENERATION).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemStack(Material.ARROW, 24),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_REGENERATION).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.LONG_REGENERATION).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_STRENGTH).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_HEALING).build(),
            new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(), new ItemBuilder(Material.SPLASH_POTION).potionType(PotionType.STRONG_SWIFTNESS).build(),
            new ItemBuilder(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemBuilder(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 4).enchant(Enchantment.UNBREAKING, 3).build(),
            new ItemStack(Material.COOKED_BEEF, 20)
    }, EnumSet.of(ArenaAttribute.NO_BLOCK_PLACE, ArenaAttribute.NO_BLOCK_BREAK, ArenaAttribute.ONE_THIRD_MORE_MELEE_DAMAGE)),

    SWORD(new ItemStack[] {
            new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.SWEEPING_EDGE, 3).build(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            new ItemBuilder(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemBuilder(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemBuilder(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION, 3).build(),
            new ItemBuilder(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION, 3).build(),
            null
    }, EnumSet.of(ArenaAttribute.NO_BLOCK_PLACE, ArenaAttribute.NO_BLOCK_BREAK, ArenaAttribute.NO_NATURAL_REGEN, ArenaAttribute.NO_HUNGER)),

    CUSTOM(new ItemStack[] {
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
    }, EnumSet.noneOf(ArenaAttribute.class));

    private final ItemStack[] defaultInv;
    private final EnumSet<ArenaAttribute> attributes;

    KitType(ItemStack[] inv, EnumSet<ArenaAttribute> attributes) {
        this.defaultInv = inv;
        this.attributes = attributes;
    }

    public boolean hasAttribute(ArenaAttribute attribute) {
        return attributes.contains(attribute);
    }
}
