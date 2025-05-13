package at.flauschigesalex.minecraftLibrary.bukkit.command

abstract class CommandArgumentType<T> protected constructor() {

    lateinit var source: CommandBase<*>
    internal fun source(base: CommandBase<*>) {
        this.source = base
    }

    abstract fun suggestType(value: String?): Boolean
    abstract fun parse(obj: String): T?

    open fun display(obj: T): String = obj.toString()
    open fun defaultChatSuggestions(provided: String): Set<String> = setOf()
}