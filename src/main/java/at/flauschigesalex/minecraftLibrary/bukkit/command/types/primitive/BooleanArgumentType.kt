package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class BooleanArgumentType private constructor() : CommandArgumentType<Boolean>() {

    companion object {
        fun bool() = BooleanArgumentType()
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return defaultChatSuggestions("").any { it.startsWith(value, true) }
    }

    override fun parse(obj: String): Boolean? {
        return if (obj.equals("true", true)) true else if (obj.equals("false", true)) false else null
    }

    override fun defaultChatSuggestions(provided: String): Set<String> = setOf("true", "false")
}