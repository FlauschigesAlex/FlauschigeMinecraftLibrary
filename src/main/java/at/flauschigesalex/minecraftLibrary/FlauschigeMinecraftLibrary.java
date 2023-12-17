package at.flauschigesalex.minecraftLibrary;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.utils.reflections.Reflector;
import at.flauschigesalex.minecraftLibrary.minecraft.api.MojangAPI;
import at.flauschigesalex.minecraftLibrary.minecraft.bukkit.PluginCommand;
import at.flauschigesalex.minecraftLibrary.minecraft.bukkit.PluginListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused"})
@Getter
public final class FlauschigeMinecraftLibrary extends FlauschigeLibrary {
    private static FlauschigeMinecraftLibrary flauschigeMinecraftLibrary;
    @Getter
    private static @Nullable String pluginName;

    public static void main(String[] args) {
        getLibrary();
    }

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the Library
     * @see #getLibrary(JavaPlugin)
     */
    public static FlauschigeMinecraftLibrary getLibrary() {
        if (flauschigeMinecraftLibrary == null) flauschigeMinecraftLibrary = new FlauschigeMinecraftLibrary();
        return flauschigeMinecraftLibrary;
    }

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the Library
     */
    public static FlauschigeMinecraftLibrary getLibrary(@NotNull JavaPlugin javaPlugin) {
        return getLibrary().setPlugin(javaPlugin);
    }

    private FlauschigeMinecraftLibrary() {
        super();
    }

    public MojangAPI mojangAPI() {
        return MojangAPI.mojangAPI();
    }

    public Reflector getReflector() {
        return Reflector.getReflector();
    }

    private FlauschigeMinecraftLibrary setPlugin(final JavaPlugin javaPlugin) {
        pluginName = javaPlugin.getName();

        for (Class<?> subClass : getReflector().reflect().getSubClasses(PluginCommand.class)) {
            if (getPluginName() == null)
                break;
            try {
                PluginCommand command = (PluginCommand) subClass.getConstructor().newInstance();
                if (!command.isEnabled()) continue;
                Bukkit.getCommandMap().register(command.getName(), command.getPluginPrefix(), command);
            } catch (Exception fail) {
                fail.printStackTrace();
            }
        }
        for (Class<?> subClass : getReflector().reflect().getSubClasses(PluginListener.class)) {
            if (getPluginName() == null)
                break;
            try {
                PluginListener listener = (PluginListener) subClass.getConstructor().newInstance();
                if (!listener.isEnabled()) continue;
                Plugin plugin = Bukkit.getPluginManager().getPlugin(getPluginName());
                if (plugin == null) break;
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            } catch (Exception fail) {
                fail.printStackTrace();
            }
        }
        return this;
    }
}
