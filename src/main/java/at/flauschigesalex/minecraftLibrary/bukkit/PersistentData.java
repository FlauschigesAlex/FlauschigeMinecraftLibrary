package at.flauschigesalex.minecraftLibrary.bukkit;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import static at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary.getLibrary;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "unchecked", "rawtypes"})
public final class PersistentData {

    private final PersistentDataContainer container;
    private final Plugin plugin;

    public PersistentData(final @NotNull PersistentDataHolder container) {
        this(container, getLibrary().getPlugin());
    }
    public PersistentData(final @NotNull PersistentDataHolder container, final @NotNull Plugin plugin) {
        this.container = container.getPersistentDataContainer();
        this.plugin = plugin;
    }

    public String getOrDefault(final @NotNull String key, final @NotNull String defaultValue) {
        return this.getOrDefault(key, (Object) defaultValue).toString();
    }

    public Object getOrDefault(final @NotNull String key, final @NotNull Object defaultValue) {
        if (!contains(key))
            return defaultValue;
        
        return get(key);
    }

    public <C> C getOrDefault(final @NotNull String key, final @NotNull PersistentDataType<C, C> type, final @NotNull C defaultValue) {
        if (!contains(key))
            return defaultValue;

        return get(key, type);
    }

    public boolean contains(final @NotNull String key) {
        return container.has(new NamespacedKey(plugin, key));
    }

    public String get(final @NotNull String key) {
        final Object object = this.get(key, PersistentDataType.STRING);
        if (object == null)
            return null;
        
        return object.toString();
    }

    public <C> C get(final @NotNull String key, final @NotNull PersistentDataType<C, C> type) {
        if (!contains(key))
            return null;

        return container.get(new NamespacedKey(plugin, key), type);
    }

    public PersistentData set(final @NotNull String key, final @NotNull String value) {
        return this.set(key, value, PersistentDataType.STRING);
    }

    public <C> PersistentData set(final @NotNull String key, final @NotNull C value, final @NotNull PersistentDataType<C, C> dataType) {
        container.set(new NamespacedKey(plugin, key), dataType, value);
        return this;
    }

    public PersistentData remove(final @NotNull String key) {
        container.remove(new NamespacedKey(plugin, key));
        return this;
    }
}
