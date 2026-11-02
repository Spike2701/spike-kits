package dev.spike.spikeKits.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.spike.spikeKits.SpikeKits;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpikeKitsCommand {
    public LiteralCommandNode<CommandSourceStack> create(SpikeKits pl) {
        return Commands.literal("spikekits")
                .requires(source -> source.getSender() instanceof Player &&
                        source.getSender().hasPermission("spike-kits.command.spikekits"))
                .executes(context -> sendHelp(context, pl))


                .then(Commands.literal("help")
                        .executes(context -> sendHelp(context, pl)))


                .then(Commands.literal("reload")
                        .executes(context -> {
                            pl.reloadConfig();
                            context.getSource().getSender().sendMessage(formatMessage("Plugin has been reloaded", pl));
                            return Command.SINGLE_SUCCESS;
                        }))


                .then(Commands.literal("create")
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(formatMessage("Correct usage: &#4a9b1d/spikekits create <name>", pl));
                            return 0;
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createKit((Player) context.getSource().getSender(), StringArgumentType.getString(context, "name"), pl))))


                .then(Commands.literal("remove")
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(formatMessage("Correct usage: &#4a9b1d/spikekits remove <name>", pl));
                            return 0;
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> removeKit((Player) context.getSource().getSender(), StringArgumentType.getString(context, "name"), pl))))


                .then(Commands.literal("setkititems")
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(formatMessage("Correct usage: &#4a9b1d/spikekits setkititems <name>", pl));
                            return 0;
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    Player player = (Player) context.getSource().getSender();
                                    String name = StringArgumentType.getString(context, "name");
                                    File file = getKitFile(pl, name);
                                    if (!file.exists()) {
                                        player.sendMessage(formatMessage("&#ff5c5cKit &#a42626" + name + "&#ff5c5c doesn't exist", pl));
                                        return 0;
                                    }

                                    if (setKitItems(player, name,
                                            YamlConfiguration.loadConfiguration(file), file, pl) != 1) return 0;

                                    player.sendMessage(formatMessage("Saved items of kit: &#4a9b1d" + name, pl));
                                    return Command.SINGLE_SUCCESS;
                                })))


                .then(Commands.literal("list")
                        .executes(context -> kitsList((Player) context.getSource().getSender(), pl)))


                .then(Commands.literal("editmenu")
                        .executes(context -> {
                            Player player = (Player) context.getSource().getSender();
                            pl.getServer().getScheduler().runTask(pl, () -> player.openInventory(pl.getEditMenu()));
                            return Command.SINGLE_SUCCESS;
                        })).build();
    }

    private int sendHelp(CommandContext<CommandSourceStack> context, SpikeKits pl) {
        context.getSource().getSender().sendMessage(formatMessage("""
                Useful commands:
                &f/spikekits help &8- &7Shows this commands
                &f/spikekits reload &8- &7Reloads configuration
                &f/spikekits create <name> &8- &7Creates kit
                &f/spikekits remove <name> &8- &7Removes kit
                &f/spikekits list &8- &7Shows list of all kits
                
                  &7(create command makes items in your inventory items of\s
                  &7the kit. Finish your configuration in kits/kit-name.yml)""", pl));
        return Command.SINGLE_SUCCESS;
    }

    private Component formatMessage(String message, SpikeKits pl) {
        return pl.format("&#4a9b1dSpike&fKits &8» &#b8ff56" + message);
    }

    private File getKitFile(SpikeKits pl, String name){
        return new File(pl.getDataFolder(), "kits/" + name + ".yml");
    }

    private int createKit(Player player, String name, SpikeKits pl){
        File file = getKitFile(pl, name);
        if(file.exists()){
            player.sendMessage(formatMessage("&#ff5c5cKit &#a42626" + name + "&#ff5c5c already exists. Use &#a42626/kit remove " + name, pl));
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

        if (setKitItems(player, name, yaml, file, pl) != 1) return 0;

        player.sendMessage(formatMessage("Created kit: &#4a9b1d" + name, pl));
        return Command.SINGLE_SUCCESS;
    }

    private int setKitItems(Player player, String name, YamlConfiguration yaml, File file, SpikeKits pl) {
        PlayerInventory inv = player.getInventory();

        List<ItemStack> items = new ArrayList<>();
        for(int i = 9; i <= 35; i++){
            items.add(inv.getItem(i));
        }

        yaml.set("items", items);

        try{
            yaml.save(file);
        } catch (IOException e) {
            player.sendMessage(formatMessage("&#ff5c5cFailed to save kit. Check console", pl));
            pl.getLogger().severe("Failed to save kit " + name + ": " + e.getMessage());
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeKit(Player player, String name, SpikeKits pl){
        File file = getKitFile(pl, name);

        if (!file.delete()) {
            player.sendMessage(formatMessage("&#ff5c5cKit &#a42626" + name + "&#ff5c5c doesn't exist or couldn't be deleted", pl));
            return 0;
        }

        player.sendMessage(formatMessage("Kit &#4a9b1d" + name + "&#b8ff56 has been removed", pl));
        return Command.SINGLE_SUCCESS;
    }

    private int kitsList(Player player, SpikeKits pl){
        File[] files = new File(pl.getDataFolder(), "kits").listFiles();

        if(files == null || files.length == 0){
            player.sendMessage(formatMessage("#&ff5c5cNo available kits", pl));
            return 0;
        }

        player.sendMessage(formatMessage("Available kits:", pl));
        for(File file : files){
            player.sendMessage(formatMessage("&8 - &f" + file.getName(), pl));
        }

        return Command.SINGLE_SUCCESS;
    }
}
