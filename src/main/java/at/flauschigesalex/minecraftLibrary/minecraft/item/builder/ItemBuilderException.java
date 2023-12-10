package at.flauschigesalex.minecraftLibrary.minecraft.item.builder;

import at.flauschigesalex.defaultLibrary.exception.LibraryException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public final class ItemBuilderException extends LibraryException {

    public static boolean isInventoryCapable(Material material) {
        Inventory inventory = Bukkit.createInventory(null, 9);
        inventory.setItem(0, new ItemStack(material));
        return inventory.getItem(0) != null;
    }

    public static ItemBuilderException materialFail(Material material) {
        return new ItemBuilderException("Item material cannot be null!");
    }
    public static ItemBuilderException inventoryFail(Material material) {
        return new ItemBuilderException("Given material "+material+" cannot be contained in an inventory!");
    }

    private ItemBuilderException(String message) {
        super(message);
    }
}
