package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.SpikeKits;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EditMenu implements InventoryHolder {
    private final Inventory inventory;

    public EditMenu(SpikeKits pl) {
        inventory = Bukkit.createInventory(this, pl.getConfig().getInt("kits-menu.rows", 5) * 9, Component.text("Edit kits menu"));

        for (int slot : pl.getKitsMenuContents().keySet()) {
            if (pl.getKitsNames().get(slot).replace(".yml", "").equals("decoration")) {
                inventory.setItem(slot, pl.getKitsMenuContents().get(slot).clone());
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
