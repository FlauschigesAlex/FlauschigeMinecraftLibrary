@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.minecraftLibrary.bukkit.ui

import at.flauschigesalex.defaultLibrary.task.Task
import at.flauschigesalex.defaultLibrary.task.Task.Controller
import at.flauschigesalex.minecraftLibrary.bukkit.BukkitException
import at.flauschigesalex.minecraftLibrary.bukkit.PluginListener
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI.Companion.getOpenGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Range
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * @since v1.5.0
 */
abstract class PluginGUI protected constructor(val size: @Range(from = 9, to = 54) Int,
                                               protected val autoUpdateTickDelay: @Range(
                                                   from = 1,
                                                   to = Long.MAX_VALUE
                                               ) Int = 0,
                                               protected val title: Component = Component.text(" ")
) {

    companion object {
        internal var openGUIs = hashMapOf<UUID, PluginGUI>()

        @JvmStatic
        fun HumanEntity.getOpenGUI(): PluginGUI? {
            return openGUIs[this.uniqueId]
        }

        val controllers = HashSet<Controller>()
    }

    init {
        if (size < 9)
            throw BukkitException("Inventory size of ${this::class.java.simpleName} must not be smaller than 9.")
        if (size > 54)
            throw BukkitException("Inventory size of ${this::class.java.simpleName} must not be larger than 54.")

        if (size % 9 != 0)
            throw BukkitException("Inventory size of ${this::class.java.simpleName} must be dividable by 9.")
    }

    protected open fun createGUI(player: Player): Inventory {
        return Bukkit.createInventory(player, size, title)
    }
    protected open fun designGUI(player: Player, inventory: Inventory) {}
    protected open fun loadGUI(player: Player, inventory: Inventory) {}
    protected open fun loadAsyncGUI(player: Player, inventory: Inventory) {
        return loadGUI(player, inventory)
    }

    open fun onClick(clickEvent: PluginGUIClick): Boolean {
        return false
    }
    open fun onOpen(player: Player, gui: Inventory): Boolean {
        return false
    }
    open fun onClose(player: Player, gui: Inventory): Boolean {
        return false
    }

    internal fun liveInventory(player: Player, gui: Inventory) {
        if (autoUpdateTickDelay <= 0)
            return

        Task.createAsyncTask { optional ->

            optional.ifPresent {
                controllers.add(it)
            }

            if (!player.isOnline || player.getOpenGUI() != this) {
                optional.ifPresent {
                    controllers.remove(it)
                    it.stop()
                }

                openGUIs.remove(player.uniqueId, this)
                return@createAsyncTask
            }

            this.loadAsyncGUI(player, gui)

        }.repeatDelayed(TimeUnit.MILLISECONDS, max(50 * autoUpdateTickDelay, 1).toLong())
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

        Task.createAsyncTask {
            this.loadGUI(player, gui)
        }.execute()
        return true
    }

    open fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player, true)
            return
        }

        val gui = createGUI(player)
        openGUIs[player.uniqueId] = this

        this.designGUI(player, gui)

        Task.createAsyncTask {
            this.loadGUI(player, gui)
        }.execute()

        player.openInventory(gui)
        this.onOpen(player, gui)
        if (autoUpdateTickDelay > 0)
            this.liveInventory(player, gui)
    }

    val viewers: List<Player> get() {
        return Bukkit.getOnlinePlayers().filter {
            it.getOpenGUI() == this
        }
    }
}

class PluginGUIListener private constructor(): PluginListener() {

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

        gui.onClose(player, player.openInventory.topInventory)
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
                          internal val event: InventoryClickEvent,
) {
    fun cancelEvent() {
        event.isCancelled = true
    }

    override fun toString(): String {
        return listOf(player, gui, inventory, clickedItem, clickedSlot, clickType, cursorItem, event).toString()
    }
}