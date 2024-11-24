@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.defaultLibrary.translation.TranslationException
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import at.flauschigesalex.minecraftLibrary.bukkit.ui.ItemBuilder
import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Consumer

fun ItemStack.isTranslated(function: (PersistentData) -> Unit = {}): Boolean {
    val data = PersistentData(this.itemMeta, FlauschigeMinecraftLibrary.getLibrary().plugin)
    if (!data.contains("translationKey"))
        return false

    function.invoke(data)
    return true
}

class TranslatedItem(key: String, material: Material) {

    companion object {
        private val buildConsumer = Consumer { parent: TranslatedItemHandler ->
            val locale: TranslatedLocale = TranslatedLocale.of(parent.player)
            val handler = TranslationHandler(locale)

            val displayKeys = arrayOf(parent.translationKey + ".displayName",
                parent.translationKey + ".displayLore")

            if (locale.contains(displayKeys[0]))
                parent.builder.setDisplayName(handler.createComponent(displayKeys[0],
                    TranslationHandler.ModifyComponent.SQUASH, parent.replacements))

            if (locale.contains(displayKeys[1]))
                parent.builder.setDisplayLore(handler.createComponentList(displayKeys[1], parent.replacements))

            parent.builder.addPersistentData("translationKey", parent.translationKey)
        }
    }

    val translationKey: String
    val material: Material

    constructor(key: String) : this(key, Material.PAPER)

    init {
        var translationKey = key
        translationKey = "$translationKey.item"
        val response = TranslatedLocale.validateKey(translationKey)
        this.translationKey = response.input

        if (!material.isItem) throw TranslationException("Material $material cannot be displayed in a GUI.")
        this.material = material
    }

    fun item(player: Player, replacements: Map<String, Any> = mapOf(), consumer: (ItemBuilder) -> Unit = {} ): ItemStack {
        val builder = ItemBuilder(material)
        buildConsumer.andThen { _: TranslatedItemHandler? -> consumer.invoke(builder) }
            .accept(TranslatedItemHandler(builder, player, translationKey, replacements))
        return builder.item()
    }

    fun skull(player: Player, uuid: UUID, replacements: Map<String, Any> = mapOf(), consumer: (ItemBuilder) -> Unit = {}): ItemStack {
        return this.skull(player, Bukkit.getOfflinePlayer(uuid), replacements, consumer)
    }
    fun skull(player: Player, offlinePlayer: OfflinePlayer, replacements: Map<String, Any> = mapOf(), consumer: (ItemBuilder) -> Unit = {}): ItemStack {
        return this.skull(player, offlinePlayer.playerProfile, replacements, consumer)
    }
    fun skull(player: Player, profile: PlayerProfile, replacements: Map<String, Any> = mapOf(), consumer: (ItemBuilder) -> Unit = {}): ItemStack {
        val builder = ItemBuilder(material)
        buildConsumer.andThen { _: TranslatedItemHandler? -> consumer.invoke(builder) }
            .accept(TranslatedItemHandler(builder, player, translationKey, replacements))
        return builder.skull(profile)
    }

    private data class TranslatedItemHandler(
        val builder: ItemBuilder, val player: Player, val translationKey: String, val replacements: Map<String, Any>
    )
}
