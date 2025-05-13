package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive.number

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType

@Suppress("unused")
class DoubleArgumentType private constructor(override val min: Double? = null, override val max: Double? = null) : CommandArgumentType<Double>(), NumberArgumentType<Double> {

    companion object {
        fun double() = DoubleArgumentType()

        fun positive() = DoubleArgumentType(min = .00000001)
        fun negative() = DoubleArgumentType(max = -.99999999)

        fun range(min: Double, max: Double) = DoubleArgumentType(min, max)
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return value.toDoubleOrNull() != null
    }

    override fun parse(obj: String): Double? {
        return obj.toDoubleOrNull()
    }
}