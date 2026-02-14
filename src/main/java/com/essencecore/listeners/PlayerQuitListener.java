package com.essencecore.listeners;

import com.essencecore.EssenceCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    private final EssenceCore plugin;
    
    public PlayerQuitListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().unloadPlayerData(event.getPlayer());
        plugin.getCooldownManager().clearCooldowns(event.getPlayer().getUniqueId());
        plugin.getCombatManager().removeTag(event.getPlayer().getUniqueId());
    }
}