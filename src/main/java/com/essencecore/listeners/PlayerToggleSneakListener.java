package com.essencecore.listeners;

import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import com.essencecore.utils.ColorUtil;
import com.essencecore.utils.RegionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakListener implements Listener {
    
    private final EssenceCore plugin;
    
    public PlayerToggleSneakListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        if (!data.hasEssence()) return;
        
        Essence essence = plugin.getEssenceManager().getEssence(data.getActiveEssence()).orElse(null);
        if (essence == null) return;
        
        for (Ability ability : essence.getAbilities()) {
            if (ability.getType() == AbilityType.SLAM || ability.getType() == AbilityType.LEAP || 
                ability.getType() == AbilityType.FROG_LEAP || ability.getType() == AbilityType.MOVE_ENTITIES) {
                
                if (!data.isAbilityEnabled(ability.getName())) {
                    continue;
                }
                
                if (ability.requiresRegion() && !RegionUtil.isInRegion(plugin, player.getLocation())) {
                    player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                        plugin.getConfigManager().getMessage("outside-region")));
                    return;
                }
                
                if (ability.isBlockedInCombat() && plugin.getCombatManager().isInCombat(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                        plugin.getConfigManager().getMessage("in-combat")));
                    return;
                }
                
                if (plugin.getCooldownManager().hasCooldown(player.getUniqueId(), ability.getName())) {
                    long remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId(), ability.getName());
                    String message = plugin.getConfigManager().getMessage("ability-cooldown")
                        .replace("{time}", String.valueOf(remaining));
                    player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
                    return;
                }
                
                ability.execute(player);
                
                if (ability.getCooldown() > 0) {
                    plugin.getCooldownManager().setCooldown(player.getUniqueId(), ability.getName(), ability.getCooldown());
                }
                
                return;
            }
        }
    }
}