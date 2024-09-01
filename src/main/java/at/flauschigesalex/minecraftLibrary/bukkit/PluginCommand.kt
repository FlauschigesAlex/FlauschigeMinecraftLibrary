@file:Suppress("DeprecatedCallableAddReplaceWith", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER", "CanBeParameter")

package at.flauschigesalex.minecraftLibrary.bukkit

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary.Companion.library
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary.Companion.pluginName
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.annotations.Range
import org.jetbrains.annotations.Unmodifiable
import java.util.*

@Suppress("unused", "DEPRECATION")
abstract class PluginCommand protected constructor(val command: String, description: String, usage: String, aliases: ArrayList<String?>)
    : Command(command, description, usage, aliases) {

    var pluginPrefix: String
        private set

    protected constructor(command: String, description: String = "") : this(command, "", "/$command")

    protected constructor(command: String, description: String, usage: String) : this(
        command, "",
        "/$command", ArrayList<String?>()
    )

    init {
        val pluginName = pluginName
        this.pluginPrefix = pluginName?.lowercase(Locale.getDefault()) ?: "flauschigesalex"
    }

    @Deprecated("", ReplaceWith("this.command")) override fun setName(name: String): Boolean {
        return super.setName(name)
    }

    @Deprecated("", ReplaceWith("this.permissible(permissible)"))
    override fun testPermission(permissible: CommandSender): Boolean {
        return this.permissible(permissible)
    }

    @Deprecated("", ReplaceWith("this.permissible(permissible)"))
    override fun testPermissionSilent(target: CommandSender): Boolean {
        return super.testPermissionSilent(target)
    }

    override fun getLabel(): String {
        library
        if (pluginName != null) return pluginName!!

        return super.getLabel()
    }

    @Deprecated("") override fun getPermissionMessage(): String? {
        return super.getPermissionMessage()
    }

    @Deprecated("") override fun setPermissionMessage(permissionMessage: String?): Command {
        return super.setPermissionMessage(permissionMessage)
    }

    override fun setAliases(aliases: List<String>): Command {
        return super.setAliases(aliases)
    }

    @Deprecated("") override fun execute(commandSender: CommandSender, s: String, strings: Array<String>): Boolean {
        this.executeCommand(commandSender, s, strings)
        return true
    }

    protected abstract fun executeCommand(sender: CommandSender, fullCommand: String, args: Array<String>)

    protected fun tabCompletes(sender: CommandSender?): Set<TabComplete> {
        return TabComplete.onlinePlayers(if (sender is Player) sender else null)
    }

    @Deprecated("") @Throws(IllegalArgumentException::class) override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<String>
    ): List<String> {
        return listOf()
    }

    @Deprecated("") @Throws(IllegalArgumentException::class)
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>, location: Location?): List<String> {
        return tabCompletes(sender).stream()
            .filter { complete: TabComplete ->
                if (complete.arg == null) return@filter true
                complete.arg == args.size - 1
            }
            .filter { complete: TabComplete ->
                val arg = args.size - 1
                if (arg < 0) return@filter true

                if (args[arg].isBlank()) return@filter true
                complete.completable.lowercase(Locale.getDefault()).startsWith(args[arg].lowercase(Locale.getDefault()))
            }
            .filter { complete: TabComplete ->
                if (complete.location == null || complete.location.world == null) return@filter true
                if (location == null || location.world == null) return@filter false

                if (complete.maxDistance < 0) return@filter complete.location.world == location.world
                complete.location.distance(location) <= complete.maxDistance
            }
            .map { complete: TabComplete -> complete.completable }.toList()
    }

    fun permissible(permissible: CommandSender): Boolean {
        return super.testPermission(permissible)
    }

    fun setPluginPrefix(pluginPrefix: String): PluginCommand {
        this.pluginPrefix = pluginPrefix
        return this
    }

    class TabComplete(val arg: Int?, val completable: String, val location: Location?,
                      val maxDistance: @Range(from = 0, to = Long.MAX_VALUE) Double) {

        companion object {
            fun onlinePlayers(player: Player? = null): Set<TabComplete> {
                return HashSet(Bukkit.getOnlinePlayers().stream()
                    .filter { onlinePlayer: Player? ->
                        if (player == null) return@filter true
                        player.canSee(onlinePlayer!!)
                    }
                    .map { onlinePlayer: Player? -> TabComplete(onlinePlayer!!.name) }
                    .toList())
            }

            fun players(arg: Int): @Unmodifiable MutableList<TabComplete> {
                return Bukkit.getOnlinePlayers().stream()
                    .map { player: Player? -> TabComplete(arg, player!!.name) }
                    .toList()
            }
        }

        constructor(completable: String) : this(null, completable, null)

        constructor(arg: Int?, completable: String, requiredWorld: World? = null)
                : this(arg, completable, Location(requiredWorld, 0.0, 0.0, 0.0), -1.0)

        override fun equals(other: Any?): Boolean {
            if (other is TabComplete) return arg == other.arg && completable.equals(other.completable, ignoreCase = true)

            return false
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}
