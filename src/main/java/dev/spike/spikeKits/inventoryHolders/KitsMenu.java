package dev.spike.spikeKits.inventoryHolders;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KitsMenu implements InventoryHolder {
    private final Inventory inventory;

    public KitsMenu(Player player, CooldownManager cm, SpikeKits pl){
        FileConfiguration config = pl.getConfig();

        inventory = Bukkit.createInventory(this, pl.getKitsMenuSlots(), pl.getKitsMenuTitle());
        for (int slot : pl.getKitsMenuContents().keySet()) {
            ItemStack kitIcon = pl.getKitsMenuContents().get(slot).clone();

            String kitName = pl.getKitsNames().get(slot).replace(".yml", "");
            if (kitName.equals("close-button") || kitName.equals("decoration")) {
                inventory.setItem(slot, kitIcon);
                continue;
            }
            String permission = "spike-kits.kit." + kitName;

            String canClaim;
            if (!player.hasPermission(permission)) {
                canClaim = pl.getConfig().getString("kits-menu.can-claim.no-permission", "failed to load kits-menu.can-claim.no-permission in config.yml");
            } else if (cm.getRemaining(kitName, player) == 0) {
                canClaim = pl.getConfig().getString("kits-menu.can-claim.available", "failed to load kits-menu.can-claim.available in config.yml");
            } else {
                canClaim = pl.getConfig().getString("kits-menu.can-claim.cooldown", "failed to load kits-menu.can-claim.cooldown in config.yml").replace("{formatted-time}",
                        DurationFormatUtils.formatDuration(cm.getRemaining(kitName, player), "[d'd' H'h' m'm' s's']"));
            }

            String formattedCooldown = PlainTextComponentSerializer.plainText().serialize(kitIcon.lore().getFirst());

            List<Component> formattedLore = new ArrayList<>();
            for (String line : config.getStringList("kits-menu.kit-lore")){
                formattedLore.add(formatLore(line, player, canClaim, formattedCooldown, config));
            }
            kitIcon.editMeta(meta -> meta.lore(formattedLore));

            inventory.setItem(slot, kitIcon);
        }
    }
    
    private Component formatLore(String line, Player player, String canClaim, String formattedCooldown, FileConfiguration config) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(line.replace("{can-claim}", canClaim)
                .replace("{cooldown}", formattedCooldown)).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
