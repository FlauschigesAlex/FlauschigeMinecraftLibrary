package at.flauschigesalex.minecraftLibrary.bukkit.command.types.primitive.number

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

internal interface NumberArgumentType<N> where N: Number, N: Comparable<N> {

    val min: N?
    val max: N?

    fun allowRange(sender: CommandSender, valueObj: Any): Boolean? {

        if (valueObj !is Number)
            return false

        val value = valueObj.toDouble()
        val min = this.min?.toDouble()
        val max = this.max?.toDouble()

        val className = valueObj::class.java.simpleName.lowercase()

        if (min != null && value < min.toDouble()) {
            sender.sendMessage(Component.translatable("argument.$className.low").color(NamedTextColor.RED).arguments(Component.text(this.min.toString()), Component.text(valueObj.toString())))
            return null
        }
        if (max != null && value > max.toDouble()) {
            sender.sendMessage(Component.translatable("argument.$className.big").color(NamedTextColor.RED).arguments(Component.text(this.max.toString()), Component.text(valueObj.toString())))
            return null
        }

        return true
    }
}