package at.flauschigesalex.minecraftLibrary;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.minecraftLibrary.bukkit.BukkitException;
import at.flauschigesalex.minecraftLibrary.bukkit.PluginCommand;
import at.flauschigesalex.minecraftLibrary.bukkit.PluginListener;
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
    private static @Getter @Nullable String pluginName;

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
     * @see #getLibrary(boolean, JavaPlugin) 
     */
    public static FlauschigeMinecraftLibrary getLibrary(final boolean autoRegisterManagers) {
        FlauschigeLibrary.autoRegisterManagers = autoRegisterManagers;
        return getLibrary();
    }
    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the Library
     * @see #getLibrary(boolean, JavaPlugin) 
     */
    public static FlauschigeMinecraftLibrary getLibrary(final @NotNull JavaPlugin javaPlugin) {
        return getLibrary().setPlugin(javaPlugin);
    }
    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the Library
     */
    public static FlauschigeMinecraftLibrary getLibrary(final boolean autoRegisterManagers, final @NotNull JavaPlugin javaPlugin) {
        return getLibrary(autoRegisterManagers).setPlugin(javaPlugin);
    }

    private FlauschigeMinecraftLibrary() {
        super();
    }

    private FlauschigeMinecraftLibrary setPlugin(final @NotNull JavaPlugin javaPlugin) {
        pluginName = javaPlugin.getName();

        for (final Class<?> subClass : getReflector().reflect().getSubClasses(PluginCommand.class)) {
            if (getPluginName() == null)
                break;
            try {
                final PluginCommand command = (PluginCommand) subClass.getConstructor().newInstance();
                if (!command.isEnabled())
                    continue;

                Bukkit.getCommandMap().register(command.getName(), command.getPluginPrefix(), command);

            } catch (Exception fail) {
                fail.printStackTrace();
            }
        }

        for (final Class<?> subClass : getReflector().reflect().getSubClasses(PluginListener.class)) {
            if (getPluginName() == null)
                break;
            try {
                final PluginListener listener = (PluginListener) subClass.getConstructor().newInstance();
                if (!listener.isEnabled())
                    continue;

                final Plugin plugin = Bukkit.getPluginManager().getPlugin(getPluginName());
                if (plugin == null)
                    break;

                Bukkit.getPluginManager().registerEvents(listener, plugin);

            } catch (Exception fail) {
                fail.printStackTrace();
            }
        }

        if (pluginName == null)
            throw BukkitException.bukkitNotFoundException;

        return this;
    }
}
