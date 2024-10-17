@file:Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "unused", "DEPRECATION")

package at.flauschigesalex.minecraftLibrary.paper

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor

object Paper {

    fun getNamedTextColorValues(): List<NamedTextColor> {
        val colorField = NamedTextColor::class.java.getDeclaredField("VALUES")
        colorField.isAccessible = true
        return colorField.get(null) as List<NamedTextColor>
    }

    val NamedTextColor.name: String
        get() = this.toString().uppercase()

    val NamedTextColor.char: Char
        get() = ChatColor.valueOf(this.name).char

    val TextDecoration.char: Char
        get() {
            val name = when (this.name) { // fuck paper
                "OBFUSCATED" -> "MAGIC"
                "UNDERLINED" -> "UNDERLINE"

                else -> this.name
            }

            return ChatColor.valueOf(name).char
        }
}