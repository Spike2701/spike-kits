package dev.spike.spikeKits.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.inventoryHolders.KitsMenu;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class KitCommand {
    private static final String PERMISSION = "spike-kits.command.kit";

    public LiteralCommandNode<CommandSourceStack> create(SpikeKits plugin) {
        return Commands.literal("kit")
                .requires(source -> source.getSender() instanceof Player && source.getSender().hasPermission(PERMISSION))

                .executes(context -> {
                    Player player = (Player) context.getSource().getSender();
                    plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(new KitsMenu(player, plugin).getInventory()));
                    return Command.SINGLE_SUCCESS;

                }).build();
    }
}