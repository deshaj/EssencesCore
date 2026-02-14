package com.essencecore.managers;

import com.essencecore.EssenceCore;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@Getter
public class ConfigManager {
    
    private final EssenceCore plugin;
    private FileConfiguration essencesConfig;
    private FileConfiguration inventoryConfig;
    
    public ConfigManager(EssenceCore plugin) {
        this.plugin = plugin;
        loadEssencesConfig();
        loadInventoryConfig();
    }
    
    private void loadEssencesConfig() {
        File essencesFile = new File(plugin.getDataFolder(), "essences.yml");
        if (!essencesFile.exists()) {
            plugin.saveResource("essences.yml", false);
        }
        essencesConfig = YamlConfiguration.loadConfiguration(essencesFile);
    }
    
    private void loadInventoryConfig() {
        File inventoryFile = new File(plugin.getDataFolder(), "inventory.yml");
        if (!inventoryFile.exists()) {
            plugin.saveResource("inventory.yml", false);
        }
        inventoryConfig = YamlConfiguration.loadConfiguration(inventoryFile);
    }
    
    public String getMessage(String path) {
        return plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
    }
    
    public String getPrefix() {
        return plugin.getConfig().getString("settings.prefix", "&d&lESSENCE &8»&r");
    }
    
    public boolean isOneEssenceOnly() {
        return plugin.getConfig().getBoolean("settings.one-essence-only", true);
    }
    
    public boolean isSwitchCostEnabled() {
        return plugin.getConfig().getBoolean("settings.switch-cost.enabled", false);
    }
    
    public String getSwitchCostType() {
        return plugin.getConfig().getString("settings.switch-cost.type", "vault");
    }
    
    public double getSwitchCostAmount() {
        return plugin.getConfig().getDouble("settings.switch-cost.amount", 1000.0);
    }
    
    public boolean isCombatTagEnabled() {
        return plugin.getConfig().getBoolean("settings.combat-tag.enabled", true);
    }
    
    public int getCombatTagDuration() {
        return plugin.getConfig().getInt("settings.combat-tag.duration", 10);
    }
}