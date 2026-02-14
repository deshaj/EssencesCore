package com.essencecore.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import com.essencecore.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
    
    private final EssenceCore plugin;
    
    public PlayerInteractListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        if (!data.hasEssence()) return;
        
        Essence essence = plugin.getEssenceManager().getEssence(data.getActiveEssence()).orElse(null);
        if (essence == null) return;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        
        for (Ability ability : essence.getAbilities()) {
            if (!data.isAbilityEnabled(ability.getName())) {
                continue;
            }
            
            boolean shouldActivate = false;
            
            if (ability.getType() == AbilityType.TELEPORT_STICK) {
                Material stickMat = XMaterial.matchXMaterial("STICK").map(XMaterial::parseMaterial).orElse(Material.STICK);
                if (item.getType() == stickMat && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                    String displayName = item.getItemMeta().getDisplayName();
                    if (ColorUtil.color("&5Enderman Teleport").equals(displayName)) {
                        shouldActivate = true;
                    }
                }
            } else if (ability.getType() == AbilityType.WIND_CHARGE) {
                Material rodMat = XMaterial.matchXMaterial("FISHING_ROD").map(XMaterial::parseMaterial).orElse(Material.FISHING_ROD);
                if (item.getType() == rodMat) {
                    shouldActivate = true;
                }
            }
            
            if (!shouldActivate) {
                continue;
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
            
            break;
        }
    }
}