package com.essencecore.listeners;

import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {
    
    private final EssenceCore plugin;
    
    public PlayerDamageListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (!data.hasEssence()) return;
        
        Essence essence = plugin.getEssenceManager().getEssence(data.getActiveEssence()).orElse(null);
        if (essence == null) return;
        
        for (Ability ability : essence.getAbilities()) {
            if (ability.getType() == AbilityType.NO_FALL_DAMAGE) {
                event.setCancelled(true);
                break;
            }
        }
    }
}