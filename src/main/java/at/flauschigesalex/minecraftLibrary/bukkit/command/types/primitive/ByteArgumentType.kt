package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class ByteArgumentType private constructor() : CommandArgumentType<Byte>() {

    companion object {
        fun byte() = ByteArgumentType()
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return value.toByteOrNull() != null
    }

    override fun parse(obj: String): Byte? {
        return obj.toByteOrNull()
    }
}