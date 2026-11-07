package dev.spike.spikeKits.inventoryHolders;

import dev.spike.spikeKits.Kit;
import dev.spike.spikeKits.utils.Messages;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.spike.spikeKits.utils.Messages.formatTime;

public class KitsMenu implements InventoryHolder {
    private final Inventory inventory;

    public KitsMenu(Player player, SpikeKits plugin){
        Messages messages = plugin.getMessages();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        Map<Integer, Kit> kits = plugin.getKits();
        Map<Integer, ItemStack> decorations = plugin.getDecorations();

        inventory = Bukkit.createInventory(this, messages.KITS_MENU_SLOTS, messages.KITS_MENU_TITLE);

        // decorations
        for (int slot : decorations.keySet()) {
            ItemStack decoration = decorations.get(slot);
            if (decoration != null) inventory.setItem(slot, decorations.get(slot));
        }

        // kits
        for (int slot : kits.keySet()) {
            Kit kit = kits.get(slot);

            Component canClaim;
            if (!player.hasPermission(kit.getPermission())) {
                canClaim = messages.KIT_LORE_NO_PERMISSION;
            } else if (cooldownManager.getRemaining(kit.getName(), player) == 0) {
                canClaim = messages.KIT_LORE_AVAILABLE;
            } else {
                canClaim = messages.format(messages.KIT_LORE_COOLDOWN,
                        Placeholder.unparsed("formatted-time", formatTime(cooldownManager.getRemaining(kit.getName(), player))));
            }

            List<Component> formattedLore = new ArrayList<>();
            for (String line : messages.KIT_LORE){
                formattedLore.add(messages.format(line, Placeholder.unparsed("cooldown", kit.getFormattedCooldown()),
                        Placeholder.component("can-claim", canClaim)).decoration(TextDecoration.ITALIC, false));
            }

            ItemStack kitIcon = kit.getIcon();
            kitIcon.editMeta(meta -> meta.lore(formattedLore));
            inventory.setItem(slot, kitIcon);
        }

        // close button
        if (messages.CLOSE_BUTTON_SLOT != -1) {
            inventory.setItem(messages.CLOSE_BUTTON_SLOT, messages.CLOSE_BUTTON);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}