package com.essencecore.listeners;

import com.cryptomorin.xseries.particles.XParticle;
import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import com.essencecore.essence.abilities.WallClimbAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class PlayerMoveListener implements Listener {
    
    private final EssenceCore plugin;
    
    public PlayerMoveListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        if (!data.hasEssence()) return;
        
        Essence essence = plugin.getEssenceManager().getEssence(data.getActiveEssence()).orElse(null);
        if (essence == null) return;
        
        handleWallClimb(player, essence, data);
    }
    
    private void handleWallClimb(Player player, Essence essence, PlayerData data) {
        WallClimbAbility climbAbility = null;
        
        for (Ability ability : essence.getAbilities()) {
            if (ability.getType() == AbilityType.WALL_CLIMB) {
                climbAbility = (WallClimbAbility) ability;
                break;
            }
        }
        
        if (climbAbility == null) return;
        
        if (!data.isAbilityEnabled(climbAbility.getName())) return;
        
        if (!player.isSneaking()) return;
        
        if (!isAgainstWall(player)) return;
        
        Vector velocity = player.getVelocity();
        velocity.setY(climbAbility.getSpeed());
        player.setVelocity(velocity);
        player.setFallDistance(0);
        
        if (climbAbility.getParticle() != null) {
            XParticle.of(climbAbility.getParticle()).ifPresent(p -> 
                player.getWorld().spawnParticle(p.get(), player.getLocation(), 3, 0.3, 0.3, 0.3, 0.01)
            );
        }
    }
    
    private boolean isAgainstWall(Player player) {
        Location loc = player.getLocation();
        
        return !loc.clone().add(0.3, 0, 0).getBlock().isPassable() ||
               !loc.clone().add(-0.3, 0, 0).getBlock().isPassable() ||
               !loc.clone().add(0, 0, 0.3).getBlock().isPassable() ||
               !loc.clone().add(0, 0, -0.3).getBlock().isPassable();
    }
}