@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage", "DEPRECATION")

package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.any.InputValidator
import at.flauschigesalex.minecraftLibrary.bukkit.ui.AnvilGUI
import at.flauschigesalex.defaultLibrary.task.Task
import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.view.AnvilView
import org.jetbrains.annotations.Range
import java.lang.IllegalStateException

/**
 * @since 1.6.0
 */
abstract class TranslatedAnvilGUI protected constructor(
    val translationKey: String,
    protected val titleCreator: Pair<(Player, Map<String, Any>) -> String, Map<String, Any>> =
        Pair({ player, _ -> TranslatedLocale.of(player).find("$translationKey.inventoryName") }, mapOf()),
    autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0
): AnvilGUI(autoUpdateTickDelay) {

    init {
        TranslatedLocale.validateKey(translationKey)
    }

    override fun anvilView(player: Player, view: AnvilView, inventory: AnvilInventory) {
        Task.createAsyncTask {
            view.title = titleCreator.first.invoke(player, titleCreator.second).toLegacyColored()
            view.repairCost = 0
        }.execute()
        view.repairCost = 0

        this.preLoad(player, inventory)
    }

    protected open fun preLoad(player: Player, inventory: AnvilInventory) {
    }

    override fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }

        val view = player.openAnvil(null, true)
            ?: return

        openGUIs[player.uniqueId] = this

        val inventory = view.topInventory
        if (inventory !is AnvilInventory || view !is AnvilView)
            return

        this.anvilView(player, view, inventory)

        Task.createAsyncTask {
            try {
                this.loadGUI(player, inventory, InputValidator(view.renameText ?: "", {
                    this.isValidInput(player, it)
                }))
            } catch (ignore: IllegalStateException) {
            }

            val firstItem = inventory.firstItem
            if (firstItem != null) {
                val meta = firstItem.itemMeta
                PersistentData(meta, FlauschigeMinecraftLibrary.getLibrary().plugin).set("uuid", player.uniqueId.toString())

                inventory.firstItem!!.setItemMeta(meta)
            }
        }.execute()

        this.onOpen(player, inventory)
        if (autoUpdateTickDelay > 0)
            this.liveInventory(player, inventory)
    }
}