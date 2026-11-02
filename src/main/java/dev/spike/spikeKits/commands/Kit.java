package dev.spike.spikeKits.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.spike.spikeKits.utils.CooldownManager;
import dev.spike.spikeKits.SpikeKits;
import dev.spike.spikeKits.inventoryHolders.KitsMenu;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class Kit {
    public LiteralCommandNode<CommandSourceStack> create(CooldownManager cm, SpikeKits pl){
        return Commands.literal("kit")
                .requires(source -> source.getSender() instanceof Player && source.getSender().hasPermission("spike-kits.command.kit"))

                .executes(context -> {
                    Player player = (Player) context.getSource().getSender();
                    pl.getServer().getScheduler().runTask(pl, () -> player.openInventory(new KitsMenu(player, cm, pl).getInventory()));
                    return Command.SINGLE_SUCCESS;

                }).build();
    }
}
