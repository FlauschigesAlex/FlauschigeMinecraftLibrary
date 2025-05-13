package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive.number

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class IntegerArgumentType private constructor(override val min: Int? = null, override val max: Int? = null) : CommandArgumentType<Int>(), NumberArgumentType<Int> {

    companion object {
        fun int() = IntegerArgumentType()

        fun positive() = IntegerArgumentType(min = 1)
        fun negative() = IntegerArgumentType(max = -1)

        fun range(min: Int, max: Int) = IntegerArgumentType(min, max)
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return value.toIntOrNull() != null
    }

    override fun parse(obj: String): Int? {
        return obj.toIntOrNull()
    }
}