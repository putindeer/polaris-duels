package us.polarismc.polarisduels.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import java.util.function.Consumer;
import us.polarismc.polarisduels.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class ItemBuilder {
    private final Main plugin = Main.pl;
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material mat) {
        ItemStack i;
        this.item = i = new ItemStack(mat);
        this.meta = i.getItemMeta();
    }

    public ItemBuilder(Material mat, int amount) {
        ItemStack i;
        this.item = i = new ItemStack(mat);
        this.item.setAmount(amount);
        this.meta = i.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item, int amount) {
        this.item = item;
        this.item.setAmount(amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder enchant() {
        this.meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder enchant(Enchantment ench) {
        this.meta.addEnchant(ench, 1, true);
        return this;
    }

    public ItemBuilder enchant(Enchantment ench, int lvl) {
        if (lvl != 0) {
            this.meta.addEnchant(ench, lvl, true);
            return this;
        }
        return this;
    }

    public ItemBuilder enchantIf(boolean condition) {
        if (condition) {
            this.meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            return this;
        }

        this.meta.getEnchants().keySet().forEach(this.meta::removeEnchant);
        return this;
    }

    public ItemBuilder durability(int i) {
        Damageable dmg = (Damageable)this.meta;
        dmg.setDamage(this.item.getType().getMaxDurability() - i);
        return this;
    }

    public ItemBuilder hideAll() {
        this.meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        this.meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        return this;
    }

    public ItemBuilder addFlag(ItemFlag itemflag) {
        this.meta.addItemFlags(itemflag);
        return this;
    }


    // Lore
    // =======================================================
    public ItemBuilder lore(String... msgs) {
        List<Component> lore = new ArrayList<>();
        Arrays.stream(msgs).forEach(s -> lore.add(plugin.utils.chat("&7" + s)));

        this.meta.lore(lore);
        return this;
    }
    public ItemBuilder lore(List<String> msgs) {
        List<Component> lore = new ArrayList<>();
        msgs.forEach(s -> lore.add(plugin.utils.chat("&7" + s)));
        this.meta.lore(lore);
        return this;
    }
    public ItemBuilder lore(String msg) {
        List<Component> lore = new ArrayList<>();
        lore.add(plugin.utils.chat("&7" + msg));
        this.meta.lore(lore);
        return this;
    }

    public ItemBuilder loreAdd(String... msg) {
        List<Component> lore = this.meta.lore() == null ? new ArrayList<>() : new ArrayList<>(Objects.requireNonNull(this.meta.lore()));
        for (String s : msg)
            lore.add(plugin.utils.chat("&7" + s));

        this.meta.lore(lore);
        return this;
    }

    public ItemBuilder loreAddIf(boolean bool, String... msgs) {
        List<Component> lore = this.meta.lore() == null ? new ArrayList<>() : new ArrayList<>(Objects.requireNonNull(this.meta.lore()));

        if (bool) {
            for (String s : msgs)
                lore.add(plugin.utils.chat("&7" + s));
        }

        this.meta.lore(lore);
        return this;
    }


    // Name
    // =======================================================
    public ItemBuilder name(String str) {
        this.meta.displayName(plugin.utils.chat(str));
        return this;
    }

    // Meta
    // =======================================================
    public ItemBuilder unbreakable() {
        this.meta.setUnbreakable(true);
        this.meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        if (metaClass.isInstance(this.meta)) {
            metaConsumer.accept(metaClass.cast(this.meta));
        }
        return this;
    }


    // Skull Meta
    // =======================================================
    public ItemBuilder owner(OfflinePlayer p) {
        return this.meta(SkullMeta.class, m -> m.setOwningPlayer(p));
    }


    // Leather Armor Meta
    // =======================================================
    public ItemBuilder color(Color color) {
        this.meta.addItemFlags(ItemFlag.HIDE_DYE);
        this.meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return this.meta(LeatherArmorMeta.class, m -> m.setColor(color));
    }


    // Potion Meta
    // =======================================================
    public ItemBuilder potColor(Color color) {
        return this.meta(PotionMeta.class, m -> m.setColor(color));
    }

    public ItemBuilder potEffect(PotionEffect pe) {
        return this.meta(PotionMeta.class, m -> m.addCustomEffect(pe, true));
    }

    public ItemBuilder potBaseEffect(PotionType type) {
        return this.meta(PotionMeta.class, m -> m.setBasePotionType(type));
    }

    // Banner Meta
    // =======================================================
    public ItemBuilder setBannerMeta(BannerMeta meta) {
        ((BannerMeta) this.meta).setPatterns(meta.getPatterns());
        this.item.setItemMeta(this.meta);
        return this;
    }

    public ItemBuilder addPattern(Pattern pattern) {
        List<Pattern> list = new ArrayList<>(((BannerMeta) this.meta).getPatterns());
        list.add(pattern);

        ((BannerMeta) this.meta).setPatterns(list);
        this.item.setItemMeta(this.meta);
        return this;
    }

    // Chest Meta
    // =======================================================
    public ItemBuilder addItem(ItemStack i) {
        Chest chest = (Chest) ((BlockStateMeta) this.meta).getBlockState();
        chest.getInventory().addItem(i);
        ((BlockStateMeta)this.meta).setBlockState(chest);
        this.item.setItemMeta(this.meta);
        return this;
    }

    // Firework Meta
    // =======================================================
    public ItemBuilder addFireworkEffect(FireworkEffect.Type t, Color c) {
        FireworkEffect e = FireworkEffect.builder().with(t).withColor(c).build();
        ((FireworkMeta) this.meta).addEffect(e);
        return this;
    }

    public ItemBuilder addFireworkEffect(FireworkEffect.Type t, List<Color> c, int amount) {
        FireworkEffect e = FireworkEffect.builder().with(t).withColor(c).build();
        for (int i = 0; i < amount; i++)
            ((FireworkMeta) this.meta).addEffect(e);
        return this;
    }

    public ItemBuilder addFireworkEffect(FireworkEffect.Type t, Color c, int amount) {
        FireworkEffect e = FireworkEffect.builder().with(t).withColor(c).build();
        for (int i = 0; i < amount; i++)
            ((FireworkMeta) this.meta).addEffect(e);
        return this;
    }

    public ItemBuilder setFireworkPower(int i) {
        ((FireworkMeta) this.meta).setPower(i);
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}

