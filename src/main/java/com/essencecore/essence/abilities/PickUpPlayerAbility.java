package com.essencecore.essence.abilities;

import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
public class PickUpPlayerAbility implements Ability {
    
    private final String name;
    private final int cooldown;
    private final String sound;
    
    public PickUpPlayerAbility(ConfigurationSection section) {
        this.name = section.getString("name", "Pick Up");
        this.cooldown = section.getInt("cooldown", 3);
        this.sound = section.getString("sound", "ENTITY_IRON_GOLEM_HURT");
    }
    
    @Override
    public AbilityType getType() {
        return AbilityType.PICK_UP_PLAYER;
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
        XSound.matchXSound(sound).ifPresent(s -> s.play(player));
    }
    
    @Override
    public String getParticle() {
        return null;
    }
}