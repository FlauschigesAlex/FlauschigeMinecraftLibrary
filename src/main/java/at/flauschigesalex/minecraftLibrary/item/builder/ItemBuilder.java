package at.flauschigesalex.minecraftLibrary.item.builder;

import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import java.util.List;
import java.util.*;

import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ItemBuilder implements Cloneable {

    private Material material;
    private Component displayName;
    private List<Component> displayLore;

    private Color leatherColor;

    private int amount = 1;
    private int durability = 0;

    private Integer modelData;
    private boolean unbreakable = false;

    private final @NotNull ArrayList<ItemFlag> flags = new ArrayList<>();
    private @NotNull Map<Enchantment, Integer> enchants = new HashMap<>();

    public ItemBuilder() {
        this(Material.PAPER);
    }

    public ItemBuilder(final @NotNull Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(final @NotNull ItemStack itemStack) {
        this.material = itemStack.getType();

        if (!itemStack.hasItemMeta())
            return;

        assert (itemStack.getItemMeta() != null);
        for (final ItemFlag value : ItemFlag.values()) {
            if (!itemStack.getItemMeta().hasItemFlag(value)) continue;
            flags.add(value);
        }

        if (itemStack.getItemMeta().hasDisplayName())
            this.displayName = itemStack.displayName();
        if (itemStack.getItemMeta().hasLore())
            this.displayLore = itemStack.lore();
        if (itemStack.getItemMeta().isUnbreakable())
            this.unbreakable = itemStack.getItemMeta().isUnbreakable();
        if (itemStack.getItemMeta().hasEnchants())
            enchants = itemStack.getItemMeta().getEnchants();
        if (itemStack.getAmount() > 1)
            this.amount = itemStack.getAmount();
    }

    public ItemBuilder addFlags(final @NotNull ItemFlag... flags) {
        this.flags.addAll(List.of(flags));
        return this;
    }

    public ItemBuilder removeFlags(final @NotNull ItemFlag... flags) {
        this.flags.removeAll(List.of(flags));
        return this;
    }

    public ItemBuilder setEnchantments(final @NotNull Map<Enchantment, Integer> enchantments) {
        this.enchants = enchantments;
        return this;
    }

    public ItemBuilder addEnchantment(final @NotNull Enchantment enchantment, final int level) {
        this.enchants.put(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantments(final @NotNull Enchantment... enchantments) {
        return this.removeEnchantments(List.of(enchantments));
    }

    public ItemBuilder removeEnchantments(final @NotNull List<Enchantment> enchantments) {
        for (final Enchantment enchantment : enchantments)
            this.removeEnchantment(enchantment);
        return this;
    }

    public ItemBuilder removeEnchantment(final @NotNull Enchantment enchantment) {
        this.enchants.remove(enchantment);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemStack build() {
        if (material == null)
            material = Material.PAPER;
        ItemStack item = new ItemStack(material);
        if (!ItemBuilderException.isInventoryCapable(material))
            throw ItemBuilderException.inventoryFail(material);

        if (!enchants.isEmpty()) enchants.forEach(item::addUnsafeEnchantment);

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return
                item;

        if (displayName != null)
            meta.displayName(displayName);
        if (displayLore != null)
            meta.lore(displayLore);
        if (unbreakable)
            meta.setUnbreakable(true);
        if (!flags.isEmpty())
            flags.forEach(meta::addItemFlags);
        if (modelData != null)
            meta.setCustomModelData(modelData);

        item.setItemMeta(meta);
        item.setDurability((short) (item.getType().getMaxDurability() - durability));
        item.setAmount(amount);

        if (leatherColor != null && (material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS)) {
            final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
            leatherMeta.setColor(org.bukkit.Color.fromRGB(leatherColor.getRGB()));
            item.setItemMeta(leatherMeta);
        }

        return item;
    }

    public ItemStack buildHead(final @NotNull String offlinePlayer) {
        material = Material.PLAYER_HEAD;

        final ItemStack item = build();
        final SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(offlinePlayer));
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack buildHead(final @NotNull UUID offlinePlayer) {
        material = Material.PLAYER_HEAD;

        final ItemStack item = build();
        final SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(offlinePlayer));
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack item() {
        return build();
    }

    public ItemStack head(final @NotNull String offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    public ItemStack head(final @NotNull UUID offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    public ItemStack skull(final @NotNull String offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    public ItemStack skull(final @NotNull UUID offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    @SneakyThrows
    @Override
    public ItemBuilder clone() {
        return (ItemBuilder) super.clone();
    }

    public ItemBuilder setDisplayName(@NotNull Component component) {
        if (component.decoration(ITALIC) == TextDecoration.State.NOT_SET)
            component = component.decoration(ITALIC, false);
        this.displayName = component;
        return this;
    }

    public ItemBuilder setDisplayLore(final @NotNull Component... components) {
        return this.setDisplayLore(new ArrayList<>(List.of(components)));
    }
    public ItemBuilder setDisplayLore(final @NotNull Collection<Component> components) {
        this.displayLore = new ArrayList<>();
        for (Component component : components) {
            if (component.decoration(ITALIC) == TextDecoration.State.NOT_SET)
                component = component.decoration(ITALIC, false);

            this.displayLore.add(component);
        }
        return this;
    }

    public ItemBuilder setMaterial(final @NotNull Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setAmount(final int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setDurability(final int durability) {
        this.durability = durability;
        return this;
    }

    public ItemBuilder setCustomModelData(final int modelData) {
        this.modelData = modelData;
        return this;
    }

    public ItemBuilder setBreakable(final boolean breakable) {
        this.unbreakable = !breakable;
        return this;
    }

    public ItemBuilder setLeatherColor(final @NotNull Color leatherColor) {
        this.leatherColor = leatherColor;
        return this;
    }
}
