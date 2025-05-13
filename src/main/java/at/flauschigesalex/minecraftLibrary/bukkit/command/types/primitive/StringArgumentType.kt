package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class StringArgumentType private constructor(val regex: Regex?) : CommandArgumentType<String>() {

    companion object {

        fun string() = string(null)
        fun string(regex: Regex?) = StringArgumentType(regex)
        fun word() = StringArgumentType(Regex("^[a-zA-Z0-9_]*\$"))

    }
    override fun suggestType(value: String?): Boolean {
        value ?: return true
        regex ?: return true
        return regex.matches(value)
    }

    override fun parse(obj: String): String = obj
}