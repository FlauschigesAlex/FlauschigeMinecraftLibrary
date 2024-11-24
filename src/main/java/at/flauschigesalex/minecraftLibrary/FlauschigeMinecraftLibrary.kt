package at.flauschigesalex.minecraftLibrary

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.any.CacheableMojangProfile
import at.flauschigesalex.defaultLibrary.any.MojangAPI
import at.flauschigesalex.defaultLibrary.any.MojangProfile
import at.flauschigesalex.defaultLibrary.any.Reflector
import at.flauschigesalex.defaultLibrary.supertypes
import at.flauschigesalex.defaultLibrary.task.Task
import at.flauschigesalex.defaultLibrary.task.TaskDelay
import at.flauschigesalex.defaultLibrary.task.TaskDelayType
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.BukkitReflect
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.PluginCommand
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.PluginListener
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI
import at.flauschigesalex.minecraftLibrary.bukkit.utils.BukkitException
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Modifier
import java.time.Duration

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

        MojangAPI.addNameLookup(MojangAPI.LookupCall.BEFORE) { uuid ->
            Bukkit.getPlayer(uuid)?.run { return@addNameLookup this.let {
                CacheableMojangProfile(MojangProfile(it.name, it.uniqueId))
            } }
        }
        MojangAPI.addUuidLookup(MojangAPI.LookupCall.BEFORE) { name ->
            Bukkit.getPlayerExact(name)?.run { return@addUuidLookup this.let {
                CacheableMojangProfile(MojangProfile(it.name, it.uniqueId))
            } }
        }

        Reflector.reflect().getSubTypes(BukkitReflect::class.java).filter {
            !Modifier.isAbstract(it.modifiers)
        }.forEach {
            try {
                val supertypes = it.supertypes()

                val constructor = it.getDeclaredConstructor()
                constructor.isAccessible = true

                if (supertypes.contains(PluginCommand::class.java)) {
                    val command = constructor.newInstance() as PluginCommand

                    Bukkit.getCommandMap().register(command.name, command.pluginPrefix, command)

                } else if (supertypes.contains(PluginListener::class.java)) {
                    val listener = constructor.newInstance() as PluginListener
                    val plugin = javaPluginInstance ?: throw BukkitException.bukkitNotFoundException

                    Bukkit.getPluginManager().registerEvents(listener, plugin)

                } else println("Useless reflection found for ${BukkitReflect::class.java.simpleName}: ${it.simpleName}")

            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        Task.createAsyncTask {
            javaPluginInstance?.name?.let { Bukkit.getPluginManager().getPlugin(it) } ?: return@createAsyncTask

            pluginShutdownHooks.forEach { it.invoke() }
            pluginShutdownHooks.clear()
            it?.stopTask()

        }.repeatDelayed(TaskDelay(Duration.ofMillis(25), TaskDelayType.ALWAYS))

        @Suppress("DEPRECATION")
        this.addPluginShutdownHook {
            PluginGUI.controllers.forEach { it.stopTask() }
        }

        return this
    }

    @Deprecated("Method should not be used outside recommended scope.")
    internal fun addPluginShutdownHook(function: () -> Unit): FlauschigeMinecraftLibrary {
        pluginShutdownHooks.add(function)
        return this
    }
}
