package com.essencecore;

import com.essencecore.commands.EssenceCommand;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import com.essencecore.essence.abilities.HungerAuraAbility;
import com.essencecore.essence.effects.EffectTrigger;
import com.essencecore.essence.effects.PassiveEffect;
import com.essencecore.inventory.gui.GUIListener;
import com.essencecore.inventory.gui.GUIManager;
import com.essencecore.listeners.*;
import com.essencecore.managers.*;
import com.essencecore.utils.ColorUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EssenceCore extends JavaPlugin {
    
    private ConfigManager configManager;
    private EssenceManager essenceManager;
    private PlayerDataManager playerDataManager;
    private CooldownManager cooldownManager;
    private CombatManager combatManager;
    private GUIManager guiManager;
    private EconomyManager economyManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("essences.yml", false);
        saveResource("inventory.yml", false);
        
        this.configManager = new ConfigManager(this);
        this.essenceManager = new EssenceManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.cooldownManager = new CooldownManager();
        this.combatManager = new CombatManager(this);
        this.guiManager = new GUIManager();
        this.economyManager = new EconomyManager(this);
        
        essenceManager.loadEssences();
        playerDataManager.loadAllData();
        
        registerCommands();
        registerListeners();
        
        startAutoSave();
        startPassiveEffectTask();
        startCooldownDisplayTask();
        startHungerAuraTask();
        startTrialExpirationTask();
        
        getLogger().info("EssenceCore has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }
        getLogger().info("EssenceCore has been disabled!");
    }
    
    private void registerCommands() {
        getCommand("essence").setExecutor(new EssenceCommand(this));
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSneakListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
    }
    
    private void startAutoSave() {
        int interval = getConfig().getInt("settings.save-interval", 300) * 20;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            playerDataManager.saveAllData();
        }, interval, interval);
    }
    
    private void startPassiveEffectTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = playerDataManager.getPlayerData(player);
                if (!data.hasEssence()) continue;
                
                Essence essence = essenceManager.getEssence(data.getActiveEssence()).orElse(null);
                if (essence == null) continue;
                
                List<PassiveEffect> alwaysEffects = essence.getPassiveEffects(EffectTrigger.ALWAYS);
                for (PassiveEffect effect : alwaysEffects) {
                    effect.apply(player);
                }
                
                if (player.isInWater()) {
                    List<PassiveEffect> waterEffects = essence.getPassiveEffects(EffectTrigger.IN_WATER);
                    for (PassiveEffect effect : waterEffects) {
                        effect.apply(player);
                    }
                }
            }
        }, 20L, 20L);
    }
    
    private void startHungerAuraTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = playerDataManager.getPlayerData(player);
                if (!data.hasEssence()) continue;
                
                Essence essence = essenceManager.getEssence(data.getActiveEssence()).orElse(null);
                if (essence == null) continue;
                
                for (Ability ability : essence.getAbilities()) {
                    if (ability.getType() == AbilityType.HUNGER_AURA) {
                        if (data.isAbilityEnabled(ability.getName())) {
                            ((HungerAuraAbility) ability).execute(player);
                        }
                    }
                }
            }
        }, 20L, 40L);
    }
    
    private void startCooldownDisplayTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = playerDataManager.getPlayerData(player);
                if (!data.hasEssence()) continue;
                
                Essence essence = essenceManager.getEssence(data.getActiveEssence()).orElse(null);
                if (essence == null) continue;
                
                List<String> statusParts = new ArrayList<>();
                statusParts.add("&d" + essence.getName());
                
                if (data.isOnTrial()) {
                    long remaining = (data.getTrialEndTime() - System.currentTimeMillis()) / 1000;
                    statusParts.add("&6Trial: &e" + remaining + "s");
                }
                
                boolean hasActiveCooldown = false;
                
                for (Ability ability : essence.getAbilities()) {
                    if (ability.getCooldown() <= 0) continue;
                    
                    if (cooldownManager.hasCooldown(player.getUniqueId(), ability.getName())) {
                        long remaining = cooldownManager.getRemainingCooldown(player.getUniqueId(), ability.getName());
                        statusParts.add("&e" + ability.getName() + ": &f" + remaining + "s");
                        hasActiveCooldown = true;
                    } else if (!data.isAbilityEnabled(ability.getName())) {
                        statusParts.add("&c" + ability.getName() + ": Disabled");
                    }
                }
                
                if (!hasActiveCooldown && statusParts.size() == 1) {
                    statusParts.add("&aReady");
                }
                
                String message = ColorUtil.color(String.join(" &8| ", statusParts));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        }, 0L, 10L);
    }
    
    private void startTrialExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = playerDataManager.getPlayerData(player);
                
                if (data.isOnTrial() && System.currentTimeMillis() >= data.getTrialEndTime()) {
                    playerDataManager.clearActiveEssence(player);
                    data.setTrialEssence(null);
                    data.setTrialEndTime(0);
                    playerDataManager.savePlayerData(player);
                    
                    player.sendMessage(ColorUtil.color(configManager.getPrefix() + " " + 
                        configManager.getMessage("trial-expired")));
                    player.sendMessage(ColorUtil.color(configManager.getPrefix() + " " + 
                        configManager.getMessage("trial-expired-info")));
                }
            }
        }, 20L, 20L);
    }
    

    
    public void reload() {
        reloadConfig();
        configManager = new ConfigManager(this);
        essenceManager.loadEssences();
        playerDataManager.loadAllData();
    }
}