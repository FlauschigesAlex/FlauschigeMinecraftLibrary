package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.defaultLibrary.translation.TranslationException
import at.flauschigesalex.defaultLibrary.translation.TranslationValidator
import at.flauschigesalex.minecraftLibrary.item.builder.ItemBuilder
import lombok.Getter
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Consumer

@Suppress("unused", "MemberVisibilityCanBePrivate")
@Getter
class TranslatedItem(key: String, material: Material) {

    companion object {
        private val empty = Consumer { _: ItemBuilder? -> }
        private val buildConsumer = Consumer<TranslatedItemHandler> { parent ->
            val locale: TranslatedLocale = TranslatedLocale.of(parent.player)
            val handler = TranslationHandler(locale)

            val displayKeys =
                arrayOf(parent.translationKey + ".displayName", parent.translationKey + ".displayLore")
            if (locale.has(displayKeys[0])) parent.builder.setDisplayName(
                    handler.createComponent(displayKeys[0], TranslationHandler.ModifyComponent.SQUASH)
            )

            if (locale.has(displayKeys[1]))
                parent.builder.setDisplayLore(handler.createComponentList(displayKeys[1]))

            parent.builder.addPersistentData("translationKey", parent.translationKey)
        }
    }

    private val translationKey: String
    private val material: Material

    @Deprecated("")
    constructor(translationKey: String) : this(translationKey, Material.PAPER)

    init {
        var translationKey = key
        translationKey = "$translationKey.item"
        val response = TranslationValidator.validateKey(translationKey)
        this.translationKey = response.translationKey

        if (!material.isItem) throw TranslationException("Material $material cannot be displayed in a GUI.")
        this.material = material
    }

    fun item(player: Player, consumer: Consumer<ItemBuilder?>): ItemStack {
        return item(player, mapOf(), consumer)
    }

    fun item(player: Player, replacements: Map<String, Any> = mapOf(), consumer: Consumer<ItemBuilder?> = empty): ItemStack {
        val builder = ItemBuilder(material)
        buildConsumer.andThen { _: TranslatedItemHandler? -> consumer.accept(builder) }
            .accept(TranslatedItemHandler(builder, player, translationKey, replacements))
        return builder.item()
    }

    fun headItem(player: Player, uuid: UUID): ItemStack {
        return headItem(player, uuid.toString())
    }

    fun headItem(player: Player, uuid: UUID, replacements: Map<String, Any>): ItemStack {
        return headItem(player, uuid.toString(), replacements)
    }

    fun headItem(player: Player, uuid: UUID, consumer: Consumer<ItemBuilder?>): ItemStack {
        return headItem(player, uuid.toString(), consumer)
    }

    fun headItem(player: Player, uuid: String, consumer: Consumer<ItemBuilder?>): ItemStack {
        return headItem(player, uuid, mapOf(), consumer)
    }

    fun headItem(player: Player, uuid: UUID, replacements: Map<String, Any>, consumer: Consumer<ItemBuilder?>): ItemStack {
        return headItem(player, uuid.toString(), replacements, consumer)
    }

    fun headItem(player: Player, uuid: String, replacements: Map<String, Any> = mapOf(), consumer: Consumer<ItemBuilder?> = empty): ItemStack {
        val builder = ItemBuilder(material)
        buildConsumer.andThen { _: TranslatedItemHandler? -> consumer.accept(builder) }
            .accept(TranslatedItemHandler(builder, player, translationKey, replacements))
        return builder.head(uuid)
    }

    private data class TranslatedItemHandler(
        val builder: ItemBuilder, val player: Player, val translationKey: String, val replacements: Map<String, Any>
    )
}
