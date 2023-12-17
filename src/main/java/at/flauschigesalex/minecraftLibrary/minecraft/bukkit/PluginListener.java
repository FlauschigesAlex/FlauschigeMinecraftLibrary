package at.flauschigesalex.minecraftLibrary.minecraft.bukkit;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public abstract class PluginListener implements Listener {

    private String name;
    private Class<?> belongingClass;

    public PluginListener() {
    }

    public PluginListener(@NotNull String name) {
        this.name = name;
    }

    public PluginListener(@NotNull Class<?> belongingClass) {
        this.belongingClass = belongingClass;
    }

    public PluginListener(@NotNull String name, @NotNull Class<?> belongingClass) {
        this.name = name;
        this.belongingClass = belongingClass;
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
