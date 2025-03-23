@file:Suppress("unused", "MemberVisibilityCanBePrivate", "DEPRECATION")

package at.flauschigesalex.minecraftLibrary.bukkit.ui

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.PluginListener
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI.Companion.getOpenGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.Range
import java.util.*
import kotlin.math.max

operator fun Inventory.get(index: Int): ItemStack? {
    return this.getItem(index)
}
operator fun Inventory.set(index: Int, item: ItemStack?) {
    this.setItem(index, item)
}

/**
 * @since v1.5.0
 */
abstract class PluginGUI protected constructor(
    open val size: Int,
    protected val autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
    protected val titleConstructor: (Player) -> Component 
) {
    
    constructor(
        size: Int,
        autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
        title: Component = Component.text(" ")) : this(size, autoUpdateTickDelay, { _ -> title})

    companion object {
        @Deprecated("")
        val openGUIs = hashMapOf<UUID, PluginGUI>()

        @JvmStatic
        fun HumanEntity.getOpenGUI(): PluginGUI? {
            return openGUIs[this.uniqueId]
        }

        val controllers = HashSet<BukkitTask>()
    }

    protected open fun createGUI(player: Player): Inventory {
        return Bukkit.createInventory(player, size, titleConstructor.invoke(player))
    }
    protected open fun designGUI(player: Player, inventory: Inventory) {
        val black = ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
            .setHideTooltip(true).item()
        val gray = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .setHideTooltip(true).item()

        for (slot in 0 until inventory.size) {
            if (slot < 9 || slot > inventory.size -10) inventory[slot] = black
            else inventory[slot] = gray
        }
    }
    protected open fun loadGUI(player: Player, inventory: Inventory) {}
    protected open fun loadLiveGUI(player: Player, inventory: Inventory) {
        return loadGUI(player, inventory)
    }

    open fun onClick(clickEvent: PluginGUIClick): Boolean {
        return false
    }
    open fun onOpen(player: Player, inventory: Inventory): Boolean {
        return false
    }
    open fun onClose(player: Player, inventory: Inventory): Boolean {
        return false
    }

    protected fun liveInventory(player: Player, inventory: Inventory) {
        if (autoUpdateTickDelay <= 0)
            return

        Bukkit.getScheduler().runTaskLaterAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin, { it ->
            it?.run { controllers.add(this) }

            if (!player.isOnline || player.getOpenGUI() != this) {
                it?.run {
                    controllers.remove(it)
                    it.cancel()
                }

                openGUIs.remove(player.uniqueId, this)
                return@runTaskLaterAsynchronously
            }

            this.loadLiveGUI(player, inventory)
        }, max(autoUpdateTickDelay, 1).toLong())
    }

    open fun reloadForAllViewers(loadBackground: Boolean = false) {
        viewers.forEach { reload(it, loadBackground) }
    }
    open fun reload(player: Player, loadBackground: Boolean = false): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val gui = player.openInventory.topInventory

        if (loadBackground)
            this.designGUI(player, gui)
        
        this.loadGUI(player, gui)
        return true
    }

    open fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player, true)
            return
        }

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        val inventory = this.createGUI(player)
        openGUIs[player.uniqueId] = this

        this.designGUI(player, inventory)
        this.loadGUI(player, inventory)

        player.openInventory(inventory)
        this.onOpen(player, inventory)
        if (autoUpdateTickDelay > 0)
            this.liveInventory(player, inventory)
    }
    
    @OptIn(ExperimentalStdlibApi::class)
    val isAnvilGUI
        get() = this is AnvilGUI

    val viewers: List<Player> get() {
        return Bukkit.getOnlinePlayers().filter {
            it.getOpenGUI() == this
        }
    }
}

private class PluginGUIListener private constructor(): PluginListener() {

    @EventHandler
    private fun inventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val gui = player.getOpenGUI()
            ?: return

        if (event.clickedInventory != event.view.topInventory)
            return

        gui.onClick(PluginGUIClick(
            player,
            gui,
            event.clickedInventory!!,
            event.currentItem,
            event.slot,
            event.click,
            event.cursor.let {
                if (it.type.isAir)
                    return@let null

                return@let it
            }, event))
    }

    @EventHandler
    private fun inventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        val gui = player.getOpenGUI() ?: return
        
        val inventory = player.openInventory.topInventory

        if (gui.isAnvilGUI)
            player.openInventory.topInventory.clear()

        Bukkit.getScheduler().runTaskLater(FlauschigeMinecraftLibrary.getLibrary().plugin, Runnable {
            player.openInventory.topInventory.location ?: return@Runnable

            PluginGUI.openGUIs.remove(player.uniqueId, gui)
            gui.onClose(player, inventory)
        }, 1)
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val gui = player.getOpenGUI() ?: return

        gui.onClose(player, player.openInventory.topInventory)
        
        if (gui.isAnvilGUI)
            player.openInventory.topInventory.clear()
        
        PluginGUI.openGUIs.remove(player.uniqueId, gui)
    }
}

data class PluginGUIClick(val player: Player,
                          val gui: PluginGUI,
                          val inventory: Inventory,
                          val clickedItem: ItemStack?,
                          val clickedSlot: Int,
                          val clickType: ClickType,
                          val cursorItem: ItemStack?,
                          @Deprecated("") val bukkitEvent: InventoryClickEvent,
) {
    fun cancelEvent() {
        bukkitEvent.isCancelled = true
    }

    override fun toString(): String {
        return listOf(player, gui, inventory, clickedItem, clickedSlot, clickType, cursorItem, bukkitEvent).toString()
    }
}