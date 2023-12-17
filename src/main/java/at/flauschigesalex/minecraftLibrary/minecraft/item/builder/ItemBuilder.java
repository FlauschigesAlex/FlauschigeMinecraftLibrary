package at.flauschigesalex.minecraftLibrary.minecraft.item.builder;

import at.flauschigesalex.minecraftLibrary.minecraft.ComponentBuilder;
import lombok.SneakyThrows;
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

@SuppressWarnings({"unused", "UnusedReturnValue", "DataFlowIssue"})
public class ItemBuilder implements Cloneable {

    private final @NotNull ArrayList<ItemFlag> flags = new ArrayList<>();
    private Material material;
    private ComponentBuilder displayName;
    private ComponentBuilder displayLore;
    private Color leatherColor;
    private int amount = 1;
    private int durability = 0;
    private Integer modelData;
    private boolean unbreakable = false;
    private @NotNull Map<Enchantment, Integer> enchants = new HashMap<>();

    public ItemBuilder() {
        this(Material.PAPER);
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack itemStack) {
        this.material = itemStack.getType();
        if (itemStack.hasItemMeta()) {
            setMaterial(Material.STICK);
            assert (itemStack.getItemMeta() != null);
            for (ItemFlag value : ItemFlag.values()) {
                if (!itemStack.getItemMeta().hasItemFlag(value)) continue;
                flags.add(value);
            }
            if (itemStack.getItemMeta().hasDisplayName()) this.displayName.setComponent(itemStack.displayName());
            if (itemStack.getItemMeta().hasLore()) this.displayLore.setText(itemStack.lore());
            if (itemStack.getItemMeta().isUnbreakable()) this.unbreakable = itemStack.getItemMeta().isUnbreakable();
            if (itemStack.getItemMeta().hasEnchants()) enchants = itemStack.getItemMeta().getEnchants();
            if (itemStack.getAmount() > 1) this.amount = itemStack.getAmount();
        }
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        this.flags.addAll(List.of(flags));
        return this;
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        this.flags.removeAll(List.of(flags));
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.enchants.put(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
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

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (displayName != null) meta.displayName(displayName.asTextComponent());
        if (displayLore != null) meta.lore(displayLore.asComponentList(false));
        if (unbreakable) meta.setUnbreakable(true);
        if (!flags.isEmpty()) flags.forEach(meta::addItemFlags);
        if (modelData != null) meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        item.setDurability((short) (item.getType().getMaxDurability() - durability));
        item.setAmount(amount);

        if (leatherColor != null && (material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS)) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
            leatherMeta.setColor(org.bukkit.Color.fromRGB(leatherColor.getRGB()));
            item.setItemMeta(leatherMeta);
        }

        return item;
    }

    public ItemStack buildHead(String offlinePlayer) {
        material = Material.PLAYER_HEAD;
        ItemStack item = build();

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(offlinePlayer));
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack buildHead(UUID offlinePlayer) {
        material = Material.PLAYER_HEAD;
        ItemStack item = build();

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(offlinePlayer));
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack item() {
        return build();
    }

    public ItemStack head(String offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    public ItemStack head(UUID offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    public ItemStack skull(String offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    public ItemStack skull(UUID offlinePlayer) {
        return buildHead(offlinePlayer);
    }

    @SneakyThrows
    @Override
    public ItemBuilder clone() {
        return (ItemBuilder) super.clone();
    }

    public ItemBuilder setDisplayName(ComponentBuilder component) {
        this.displayName = component;
        return this;
    }

    public ItemBuilder setDisplayLore(ComponentBuilder component) {
        this.displayLore = component;
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setDurability(int durability) {
        this.durability = durability;
        return this;
    }

    public ItemBuilder setCustomModelData(int modelData) {
        this.modelData = modelData;
        return this;
    }

    public ItemBuilder setBreakable(boolean breakable) {
        this.unbreakable = !breakable;
        return this;
    }

    public ItemBuilder setLeatherColor(Color leatherColor) {
        this.leatherColor = leatherColor;
        return this;
    }
}
