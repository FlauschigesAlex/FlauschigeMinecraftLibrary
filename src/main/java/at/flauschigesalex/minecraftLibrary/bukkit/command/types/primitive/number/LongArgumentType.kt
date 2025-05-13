package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive.number

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class LongArgumentType private constructor(override val min: Long? = null, override val max: Long? = null) : CommandArgumentType<Long>(), NumberArgumentType<Long> {

    companion object {
        fun long() = LongArgumentType()

        fun positive() = LongArgumentType(min = 1)
        fun negative() = LongArgumentType(max = -1)

        fun range(min: Long, max: Long) = LongArgumentType(min, max)
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return value.toLongOrNull() != null
    }

    override fun parse(obj: String): Long? {
        return obj.toLongOrNull()
    }
}