@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage", "DEPRECATION")

package at.flauschigesalex.minecraftLibrary.translation

import at.flauschigesalex.defaultLibrary.any.Validator
import at.flauschigesalex.defaultLibrary.translation.TranslatedLocale
import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary
import at.flauschigesalex.minecraftLibrary.bukkit.PersistentData
import at.flauschigesalex.minecraftLibrary.bukkit.ui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.view.AnvilView
import org.jetbrains.annotations.Range

/**
 * @since 1.6.0
 */
@ExperimentalStdlibApi
abstract class TranslatedAnvilGUI protected constructor(
    val translationKey: String,
    protected val titleCreator: Pair<(Player, Map<String, Any>) -> String, Map<String, Any>> =
        Pair({ player, _ -> TranslatedLocale.of(player).find("$translationKey.inventoryName").value }, mapOf()),
    autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0
): AnvilGUI(autoUpdateTickDelay) {

    init {
        TranslatedLocale.validateKey(translationKey)
    }

    override fun anvilView(player: Player, view: AnvilView, inventory: AnvilInventory) {
        Bukkit.getScheduler().runTaskAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin) { _ ->
            view.title = titleCreator.first.invoke(player, titleCreator.second).toLegacyColored()
            view.repairCost = 0
        }
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

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        val view = player.openAnvil(null, true)
            ?: return

        openGUIs[player.uniqueId] = this

        val inventory = view.topInventory
        if (inventory !is AnvilInventory || view !is AnvilView)
            return

        this.anvilView(player, view, inventory)

        Bukkit.getScheduler().runTaskAsynchronously(FlauschigeMinecraftLibrary.getLibrary().plugin) { _ ->
            try {
                this.loadGUI(player, inventory, Validator(view.renameText ?: "", {
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
        }

        this.onOpen(player, inventory)
        if (autoUpdateTickDelay > 0)
            this.liveInventory(player, inventory)
    }
}