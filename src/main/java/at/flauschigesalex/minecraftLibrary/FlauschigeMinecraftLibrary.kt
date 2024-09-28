package at.flauschigesalex.minecraftLibrary

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.task.Task
import at.flauschigesalex.minecraftLibrary.bukkit.BukkitException
import at.flauschigesalex.minecraftLibrary.bukkit.PluginCommand
import at.flauschigesalex.minecraftLibrary.bukkit.PluginListener
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Modifier
import java.util.concurrent.TimeUnit

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FlauschigeMinecraftLibrary private constructor() : FlauschigeLibrary() {

    companion object {
        private var flauschigeMinecraftLibrary: FlauschigeMinecraftLibrary? = null

        @JvmStatic
        var pluginName: String? = null
            private set

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
        @JvmStatic fun getLibrary(javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
            return library.setPlugin(javaPlugin)
        }
    }

    val plugin: Plugin get() {
        if (pluginName == null)
            throw NullPointerException("Could not retrieve plugin because plugin-name is null!")

        val plugin = Bukkit.getPluginManager().getPlugin(pluginName!!)
            ?: throw NullPointerException("Could not retrieve plugin because plugin '$pluginName' does not exist.")

        return plugin
    }
    private val pluginShutdownHooks = HashSet<() -> Unit>()

    private fun setPlugin(javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
        pluginName = javaPlugin.name

        if (pluginName == null)
            throw BukkitException.bukkitNotFoundException

        for (subClass in reflector.reflect().getSubClasses(PluginCommand::class.java)) {
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
            try {
                if (Modifier.isAbstract(subClass.modifiers))
                    continue

                val constructor = subClass.getDeclaredConstructor()
                constructor.isAccessible = true

                val listener = constructor.newInstance() as PluginListener
                val plugin = Bukkit.getPluginManager().getPlugin(pluginName!!) ?: break

                Bukkit.getPluginManager().registerEvents(listener, plugin)
            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        Task.createAsyncTask { optional ->
            val bukkitPlugin = Bukkit.getPluginManager().getPlugin(pluginName!!)
            if (bukkitPlugin != null)
                return@createAsyncTask

            optional.ifPresent { it.stop() }
            pluginShutdownHooks.forEach { it.invoke() }

        }.repeatDelayed(TimeUnit.MILLISECONDS, 25)

        this.addPluginShutdownHook {
            PluginGUI.controllers.forEach { it.stop() }
        }

        return this
    }

    @Deprecated("Method should not be used outside recommended scope.")
    fun addPluginShutdownHook(function: () -> Unit): FlauschigeMinecraftLibrary {
        pluginShutdownHooks.add(function)
        return this
    }
}
