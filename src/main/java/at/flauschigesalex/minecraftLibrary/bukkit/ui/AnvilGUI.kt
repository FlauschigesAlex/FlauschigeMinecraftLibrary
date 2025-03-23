@file:Suppress("DeprecatedCallableAddReplaceWith", "UnstableApiUsage", "unused", "MemberVisibilityCanBePrivate", "DEPRECATION")
@file:OptIn(ExperimentalStdlibApi::class)

package at.flauschigesalex.minecraftLibrary.bukkit.ui

import at.flauschigesalex.defaultLibrary.any.InputValidator
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.Paper
import at.flauschigesalex.minecraftLibrary.bukkit.Paper.char
import at.flauschigesalex.minecraftLibrary.bukkit.Paper.name
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import at.flauschigesalex.minecraftLibrary.bukkit.reflect.PluginListener
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI.Companion.getOpenGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import org.jetbrains.annotations.Range
import java.util.*
import java.util.concurrent.CompletableFuture

private val anvilTypingControllers = HashMap<Player, UUID>()

/**
 * @since 1.6.0
 */

@ExperimentalStdlibApi
abstract class AnvilGUI(autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
                        val legacyTitle: String = " ",
) : PluginGUI(9, autoUpdateTickDelay, Component.text(legacyTitle)) {

    companion object {
        @JvmStatic
        protected fun String.toLegacyColored(): String {
            var string = this

            Paper.getNamedTextColorValues().forEach {
                string = string.replace("<${it.name.lowercase()}>", "§${it.char}")
                string = string.replace("&${it.char}", "§${it.char}")
            }
            TextDecoration.entries.forEach {
                string = string.replace("<${it.name.lowercase()}>", "§${it.char}")
                string = string.replace("&${it.char}", "§${it.char}")
            }
            return string
        }

        val defaultInputItem: ItemStack =
            ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setHideTooltip(true).item()
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override val size: Int
        get() = super.size

    @Deprecated("Unused", level = DeprecationLevel.HIDDEN)
    final override fun createGUI(player: Player): Inventory {
        return super.createGUI(player)
    }

    /**
     * Called before running [AnvilGUI.onTyping] to check if the provided input is valid.
     * @return null if the string is valid, else item to display.
     */
    open fun catchInvalidInput(player: Player, inputString: String): ItemStack? {
        return null
    }
    internal fun isValidInput(player: Player, inputString: String): Boolean {
        return catchInvalidInput(player, inputString) == null
    }

    open fun onTyping(player: Player, inventory: AnvilInventory, input: InputValidator<String>): ItemStack? {
        return null
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onClick(clickEvent: PluginGUIClick): Boolean {
        val anvilView = clickEvent.player.openInventory as AnvilView
        val renameText = anvilView.renameText ?: ""
        
        val s = CompletableFuture<InputValidator<String>>().completeAsync {
            InputValidator(renameText, {
                this.isValidInput(clickEvent.player, it)
            })
        }
        
        return onClick(clickEvent, s.join())
    }
    protected open fun onClick(clickEvent: PluginGUIClick, input: InputValidator<String>): Boolean {
        clickEvent.cancelEvent()
        return false
    }

    @Deprecated("Unused", level = DeprecationLevel.HIDDEN)
    final override fun designGUI(player: Player, inventory: Inventory) {
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun loadGUI(player: Player, inventory: Inventory) {
        val anvilView = player.openInventory as AnvilView
        this.loadGUI(player, inventory as AnvilInventory, InputValidator(anvilView.renameText ?: "", {
            this.isValidInput(player, it)
        }))
    }
    protected open fun loadGUI(player: Player, inventory: AnvilInventory, input: InputValidator<String>) {
        inventory.firstItem = defaultInputItem
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun loadLiveGUI(player: Player, inventory: Inventory) {
        try {
            val anvilView = player.openInventory as AnvilView
            this.loadLiveGUI(player, inventory as AnvilInventory, InputValidator(anvilView.renameText ?: "", {
                this.isValidInput(player, it)
            }))
        } catch (ignore: Exception) {}
    }
    protected open fun loadLiveGUI(player: Player, inventory: AnvilInventory, input: InputValidator<String>) {
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onOpen(player: Player, inventory: Inventory): Boolean {
        return this.onOpen(player, inventory as AnvilInventory)
    }
    open fun onOpen(player: Player, inventory: AnvilInventory): Boolean {
        return false
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onClose(player: Player, inventory: Inventory): Boolean {
        return this.onClose(player, inventory as AnvilInventory)
    }
    open fun onClose(player: Player, inventory: AnvilInventory): Boolean {
        inventory.clear()
        return false
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun reload(player: Player, loadBackground: Boolean): Boolean {
        return this.reload(player)
    }
    open fun reload(player: Player): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val view = player.openInventory
        if (view !is AnvilView) return false

        val inventory = view.topInventory

        this.loadGUI(player, inventory, InputValidator(view.renameText ?: "", {
            this.isValidInput(player, it)
        }))
        return true
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun reloadForAllViewers(loadBackground: Boolean) {
    }

    open fun reloadForAllViewers() {
        super.reloadForAllViewers(false)
    }

    protected open fun anvilView(player: Player, view: AnvilView, inventory: AnvilInventory) {
        view.title = legacyTitle.toLegacyColored()
        view.repairCost = 0
    }

    override fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        val view = player.openAnvil(null, true)
            ?: return

        openGUIs[player.uniqueId] = this

        val inventory = view.topInventory
        if (inventory !is AnvilInventory || view !is AnvilView)
            return

        this.anvilView(player, view, inventory)
        this.loadGUI(player, inventory, InputValidator(view.renameText ?: "", {
            this.isValidInput(player, it)
        }))

        val firstItem = inventory.firstItem
        if (firstItem != null) {
            val meta = firstItem.itemMeta
            PersistentData(meta, FlauschigeMinecraftLibrary.getLibrary().plugin).set("uuid", player.uniqueId.toString())

            inventory.firstItem!!.setItemMeta(meta)
        }

        this.onOpen(player, inventory)
        if (autoUpdateTickDelay > 0)
            this.liveInventory(player, inventory)
    }
}

/**
 * @since 1.6.0
 */
private class AnvilListener private constructor(): PluginListener() {

    @EventHandler
    private fun onTyping(event: PrepareAnvilEvent) {
        val inventory = event.inventory

        val uuid = inventory.firstItem?.itemMeta
            ?.let { PersistentData(it, FlauschigeMinecraftLibrary.getLibrary().plugin) }?.get("uuid")
            ?.let { UUID.fromString(it) } ?: return
        val player = Bukkit.getPlayer(uuid) ?: return

        val gui = player.getOpenGUI() ?: return
        if (gui !is AnvilGUI) return

        inventory.result = ItemBuilder(Material.AIR).item()
        event.view.repairCost = 0

        val renameText = event.view.renameText ?: ""

        Bukkit.getScheduler().runTaskAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin, Runnable {
            val invalidInput = gui.catchInvalidInput(player, renameText)
            
            if (invalidInput != null)
                inventory.result = invalidInput

            val result = gui.onTyping(player, inventory, InputValidator(renameText, invalidInput == null))

            if (result != null && invalidInput == null)
                inventory.result = result
        })
    }
}