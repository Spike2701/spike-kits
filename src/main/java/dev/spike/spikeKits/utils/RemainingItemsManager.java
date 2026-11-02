package dev.spike.spikeKits.utils;

import dev.spike.spikeKits.inventoryHolders.ClaimItems;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemainingItemsManager {
    private final Map<UUID, Map<String, ClaimItems>> playerWithRemainingItems = new HashMap<>();

    public void addPlayerWithNotClaimedItems(UUID uuid, ClaimItems claimItems, String kitName) {
        playerWithRemainingItems.computeIfAbsent(uuid, k -> new HashMap<>()).put(kitName, claimItems);
    }

    public boolean hasNotClaimedItems(UUID uuid, String kitName) {
        Map<String, ClaimItems> kit = playerWithRemainingItems.get(uuid);
        return kit != null && kit.containsKey(kitName);
    }

    public void removePlayerWithNotClaimedItems(UUID uuid, String kitName) {
        Map<String, ClaimItems> kit = playerWithRemainingItems.get(uuid);

        if (kit == null) return;

        kit.remove(kitName);
        if (kit.isEmpty()) playerWithRemainingItems.remove(uuid);
    }

    public Inventory getNotClaimedItems(UUID uuid, String kitName) {
        Map<String, ClaimItems> kit = playerWithRemainingItems.get(uuid);
        return kit.get(kitName).getInventory();
    }
}
