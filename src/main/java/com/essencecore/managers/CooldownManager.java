package com.essencecore.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final Map<UUID, Map<String, Long>> cooldowns;
    
    public CooldownManager() {
        this.cooldowns = new HashMap<>();
    }
    
    public void setCooldown(UUID player, String ability, long seconds) {
        cooldowns.computeIfAbsent(player, k -> new HashMap<>()).put(ability, System.currentTimeMillis() + (seconds * 1000));
    }
    
    public boolean hasCooldown(UUID player, String ability) {
        Map<String, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns == null) return false;
        
        Long expiry = playerCooldowns.get(ability);
        if (expiry == null) return false;
        
        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(ability);
            return false;
        }
        
        return true;
    }
    
    public long getRemainingCooldown(UUID player, String ability) {
        Map<String, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns == null) return 0;
        
        Long expiry = playerCooldowns.get(ability);
        if (expiry == null) return 0;
        
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    public void removeCooldown(UUID player, String ability) {
        Map<String, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns != null) {
            playerCooldowns.remove(ability);
        }
    }
    
    public void clearCooldowns(UUID player) {
        cooldowns.remove(player);
    }
}