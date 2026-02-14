package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class TeleportStickAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final double radius;
    private final String particle;
    private final String sound;
    
    public TeleportStickAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Teleport");
        this.cooldown = section.getInt("cooldown", 30);
        this.radius = section.getDouble("radius", 6.0);
        this.particle = section.getString("particle", "PORTAL");
        this.sound = section.getString("sound", "ENTITY_ENDERMAN_TELEPORT");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.TELEPORT_STICK;
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
        Location target = player.getTargetBlock(null, (int) radius).getLocation().add(0, 1, 0);
        
        if (target.distance(player.getLocation()) > radius) {
            return;
        }
        
        if (!target.getBlock().isPassable() || !target.clone().add(0, 1, 0).getBlock().isPassable()) {
            return;
        }
        
        Location oldLoc = player.getLocation().clone();
        
        XParticle.of(particle).ifPresent(p -> 
            player.getWorld().spawnParticle(p.get(), oldLoc, 20, 0.5, 1, 0.5, 0.1)
        );
        
        player.teleport(target);
        
        XParticle.of(particle).ifPresent(p -> 
            player.getWorld().spawnParticle(p.get(), target, 20, 0.5, 1, 0.5, 0.1)
        );
        
        XSound.matchXSound(sound).ifPresent(s -> s.play(player));
    }
}