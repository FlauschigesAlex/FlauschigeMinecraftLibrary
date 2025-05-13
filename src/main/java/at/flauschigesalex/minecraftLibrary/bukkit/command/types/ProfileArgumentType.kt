package at.flauschigesalex.minecraftLibrary.bukkit.command.types

import at.flauschigesalex.defaultLibrary.any.MojangAPI
import at.flauschigesalex.defaultLibrary.any.MojangProfile
import at.flauschigesalex.minecraftLibrary.bukkit.command.CommandArgumentType
import org.bukkit.Bukkit
import java.util.UUID

@Suppress("unused")
class ProfileArgumentType private constructor() : CommandArgumentType<MojangProfile>() {

    companion object {
        fun profile() = ProfileArgumentType()
    }

    override fun suggestType(value: String?): Boolean {
        value ?: return true
        return true
    }

    override fun parse(obj: String): MojangProfile? {
        try {
            val uuid = UUID.fromString(obj)
            if (uuid != null) return MojangAPI.profile(uuid)
        } catch (_: Exception) {}

        return MojangAPI.profile(obj)
    }

    override fun display(obj: MojangProfile): String = obj.name
    override fun defaultChatSuggestions(provided: String): Set<String> = Bukkit.getOnlinePlayers().map { it.name }.toSet()
}