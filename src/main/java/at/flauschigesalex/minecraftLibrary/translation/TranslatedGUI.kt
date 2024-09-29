@file:Suppress("MemberVisibilityCanBePrivate", "unused")

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

abstract class TranslatedGUI(
    val translationKey: String,
    protected val titleCreator: Pair<(Player, Map<String, Any>) -> Component, Map<String, Any>> =
        Pair({ player, _ -> TranslationHandler(player).createComponent("$translationKey.inventoryName") }, mapOf()),
    size: @Range(from = 9.toLong(), to = 54.toLong()) Int,
    autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0
) : PluginGUI(size, autoUpdateTickDelay) {

    init {
        TranslationValidator.validateKey(translationKey)
    }

    override fun createGUI(player: Player): Inventory {
        return Bukkit.createInventory(player, size, titleCreator.first.invoke(player, titleCreator.second))
    }

    protected open fun onLoad(player: Player) {}

    @Deprecated("")
    final override fun onClick(clickEvent: PluginGUIClick): Boolean {
        if (super.onClick(clickEvent))
            return true

        val translationKey = clickEvent.clickedItem?.let { PersistentData(it.itemMeta).get("translationKey") }
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
        this.onLoad(player)

        Task.createAsyncTask {

            val gui = createGUI(player)
            openGUIs[player.uniqueId] = this

            this.designGUI(player, gui)

            Task.createAsyncTask {
                this.loadGUI(player, gui)
            }.execute()

            Bukkit.getScheduler().runTask(FlauschigeMinecraftLibrary.library.plugin, Runnable {

                player.openInventory(gui)
                this.onOpen(player, gui)
                if (autoUpdateTickDelay > 0)
                    this.liveInventory(player, gui)
            })
        }.execute()
    }
}