package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClaimItems implements InventoryHolder {
    private final Inventory inventory;
    private final int slotInKitsMenu;
    private int itemsQuantity = 0;

    public ClaimItems(int slotInKitsMenu, SpikeKits plugin) {
        this.slotInKitsMenu = slotInKitsMenu;
        Messages messages = plugin.getMessages();

        List<ItemStack> items = plugin.getKits().get(slotInKitsMenu).getItems();
        int rows = Math.max(1, (int) Math.ceil(items.size() / 9.0));

        inventory = Bukkit.createInventory(this, rows * 9, messages.CLAIM_ITEMS_TITLE);

        for (ItemStack item : items) {
            inventory.addItem(item.clone());
            itemsQuantity++;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getSlotInKitsMenu() {
        return slotInKitsMenu;
    }

    public int getItemsQuantity() {
        return itemsQuantity;
    }

    public void subtrackOneItem() {
        itemsQuantity--;
    }
}