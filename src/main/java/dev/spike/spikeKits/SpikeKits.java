package dev.spike.spikeKits;

import dev.spike.spikeKits.commands.Kit;
import dev.spike.spikeKits.commands.SpikeKitsCommand;
import dev.spike.spikeKits.inventoryHolders.EditMenu;
import dev.spike.spikeKits.listeners.InvClick;
import dev.spike.spikeKits.listeners.InvClose;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.utils.RemainingItemsManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class SpikeKits extends JavaPlugin {
    // kits menu things
    private Component kitsMenuTitle;
    private int kitsMenuSlots;
    private final Map<Integer, ItemStack> kitsMenuContents = new HashMap<>();
    private int closeButtonSlot;
    private final Map<Integer, String> kitsNames = new HashMap<>();

    // kit preview things
    private Component kitPreviewTitle;
    private int kitPreviewSlots;
    private final Map<String, List<ItemStack>> kitItems = new HashMap<>();
    private ItemStack backButton;
    private int backButtonSlot;
    private int claimButtonSlot;

    private CooldownManager cm;
    private RemainingItemsManager rim;
    private Inventory editMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        File kits = new File(getDataFolder(), "kits");
        if (kits.mkdirs()){
            getLogger().info("Folder 'kits' has been created");
        } else if (!kits.exists()) {
            getLogger().severe("Failed to create folder 'kits'");
        }

        if (kits.isDirectory() && kits.list().length == 0) {
            this.saveResource("kits/player.yml", false);
            this.saveResource("kits/vip.yml", false);
            this.saveResource("kits/svip.yml", false);
            this.saveResource("kits/gvip.yml", false);
        }

        this.cm = new CooldownManager(this);
        this.rim = new RemainingItemsManager();
        this.editMenu = new EditMenu(this).getInventory();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(new Kit().create(cm, this));
            event.registrar().register(new SpikeKitsCommand().create(this));
        });
        getServer().getPluginManager().registerEvents(new InvClick(this, cm, rim), this);
        getServer().getPluginManager().registerEvents(new InvClose(this, rim), this);

        load();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        load();
    }

    public void load() {
        kitsNames.clear();
        kitsMenuContents.clear();
        kitItems.clear();

        // KITS MENU
        kitsMenuTitle = format("kits-menu.title", getConfig());
        kitsMenuSlots = getConfig().getInt("kits-menu.rows", 5) * 9;

        List<?> decorations = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "kits-menu.yml"))
                .getList("decorations", List.of());
        for(int i = 0; i < decorations.size(); i++){
            if(decorations.get(i) instanceof ItemStack item){
                kitsMenuContents.put(i, item.clone());
                kitsNames.put(i, "decoration");
            }
        }

        File[] kitsFiles = new File(getDataFolder(), "kits").listFiles();
        if (kitsFiles == null) return;
        for (File kit : kitsFiles) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(kit);
            int slot = yaml.getInt("icon.slot");
            kitsMenuContents.put(slot, loadItem("icon.", yaml));
            kitsNames.put(slot, kit.getName().replace(".yml", ""));
        }
        // close button (if -1, don't add to kits menu)
        closeButtonSlot = getConfig().getInt("kits-menu.close-button.slot", 40);
        if (closeButtonSlot != -1) {
            kitsMenuContents.put(closeButtonSlot, loadItem("kits-menu.close-button.", getConfig(), "", null));
            kitsNames.put(closeButtonSlot, "close-button");
        }

        // KIT PREVIEW
        kitPreviewTitle = format("kit-preview.title", getConfig());
        kitPreviewSlots = getConfig().getInt("kit-preview.rows", 5) * 9;

        // back button
        backButtonSlot = getConfig().getInt("kit-preview.back-button.slot", 44);
        backButton = loadItem("kit-preview.back-button.", getConfig(), "", null);

        // claim button
        claimButtonSlot = getConfig().getInt("kit-preview.claim-button.slot", 43);

        // items
        for (String kit : kitsNames.values()) {
            if (kit.equals("close-button")) continue;

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "kits/" + kit + ".yml"));
            List<?> itemsList = yaml.getList("items", List.of());
            List<ItemStack> items = new ArrayList<>();
            for (Object o : itemsList) {
                if (o instanceof ItemStack item) {
                    items.add(item);
                }
            }
            kitItems.put(kit, items);
        }
    }

    public ItemStack loadItem(String startingPath, YamlConfiguration yaml) {
        Material material = Material.matchMaterial(yaml.getString(startingPath + "material", "barrier"));
        if (material == null) material = Material.BARRIER;
        ItemStack item = new ItemStack(material, yaml.getInt(startingPath + "amount", 1));
        item.editMeta(meta -> {
            meta.customName(format(startingPath + "name", yaml));
            String formattedCooldown = yaml.getString("formatted-cooldown");
            if (formattedCooldown != null) meta.lore(List.of(Component.text(yaml.getString("formatted-cooldown", "&fformatted-cooldown&c not found"))));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
        });

        return item;
    }

    public ItemStack loadItem(String startingPath, FileConfiguration config, String kitName, Player player) {
        Material material = Material.matchMaterial(config.getString(startingPath + "material", "barrier"));
        if (material == null) material = Material.BARRIER;
        ItemStack item = new ItemStack(material, config.getInt(startingPath + "amount", 1));
        item.editMeta(meta -> {
            meta.customName(format(startingPath + "name", config));

            List<Component> formattedLore = new ArrayList<>();
            for (String line : config.getStringList(startingPath + "lore")){
                if (!kitName.isEmpty()) {
                    line = line.replace("{formatted-time}", formatTime(kitName, player));
                }
                formattedLore.add(format(line));
            }
            meta.lore(formattedLore);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
        });

        return item;
    }

    // format time & format messages
    public String formatTime(String kitName, Player player) {
        return DurationFormatUtils.formatDuration(cm.getRemaining(kitName, player), "[d'd' H'h' m'm' s's']");
    }

    public Component format(String path, FileConfiguration config) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(config.getString(path, path + "&c not found in config.yml"))
                .decoration(TextDecoration.ITALIC, false);
    }

    public Component format(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    // getters
    public Inventory getEditMenu() {
        return editMenu;
    }

    public Component getKitsMenuTitle() {
        return kitsMenuTitle;
    }

    public Component getKitPreviewTitle() {
        return kitPreviewTitle;
    }

    public int getKitsMenuSlots() {
        return kitsMenuSlots;
    }

    public int getKitPreviewSlots() {
        return kitPreviewSlots;
    }

    public Map<Integer, ItemStack> getKitsMenuContents() {
        return kitsMenuContents;
    }

    public Map<Integer, String> getKitsNames() {
        return kitsNames;
    }

    public int getCloseButtonSlot() {
        return closeButtonSlot;
    }

    public int getBackButtonSlot() {
        return backButtonSlot;
    }

    public int getClaimButtonSlot() {
        return claimButtonSlot;
    }

    public ItemStack getBackButton() {
        return backButton;
    }

    public Map<String, List<ItemStack>> getKitItems() {
        return kitItems;
    }
}
