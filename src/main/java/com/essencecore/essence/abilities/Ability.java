package com.essencecore.essence.abilities;

import org.bukkit.entity.Player;

public interface Ability {
    
    String getName();
    
    AbilityType getType();
    
    int getCooldown();
    
    boolean isBlockedInCombat();
    
    boolean requiresRegion();
    
    void execute(Player player);
    
    String getParticle();
    
    String getSound();
}