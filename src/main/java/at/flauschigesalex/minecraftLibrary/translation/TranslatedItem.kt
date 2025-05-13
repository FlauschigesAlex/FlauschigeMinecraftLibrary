@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import at.flauschigesalex.minecraftLibrary.bukkit.ui.ColoredBuilder
import at.flauschigesalex.minecraftLibrary.bukkit.ui.DefaultBuilder
import at.flauschigesalex.minecraftLibrary.bukkit.ui.ItemBuilder
import at.flauschigesalex.minecraftLibrary.bukkit.ui.SkullBuilder
import org.bukkit.Bukkit
import org.bukkit.Color
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

fun ItemStack.hasTranslationKey(key: String): Boolean {
    return PersistentData(this.itemMeta, FlauschigeMinecraftLibrary.getLibrary().plugin)["translationKey"]?.equals("${key}.item") ?: false
}

class TranslatedItem<A: DefaultBuilder<*>> private constructor(key: String, private val builder: A, private val function: (A) -> ItemStack) {

    companion object {
        operator fun invoke(key: String, material: Material) : TranslatedItem<DefaultBuilder<*>> {
            return TranslatedItem(key, ItemBuilder.invoke(material)) { it.item() }
        }
        operator fun invoke(key: String) : TranslatedItem<DefaultBuilder<*>> {
            return invoke(key, ItemBuilder.defaultMaterial)
        }
        
        fun skull(key: String, player: OfflinePlayer) : TranslatedItem<SkullBuilder> {
            return TranslatedItem(key, ItemBuilder.skull()) { it.item(player) }
        }
        fun skull(key: String, playerName: String) : TranslatedItem<SkullBuilder> {
            return skull(key, Bukkit.getOfflinePlayer(playerName))
        }
        fun skull(key: String, playerUUID: UUID) : TranslatedItem<SkullBuilder> {
            return skull(key, Bukkit.getOfflinePlayer(playerUUID))
        }
        
        fun colored(key: String, material: Material, color: Color?) : TranslatedItem<ColoredBuilder> {
            return TranslatedItem(key, ItemBuilder.colored(material)) {
                if (color != null)
                    it.setColor(color)
                
                it.item()
            }
        }
        fun colored(key: String, material: Material) : TranslatedItem<ColoredBuilder> {
            return colored(key, material, null)
        }
        
        private val buildConsumer = Consumer { parent: TranslatedItemHandler ->
            val locale: TranslatedLocale = TranslatedLocale.of(parent.player)
            val handler = TranslatedMessage(locale)

            val displayKeys = arrayOf(parent.translationKey + ".displayName",
                parent.translationKey + ".displayLore")

            if (locale.contains(displayKeys[0]))
                parent.builder.setName(handler.createComponent(displayKeys[0],
                    TranslatedMessage.ModifyComponent.SQUASH, parent.replacements).value)

            if (locale.contains(displayKeys[1]))
                parent.builder.setLore(handler.createComponentList(displayKeys[1], parent.replacements).value)

            parent.builder.persistentData { 
                it.set("translationKey", parent.translationKey)
            }
        }
    }

    val translationKey: String

    init {
        val translationKey = "${key}.item"
        val response = TranslatedLocale.validateKey(translationKey)
        assert(response.isValid)
        this.translationKey = response.value
    }

    fun item(player: Player): ItemStack {
        return item(player, mapOf()) { }
    }
    fun item(player: Player, replacements: Map<String, Any> = mapOf()): ItemStack {
        return item(player, replacements) { }
    }
    fun item(player: Player, consumer: (A) -> Unit = {} ): ItemStack {
        return item(player, mapOf(), consumer)
    }
    fun item(player: Player, replacements: Map<String, Any> = mapOf(), consumer: (A) -> Unit = {} ): ItemStack {
        buildConsumer.andThen { _: TranslatedItemHandler? -> consumer.invoke(builder) }
            .accept(TranslatedItemHandler(builder, player, translationKey, replacements))
        return function.invoke(builder)
    }
    
    private data class TranslatedItemHandler(
        val builder: DefaultBuilder<*>, val player: Player, val translationKey: String, val replacements: Map<String, Any>
    )
}
