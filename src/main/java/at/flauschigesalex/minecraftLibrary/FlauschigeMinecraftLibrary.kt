package at.flauschigesalex.minecraftLibrary

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.reflections.Reflector
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

        private var javaPluginInstance: JavaPlugin? = null

        private val library = null

        @JvmStatic
        fun getLibrary(): FlauschigeMinecraftLibrary {
            return getLibrary(javaPluginInstance!!)
        }

        @JvmStatic
        fun getLibrary(plugin: JavaPlugin = javaPluginInstance!!): FlauschigeMinecraftLibrary {

            if (flauschigeMinecraftLibrary == null)
                flauschigeMinecraftLibrary = FlauschigeMinecraftLibrary()

            if (javaPluginInstance == null)
                flauschigeMinecraftLibrary!!.setPlugin(plugin)

            return flauschigeMinecraftLibrary!!
        }
    }

    val plugin: Plugin get() {
        return javaPluginInstance ?: throw BukkitException.bukkitNotFoundException
    }

    private val pluginShutdownHooks = HashSet<() -> Unit>()

    private fun setPlugin(javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
        javaPluginInstance = javaPlugin

        for (commandClass in Reflector.reflect().getSubTypes(PluginCommand::class.java)) {
            try {
                if (Modifier.isAbstract(commandClass.modifiers))
                    continue

                val constructor = commandClass.getDeclaredConstructor()
                constructor.isAccessible = true

                val command = constructor.newInstance() as PluginCommand

                Bukkit.getCommandMap().register(command.name, command.pluginPrefix, command)
            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        for (listenerClass in Reflector.reflect().getSubTypes(PluginListener::class.java)) {
            try {
                if (Modifier.isAbstract(listenerClass.modifiers))
                    continue

                val constructor = listenerClass.getDeclaredConstructor()
                constructor.isAccessible = true

                val listener = constructor.newInstance() as PluginListener
                val plugin = javaPluginInstance ?: break

                Bukkit.getPluginManager().registerEvents(listener, plugin)
            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        Task.createAsyncTask { optional ->
            javaPluginInstance?.name?.let { Bukkit.getPluginManager().getPlugin(it) } ?: return@createAsyncTask

            optional.ifPresent { it.stop() }
            pluginShutdownHooks.forEach { it.invoke() }

        }.repeatDelayed(TimeUnit.MILLISECONDS, 25)

        @Suppress("DEPRECATION")
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
