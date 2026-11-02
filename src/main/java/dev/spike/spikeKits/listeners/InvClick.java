package dev.spike.spikeKits.listeners;

import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.inventoryHolders.ClaimItems;
import dev.spike.spikeKits.inventoryHolders.KitPreview;
import dev.spike.spikeKits.inventoryHolders.KitsMenu;
import dev.spike.spikeKits.utils.RemainingItemsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;

public class InvClick implements Listener {
    private final SpikeKits pl;
    private final CooldownManager cm;
    private final RemainingItemsManager rim;

    public InvClick(SpikeKits pl, CooldownManager cm, RemainingItemsManager rim) {
        this.pl = pl;
        this.cm = cm;
        this.rim = rim;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (e.getInventory().getHolder() instanceof KitsMenu) {
            e.setCancelled(true);

            if (slot == pl.getCloseButtonSlot()) {
                pl.getServer().getScheduler().runTask(pl, () -> player.closeInventory());
            } else if (pl.getKitsNames().containsKey(slot) && !pl.getKitsNames().get(slot).equals("decoration")) {
                pl.getServer().getScheduler().runTask(pl, () -> player.openInventory(new KitPreview(player, pl.getKitsNames().get(slot), cm, pl).getInventory()));
            }



        } else if (e.getInventory().getHolder() instanceof KitPreview kitPreview) {
            e.setCancelled(true);

            if (slot == pl.getBackButtonSlot()) {
                pl.getServer().getScheduler().runTask(pl, () -> player.openInventory(new KitsMenu(player, cm, pl).getInventory()));

            } else if (slot == pl.getClaimButtonSlot()) {
                String kitName = kitPreview.getKitName().replace(".yml", "");
                String formattedTime = pl.formatTime(kitName, player);

                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(pl.getDataFolder(), "kits/" + kitName + ".yml"));
                String formattedKitName = yaml.getString("icon.name", "failed to load icon.name");
                long cooldown = yaml.getLong("cooldown-in-seconds", 60) * 1000;

                if (!player.hasPermission("spike-kits.kit." + kitName)) {
                    sendMessagesAndTrigger(player, "no-permission", formattedKitName, formattedTime, kitName);
                } else if (cm.getRemaining(kitName, player) == 0) {
                    sendMessagesAndTrigger(player, "claimed", formattedKitName, formattedTime, kitName);
                    cm.setCooldown(kitName, player, cooldown);
                } else {
                    if (rim.hasNotClaimedItems(player.getUniqueId(), kitName)) {
                        pl.getServer().getScheduler().runTask(pl, () -> player.openInventory(rim.getNotClaimedItems(player.getUniqueId(), kitName)));
                    } else {
                        sendMessagesAndTrigger(player, "cooldown", formattedKitName, formattedTime, kitName);
                    }
                }
            }



        } else if (e.getInventory().getHolder() instanceof ClaimItems) {
            e.setCancelled(true);
            Inventory clickedInv = e.getClickedInventory();

            if (clickedInv != null && clickedInv.getHolder() instanceof ClaimItems claimItems) {
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) return;

                clickedInv.setItem(e.getRawSlot(), null);
                HashMap<Integer, ItemStack> itemsIfFullInv = player.getInventory().addItem(clickedItem.clone());
                if (!itemsIfFullInv.isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemsIfFullInv.get(0));
                }
                claimItems.subtrackOneItem();

                if (claimItems.getItemsQuantity() == 0) {
                    sendMessagesAndTrigger(player, "claimed-all", "", "", claimItems.getKitName());
                } else {
                    player.playSound(player.getLocation(), "entity.item.pickup", 1.0f, 1.0f);
                }
            }
        }
    }

    private void sendMessagesAndTrigger(Player player, String type, String formattedKitName, String formattedTime, String kitName) {
        Component message = formatAndReplace(pl, "messages." + type + ".chat", formattedKitName, formattedTime);
        if (!message.equals(Component.text(""))) player.sendMessage(message);

        Component title = formatAndReplace(pl, "messages." + type + ".title", formattedKitName, formattedTime);
        Component subtitle = formatAndReplace(pl, "messages." + type + ".subtitle", formattedKitName, formattedTime);
        player.showTitle(Title.title(title, subtitle));

        if (type.equals("claimed")) {
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
            pl.getServer().getScheduler().runTask(pl, () -> player.openInventory(new ClaimItems(kitName, pl).getInventory()));
        } else if (type.equals("claimed-all")){
            player.playSound(player.getLocation(), "block.chest.close", 1.0f, 1.0f);
            pl.getServer().getScheduler().runTask(pl, () -> player.closeInventory());
        } else {
            player.playSound(player.getLocation(), "entity.villager.no", 1.0f, 1.0f);
            pl.getServer().getScheduler().runTask(pl, () -> player.closeInventory());
        }
    }

    private Component formatAndReplace(SpikeKits pl, String path, String formattedKitName, String formattedTime) {
        String text = pl.getConfig().getString(path, "failed to load " + path);

        if (text.isEmpty()) return Component.text("");

        return pl.format(text.replace("{formatted-time}", formattedTime).replace("{formatted-name}", formattedKitName));
    }
}
