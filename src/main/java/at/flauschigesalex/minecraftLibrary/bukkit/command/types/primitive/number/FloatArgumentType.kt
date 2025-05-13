package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive.number

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class FloatArgumentType private constructor(override val min: Float? = null, override val max: Float? = null) : CommandArgumentType<Float>(), NumberArgumentType<Float> {

    companion object {
        fun float() = FloatArgumentType()

        fun positive() = FloatArgumentType(min = .00000001f)
        fun negative() = FloatArgumentType(max = -.99999999f)

        fun range(min: Float, max: Float) = FloatArgumentType(min, max)
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return value.toFloatOrNull() != null
    }

    override fun parse(obj: String): Float? {
        return obj.toFloatOrNull()
    }
}