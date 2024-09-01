package at.flauschigesalex.minecraftLibrary

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.BukkitException
import at.flauschigesalex.minecraftLibrary.bukkit.PluginCommand
import at.flauschigesalex.minecraftLibrary.bukkit.PluginListener
import lombok.Getter
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Modifier

@Suppress("unused") @Getter
class FlauschigeMinecraftLibrary private constructor() : FlauschigeLibrary() {
    val plugin: Plugin get() {
        if (pluginName == null)
            throw NullPointerException("Could not retrieve plugin because plugin-name is null!")

        val plugin = Bukkit.getPluginManager().getPlugin(pluginName!!)
            ?: throw NullPointerException("Could not retrieve plugin because plugin '$pluginName' does not exist.")

        return plugin
    }

    private fun setPlugin(javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
        pluginName = javaPlugin.name

        for (subClass in reflector.reflect().getSubClasses(PluginCommand::class.java)) {
            if (pluginName == null) break
            try {
                if (Modifier.isAbstract(subClass.modifiers)) continue

                val constructor = subClass.getDeclaredConstructor()
                constructor.isAccessible = true

                val command = constructor.newInstance() as PluginCommand

                Bukkit.getCommandMap().register(command.name, command.pluginPrefix, command)
            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        for (subClass in reflector.reflect().getSubClasses(PluginListener::class.java)) {
            if (pluginName == null) break
            try {
                if (Modifier.isAbstract(subClass.modifiers)) continue

                val constructor = subClass.getDeclaredConstructor()
                constructor.isAccessible = true

                val listener = constructor.newInstance() as PluginListener
                if (!listener.isEnabled) continue

                val plugin = Bukkit.getPluginManager().getPlugin(pluginName!!) ?: break

                Bukkit.getPluginManager().registerEvents(listener, plugin)
            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        if (pluginName == null) throw BukkitException.bukkitNotFoundException

        return this
    }

    companion object {
        private var flauschigeMinecraftLibrary: FlauschigeMinecraftLibrary? = null

        @JvmStatic
        var pluginName: String? = null
            private set

        @JvmStatic fun main(args: Array<String>) {
            library
        }

        /**
         * Make sure to run this method in your main class!
         * This is extremely important for reflections!
         *
         * @return an instance of the Library
         * @see .getLibrary
         */
        @JvmStatic val library: FlauschigeMinecraftLibrary get() {
            if (flauschigeMinecraftLibrary == null)
                flauschigeMinecraftLibrary = FlauschigeMinecraftLibrary()
            return flauschigeMinecraftLibrary!!
        }

        /**
         * Make sure to run this method in your main class!
         * This is extremely important for reflections!
         *
         * @return an instance of the Library
         * @see .getLibrary
         */
        @JvmStatic fun getLibrary(autoRegisterManagers: Boolean): FlauschigeMinecraftLibrary {
            FlauschigeLibrary.autoRegisterManagers = autoRegisterManagers
            return library
        }

        /**
         * Make sure to run this method in your main class!
         * This is extremely important for reflections!
         *
         * @return an instance of the Library
         * @see .getLibrary
         */
        @JvmStatic fun getLibrary(javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
            return library.setPlugin(javaPlugin)
        }

        /**
         * Make sure to run this method in your main class!
         * This is extremely important for reflections!
         *
         * @return an instance of the Library
         */
        @JvmStatic fun getLibrary(autoRegisterManagers: Boolean, javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
            return getLibrary(autoRegisterManagers).setPlugin(javaPlugin)
        }
    }
}
