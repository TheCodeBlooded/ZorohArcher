package com.thecodeblooded.zoroharcher;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by TheCo on 28/01/2017.
 */
public class ZorohArcher extends JavaPlugin implements Listener {

    private HashMap<DyeColor, PotionEffect> potionEffects = new HashMap<>();

    private HashMap<UUID, PlayerProfile> playerProfiles = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        this.getConfig().getConfigurationSection("PotionEffects").getKeys(false).forEach(key -> {
            DyeColor color = DyeColor.valueOf(this.getConfig().getString("PotionEffects." + key + ".Color"));

            PotionEffectType effectType = PotionEffectType.getByName(this.getConfig().getString("PotionEffects." + key + ".PotionEffect"));

            if(color != null && effectType != null)
                this.potionEffects.put(color, new PotionEffect(effectType, this.getConfig().getInt("Time") * 20, this.getConfig().getInt("Strength")));
        });

        this.getServer().getOnlinePlayers().forEach(player ->
            this.playerProfiles.put(player.getUniqueId(), new PlayerProfile(player))
        );
    }

    @Override
    public void onDisable() {
        this.playerProfiles.forEach((uuid, playerProfile) -> {
            Player player = Bukkit.getPlayer(uuid);

            if(player != null) {
                if(playerProfile.getPotionEffectType() != null) {
                    if (player.hasPotionEffect(playerProfile.getPotionEffectType()))
                        player.removePotionEffect(playerProfile.getPotionEffectType());
                }
            }
        });

        this.playerProfiles.clear();

        this.potionEffects.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!(this.playerProfiles.containsKey(player.getUniqueId())))
            this.playerProfiles.put(player.getUniqueId(), new PlayerProfile(player));
    }

    @EventHandler
    public void onPlayerQuick(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if(this.playerProfiles.containsKey(player.getUniqueId()))
            this.playerProfiles.remove(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if((!(event.getEntity() instanceof Player)) || (!(event.getDamager() instanceof Arrow)))
            return;

        Player player = (Player) event.getEntity(), damager = null;

        Arrow arrow = (Arrow) event.getDamager();

        if(arrow.getShooter() instanceof Player)
            damager = (Player) arrow.getShooter();

        if(this.isWearingLeather(damager)) {
            DyeColor armorColor = this.getArmorColor(damager);

            if(armorColor != null) {
                if(this.potionEffects.containsKey(armorColor)) {
                    PotionEffect potionEffect = this.potionEffects.get(armorColor);

                    if(!(this.playerProfiles.containsKey(player.getUniqueId())))
                        this.playerProfiles.put(player.getUniqueId(), new PlayerProfile(player));

                    PlayerProfile playerProfile = this.playerProfiles.get(player.getUniqueId());

                    if(playerProfile.elapsed()) {
                        player.addPotionEffect(potionEffect);

                        playerProfile.setLastTagged(System.currentTimeMillis());

                        playerProfile.setPotionEffectType(potionEffect.getType());
                    }
                }
            }
        }
    }

    protected boolean isWearingLeather(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        return playerInventory.getHelmet().getType().equals(Material.LEATHER_HELMET) && playerInventory.getChestplate().getType().equals(Material.LEATHER_CHESTPLATE) && playerInventory.getLeggings().getType().equals(Material.LEATHER_LEGGINGS) && playerInventory.getBoots().getType().equals(Material.LEATHER_BOOTS);
    }

    protected DyeColor getArmorColor(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        Color color = ((LeatherArmorMeta) playerInventory.getHelmet().getItemMeta()).getColor();

        if (((LeatherArmorMeta) playerInventory.getChestplate().getItemMeta()).getColor().equals(color) && ((LeatherArmorMeta) playerInventory.getLeggings().getItemMeta()).getColor().equals(color) && ((LeatherArmorMeta) playerInventory.getBoots().getItemMeta()).getColor().equals(color))
            return DyeColor.getByColor(color);
        return null;
    }
}
