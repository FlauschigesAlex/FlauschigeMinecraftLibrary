@file:Suppress("UnstableApiUsage", "unused")

package at.flauschigesalex.minecraftLibrary.bukkit.utils

import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.inventory.meta.ColorableArmorMeta

val Material.isColorable : Boolean
    get() = this.asItemType()?.typed()?.itemMetaClass?.isAssignableFrom(ColorableArmorMeta::class.java) ?: false

val Material.isContainer : Boolean
    get() = asBlockType()?.createBlockData()?.createBlockState() is Container