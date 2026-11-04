package dev.spike.spikeKits;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static dev.spike.spikeKits.utils.Messages.formatTime;

public class Kit {
    private final String name;
    private final long cooldown;
    private final String formattedCooldown;
    private final String displayName;
    private final String permission;
    private final ItemStack icon;
    private final List<ItemStack> items;

    public Kit(String name, long cooldown, ItemStack icon, List<ItemStack> items) {
        this.name = name;
        this.cooldown = cooldown;
        this.formattedCooldown = formatTime(cooldown);
        this.displayName = MiniMessage.miniMessage().serialize(icon.getItemMeta().displayName());
        this.permission = "spike-kits.kit." + name;
        this.icon = icon;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public long getCooldown() {
        return cooldown;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public String getFormattedCooldown() {
        return formattedCooldown;
    }
}
