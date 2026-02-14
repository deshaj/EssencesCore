package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XPotion;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@Getter
public class HungerAuraAbility implements Ability {
    
    private final String name;
    private final double radius;
    
    public HungerAuraAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Hunger Aura");
        this.radius = section.getDouble("radius", 5.0);
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.HUNGER_AURA;
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
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !entity.equals(player)) {
                XPotion.matchXPotion("HUNGER")
                    .map(xp -> xp.buildPotionEffect(100, 0))
                    .ifPresent(living::addPotionEffect);
            }
        }
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