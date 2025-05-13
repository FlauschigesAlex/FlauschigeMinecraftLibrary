@file:Suppress("UNCHECKED_CAST", "unused")
package at.flauschigesalex.minecraftLibrary.bukkit.command

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.command.types.internal.GreedyArgumentType
import at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive.number.NumberArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.jetbrains.annotations.Range
import java.lang.IllegalArgumentException

internal typealias CommandConsumer = (CommandSender, String, CommandArgumentDataList, Array<out String>) -> Boolean

fun CommandSender.sendIncompleteCommand(fullCommand: String) {
    this.sendMessage(Component.translatable("command.unknown.command").color(NamedTextColor.RED)
        .append(Component.newline())
        .append(Component.text(fullCommand).decorate(TextDecoration.UNDERLINED))
        .append(Component.translatable("command.context.here").decorate(TextDecoration.ITALIC)))
}

abstract class CommandBase<B: CommandBase<B>> protected constructor(val command: String) {

    companion object {

        @JvmStatic var DEFAULT_COMMAND_FAIL: CommandConsumer = { sender, fullCommand, _, _ ->
            sender.sendIncompleteCommand(fullCommand)
            false
        }
        @JvmStatic var DEFAULT_COMMAND_SUCCESS: CommandConsumer = { _, _, _, _ ->
            true
        }
        @JvmStatic var DEFAULT_COMMAND_LABEL: String? = null
    }

    internal var permission: String? = null
    fun permission(permission: String?): B {
        this.permission = permission
        return this as B
    }

    internal val arguments = mutableListOf<CommandArgument<*>>()
    open fun argument(argument: CommandArgument<*>): B {
        argument.type.source(argument)
        argument.parent(this)

        arguments.add(argument)
        return this as B
    }

    val depth get(): Int {
        if (arguments.isEmpty())
            return 1

        var depth = arguments.maxOf { it.depth }
        if (this !is CommandBuilder)
            depth++;

        return depth
    }

    internal var commandFail: CommandConsumer = DEFAULT_COMMAND_FAIL
    open fun fail(fail: CommandConsumer): B {
        this.commandFail = fail
        return this as B
    }

    internal var commandSuccess: CommandConsumer = DEFAULT_COMMAND_SUCCESS
    open fun execute(execute: CommandConsumer): B {
        this.commandSuccess = execute
        return this as B
    }
}

internal interface CommandAlias<B: CommandBase<B>> {

    val aliases: MutableList<String>
    fun alias(vararg alias: String): B = this.alias(alias.toList())
    fun alias(alias: Collection<String>): B {
        this.aliases.addAll(alias)
        return this as B
    }
}

class CommandBuilder constructor(command: String): CommandBase<CommandBuilder>(command), CommandAlias<CommandBuilder> {

    override val aliases: MutableList<String> = mutableListOf()
    internal var isRegistered = false

    internal var label: String = DEFAULT_COMMAND_LABEL?.lowercase() ?: command.lowercase()
    fun label(label: String): CommandBuilder {
        this.label = label.lowercase()
        return this
    }

    internal var isArgless: Boolean = false
    fun argless(): CommandBuilder {
        this.isArgless = true
        return this
    }

    fun register() {
        if (isRegistered)
            return

        if (isArgless && arguments.isNotEmpty())
            System.err.println("Unused arguments found for $command: command is argless!")

        val instance = object : Command(command, "", "", aliases) {
            override fun execute(sender: CommandSender, command: String, stringArgs: Array<out String>): Boolean {

                var commandArg: CommandBase<*> = this@CommandBuilder
                val fullCommand = "$command ${stringArgs.joinToString(" ")}"

                var successExecutor = commandArg.commandSuccess

                val dataArgs = CommandArgumentDataList()

                // arguments are empty, no further checks needed
                if (stringArgs.isEmpty() || isArgless)
                    return successExecutor(sender, fullCommand, dataArgs, stringArgs)

                val depth = commandArg.depth

                for (index in 0 until depth) {
                    val argument = stringArgs.getOrNull(index)

                    val arg = commandArg.arguments.firstOrNull {

                        // provided string is not compatible for this argument
                        if (!it.type.suggestType(argument))
                            return@firstOrNull false

                        // sender is not permitted to use this argument
                        val tempArg = commandArg
                        if (tempArg is CommandArgument<*> && !tempArg.canUse(sender))
                            return@firstOrNull false

                        // skip null argument if required
                        val value = argument?.let { any -> it.type.parse(any) }
                        if (value == null) {
                            if (!it.optional)
                                return@firstOrNull false

                            // allow argument since it is optional
                            dataArgs.add(CommandArgumentData(index, value, commandArg, it.type))
                            return@firstOrNull true
                        }

                        // number argument range check
                        if (it.type is NumberArgumentType<*>) {
                            val numberVal = it.type.allowRange(sender, value)

                            // object is not a number - this should not happen
                            if (numberVal == false)
                                return@firstOrNull false

                            // number is not in range of provided arguments
                            else if (numberVal == null)
                                return false
                        }

                        if (it.type is GreedyArgumentType<*, *>) {
                            if (index > stringArgs.size)
                                return@firstOrNull false

                            val values = stringArgs.copyOfRange(index, stringArgs.size).map { arg -> it.type.parse(arg) }
                            if (values.any { it == null })
                                return@firstOrNull false

                            dataArgs.add(GreedyCommandArgumentData(index, values, commandArg, it.type))

                            return@firstOrNull true
                        }

                        dataArgs.add(CommandArgumentData(index, value, commandArg, it.type))
                    } ?: return commandArg.commandFail(sender, fullCommand, dataArgs, stringArgs)

                    successExecutor = arg.commandSuccess
                    if (arg.type !is GreedyArgumentType<*, *>)
                        commandArg = arg
                }

                return successExecutor(sender, fullCommand, dataArgs, stringArgs)
            }
            override fun tabComplete(
                sender: CommandSender,
                alias: String,
                args: Array<out String>,
                location: Location?
            ): MutableList<String> {
                val base: CommandBase<*> = this@CommandBuilder
                var parents: Set<CommandArgument<*>>? = null

                args.forEachIndexed { index, argument ->

                    // parent is vararg (greedy) argument
                    if (parents != null && parents!!.size == 1 && parents!!.first().type is GreedyArgumentType<*, *>)
                        return@forEachIndexed

                    // arguments of the current (sub-) command
                    val arguments = parents?.flatMap { it.arguments } ?: base.arguments

                    val valid = arguments.filter {
                        // sender is not permitted to use this argument

                        val permission = it.permission
                        return@filter permission == null || sender.hasPermission(permission)
                    }.filter { cmd ->
                        // provided string is not compatible for this argument

                        return@filter cmd.type.suggestType(argument)
                    }

                    // no arguments are compatible for the provided string
                    if (valid.isEmpty())
                        return mutableListOf()

                    parents = valid.toSet()
                }

                // no arguments are compatible for the provided string
                val suggest = parents ?: return mutableListOf()
                val arg = args.lastOrNull() ?: ""

                // add default chat suggestions
                return suggest.flatMap {
                    val suggestions = mutableListOf<String>()
                    suggestions.addAll(it.type.defaultChatSuggestions(arg))

                    return@flatMap suggestions
                }.filter {
                    // only suggest arguments that start with the provided string
                    it.startsWith(arg, true)
                }.toMutableList()
            }
        }

        // sets the permission of this command (can be null)
        instance.permission = this.permission

        // registers this command to the server
        Bukkit.getCommandMap().register(command, this.label, instance)
        isRegistered = true
    }

    override fun execute(execute: CommandConsumer): CommandBuilder {
        super.execute(execute)
        this.register()
        return this
    }

}

class CommandArgument<T: CommandArgumentType<*>> constructor(val name: String, val type: T) : CommandBase<CommandArgument<*>>(name) {

    lateinit var parent: CommandBase<*>
        private set
    internal fun parent(base: CommandBase<*>): CommandArgument<T> {
        this.parent = base
        return this
    }

    internal var optional = false
    fun optional(): CommandArgument<T> {
        this.optional = true
        return this
    }

    override fun argument(argument: CommandArgument<*>): CommandArgument<*> {
        if (type is GreedyArgumentType<*, *>)
            throw IllegalArgumentException("Cannot follow with another argument after a greedy argument.")

        return super.argument(argument)
    }

    // TODO IMPLEMENT CUSTOM REQUIRE
    fun canUse(sender: CommandSender): Boolean = hasPermission(sender)

    fun hasPermission(sender: CommandSender): Boolean {

        var current: CommandBase<*>? = this
        while (current != null) {

            val permission = current.permission
            if (permission != null)
                if (!sender.hasPermission(permission))
                    return false

            current = if (current is CommandArgument<*>) current.parent
                      else null
        }

        return true
    }

}

interface AbstractCommandArgumentData<out T> {
    val value: T?
    val index: Int
    val type: CommandArgumentType<*>
}
class CommandArgumentData<out T>(override val index: Int, override val value: T?, internal val base: CommandBase<*>, override val type: CommandArgumentType<out T>): AbstractCommandArgumentData<T>
class GreedyCommandArgumentData<out T, out TL : Collection<T>>(override val index: Int, override val value: TL, val base: CommandBase<*>, override val type: CommandArgumentType<out T>): AbstractCommandArgumentData<TL>, Iterable<T> {
    override fun iterator(): Iterator<T> = (value as Collection<T>).iterator()
}

class CommandArgumentDataList(val arguments: MutableSet<AbstractCommandArgumentData<*>> = mutableSetOf()): Iterable<AbstractCommandArgumentData<*>> {

    internal fun add(data: AbstractCommandArgumentData<*>) = arguments.add(data)
    private val entries get(): Set<AbstractCommandArgumentData<*>> = arguments.toSet()
    override fun iterator(): Iterator<AbstractCommandArgumentData<*>> = entries.iterator()

    fun byName(name: String): CommandArgumentData<*>? {
        return arguments
            .filterIsInstance(CommandArgumentData::class.java)
            .find { it.base.command.equals(name, true) }
    }

    inline fun <reified T> byType(): CommandArgumentData<T>? {
        return arguments
            .filterIsInstance(CommandArgumentData::class.java)
            .firstOrNull { it.value is T } as? CommandArgumentData<T>
    }
    fun byIndex(index: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int): CommandArgumentData<*>? {
        return arguments
            .filterIsInstance(CommandArgumentData::class.java)
            .find { it.index == index}
    }

    fun greedyByName(name: String): GreedyCommandArgumentData<*, *>? {
        return arguments
            .filterIsInstance(GreedyCommandArgumentData::class.java)
            .find { it.base.command.equals(name, true) }
    }
    fun <T> greedyByType(): GreedyCommandArgumentData<T, Collection<T>>? {
        return arguments
            .filterIsInstance(GreedyCommandArgumentData::class.java)
            .firstNotNullOfOrNull { it as? GreedyCommandArgumentData<T, Collection<T>> }
    }
    fun greedyByIndex(index: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int): GreedyCommandArgumentData<*, *>? {
        return arguments
            .filterIsInstance(GreedyCommandArgumentData::class.java)
            .find { it.index == index }
    }
}