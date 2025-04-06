package at.flauschigesalex.minecraftLibrary

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.any.CacheableMojangProfile
import at.flauschigesalex.defaultLibrary.any.MojangAPI
import at.flauschigesalex.defaultLibrary.any.MojangProfile
import at.flauschigesalex.defaultLibrary.any.Reflector
import at.flauschigesalex.defaultLibrary.utils.supertypes
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.BukkitReflect
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.PluginCommand
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.PluginListener
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI
import at.flauschigesalex.minecraftLibrary.bukkit.utils.BukkitException
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Modifier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FlauschigeMinecraftLibrary private constructor(packageCollection: Collection<String>) : FlauschigeLibrary() {
    
    companion object {
        private var flauschigeMinecraftLibrary: FlauschigeMinecraftLibrary? = null

        private var javaPluginInstance: JavaPlugin? = null
        
        @JvmStatic
        fun getLibrary(): FlauschigeMinecraftLibrary {
            return getLibrary(javaPluginInstance!!)
        }
        
        @JvmStatic
        fun getLibrary(plugin: JavaPlugin = javaPluginInstance!!, vararg packages: String): FlauschigeMinecraftLibrary {
            return this.getLibrary(plugin, packages.toList())
        }

        @JvmStatic
        fun getLibrary(plugin: JavaPlugin = javaPluginInstance!!, packages: Collection<String> = listOf()): FlauschigeMinecraftLibrary {
            if (flauschigeMinecraftLibrary == null)
                flauschigeMinecraftLibrary = FlauschigeMinecraftLibrary(packages)

            if (javaPluginInstance == null)
                flauschigeMinecraftLibrary!!.setPlugin(plugin)

            return flauschigeMinecraftLibrary!!
        }
    }
    
    private var packages = ArrayList<String>().apply {
        this.addAll(packageCollection)
        this.add(FlauschigeMinecraftLibrary::class.java.packageName)
    }

    val plugin: Plugin get() {
        return javaPluginInstance ?: throw BukkitException.bukkitNotFoundException
    }

    private val pluginShutdownHooks = HashSet<() -> Unit>()
    private val statement = Reflector.reflect(packages)

    private fun setPlugin(javaPlugin: JavaPlugin): FlauschigeMinecraftLibrary {
        javaPluginInstance = javaPlugin

        MojangAPI.addNameLookup(MojangAPI.LookupCall.BEFORE) { uuid ->
            Bukkit.getPlayer(uuid)?.run { return@addNameLookup this.let {
                CacheableMojangProfile(MojangProfile(it.name, it.uniqueId,
                    it.playerProfile.properties.first { it.name.equals("textures", true) }.value))
            } }
        }
        MojangAPI.addUuidLookup(MojangAPI.LookupCall.BEFORE) { name ->
            Bukkit.getPlayerExact(name)?.run { return@addUuidLookup this.let {
                CacheableMojangProfile(MojangProfile(it.name, it.uniqueId,
                    it.playerProfile.properties.first { it.name.equals("textures", true) }.value))
            } }
        }

        statement.getSubTypes(BukkitReflect::class.java).filter {
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

                } else println("Useless reflection found for ${BukkitReflect::class.java.simpleName}: ${it.name}")

            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }

        val executor = Executors.newScheduledThreadPool(1)
        executor.scheduleAtFixedRate({
            javaPluginInstance?.name?.let { Bukkit.getPluginManager().getPlugin(it) } ?: return@scheduleAtFixedRate

            pluginShutdownHooks.forEach { it.invoke() }
            pluginShutdownHooks.clear()
            
            executor.shutdownNow()
        }, 0, 50, TimeUnit.MILLISECONDS)

        @Suppress("DEPRECATION")
        this.addPluginShutdownHook {
            PluginGUI.controllers.forEach { it.cancel() }
            
            Bukkit.getOnlinePlayers().filter { 
                PluginGUI.openGUIs.containsKey(it.uniqueId)
            }.forEach { it.closeInventory() }
        }

        return this
    }

    @Deprecated("Method should not be used outside recommended scope.")
    internal fun addPluginShutdownHook(function: () -> Unit): FlauschigeMinecraftLibrary {
        pluginShutdownHooks.add(function)
        return this
    }
}
