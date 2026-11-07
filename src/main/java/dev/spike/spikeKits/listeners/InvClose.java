package dev.spike.spikeKits.listeners;

import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.inventoryHolders.ClaimItems;
import dev.spike.spikeKits.inventoryHolders.EditMenu;
import dev.spike.spikeKits.utils.RemainingItemsManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvClose implements Listener {
    private final SpikeKits plugin;
    private final RemainingItemsManager remainingItemsManager;

    public InvClose(SpikeKits plugin) {
        this.plugin = plugin;
        this.remainingItemsManager = plugin.getRemainingItemsManager();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof ClaimItems claimItems) {
            Player player = (Player) e.getPlayer();
            String kitName = plugin.getKits().get(claimItems.getSlotInKitsMenu()).getName();

            if (claimItems.getItemsQuantity() == 0) {
                remainingItemsManager.removePlayerWithNotClaimedItems(player.getUniqueId(), kitName);
                return;
            }
            remainingItemsManager.addPlayerWithNotClaimedItems(player.getUniqueId(), claimItems, kitName);


        } else if (e.getInventory().getHolder() instanceof EditMenu) {
            File file = new File(plugin.getDataFolder(), "kits-menu.yml");
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            List<ItemStack> items = new ArrayList<>();
            for(int i = 0; i < e.getInventory().getSize(); i++){
                ItemStack item = e.getInventory().getItem(i);
                if (item != null && !item.getType().isAir()) item.editMeta(meta -> meta.setHideTooltip(true));
                items.add(item);
            }

            yaml.set("decorations", items);
            try {
                yaml.save(file);
            } catch (IOException exception) {
                plugin.getLogger().severe("Failed to save kits-menu.yml: " + exception.getMessage());
                return;
            }

            plugin.reloadAll();
        }
    }
}
