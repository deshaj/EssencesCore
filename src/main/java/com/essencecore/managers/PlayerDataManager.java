package com.essencecore.managers;

import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XMaterial;
import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import com.essencecore.utils.ColorUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerDataManager {
    
    private final EssenceCore plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private final File dataFolder;
    
    public PlayerDataManager(EssenceCore plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(uuid));
    }
    
    public void loadPlayerData(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            playerDataMap.put(player.getUniqueId(), new PlayerData(player.getUniqueId()));
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        PlayerData data = new PlayerData(player.getUniqueId());
        
        if (config.contains("essence")) {
            data.setActiveEssence(config.getString("essence"));
        }
        
        if (config.contains("ability-toggles")) {
            Map<String, Boolean> toggles = new HashMap<>();
            for (String key : config.getConfigurationSection("ability-toggles").getKeys(false)) {
                toggles.put(key, config.getBoolean("ability-toggles." + key));
            }
            data.setAbilityToggles(toggles);
        }
        
        playerDataMap.put(player.getUniqueId(), data);
        
        if (data.hasEssence()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> giveEssenceItems(player), 1L);
        }
    }
    
    public void savePlayerData(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;
        
        File playerFile = new File(dataFolder, player.getUniqueId() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        if (data.getActiveEssence() != null) {
            config.set("essence", data.getActiveEssence());
        }
        
        if (!data.getAbilityToggles().isEmpty()) {
            for (Map.Entry<String, Boolean> entry : data.getAbilityToggles().entrySet()) {
                config.set("ability-toggles." + entry.getKey(), entry.getValue());
            }
        }
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for " + player.getName());
        }
    }
    
    public void setActiveEssence(Player player, String essenceId) {
        clearPotionEffects(player);
        removeEssenceItems(player);
        resetScale(player);
        
        PlayerData data = getPlayerData(player);
        data.setActiveEssence(essenceId);
        data.getAbilityToggles().clear();
        
        giveEssenceItems(player);
        applyScale(player, essenceId);
        savePlayerData(player);
    }
    
    public void clearActiveEssence(Player player) {
        clearPotionEffects(player);
        removeEssenceItems(player);
        resetScale(player);
        
        PlayerData data = getPlayerData(player);
        data.setActiveEssence(null);
        data.getAbilityToggles().clear();
        savePlayerData(player);
    }
    
    private void giveEssenceItems(Player player) {
        PlayerData data = getPlayerData(player);
        if (!data.hasEssence()) return;
        
        Essence essence = plugin.getEssenceManager().getEssence(data.getActiveEssence()).orElse(null);
        if (essence == null) return;
        
        for (Ability ability : essence.getAbilities()) {
            if (ability.getType() == AbilityType.TELEPORT_STICK) {
                ItemStack stick = XMaterial.matchXMaterial("STICK")
                    .map(XMaterial::parseItem)
                    .orElse(new ItemStack(Material.STICK));
                
                ItemMeta meta = stick.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColorUtil.color("&5Enderman Teleport"));
                    stick.setItemMeta(meta);
                }
                player.getInventory().setItem(8, stick);
            }
        }
    }
    
    private void removeEssenceItems(Player player) {
        ItemStack slot8 = player.getInventory().getItem(8);
        if (slot8 != null && slot8.hasItemMeta() && slot8.getItemMeta().hasDisplayName()) {
            Material stickMat = XMaterial.matchXMaterial("STICK").map(XMaterial::parseMaterial).orElse(Material.STICK);
            if (slot8.getType() == stickMat) {
                String displayName = ColorUtil.color("&5Enderman Teleport");
                if (displayName.equals(slot8.getItemMeta().getDisplayName())) {
                    player.getInventory().setItem(8, null);
                }
            }
        }
    }
    
    private void clearPotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    private void applyScale(Player player, String essenceId) {
        Essence essence = plugin.getEssenceManager().getEssence(essenceId).orElse(null);
        if (essence == null) return;
        
        double scale = essence.getScale();
        if (scale != 1.0) {
            XAttribute.of("scale").ifPresent(attr -> {
                if (player.getAttribute(attr.get()) != null) {
                    player.getAttribute(attr.get()).setBaseValue(scale);
                }
            });
        }
    }
    
    private void resetScale(Player player) {
        XAttribute.of("scale").ifPresent(attr -> {
            if (player.getAttribute(attr.get()) != null) {
                player.getAttribute(attr.get()).setBaseValue(1.0);
            }
        });
    }
    
    public void loadAllData() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        
        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                PlayerData data = new PlayerData(uuid);
                
                if (config.contains("essence")) {
                    data.setActiveEssence(config.getString("essence"));
                }
                
                if (config.contains("ability-toggles")) {
                    Map<String, Boolean> toggles = new HashMap<>();
                    for (String key : config.getConfigurationSection("ability-toggles").getKeys(false)) {
                        toggles.put(key, config.getBoolean("ability-toggles." + key));
                    }
                    data.setAbilityToggles(toggles);
                }
                
                playerDataMap.put(uuid, data);
            } catch (IllegalArgumentException ignored) {}
        }
    }
    
    public void saveAllData() {
        for (PlayerData data : playerDataMap.values()) {
            File playerFile = new File(dataFolder, data.getUuid() + ".yml");
            FileConfiguration config = new YamlConfiguration();
            
            if (data.getActiveEssence() != null) {
                config.set("essence", data.getActiveEssence());
            }
            
            if (!data.getAbilityToggles().isEmpty()) {
                for (Map.Entry<String, Boolean> entry : data.getAbilityToggles().entrySet()) {
                    config.set("ability-toggles." + entry.getKey(), entry.getValue());
                }
            }
            
            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save data for " + data.getUuid());
            }
        }
    }
    
    public void unloadPlayerData(Player player) {
        savePlayerData(player);
        playerDataMap.remove(player.getUniqueId());
    }
}