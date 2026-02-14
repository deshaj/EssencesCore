package com.essencecore.essence.abilities;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class NoFallDamageAbility implements Ability {
    
    private final String name;
    
    public NoFallDamageAbility(ConfigurationSection section) {
        this.name = section.getString("name", "No Fall Damage");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.NO_FALL_DAMAGE;
    }
    
    @Override
    public int getCooldown() {
        return 0;
    }
    
    @Override
    public boolean isBlockedInCombat() {
        return false;
    }
    
    @Override
    public boolean requiresRegion() {
        return false;
    }
    
    @Override
    public void execute(Player player) {
    }
    
    @Override
    public String getParticle() {
        return null;
    }
    
    @Override
    public String getSound() {
        return null;
    }
}