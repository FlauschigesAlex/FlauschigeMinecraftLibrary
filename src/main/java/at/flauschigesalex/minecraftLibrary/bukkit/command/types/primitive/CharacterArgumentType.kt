package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class CharacterArgumentType private constructor() : CommandArgumentType<Char>() {

    companion object {
        fun char() = CharacterArgumentType()
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        val chars = value.toCharArray()
        return chars.size == 1
    }

    override fun parse(obj: String): Char? {
        return obj.toCharArray().firstOrNull()
    }
}