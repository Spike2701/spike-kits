package dev.spike.spikeKits.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.utils.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpikeKitsCommand {
    private static final String PERMISSION = "spike-kits.command.spikekits";
    private Messages messages;
    private SpikeKits plugin;

    public LiteralCommandNode<CommandSourceStack> create(SpikeKits plugin) {
        this.messages = plugin.getMessages();
        this.plugin = plugin;

        return Commands.literal("spikekits")
                .requires(source -> source.getSender() instanceof Player &&
                        source.getSender().hasPermission(PERMISSION))


                .executes(context -> messages.sendMessage(context, messages.HELP))


                .then(Commands.literal("help")
                        .executes(context -> messages.sendMessage(context, messages.HELP)))


                .then(Commands.literal("reload")
                        .executes(context -> {
                            plugin.reloadAll();
                            return messages.sendMessage(context, messages.RELOADED);
                        }))


                .then(Commands.literal("create")
                        .executes(context -> messages.sendMessage(context, messages.CREATE_CORRECT_USAGE))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createKit((Player) context.getSource().getSender(), StringArgumentType.getString(context, "name")))))


                .then(Commands.literal("remove")
                        .executes(context -> messages.sendMessage(context, messages.REMOVE_CORRECT_USAGE))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> removeKit((Player) context.getSource().getSender(), StringArgumentType.getString(context, "name")))))


                .then(Commands.literal("setkititems")
                        .executes(context -> messages.sendMessage(context, messages.SET_KIT_ITEMS_CORRECT_USAGE))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    File file = getKitFile(name);
                                    if (!file.exists()) {
                                        return messages.sendMessage(context, messages.KIT_DOES_NOT_EXIST, Placeholder.unparsed("name", name));
                                    }

                                    if (setKitItems((Player) context.getSource().getSender(), name,
                                            YamlConfiguration.loadConfiguration(file), file) != 1) return 0;

                                    return messages.sendMessage(context, messages.KIT_SAVED_ITEMS, Placeholder.unparsed("name", name));
                                })))


                .then(Commands.literal("list")
                        .executes(context -> kitsList((Player) context.getSource().getSender())))


                .then(Commands.literal("editmenu")
                        .executes(context -> {
                            Player player = (Player) context.getSource().getSender();
                            plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(plugin.getEditMenu()));
                            return Command.SINGLE_SUCCESS;
                        })).build();
    }

    private File getKitFile(String name){
        return new File(plugin.getDataFolder(), "kits/" + name + ".yml");
    }

    private int createKit(Player player, String name){
        File file = getKitFile(name);
        if(file.exists()){
            player.sendMessage(messages.format(messages.ALREADY_EXISTS, Placeholder.unparsed("name", name)));
            return 0;
        }

        // default values
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set("cooldown-in-seconds", 60);
        yaml.set("formatted-cooldown", "1m");
        yaml.set("icon.name", "&eEdit name in kits/kit-name.yml");
        yaml.set("icon.material", "wooden_pickaxe");
        yaml.set("icon.amount", 1);
        yaml.set("icon.slot", 0);

        if (setKitItems(player, name, yaml, file) != 1) return 0;

        player.sendMessage(messages.format(messages.CREATED_KIT, Placeholder.unparsed("name", name)));
        return Command.SINGLE_SUCCESS;
    }

    private int setKitItems(Player player, String name, YamlConfiguration yaml, File file) {
        PlayerInventory inv = player.getInventory();

        List<ItemStack> items = new ArrayList<>();
        for(int i = 9; i <= 35; i++){
            items.add(inv.getItem(i));
        }

        yaml.set("items", items);

        try {
            yaml.save(file);
        } catch (IOException e) {
            player.sendMessage(messages.FAILED_TO_SAVE);
            plugin.getLogger().severe("Failed to save kit " + name + ": " + e.getMessage());
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeKit(Player player, String name){
        File file = getKitFile(name);

        if (!file.delete()) {
            player.sendMessage(messages.format(messages.KIT_DOES_NOT_EXIST, Placeholder.unparsed("name", file.getName())));
            return 0;
        }

        player.sendMessage(messages.format(messages.KIT_REMOVED, Placeholder.unparsed("name", file.getName())));
        return Command.SINGLE_SUCCESS;
    }

    private int kitsList(Player player){
        File[] files = new File(plugin.getDataFolder(), "kits").listFiles();

        if(files == null || files.length == 0){
            player.sendMessage(messages.NO_AVAILABLE_KITS);
            return 0;
        }

        player.sendMessage(messages.AVAILABLE_KITS);
        for(File file : files){
            player.sendMessage(messages.format(messages.KIT_IN_LIST, Placeholder.unparsed("name", file.getName())));
        }

        return Command.SINGLE_SUCCESS;
    }
}