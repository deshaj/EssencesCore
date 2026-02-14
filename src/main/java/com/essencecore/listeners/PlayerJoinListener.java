package com.essencecore.listeners;

import com.essencecore.EssenceCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final EssenceCore plugin;
    
    public PlayerJoinListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().loadPlayerData(event.getPlayer());
    }
}