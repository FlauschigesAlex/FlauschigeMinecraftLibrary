package at.flauschigesalex.minecraftLibrary.bukkit.ui;

import at.flauschigesalex.defaultLibrary.utils.LibraryException;
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary;
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData;
import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

import static at.flauschigesalex.minecraftLibrary.bukkit.utils.ComponentManager.spliterator;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ItemBuilder implements Cloneable {

    private Material material;
    private Component displayName;
    private final List<Component> displayLore = new ArrayList<>();

    private Color leatherColor;

    private int amount = 1;
    private int durability = 0;

    private Integer modelData;
    private boolean unbreakable = false;

    private PersistentDataContainer container;
    private final HashMap<String, String> customData = new HashMap<>();

    private final @NotNull ArrayList<ItemFlag> flags = new ArrayList<>();
    private @NotNull Map<Enchantment, Integer> enchants = new HashMap<>();

    public ItemBuilder() {
        this(Material.PAPER);
    }

    public ItemBuilder(final @NotNull Material material) {
        this.material = material;
    }

    public ItemBuilder addFlags(final @NotNull ItemFlag... flags) {
        return this.addFlags(List.of(flags));
    }
    public ItemBuilder addFlags(final @NotNull Collection<ItemFlag> flags) {
        this.flags.addAll(flags);
        return this;
    }

    public ItemBuilder removeFlags(final @NotNull ItemFlag... flags) {
        return this.removeFlags(List.of(flags));
    }
    public ItemBuilder removeFlags(final @NotNull Collection<ItemFlag> flags) {
        this.flags.removeAll(flags);
        return this;
    }

    public ItemBuilder setFlags(final @NotNull ItemFlag... flags) {
        return this.setFlags(List.of(flags));
    }
    public ItemBuilder setFlags(final @NotNull Collection<ItemFlag> flags) {
        this.flags.clear();
        this.flags.addAll(flags);

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
        for (final Enchantment ench : enchantments)
            this.enchants.remove(ench);
        return this;
    }

    public ItemBuilder setDisplayName(final @NotNull String miniString) {
        return this.setDisplayName(MiniMessage.miniMessage().deserialize(miniString));
    }

    public ItemBuilder setDisplayName(@NotNull Component component) {
        if (component.decoration(ITALIC) == TextDecoration.State.NOT_SET)
            component = component.decoration(ITALIC, false);

        this.displayName = component;
        return this;
    }

    public ItemBuilder setDisplayLore(final @NotNull String... miniString) {
        return this.setDisplayLore(new ArrayList<>(List.of(miniString)));
    }
    public ItemBuilder setDisplayLore(final @NotNull List<String> miniString) {
        final ArrayList<Component> list = new ArrayList<>();
        for (final String string : miniString)
            list.add(MiniMessage.miniMessage().deserialize(string));

        return this.setDisplayLore(list);
    }

    public ItemBuilder setDisplayLore(final @NotNull Component... components) {
        return this.setDisplayLore(new ArrayList<>(List.of(components)));
    }
    public ItemBuilder setDisplayLore(final @NotNull Collection<Component> components) {
        this.clearDisplayLore();
        this.addDisplayLore(components);
        return this;
    }
    
    public ItemBuilder clearDisplayLore() {
        this.displayLore.clear();
        return this;
    }

    public ItemBuilder addDisplayLore(final @NotNull String... miniString) {
        return this.addDisplayLore(new ArrayList<>(List.of(miniString)));
    }
    public ItemBuilder addDisplayLore(final @NotNull List<String> miniString) {
        return this.addDisplayLore(miniString.stream().map(
                string -> MiniMessage.miniMessage().deserialize(string.isEmpty() ? " " : string)
        ).toList());
    }

    public ItemBuilder addDisplayLore(final @NotNull Component... components) {
        return this.addDisplayLore(List.of(components));
    }
    public ItemBuilder addDisplayLore(final @NotNull Collection<Component> components) {
        final ArrayList<Component> componentList = new ArrayList<>();
        components.forEach(component -> componentList.addAll(spliterator(component)));

        for (Component component : componentList) {
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

    public ItemBuilder addPersistentData(final @NotNull String key, final @NotNull String value) {
        this.customData.put(key, value);
        return this;
    }

    @SneakyThrows
    @SuppressWarnings("deprecation")
    public ItemStack item() {
        if (material == null)
            material = Material.PAPER;

        final ItemStack item = new ItemStack(material);

        if (!material.isItem())
            throw new ItemBuilderException("Material '"+material+"' cannot be displayed in an inventory.");

        if (!enchants.isEmpty())
            enchants.forEach(item::addUnsafeEnchantment);

        final ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        if (displayName != null)
            meta.displayName(displayName);

        if (!displayLore.isEmpty())
            meta.lore(new ArrayList<>(displayLore));

        if (unbreakable)
            meta.setUnbreakable(true);

        if (!flags.isEmpty())
            flags.forEach(meta::addItemFlags);

        if (modelData != null)
            meta.setCustomModelData(modelData);

        item.setItemMeta(meta);
        item.setDurability((short) (item.getType().getMaxDurability() - durability));
        item.setAmount(amount);

        if (container != null)
            item.getItemMeta().getPersistentDataContainer().readFromBytes(container.serializeToBytes());

        if (!customData.isEmpty()) {
            final PersistentData data = new PersistentData(meta, FlauschigeMinecraftLibrary.getLibrary().getPlugin());
            customData.forEach(data::set);
            item.setItemMeta(meta);
        }

        if (leatherColor != null && (material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS)) {
            final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
            leatherMeta.setColor(org.bukkit.Color.fromRGB(leatherColor.getRGB()));
            item.setItemMeta(leatherMeta);
        }

        return item;
    }

    public ItemStack skull(final @NotNull String playerName) {
        return this.skull(Bukkit.getOfflinePlayer(playerName));
    }

    public ItemStack skull(final @NotNull OfflinePlayer player) {
        return this.skull(player.getPlayerProfile());
    }

    public ItemStack skull(final @NotNull PlayerProfile profile) {
        material = Material.PLAYER_HEAD;

        final ItemStack item = item();
        final SkullMeta meta = (SkullMeta) item.getItemMeta();

        profile.complete();
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);

        return item;
    }

    @SneakyThrows
    @Override
    public ItemBuilder clone() {
        return (ItemBuilder) super.clone();
    }

    public static final class ItemBuilderException extends LibraryException {
        private ItemBuilderException(@Nullable String message) {
            super(message);
        }
    }
}

