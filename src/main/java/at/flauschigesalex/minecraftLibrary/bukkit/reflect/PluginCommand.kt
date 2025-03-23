@file:Suppress("DeprecatedCallableAddReplaceWith", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER", "CanBeParameter")

package at.flauschigesalex.minecraftLibrary.bukkit.reflect

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.*

@Suppress("unused", "DEPRECATION", "RedundantModalityModifier")
abstract class PluginCommand protected constructor(val command: String, description: String, usage: String, aliases: ArrayList<String?>)
    : Command(command, description, usage, aliases), BukkitReflect {
        
        companion object {
            var defaultCommandPermission: String? = null
        }

    var pluginPrefix: String
        private set

    protected constructor(command: String) : this(command, "", "/$command")
    protected constructor(command: String, description: String = "") : this(command, "", "/$command")

    protected constructor(command: String, description: String, usage: String) : this(
        command, "",
        "/$command", ArrayList<String?>()
    )

    init {
        val pluginName = FlauschigeMinecraftLibrary.getLibrary().plugin.name
        this.pluginPrefix = pluginName.lowercase(Locale.getDefault())
        
        this.permission = this.permission
    }

    @Deprecated("", ReplaceWith("this.command")) override fun setName(name: String): Boolean {
        return super.setName(name)
    }

    @Deprecated("", ReplaceWith("this.permissible(permissible)"), level = DeprecationLevel.ERROR)
    override fun testPermission(permissible: CommandSender): Boolean {
        return this.permissible(permissible)
    }

    @Deprecated("", ReplaceWith("this.permissible(permissible)"), level = DeprecationLevel.ERROR)
    override fun testPermissionSilent(target: CommandSender): Boolean {
        return super.testPermissionSilent(target)
    }

    override fun getLabel(): String {
        return pluginPrefix
    }

    @Deprecated("") override fun getPermissionMessage(): String? {
        return super.getPermissionMessage()
    }

    @Deprecated("") override fun setPermissionMessage(permissionMessage: String?): Command {
        return super.setPermissionMessage(permissionMessage)
    }

    open override fun getPermission(): String? {
        return defaultCommandPermission?.replace("%command%", command)
    }
    
    override fun setAliases(aliases: List<String>): Command {
        return super.setAliases(aliases)
    }

    @Deprecated("") override fun execute(commandSender: CommandSender, s: String, strings: Array<String>): Boolean {
        this.executeCommand(commandSender, s, strings)
        return true
    }

    protected abstract fun executeCommand(sender: CommandSender, fullCommand: String, args: Array<String>)

    fun permissible(permissible: CommandSender): Boolean {
        return super.testPermission(permissible)
    }

    fun setPluginPrefix(pluginPrefix: String): PluginCommand {
        this.pluginPrefix = pluginPrefix
        return this
    }
}
