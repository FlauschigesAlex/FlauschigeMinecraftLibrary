package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.any.Validator
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
    TranslatedMessage(this).sendMessage(translationKey, replacements)
}

@Deprecated("Legacy Name", ReplaceWith("TranslatedMessage", "at.flauschigesalex.minecraftLibrary.translation.TranslatedMessage"))
typealias TranslationHandler = TranslatedMessage

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TranslatedMessage private constructor(private val sender: CommandSender?, private val locale: TranslatedLocale) {

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
    ): Validator<Component> {
        val validator = createStringList(translationKey, replacements)
        return Validator(modifyComponent.function.apply(validator.value.toMutableList()), validator.isValid)
    }

    fun createComponentList(translationKey: String, replacements: Map<String, Any> = mapOf()): Validator<List<Component>> {
        val validator = createStringList(translationKey, replacements)
        return Validator(validator.value.map { MiniMessage.miniMessage().deserialize(it) }, validator.isValid)
    }

    private fun createStringList(translationKey: String, replacements: Map<String, Any>): Validator<List<String>> {
        val spacer = "<newLine>"
        val builder = StringBuilder()

        val validator = locale.findList(translationKey, replacements)

        for (string in validator.value)
            builder.append(spacer).append(string)

        var found = builder.toString().replace("<br>", "<newLine>")
        if (found.startsWith(spacer)) found = found.substring(spacer.length)

        return Validator(listOf(*found.split(spacer.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()), validator.isValid)
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
            sender.sendMessage(base.append(this.createComponent(translationKey, replacements = replacements).value))
        }
    }

    enum class ModifyComponent(func: Function<MutableList<String>, Component>) {
        BREAK(Function { list ->
            val builder = StringBuilder(list.first())
            list.removeFirst()

            for (append in list)
                builder.append("<newLine>").append(append)

            MiniMessage.miniMessage().deserialize(builder.toString())
        }),
        SQUASH(Function { list ->
            val builder = StringBuilder(list.first())
            list.removeFirst()

            for (append in list)
                builder.append(" ").append(append)

            MiniMessage.miniMessage().deserialize(builder.toString())
        }),
        SUPPRESS(Function { list -> MiniMessage.miniMessage().deserialize(list.first()) }),
        ;

        val function: Function<MutableList<String>, Component> = func

        companion object {
            var default = SQUASH
        }
    }
}
