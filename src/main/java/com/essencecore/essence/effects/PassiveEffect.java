package com.essencecore.essence.effects;

import com.cryptomorin.xseries.XPotion;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

@Data
public class PassiveEffect {
    
    private final String type;
    private final int amplifier;
    private final int duration;
    
    public PassiveEffect(String type, int amplifier, int duration) {
        this.type = type;
        this.amplifier = amplifier;
        this.duration = duration;
    }
    
    public void apply(Player player) {
        XPotion.matchXPotion(type)
            .map(xp -> xp.buildPotionEffect(duration, amplifier))
            .ifPresent(player::addPotionEffect);
    }
}