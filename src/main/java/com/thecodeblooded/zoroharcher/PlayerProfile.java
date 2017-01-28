package com.thecodeblooded.zoroharcher;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Created by TheCo on 28/01/2017.
 */
public class PlayerProfile {

    private UUID uuid;

    private Long lastTagged = null;

    private PotionEffectType potionEffectType = null;

    public PlayerProfile(Player player) {
        this.uuid = player.getUniqueId();
    }

    public UUID getUUID() {
        return uuid;
    }

    public Long getLastTagged() {
        return lastTagged;
    }

    public void setLastTagged(Long lastTagged) {
        this.lastTagged = lastTagged;
    }

    public boolean elapsed() {
        if(lastTagged == null)
            return true;
        return System.currentTimeMillis() - this.lastTagged >= 10000;
    }

    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    public void setPotionEffectType(PotionEffectType potionEffectType) {
        this.potionEffectType = potionEffectType;
    }
}
