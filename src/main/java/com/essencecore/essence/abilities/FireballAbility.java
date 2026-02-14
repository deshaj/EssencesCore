package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FireballAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final double damage;
    private final int fireTicks;
    private final boolean blockDamage;
    private final String particle;
    private final String sound;
    
    public FireballAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Fireball");
        this.cooldown = section.getInt("cooldown", 90);
        this.damage = section.getDouble("damage", 4.0);
        this.fireTicks = section.getInt("fire-ticks", 100);
        this.blockDamage = section.getBoolean("block-damage", false);
        this.particle = section.getString("particle", "FLAME");
        this.sound = section.getString("sound", "ENTITY_BLAZE_SHOOT");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.FIREBALL;
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
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setYield(0);
        fireball.setIsIncendiary(false);
        fireball.setMetadata("essence_fireball", new FixedMetadataValue(JavaPlugin.getProvidingPlugin(getClass()), damage));
        fireball.setMetadata("essence_fire_ticks", new FixedMetadataValue(JavaPlugin.getProvidingPlugin(getClass()), fireTicks));
        
        XSound.matchXSound(sound).ifPresent(s -> s.play(player));
        
        XParticle.of(particle).ifPresent(p -> 
            player.getWorld().spawnParticle(p.get(), player.getEyeLocation(), 10, 0.2, 0.2, 0.2, 0.05)
        );
    }
}