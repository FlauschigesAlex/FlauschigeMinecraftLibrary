@file:Suppress("MemberVisibilityCanBePrivate", "DeprecatedCallableAddReplaceWith", "unused")

package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUIClick
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.jetbrains.annotations.Range

abstract class TranslatedGUI protected constructor(
    val translationKey: String,
    override val size: Int,
    val replacements: Map<String, Any> = mapOf(),
    autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0
) : PluginGUI(size, autoUpdateTickDelay) {
    
    init {
        TranslatedLocale.validateKey(translationKey)
    }

    override fun createGUI(player: Player): Inventory {
        return Bukkit.createInventory(player, size, TranslatedMessage(player)
            .createComponent("${translationKey}.inventoryName", replacements = replacements).value)
    }

    protected open fun preLoad(player: Player) {}

    @Deprecated("")
    final override fun onClick(clickEvent: PluginGUIClick): Boolean {
        if (super.onClick(clickEvent))
            return true

        val translationKey = clickEvent.clickedItem?.let { PersistentData(it.itemMeta, FlauschigeMinecraftLibrary.getLibrary().plugin).get("translationKey") }
        return onClick(clickEvent, translationKey)
    }

    protected open fun onClick(clickEvent: PluginGUIClick, translationKey: String?): Boolean {
        return false
    }
    
    override fun reload(player: Player, loadBackground: Boolean): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val gui = player.openInventory.topInventory

        if (loadBackground)
            this.designGUI(player, gui)

        Bukkit.getScheduler().runTaskAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin, Runnable {
            this.loadGUI(player, gui)
        })
        return true
    }

    @Suppress("DEPRECATION")
    override fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }
        this.preLoad(player)

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        Bukkit.getScheduler().runTaskAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin, Runnable {
            val gui = createGUI(player)
            openGUIs[player.uniqueId] = this

            this.designGUI(player, gui)
            this.loadGUI(player, gui)

            Bukkit.getScheduler().runTask(FlauschigeMinecraftLibrary.getLibrary().plugin, Runnable {

                player.openInventory(gui)
                this.onOpen(player, gui)
                if (autoUpdateTickDelay > 0)
                    this.liveInventory(player, gui)
            })
        })
    }
}