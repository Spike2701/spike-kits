package dev.spike.spikeKits;

import dev.spike.spikeKits.commands.KitCommand;
import dev.spike.spikeKits.commands.SpikeKitsCommand;
import dev.spike.spikeKits.inventoryHolders.EditMenu;
import dev.spike.spikeKits.listeners.InvClick;
import dev.spike.spikeKits.listeners.InvClose;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.utils.Messages;
import dev.spike.spikeKits.utils.RemainingItemsManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class SpikeKits extends JavaPlugin {
    private final Map<Integer, Kit> kits = new HashMap<>();
    private final Map<Integer, ItemStack> decorations = new HashMap<>();
    private CooldownManager cooldownManager;
    private Messages messages;
    private RemainingItemsManager remainingItemsManager;
    private Inventory editMenu;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager(this);
        messages = new Messages(this);
        messages.init();
        remainingItemsManager = new RemainingItemsManager();
        editMenu = new EditMenu(this).getInventory();

        saveDefaultKits();
        registerCommands();
        registerEvents();

        loadKits();
        loadDecorations();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        saveDefaultKits();
        loadKits();
        loadDecorations();
    }

    private void saveDefaultKits() {
        saveDefaultConfig();
        File kitsMenu = new File(getDataFolder(), "kits-menu.yml");
        if (!kitsMenu.exists()) {
            saveResource("kits-menu.yml", false);
        }

        File kits = new File(getDataFolder(), "kits");
        if (kits.mkdirs()){
            getLogger().info("Folder 'kits' has been created");
        } else if (!kits.exists()) {
            getLogger().severe("Failed to create folder 'kits'");
        }

        if (kits.isDirectory() && kits.list().length == 0) {
            saveResource("kits/player.yml", false);
            saveResource("kits/vip.yml", false);
            saveResource("kits/svip.yml", false);
            saveResource("kits/gvip.yml", false);
        }
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(new KitCommand().create(this));
            event.registrar().register(new SpikeKitsCommand().create(this));
        });
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new InvClick(this), this);
        getServer().getPluginManager().registerEvents(new InvClose(this), this);
    }

    private void loadKits() {
        kits.clear();

        File[] kitsFiles = new File(getDataFolder(), "kits").listFiles();
        if (kitsFiles == null) return;
        for (File kit : kitsFiles) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(kit);

            // items
            List<?> itemsList = yaml.getList("items", List.of());
            List<ItemStack> items = new ArrayList<>();
            for (Object o : itemsList) {
                if (o instanceof ItemStack item) {
                    items.add(item);
                }
            }

            // kit properties
            int slot = yaml.getInt("icon.slot");
            String kitName = kit.getName().replace(".yml", "");
            long cooldown = yaml.getLong("cooldown-in-seconds") * 1000;
            ItemStack icon = loadItem("icon.", yaml);

            kits.put(slot, (new Kit(kitName, cooldown, icon, items)));
        }
    }

    private void loadDecorations() {
        decorations.clear();

        List<?> decorationsList = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "kits-menu.yml")).getList("decorations", List.of());
        for(int i = 0; i < decorationsList.size(); i++){
            if(decorationsList.get(i) instanceof ItemStack item){
                decorations.put(i, item.clone());
            }
        }
    }

    public ItemStack loadItem(String startingPath, FileConfiguration config) {
        Material material = Material.matchMaterial(config.getString(startingPath + "material", "barrier"));
        if (material == null) material = Material.BARRIER;
        ItemStack item = new ItemStack(material, config.getInt(startingPath + "amount", 1));
        item.editMeta(meta -> {
            meta.customName(messages.format(config.getString(startingPath + "name")).decoration(TextDecoration.ITALIC, false));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);

            if (!config.getStringList(startingPath + "lore").isEmpty()) {
                List<Component> formattedLore = new ArrayList<>();
                for (String line : config.getStringList(startingPath + "lore")){
                    formattedLore.add(messages.format(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(formattedLore);
            }
        });

        return item;
    }

    // getters
    public Map<Integer, ItemStack> getDecorations() {
        return decorations;
    }

    public Messages getMessages() {
        return messages;
    }

    public Map<Integer, Kit> getKits() {
        return kits;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public RemainingItemsManager getRemainingItemsManager() {
        return remainingItemsManager;
    }

    public Inventory getEditMenu() {
        return editMenu;
    }
}
