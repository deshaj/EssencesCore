package com.essencecore.essence.effects;

import com.cryptomorin.xseries.XPotion;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

@Data
@AllArgsConstructor
public class PassiveEffect {
    
    private final String type;
    private final int amplifier;
    private final int duration;
    
    public void apply(Player player) {
        XPotion.matchXPotion(type)
            .map(xp -> xp.buildPotionEffect(duration, amplifier))
            .ifPresent(player::addPotionEffect);
    }
}