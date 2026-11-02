package dev.spike.spikeKits.utils;

import dev.spike.spikeKits.SpikeKits;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CooldownManager {
    private final SpikeKits pl;

    public CooldownManager(SpikeKits pl) {
        this.pl = pl;
    }

    public NamespacedKey key(String kitName) {
        return new NamespacedKey(pl, "spike-kits_kit_" + kitName + "_cooldown");
    }

    public void setCooldown(String kitName, Player player, long duration) {
        player.getPersistentDataContainer().set(key(kitName), PersistentDataType.LONG, System.currentTimeMillis() + duration);
    }

    public long getRemaining(String kitName, Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        Long canClaimAt = pdc.get(key(kitName), PersistentDataType.LONG);
        if (canClaimAt == null || canClaimAt <= System.currentTimeMillis()) {
            pdc.remove(key(kitName));
            return 0;
        }

        return canClaimAt - System.currentTimeMillis();
    }
}
