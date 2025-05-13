package at.flauschigesalex.minecraftLibrary.bukkit.command.internal

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandAlias
import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgument
import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class LiteralArgumentType private constructor() : CommandArgumentType<String>(), CommandAlias<CommandArgument<*>> {

    companion object {
        fun literal() = LiteralArgumentType()
    }

    override val aliases: MutableList<String> = mutableListOf()

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        val set = mutableSetOf(source.command)

        source.apply { if (this is CommandAlias<*>) set.addAll(aliases) }

        return set.any { it.startsWith(value, true) }
    }

    override fun parse(obj: String): String = obj

    override fun defaultChatSuggestions(provided: String): Set<String> {
        if (source.command.startsWith(provided, true))
            return setOf(source.command)

        source.apply { if (this is CommandAlias<*>) return aliases.filter { it.startsWith(provided, true) }.toSet() }

        return emptySet()
    }

}