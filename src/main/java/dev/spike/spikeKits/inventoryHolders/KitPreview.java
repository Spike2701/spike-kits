package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KitPreview implements InventoryHolder {
    private final Inventory inventory;
    private final String kitName;

    public KitPreview(Player player, String kitName, CooldownManager cm, SpikeKits pl) {
        inventory = Bukkit.createInventory(this, pl.getKitPreviewSlots(), pl.getKitPreviewTitle());
        this.kitName = kitName;

        for (ItemStack item : pl.getKitItems().get(kitName)) {
            inventory.addItem(item);
        }

        inventory.setItem(pl.getBackButtonSlot(), pl.getBackButton());

        String permission = "spike-kits.kit." + kitName;
        int claimButtonSlot = pl.getClaimButtonSlot();

        if (!player.hasPermission(permission)) {
            inventory.setItem(claimButtonSlot, pl.loadItem("kit-preview.claim-button.no-permission.", pl.getConfig(), "", null));
        } else if (cm.getRemaining(kitName, player) == 0) {
            inventory.setItem(claimButtonSlot, pl.loadItem("kit-preview.claim-button.can-claim.", pl.getConfig(), "", null));
        } else {
            inventory.setItem(claimButtonSlot, pl.loadItem("kit-preview.claim-button.cooldown.", pl.getConfig(), kitName, player));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getKitName() {
        return kitName;
    }
}
