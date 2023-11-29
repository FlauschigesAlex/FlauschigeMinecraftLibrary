package at.flauschigesalex.minecraftLibrary;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.utils.reflections.Reflector;
import at.flauschigesalex.minecraftLibrary.minecraft.api.MojangAPI;
import at.flauschigesalex.minecraftLibrary.minecraft.bukkit.PluginCommand;
import at.flauschigesalex.minecraftLibrary.minecraft.bukkit.PluginListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused"})
@Getter
public class FlauschigeMinecraftLibrary extends FlauschigeLibrary {
    public static void main(String[] args) {
        getAPI();
    }

    private static FlauschigeMinecraftLibrary flauschigeMinecraftLibrary;

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the API
     * @see #getAPI(JavaPlugin)
     */
    public static FlauschigeMinecraftLibrary getAPI() {
        if (flauschigeMinecraftLibrary == null) flauschigeMinecraftLibrary = new FlauschigeMinecraftLibrary();
        return flauschigeMinecraftLibrary;
    }
    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the API
     */
    public static FlauschigeMinecraftLibrary getAPI(@NotNull JavaPlugin javaPlugin) {
        return getAPI().setPluginName(javaPlugin);
    }

    protected FlauschigeMinecraftLibrary() {
        super();
        ownDirectoryPath = getClass().getPackage().getName();
        this.addWorkingDirectory(getClass().getPackageName());
        for (Class<?> subClass : getReflector().reflect().getSubClasses(PluginCommand.class)) {
            try {
                PluginCommand command = (PluginCommand) subClass.getConstructor().newInstance();
                if (!command.isEnabled()) continue;
                Bukkit.getCommandMap().register(command.getName(), command);
            } catch (Exception fail) {
                fail.printStackTrace();
            }
        }
        for (Class<?> subClass : getReflector().reflect().getSubClasses(PluginListener.class)) {
            try {
                PluginListener listener = (PluginListener) subClass.getConstructor().newInstance();
                if (!listener.isEnabled()) continue;
                Plugin plugin = javaPlugin;
                if (plugin == null) break;
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            } catch (Exception fail) {
                fail.printStackTrace();
            }
        }
    }

    private JavaPlugin javaPlugin;
    private FlauschigeMinecraftLibrary setPluginName(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        return this;
    }

    public boolean isMinecraftServer() {
        try {
            Server server = Bukkit.getServer();
        } catch (NoClassDefFoundError fail) {
            return false;
        }
        return true;
    }
    public MojangAPI mojangAPI() {
        return MojangAPI.mojangAPI();
    }
    public Reflector getReflector() {
        return Reflector.getReflector();
    }
}
