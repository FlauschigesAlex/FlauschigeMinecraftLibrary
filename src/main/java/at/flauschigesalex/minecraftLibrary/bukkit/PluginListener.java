package at.flauschigesalex.minecraftLibrary.bukkit;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@SuppressWarnings({"unused", "unchecked"})
public abstract class PluginListener implements Listener {

    private static final ArrayList<PluginListener> list = new ArrayList<>();
    private static boolean containsName(final @NotNull String name) {
        return getListenerByName(name) != null;
    }

    public static <L extends PluginListener> @Nullable L getListenerByName(final @NotNull String name) {
        for (final PluginListener listener : list)
            if (listener.getName().equalsIgnoreCase(name))
                return (L) listener;
        return null;
    }

    public static <L extends PluginListener> @Nullable L getListenerByClass(final @NotNull Class<? extends PluginListener> listenerClass) {
        for (final PluginListener listener : list)
            if (listener.getClass().equals(listenerClass))
                return (L) listener;
        return null;
    }

    private String name;
    private Class<?> belongingClass;

    public PluginListener() {
    }

    public PluginListener(final @NotNull String name) {
        this.name = name;
    }

    public PluginListener(final @NotNull Class<?> belongingClass) {
        this.belongingClass = belongingClass;
    }

    public PluginListener(final @NotNull String name, final @NotNull Class<?> belongingClass) {
        this.name = name;
        this.belongingClass = belongingClass;

        if (containsName(name))
            throw BukkitException.bukkitListenerNameNotUniqueException(this);

        list.add(this);
    }

    public boolean isEnabled() {
        return true;
    }

    public @NotNull String getName() {
        if (name == null)
            return getClass().getSimpleName();

        return name;
    }

    public @Nullable Class<?> getBelongingClass() {
        return belongingClass;
    }
}
