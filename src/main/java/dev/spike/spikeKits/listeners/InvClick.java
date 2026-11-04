package dev.spike.spikeKits.listeners;

import dev.spike.spikeKits.Kit;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.inventoryHolders.ClaimItems;
import dev.spike.spikeKits.inventoryHolders.KitPreview;
import dev.spike.spikeKits.inventoryHolders.KitsMenu;
import dev.spike.spikeKits.utils.Messages;
import dev.spike.spikeKits.utils.RemainingItemsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static dev.spike.spikeKits.utils.Messages.formatTime;

public class InvClick implements Listener {
    private final SpikeKits plugin;
    private final CooldownManager cooldownManager;
    private final RemainingItemsManager remainingItemsManager;
    private final Messages messages;

    public InvClick(SpikeKits plugin) {
        this.plugin = plugin;
        this.cooldownManager = plugin.getCooldownManager();
        this.remainingItemsManager = plugin.getRemainingItemsManager();
        this.messages = plugin.getMessages();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (e.getInventory().getHolder() instanceof KitsMenu) {
            e.setCancelled(true);

            if (slot == messages.CLOSE_BUTTON_SLOT) {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
            } else if (plugin.getKits().containsKey(slot)) {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(new KitPreview(player, slot, plugin).getInventory()));
            }



        } else if (e.getInventory().getHolder() instanceof KitPreview kitPreview) {
            e.setCancelled(true);

            if (slot == messages.BACK_BUTTON_SLOT) {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(new KitsMenu(player, plugin).getInventory()));

            } else if (slot == messages.CLAIM_BUTTON_SLOT) {
                int slotInKitsMenu = kitPreview.getSlotInKitsMenu();
                Kit kit = plugin.getKits().get(slotInKitsMenu);

                String kitName = kit.getName();
                String formattedTime = formatTime(cooldownManager.getRemaining(kitName, player));
                String formattedKitName = kit.getDisplayName();
                long cooldown = kit.getCooldown();

                if (!player.hasPermission(kit.getPermission())) {
                    messages.claimButtonClickedAction(player, "no-permission", formattedKitName, formattedTime, slotInKitsMenu);
                } else if (cooldownManager.getRemaining(kitName, player) == 0) {
                    messages.claimButtonClickedAction(player, "claimed", formattedKitName, formattedTime, slotInKitsMenu);
                    cooldownManager.setCooldown(kitName, player, cooldown);
                } else {
                    if (remainingItemsManager.hasNotClaimedItems(player.getUniqueId(), kitName)) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(remainingItemsManager.getNotClaimedItems(player.getUniqueId(), kitName)));
                    } else {
                        messages.claimButtonClickedAction(player, "cooldown", formattedKitName, formattedTime, slotInKitsMenu);
                    }
                }
            }



        } else if (e.getInventory().getHolder() instanceof ClaimItems) {
            e.setCancelled(true);
            Inventory clickedInv = e.getClickedInventory();

            if (clickedInv != null && clickedInv.getHolder() instanceof ClaimItems claimItems) {
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) return;

                clickedInv.setItem(slot, null);
                HashMap<Integer, ItemStack> itemsIfFullInv = player.getInventory().addItem(clickedItem.clone());
                if (!itemsIfFullInv.isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemsIfFullInv.get(0));
                }
                claimItems.subtrackOneItem();

                if (claimItems.getItemsQuantity() == 0) {
                    messages.claimButtonClickedAction(player, "claimed-all", "", "", slot);
                } else {
                    player.playSound(player.getLocation(), "entity.item.pickup", 1.0f, 1.0f);
                }
            }
        }
    }
}
