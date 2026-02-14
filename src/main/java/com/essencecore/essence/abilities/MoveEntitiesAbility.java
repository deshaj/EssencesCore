package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class MoveEntitiesAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final double radius;
    private final double power;
    private final String particle;
    private final String sound;
    
    public MoveEntitiesAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Pull Entities");
        this.cooldown = section.getInt("cooldown", 10);
        this.radius = section.getDouble("radius", 8.0);
        this.power = section.getDouble("power", 0.5);
        this.particle = section.getString("particle", "ENCHANTMENT_TABLE");
        this.sound = section.getString("sound", "ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.MOVE_ENTITIES;
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
            if (entity instanceof LivingEntity) {
                Vector direction = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                direction.multiply(power);
                entity.setVelocity(direction);
                
                XParticle.of(particle).ifPresent(p -> 
                    entity.getWorld().spawnParticle(p.get(), entity.getLocation(), 10, 0.3, 0.3, 0.3, 0.05)
                );
            }
        }
        
        XSound.matchXSound(sound).ifPresent(s -> s.play(player));
    }
}