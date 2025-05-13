package at.flauschigesalex.minecraftLibrary.bukkit.command.types.internal

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

class GreedyArgumentType<A, T: CommandArgumentType<A>>(val any: T) : CommandArgumentType<A>() {

    companion object {
        fun <A, T: CommandArgumentType<A>> greedy(any: T) = GreedyArgumentType(any)
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return false
        return any.suggestType(value)
    }

    override fun parse(obj: String): A? {
        return any.parse(obj)
    }

    override fun defaultChatSuggestions(provided: String): Set<String> {
        return any.defaultChatSuggestions(provided)
    }
}