package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.defaultLibrary.translation.TranslationException
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function

fun TranslatedLocale.Companion.of(player: Player): TranslatedLocale {
    return this.of(player.locale())
}

fun CommandSender.sendTranslated(translationKey: String, replacements: Map<String, Any>) {
    TranslationHandler(this).sendMessage(translationKey, replacements)
}

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TranslationHandler private constructor(private val sender: CommandSender?, private val locale: TranslatedLocale) {

    companion object {
        var prefix: Component? = null
    }

    constructor(sender: CommandSender) : this(sender,
        if (sender is Player) sender.locale()
        else TranslatedLocale.fallbackLocale.locale)

    constructor(locale: Locale) : this(null, locale)
    constructor(locale: TranslatedLocale) : this(null, locale)

    private constructor(sender: CommandSender?, locale: Locale) : this(sender, TranslatedLocale.of(locale))

    fun locale(): TranslatedLocale {
        return locale
    }

    fun createComponent(
        translationKey: String,
        modifyComponent: ModifyComponent = ModifyComponent.default,
        replacements: Map<String, Any> = mapOf()
    ): Component {
         return modifyComponent.function.apply(ArrayList(createStringList(translationKey, replacements)))
    }

    fun createComponentList(translationKey: String, replacements: Map<String, Any> = mapOf()): List<Component> {
        return createStringList(translationKey, replacements)
            .map { string: String -> MiniMessage.miniMessage().deserialize(string) }
            .toList()
    }

    private fun createStringList(translationKey: String, replacements: Map<String, Any>): List<String> {
        val spacer = "<newLine>"
        val builder = StringBuilder()

        for (string in locale.findList(translationKey, replacements))
            builder.append(spacer).append(string)

        var found = builder.toString().replace("<br>", "<newLine>")
        if (found.startsWith(spacer)) found = found.substring(spacer.length)

        return listOf(*found.split(spacer.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
    }

    fun sendMessage(translationKey: String) {
        this.sendMessage(translationKey, mapOf())
    }

    fun sendMessage(translationKey: String, replacements: Map<String, Any>) {
        this.sendMessage(translationKey, true, replacements)
    }

    fun sendMessage(translationKey: String, displayPrefix: Boolean, replacements: Map<String, Any> = mapOf()) {
        if (sender == null)
            throw TranslationException("Cannot send message to the player since player is not defined.")

        val base = if (displayPrefix) prefix ?: Component.empty() else Component.empty()

        Bukkit.getScheduler().runTaskAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin) { _ ->
            sender.sendMessage(base.append(this.createComponent(translationKey, replacements = replacements)))
        }
    }

    enum class ModifyComponent(func: Function<ArrayList<String>, Component>) {
        BREAK(Function { list: ArrayList<String> ->
            val builder = StringBuilder(list.first())
            list.removeFirst()

            for (append in list)
                builder.append("<newLine>").append(append)

            MiniMessage.miniMessage().deserialize(builder.toString())
        }),
        SQUASH(Function { list: ArrayList<String> ->
            val builder = StringBuilder(list.first())
            list.removeFirst()

            for (append in list)
                builder.append(" ").append(append)

            MiniMessage.miniMessage().deserialize(builder.toString())
        }),
        SUPPRESS(Function { strings: ArrayList<String> -> MiniMessage.miniMessage().deserialize(strings.first()) }),
        ;

        val function: Function<ArrayList<String>, Component> = func

        companion object {
            var default = SQUASH
        }
    }
}
