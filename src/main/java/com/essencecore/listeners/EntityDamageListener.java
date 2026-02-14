package com.essencecore.listeners;

import com.essencecore.EssenceCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {
    
    private final EssenceCore plugin;
    
    public EntityDamageListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball fireball) {
            if (fireball.hasMetadata("essence_fireball")) {
                double damage = fireball.getMetadata("essence_fireball").get(0).asDouble();
                int fireTicks = fireball.getMetadata("essence_fire_ticks").get(0).asInt();
                
                event.setDamage(damage);
                
                if (event.getEntity() instanceof LivingEntity living) {
                    living.setFireTicks(fireTicks);
                }
            }
        }
        
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        
        if (damager instanceof Player) {
            plugin.getCombatManager().tagPlayer(damager.getUniqueId());
        }
        
        if (victim instanceof Player) {
            plugin.getCombatManager().tagPlayer(victim.getUniqueId());
        }
    }
}