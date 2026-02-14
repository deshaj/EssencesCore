package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

@Getter
public class SlamAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final double damage;
    private final double range;
    private final String particle;
    private final String sound;
    
    public SlamAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Slam");
        this.cooldown = section.getInt("cooldown", 10);
        this.damage = section.getDouble("damage", 6.0);
        this.range = section.getDouble("range", 10.0);
        this.particle = section.getString("particle", "EXPLOSION_LARGE");
        this.sound = section.getString("sound", "ENTITY_WARDEN_ATTACK_IMPACT");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.SLAM;
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
        RayTraceResult result = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getLocation().getDirection(),
            range,
            entity -> entity instanceof LivingEntity && !entity.equals(player)
        );
        
        if (result != null && result.getHitEntity() != null) {
            Entity target = result.getHitEntity();
            
            if (target instanceof LivingEntity living) {
                living.damage(damage, player);
                
                XParticle.of(particle).ifPresent(p -> 
                    target.getWorld().spawnParticle(p.get(), target.getLocation(), 15, 0.5, 0.5, 0.5, 0.1)
                );
                
                XSound.matchXSound(sound).ifPresent(s -> s.play(player));
            }
        }
    }
}