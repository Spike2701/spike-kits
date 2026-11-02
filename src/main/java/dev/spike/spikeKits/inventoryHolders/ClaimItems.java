package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.SpikeKits;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClaimItems implements InventoryHolder {
    private final Inventory inventory;
    private final String kitName;
    private int itemsQuantity = 0;

    public ClaimItems(String kitName, SpikeKits pl) {
        this.kitName = kitName;

        Component claimItemsGuiTitle = pl.format("kit-claim.title", pl.getConfig());
        List<ItemStack> items = pl.getKitItems().get(kitName);
        int rows = Math.max(1, (int) Math.ceil(items.size() / 9.0));

        inventory = Bukkit.createInventory(this, rows * 9, claimItemsGuiTitle);

        for (ItemStack item : items) {
            inventory.addItem(item.clone());
            itemsQuantity++;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getKitName() {
        return kitName;
    }

    public int getItemsQuantity() {
        return itemsQuantity;
    }

    public void subtrackOneItem() {
        itemsQuantity--;
    }
}
