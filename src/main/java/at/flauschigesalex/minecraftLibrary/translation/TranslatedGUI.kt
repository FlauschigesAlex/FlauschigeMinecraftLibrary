@file:Suppress("MemberVisibilityCanBePrivate", "unused", "DeprecatedCallableAddReplaceWith")

package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.task.Task
import at.flauschigesalex.defaultLibrary.translation.TranslationValidator
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUI
import at.flauschigesalex.minecraftLibrary.bukkit.ui.PluginGUIClick
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.jetbrains.annotations.Range

abstract class TranslatedGUI protected constructor(
    val translationKey: String,
    protected val titleCreator: Pair<(Player, Map<String, Any>) -> Component, Map<String, Any>> =
        Pair({ player, _ -> TranslationHandler(player).createComponent("$translationKey.inventoryName") }, mapOf()),
    final override val size: @Range(from = 9.toLong(), to = 54.toLong()) Int,
    autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0
) : PluginGUI(size, autoUpdateTickDelay) {

    @Deprecated("")
    override val title: Component
        get() = super.title

    init {
        TranslationValidator.validateKey(translationKey)
    }

    override fun createGUI(player: Player): Inventory {
        return Bukkit.createInventory(player, size, titleCreator.first.invoke(player, titleCreator.second))
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

    override fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }
        this.preLoad(player)

        Task.createAsyncTask {

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
        }.execute()
    }
}