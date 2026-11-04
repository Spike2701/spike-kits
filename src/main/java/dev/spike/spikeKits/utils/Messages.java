package dev.spike.spikeKits.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.inventoryHolders.ClaimItems;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Messages {
    private final SpikeKits plugin;

    public String PREFIX;
    public String HELP;
    public String RELOADED;
    public String CREATE_CORRECT_USAGE;
    public String REMOVE_CORRECT_USAGE;
    public String SET_KIT_ITEMS_CORRECT_USAGE;
    public String KIT_DOES_NOT_EXIST;
    public String KIT_SAVED_ITEMS;
    public String ALREADY_EXISTS;
    public String CREATED_KIT;
    public String FAILED_TO_SAVE;
    public String KIT_REMOVED;
    public String NO_AVAILABLE_KITS;
    public String AVAILABLE_KITS;
    public String KIT_IN_LIST;
    public String KITS_MENU_TITLE;
    public int KITS_MENU_SLOTS;
    public String KIT_PREVIEW_TITLE;
    public int KIT_PREVIEW_SLOTS;
    public String CLAIM_ITEMS_TITLE;
    public int CLOSE_BUTTON_SLOT;
    public ItemStack CLOSE_BUTTON;
    public int BACK_BUTTON_SLOT;
    public ItemStack BACK_BUTTON;
    public int CLAIM_BUTTON_SLOT;
    public ItemStack CLAIM_BUTTON_AVAILABLE;
    public ItemStack CLAIM_BUTTON_NO_PERMISSION;
    public ItemStack CLAIM_BUTTON_COOLDOWN;
    public String KIT_LORE_AVAILABLE;
    public String KIT_LORE_NO_PERMISSION;
    public String KIT_LORE_COOLDOWN;
    public List<String> KIT_LORE;
    public String MESSAGES_CLAIMED_CHAT;
    public String MESSAGES_CLAIMED_TITLE;
    public String MESSAGES_CLAIMED_SUBTITLE;
    public String MESSAGES_CLAIMED_ALL_CHAT;
    public String MESSAGES_CLAIMED_ALL_TITLE;
    public String MESSAGES_CLAIMED_ALL_SUBTITLE;
    public String MESSAGES_NO_PERMISSION_CHAT;
    public String MESSAGES_NO_PERMISSION_TITLE;
    public String MESSAGES_NO_PERMISSION_SUBTITLE;
    public String MESSAGES_COOLDOWN_CHAT;
    public String MESSAGES_COOLDOWN_TITLE;
    public String MESSAGES_COOLDOWN_SUBTITLE;

    public Messages(SpikeKits plugin) {
        this.plugin = plugin;
    }

    public void init() {
        FileConfiguration config = plugin.getConfig();

        PREFIX = "<#4a9b1d>Spike<white>Kits <dark_gray>» <#b8ff56>";
        HELP = PREFIX + """
            Useful commands:
            <white>/spikekits help <dark_gray>- <gray>Shows this commands
            <white>/spikekits reload <dark_gray>- <gray>Reloads configuration
            <white>/spikekits create \\<name> <dark_gray>- <gray>Creates kit
            <white>/spikekits remove \\<name> <dark_gray>- <gray>Removes kit
            <white>/spikekits list <dark_gray>- <gray>Shows list of all kits

            <gray>(create command makes items in your inventory items of\s
            <gray>the kit. Finish your configuration in kits/kit-name.yml)""";

        RELOADED = PREFIX + "Plugin has been reloaded";
        CREATE_CORRECT_USAGE = PREFIX + "Correct usage: <#4a9b1d>/spikekits create \\<name>";
        REMOVE_CORRECT_USAGE = PREFIX + "Correct usage: <#4a9b1d>/spikekits remove \\<name>";
        SET_KIT_ITEMS_CORRECT_USAGE = PREFIX + "Correct usage: <#4a9b1d>/spikekits setkititems \\<name>";
        KIT_DOES_NOT_EXIST = PREFIX + "<#ff5c5c>Kit <#a42626><name> <#ff5c5c>doesn't exist";
        KIT_SAVED_ITEMS = PREFIX + "Saved items of kit: <#4a9b1d><name>. <#b8ff56>Now do: <#4a9b1d>/spikekits reload";
        ALREADY_EXISTS = PREFIX + "<#ff5c5c>Kit <#a42626><name> <#ff5c5c>already exists. Use <#a42626>/kit remove \\<name>";
        CREATED_KIT = PREFIX + "Created kit: <#4a9b1d><name>. <#b8ff56>Now do: <#4a9b1d>/spikekits reload";
        FAILED_TO_SAVE = PREFIX + "<#ff5c5c>Failed to save kit. Check console";
        KIT_REMOVED = PREFIX + "Kit <#4a9b1d><name> <#b8ff56>has been removed. Now do: <#4a9b1d>/spikekits reload";
        NO_AVAILABLE_KITS = PREFIX + "<#ff5c5c>No available kits";
        AVAILABLE_KITS = PREFIX + "Available kits:";
        KIT_IN_LIST = " <dark_gray>- <white><name>";

        KITS_MENU_TITLE = config.getString("kits-menu.title");
        KITS_MENU_SLOTS = config.getInt("kits-menu.rows", 5) * 9;
        KIT_PREVIEW_TITLE = config.getString("kit-preview.title");
        KIT_PREVIEW_SLOTS = config.getInt("kit-preview.rows", 5) * 9;
        CLAIM_ITEMS_TITLE = config.getString("claim-items.title");

        CLOSE_BUTTON_SLOT = config.getInt("kits-menu.close-button.slot", 40);
        CLOSE_BUTTON = plugin.loadItem("kits-menu.close-button.", config);
        BACK_BUTTON_SLOT = config.getInt("kit-preview.back-button.slot", 44);
        BACK_BUTTON = plugin.loadItem("kit-preview.back-button.", config);
        CLAIM_BUTTON_SLOT = config.getInt("kit-preview.claim-button.slot", 43);
        CLAIM_BUTTON_AVAILABLE = plugin.loadItem("kit-preview.claim-button.can-claim.", config);
        CLAIM_BUTTON_NO_PERMISSION = plugin.loadItem("kit-preview.claim-button.no-permission.", config);
        CLAIM_BUTTON_COOLDOWN = plugin.loadItem("kit-preview.claim-button.cooldown.", config);

        KIT_LORE_AVAILABLE = config.getString("kits-menu.can-claim.available");
        KIT_LORE_NO_PERMISSION = config.getString("kits-menu.can-claim.no-permission");
        KIT_LORE_COOLDOWN = config.getString("kits-menu.can-claim.cooldown");
        KIT_LORE = config.getStringList("kits-menu.kit-lore");

        MESSAGES_CLAIMED_CHAT = config.getString("messages.claimed.chat");
        MESSAGES_CLAIMED_TITLE = config.getString("messages.claimed.title");
        MESSAGES_CLAIMED_SUBTITLE = config.getString("messages.claimed.subtitle");

        MESSAGES_CLAIMED_ALL_CHAT = config.getString("messages.claimed-all.chat");
        MESSAGES_CLAIMED_ALL_TITLE = config.getString("messages.claimed-all.title");
        MESSAGES_CLAIMED_ALL_SUBTITLE = config.getString("messages.claimed-all.subtitle");

        MESSAGES_NO_PERMISSION_CHAT = config.getString("messages.no-permission.chat");
        MESSAGES_NO_PERMISSION_TITLE = config.getString("messages.no-permission.title");
        MESSAGES_NO_PERMISSION_SUBTITLE = config.getString("messages.no-permission.subtitle");

        MESSAGES_COOLDOWN_CHAT = config.getString("messages.cooldown.chat");
        MESSAGES_COOLDOWN_TITLE = config.getString("messages.cooldown.title");
        MESSAGES_COOLDOWN_SUBTITLE = config.getString("messages.cooldown.subtitle");
    }

    public int sendMessage(CommandContext<CommandSourceStack> context, String message, TagResolver... resolvers) {
        context.getSource().getExecutor().sendMessage(format(message, resolvers));
        return Command.SINGLE_SUCCESS;
    }

    public Component format(String message, TagResolver... resolvers) {
        return MiniMessage.miniMessage().deserialize(message, resolvers);
    }

    public static String formatTime(long time) {
        return DurationFormatUtils.formatDuration(time, "[d'd' H'h' m'm' s's']");
    }

    public void claimButtonClickedAction(Player player, String type, String formattedKitName, String formattedTime, int slotInKitsMenu) {
        String chat, title, subtitle;

        switch (type) {
            case "claimed" -> {
                chat = MESSAGES_CLAIMED_CHAT; title = MESSAGES_CLAIMED_TITLE; subtitle = MESSAGES_CLAIMED_SUBTITLE;
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(new ClaimItems(slotInKitsMenu, plugin).getInventory()));
            }
            case "claimed-all" -> {
                chat = MESSAGES_CLAIMED_ALL_CHAT; title = MESSAGES_CLAIMED_ALL_TITLE; subtitle = MESSAGES_CLAIMED_ALL_SUBTITLE;
                player.playSound(player.getLocation(), "block.chest.close", 1.0f, 1.0f);
                plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
            }
            case "no-permission" -> {
                chat = MESSAGES_NO_PERMISSION_CHAT; title = MESSAGES_NO_PERMISSION_TITLE; subtitle = MESSAGES_NO_PERMISSION_SUBTITLE;
                player.playSound(player.getLocation(), "entity.villager.no", 1.0f, 1.0f);
                plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
            }
            default -> {
                chat = MESSAGES_COOLDOWN_CHAT; title = MESSAGES_COOLDOWN_TITLE; subtitle = MESSAGES_COOLDOWN_SUBTITLE;
                player.playSound(player.getLocation(), "entity.villager.no", 1.0f, 1.0f);
                plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
            }
        }

        TagResolver[] resolvers = {Placeholder.parsed("formatted-name", formattedKitName), Placeholder.unparsed("formatted-time", formattedTime)};

        if (!chat.isEmpty()) player.sendMessage(format(chat, resolvers));
        player.showTitle(Title.title(format(title, resolvers), format(subtitle, resolvers)));
    }
}
