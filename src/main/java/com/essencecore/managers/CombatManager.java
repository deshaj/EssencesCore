package com.essencecore.managers;

import com.essencecore.EssenceCore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    
    private final EssenceCore plugin;
    private final Map<UUID, Long> combatTags;
    
    public CombatManager(EssenceCore plugin) {
        this.plugin = plugin;
        this.combatTags = new HashMap<>();
    }
    
    public void tagPlayer(UUID player) {
        if (!plugin.getConfigManager().isCombatTagEnabled()) return;
        
        int duration = plugin.getConfigManager().getCombatTagDuration();
        combatTags.put(player, System.currentTimeMillis() + (duration * 1000L));
    }
    
    public boolean isInCombat(UUID player) {
        if (!plugin.getConfigManager().isCombatTagEnabled()) return false;
        
        Long expiry = combatTags.get(player);
        if (expiry == null) return false;
        
        if (System.currentTimeMillis() >= expiry) {
            combatTags.remove(player);
            return false;
        }
        
        return true;
    }
    
    public void removeTag(UUID player) {
        combatTags.remove(player);
    }
}