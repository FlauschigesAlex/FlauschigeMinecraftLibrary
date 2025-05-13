package at.flauschigesalex.minecraftLibrary.bukkit.command.types.bukkit

import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Suppress("unused")
class PlayerArgumentType private constructor() : CommandArgumentType<Player>() {

    companion object {
        fun player() = PlayerArgumentType()
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return Bukkit.getOnlinePlayers().any { it.name.startsWith(value, true) }
    }

    override fun parse(obj: String): Player? {
        return Bukkit.getPlayer(obj)
    }

    override fun display(obj: Player): String = obj.name
    override fun defaultChatSuggestions(provided: String): Set<String> = Bukkit.getOnlinePlayers().map { it.name }.toSet()
}