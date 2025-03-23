@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage")

package at.flauschigesalex.minecraftLibrary.bukkit.ui

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import javassist.expr.NewArray
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ColorableArmorMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.annotations.Range
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.max
import kotlin.math.min

val Material.isColorable : Boolean
    get() = this.asItemType()?.typed()?.itemMetaClass?.isAssignableFrom(ColorableArmorMeta::class.java) ?: false

@Suppress("unused")
abstract class ItemBuilder private constructor() {
    companion object {
        operator fun invoke(material: Material): DefaultBuilder<*> {
            return DefaultBuilder(ItemStack(material))
        }
        operator fun invoke(): DefaultBuilder<*> {
            return DefaultBuilder(ItemStack(defaultMaterial))
        }

        fun colored(material: Material): ColoredBuilder {
            if (!material.isColorable)
                throw IllegalArgumentException("Material $material is not instance of ${ColorableArmorMeta::class.java.simpleName}.")
            
            return ColoredBuilder(ItemStack(material))
        }
        fun skull(): SkullBuilder {
            return SkullBuilder(ItemStack(Material.PLAYER_HEAD))
        }
        
        fun modify(itemStack: ItemStack, consumer: (Modifier) -> Unit) {
            val modifier = Modifier(itemStack)
            consumer.invoke(modifier)
            modifier.apply(itemStack)
        }
        
        var defaultMaterial = Material.PAPER

        var defaultMaxStackSize: Int? = null
        var defaultEnchantmentGlint: Boolean? = null
        var defaultUnbreakable: Boolean? = null
        var defaultHideTooltip: Boolean? = null
        var defaultFireResistant: Boolean? = null
    }
}

open class DefaultBuilder<I: DefaultBuilder<I>> internal constructor(private val itemStack: ItemStack) {
    
    companion object {
        internal val miniMessage = MiniMessage.miniMessage()
    }
    
    private val material = itemStack.type
    
    init {
        if (!material.isItem) throw IllegalArgumentException("Material $material cannot be displayed in a GUI.")
    }
    
    // VARIABLES
    
    private var name: Component? = null
    private val lore = ArrayList<Component>()
    
    private var amount = 1
    private var maxStackSize: Int? = ItemBuilder.defaultMaxStackSize
    private var damage: Int? = null
    
    private var customModelData: Int? = null
    private var enchantmentGlint: Boolean? = ItemBuilder.defaultEnchantmentGlint
    
    private var unbreakable: Boolean? = ItemBuilder.defaultUnbreakable
    private var hideTooltip = ItemBuilder.defaultHideTooltip
    private var fireResistant: Boolean? = ItemBuilder.defaultFireResistant
    
    private var persistentDataConsumer: ((PersistentData) -> Unit)? = null
    
    private val flags = HashSet<ItemFlag>()
    private val enchants = HashMap<Enchantment, Int>()
    
    // SETTERS
    
    fun setName(name: Component): I {
        this.name = name
        
        if (this.name!!.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET)
            this.name = this.name!!.decoration(TextDecoration.ITALIC, false)
        
        return this as I
    }
    fun setPlainName(name: String): I {
        this.setName(Component.text(name))
        return this as I
    }
    fun setRichName(name: String): I {
        this.setName(miniMessage.deserialize(name))
        return this as I
    }
    
    fun clearLore(): I {
        this.lore.clear()
        return this as I
    }
    fun addLore(lore: List<Component>): I {
        this.lore.addAll(lore.map {
            if (it.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET)
                return@map it.decoration(TextDecoration.ITALIC, false)
            return@map it
        })
        return this as I
    }
    fun addLore(vararg lore: Component): I {
        return this.addLore(lore.toList())
    }
    fun addPlainLore(lore: List<String>): I {
        return this.addLore(lore.map { Component.text(it) })
    }
    fun addPlainLore(vararg lore: String): I {
        return this.addPlainLore(lore.toList())
    }
    fun addRichLore(lore: List<String>): I {
        return this.addLore(lore.map { miniMessage.deserialize(it) })
    }
    fun addRichLore(vararg lore: String): I {
        return this.addRichLore(lore.toList())
    }
    fun setLore(lore: List<Component>): I {
        this.clearLore()
        return this.addLore(lore)
    }
    fun setLore(vararg lore: Component): I {
        return this.setLore(lore.toList())
    }
    fun setPlainLore(lore: List<String>): I {
        this.clearLore()
        return this.addPlainLore(lore)
    }
    fun setPlainLore(vararg lore: String): I {
        return this.setPlainLore(lore.toList())
    }
    fun setRichLore(lore: List<String>): I {
        this.clearLore()
        return this.addRichLore(lore)
    }
    fun setRichLore(vararg lore: String): I {
        return this.setRichLore(lore.toList())
    }
    
    fun setAmount(amount: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int): I {
        this.amount = amount
        return this as I
    }
    
    fun setMaxStackSize(maxSize: @Range(from = 1, to = 99) Int): I {
        this.maxStackSize = min(max(maxSize, 0), 99)
        return this as I
    }
    
    fun setDurability(durability: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int): I {
        this.damage = material.maxDurability - durability
        return this as I
    }
    fun setDamage(damage: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int): I {
        this.damage = damage
        return this as I
    }
    
    fun setCustomModelData(data: Int): I {
        this.customModelData = data
        return this as I
    }

    open fun setEnchantmentGlint(glint: Boolean): I {
        this.enchantmentGlint = glint
        return this as I
    }
    
    fun setUnbreakable(unbreakable: Boolean): I {
        this.unbreakable = unbreakable
        return this as I
    }
    fun setHideTooltip(hide: Boolean): I {
        this.hideTooltip = hide
        return this as I
    }
    fun setFireResistant(resistant: Boolean): I {
        this.fireResistant = resistant
        return this as I
    }
    
    fun persistentData(consumer: (PersistentData) -> Unit): I {
        this.persistentDataConsumer = consumer
        return this as I
    }
    
    fun clearItemFlags(): I {
        this.flags.clear()
        return this as I
    }
    fun addItemFlags(flags: Set<ItemFlag>): I {
        this.flags.addAll(flags)
        return this as I
    }
    fun addItemFlags(vararg flags: ItemFlag): I {
        return this.addItemFlags(flags.toSet())
    }
    fun removeItemFlags(flags: Set<ItemFlag>): I {
        this.flags.removeAll(flags)
        return this as I
    }
    fun removeItemFlags(vararg flags: ItemFlag): I {
        return this.removeItemFlags(flags.toSet())
    }
    fun setItemFlags(flags: Set<ItemFlag>): I {
        this.clearItemFlags()
        return this.addItemFlags(flags)
    }
    fun setItemFlags(vararg flags: ItemFlag): I {
        return this.setItemFlags(flags.toSet())
    }
    
    open fun clearEnchantments(): I {
        this.enchants.clear()
        return this as I
    }
    open fun addEnchantment(enchantment: Enchantment, level: Int): I {
        this.enchants[enchantment] = level
        return this as I
    }
    open fun removeEnchantments(enchantment: Set<Enchantment>): I {
        enchantment.forEach { this.enchants.remove(it) }
        return this as I
    }
    open fun removeEnchantments(vararg enchantment: Enchantment): I {
        return this.removeEnchantments(enchantment.toSet())
    }
    open fun removeEnchantment(enchantment: Enchantment, level: Int): I {
        this.enchants.remove(enchantment, level)
        return this as I
    }
    open fun setEnchantments(enchantments: Map<Enchantment, Int>): I {
        this.enchants.putAll(enchantments)
        return this as I
    }
    
    protected var consumer: (item: ItemStack, meta: ItemMeta) -> Unit = { _, _ -> }
    open fun item(): ItemStack {
        return itemStack.also { this.apply(it) }
    }
    
    internal fun apply(item: ItemStack) {
        item.amount = this.amount
        item.itemFlags.addAll(this.flags)
        item.addUnsafeEnchantments(this.enchants)

        item.editMeta { meta ->

            this.name?.apply {
                meta.displayName(this)
            }

            if (this.lore.isNotEmpty())
                meta.lore(this.lore)

            if (this.maxStackSize != null) meta.setMaxStackSize(this.maxStackSize)
            if (meta is Damageable && this.damage != null)
                meta.damage = this.damage!!

            if (this.customModelData != null) meta.setCustomModelData(this.customModelData)
            if (this.enchantmentGlint != null) meta.setEnchantmentGlintOverride(this.enchantmentGlint)

            if (this.unbreakable != null) meta.isUnbreakable = this.unbreakable == true
            if (this.hideTooltip != null) meta.isHideTooltip = this.hideTooltip == true
            if (this.fireResistant != null) meta.isFireResistant = this.fireResistant == true

            if (this.persistentDataConsumer != null) {
                val data = PersistentData(meta, FlauschigeMinecraftLibrary.getLibrary().plugin)
                this.persistentDataConsumer?.invoke(data)
            }

            consumer.invoke(item, meta)
        }
    }
}
class ColoredBuilder internal constructor(itemStack: ItemStack) : DefaultBuilder<ColoredBuilder>(itemStack) {
    private var color: Color? = null
    
    fun setColor(color: Color): ColoredBuilder {
        this.color = color
        return this
    }

    override fun item(): ItemStack {
        consumer = { _, meta -> (meta as ColorableArmorMeta).setColor(color) }
        return super.item()
    }
}
class SkullBuilder internal constructor(itemStack: ItemStack) : DefaultBuilder<SkullBuilder>(itemStack) {

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun clearEnchantments(): SkullBuilder {
        throw IllegalAccessException()
    }
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun addEnchantment(enchantment: Enchantment, level: Int): SkullBuilder {
        throw IllegalAccessException()
    }
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun removeEnchantment(enchantment: Enchantment, level: Int): SkullBuilder {
        throw IllegalAccessException()
    }
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun removeEnchantments(vararg enchantment: Enchantment): SkullBuilder {
        throw IllegalAccessException()
    }
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun removeEnchantments(enchantment: Set<Enchantment>): SkullBuilder {
        throw IllegalAccessException()
    }
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun setEnchantments(enchantments: Map<Enchantment, Int>): SkullBuilder {
        throw IllegalAccessException()
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun setEnchantmentGlint(glint: Boolean): SkullBuilder {
        throw IllegalAccessException()
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun item(): ItemStack {
        throw IllegalAccessException()
    }
    
    fun item(player: OfflinePlayer): ItemStack {
        this.consumer = { _, meta -> (meta as SkullMeta).setOwningPlayer(player) }
        return super.item()
    }
    fun item(playerName: String): ItemStack {
        return this.item(Bukkit.getOfflinePlayer(playerName))
    }
    fun item(playerUUID: UUID): ItemStack {
        return this.item(Bukkit.getOfflinePlayer(playerUUID))
    }
}
class Modifier internal constructor(itemStack: ItemStack) : DefaultBuilder<Modifier>(itemStack) {

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    override fun item(): ItemStack {
        throw IllegalAccessException()
    }
}