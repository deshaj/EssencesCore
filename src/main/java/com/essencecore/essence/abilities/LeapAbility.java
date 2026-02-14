package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class LeapAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final double power;
    private final String particle;
    private final String sound;
    
    public LeapAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Leap");
        this.cooldown = section.getInt("cooldown", 5);
        this.power = section.getDouble("power", 1.5);
        this.particle = section.getString("particle", "CLOUD");
        this.sound = section.getString("sound", "ENTITY_ENDER_DRAGON_FLAP");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.LEAP;
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
        Vector direction = player.getLocation().getDirection().normalize();
        direction.setY(0.5);
        direction.multiply(power);
        
        player.setVelocity(direction);
        
        XParticle.of(particle).ifPresent(p -> 
            player.getWorld().spawnParticle(p.get(), player.getLocation(), 20, 0.3, 0.1, 0.3, 0.05)
        );
        
        XSound.matchXSound(sound).ifPresent(s -> s.play(player));
    }
}