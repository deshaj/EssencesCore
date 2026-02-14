package com.essencecore.essence.abilities;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class WallClimbAbility implements Ability {
    
    private final String name;
    private final double speed;
    private final String particle;
    
    public WallClimbAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Wall Climb");
        this.speed = section.getDouble("speed", 0.2);
        this.particle = section.getString("particle", "WEB");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.WALL_CLIMB;
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
    public String getSound() {
        return null;
    }
}