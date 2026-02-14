package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class WindChargeAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final double power;
    private final String particle;
    private final String sound;
    
    public WindChargeAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Wind Charge");
        this.cooldown = section.getInt("cooldown", 8);
        this.power = section.getDouble("power", 2.0);
        this.particle = section.getString("particle", "CLOUD");
        this.sound = section.getString("sound", "ENTITY_BREEZE_SHOOT");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.WIND_CHARGE;
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
        direction.multiply(power);
        direction.setY(Math.max(direction.getY(), 0.5));
        
        player.setVelocity(direction);
        
        XParticle.of(particle).ifPresent(p -> 
            player.getWorld().spawnParticle(p.get(), player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1)
        );
        
        XSound.matchXSound(sound).ifPresent(s -> s.play(player));
    }
}