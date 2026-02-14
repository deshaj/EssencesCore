package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public class BlinkAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final int minDistance;
    private final int maxDistance;
    private final boolean blockedInCombat;
    private final boolean requiresRegion;
    private final String particle;
    private final String sound;
    
    public BlinkAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Blink");
        this.cooldown = section.getInt("cooldown", 35);
        this.minDistance = section.getInt("min-distance", 4);
        this.maxDistance = section.getInt("max-distance", 5);
        this.blockedInCombat = section.getBoolean("blocked-in-combat", true);
        this.requiresRegion = section.getBoolean("requires-region", false);
        this.particle = section.getString("particle", "PORTAL");
        this.sound = section.getString("sound", "ENTITY_ENDERMAN_TELEPORT");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.BLINK;
    }

    @Override
    public boolean isBlockedInCombat() {
        return blockedInCombat;
    }

    @Override
    public boolean requiresRegion() {
        return requiresRegion;
    }
    
    @Override
    public void execute(Player player) {
        int distance = ThreadLocalRandom.current().nextInt(minDistance, maxDistance + 1);
        
        Vector direction = player.getLocation().getDirection().normalize();
        Location target = player.getLocation().add(direction.multiply(distance));
        
        Location safe = findSafeLocation(target);
        if (safe != null) {
            Location oldLoc = player.getLocation().clone();
            
            XParticle.of(particle).ifPresent(p -> 
                player.getWorld().spawnParticle(p.get(), oldLoc, 20, 0.5, 1, 0.5, 0.1)
            );
            
            player.teleport(safe);
            
            XParticle.of(particle).ifPresent(p -> 
                player.getWorld().spawnParticle(p.get(), safe, 20, 0.5, 1, 0.5, 0.1)
            );
            
            XSound.matchXSound(sound).ifPresent(s -> s.play(player));
        }
    }
    
    private Location findSafeLocation(Location location) {
        Location check = location.clone();
        
        for (int y = 0; y < 5; y++) {
            if (check.getBlock().isPassable() && 
                check.clone().add(0, 1, 0).getBlock().isPassable() &&
                !check.clone().add(0, -1, 0).getBlock().isPassable()) {
                return check;
            }
            check.add(0, 1, 0);
        }
        
        check = location.clone();
        for (int y = 0; y < 5; y++) {
            if (check.getBlock().isPassable() && 
                check.clone().add(0, 1, 0).getBlock().isPassable() &&
                !check.clone().add(0, -1, 0).getBlock().isPassable()) {
                return check;
            }
            check.add(0, -1, 0);
        }
        
        return null;
    }
}