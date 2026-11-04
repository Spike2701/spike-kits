package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.SpikeKits;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EditMenu implements InventoryHolder {
    private final Inventory inventory;

    public EditMenu(SpikeKits plugin) {
        inventory = Bukkit.createInventory(this, plugin.getMessages().KITS_MENU_SLOTS, Component.text("Edit kits menu"));

        Map<Integer, ItemStack> decorations = plugin.getDecorations();
        for (int slot : decorations.keySet()) {
            inventory.setItem(slot, decorations.get(slot));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
