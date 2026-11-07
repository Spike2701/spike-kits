package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.Kit;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.utils.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.spike.spikeKits.utils.Messages.formatTime;

public class KitPreview implements InventoryHolder {
    private final Inventory inventory;
    private final int slotInKitsMenu;

    public KitPreview(Player player, int slotInKitsMenu, SpikeKits plugin) {
        Messages messages = plugin.getMessages();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        Kit kit = plugin.getKits().get(slotInKitsMenu);
        this.slotInKitsMenu = slotInKitsMenu;

        inventory = Bukkit.createInventory(this, messages.KIT_PREVIEW_SLOTS, messages.KIT_PREVIEW_TITLE);

        // kit items
        for (ItemStack item : kit.getItems()) {
            inventory.addItem(item);
        }

        // back button
        inventory.setItem(messages.BACK_BUTTON_SLOT, messages.BACK_BUTTON);

        // claim button (available || no permission || cooldown)
        ItemStack claimButton;
        if (!player.hasPermission(kit.getPermission())) {
            claimButton = messages.CLAIM_BUTTON_NO_PERMISSION;
        } else if (cooldownManager.getRemaining(kit.getName(), player) == 0) {
            claimButton = messages.CLAIM_BUTTON_AVAILABLE;
        } else {
            claimButton = messages.CLAIM_BUTTON_COOLDOWN.clone();
            List<Component> lore = claimButton.lore();

            if (lore != null) {
                Component formattedTime = Component.text(formatTime(cooldownManager.getRemaining(kit.getName(), player)));

                List<Component> formattedLore = new ArrayList<>();
                for (Component line : lore) {
                    formattedLore.add(line.replaceText(config -> {
                        config.matchLiteral("<formatted-time>");
                        config.replacement(formattedTime);
                    }));
                }
                claimButton.editMeta(meta -> meta.lore(formattedLore));
            }
        }


        inventory.setItem(messages.CLAIM_BUTTON_SLOT, claimButton);
    }

    public int getSlotInKitsMenu() {
        return slotInKitsMenu;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}