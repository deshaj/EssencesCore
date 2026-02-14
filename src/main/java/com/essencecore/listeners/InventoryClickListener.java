package com.essencecore.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.abilities.AbilityType;
import com.essencecore.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    
    private final EssenceCore plugin;
    
    public InventoryClickListener(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (!data.hasEssence()) return;
        
        Essence essence = plugin.getEssenceManager().getEssence(data.getActiveEssence()).orElse(null);
        if (essence == null) return;
        
        for (Ability ability : essence.getAbilities()) {
            if (ability.getType() == AbilityType.TELEPORT_STICK) {
                if (event.getSlot() == 8) {
                    if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                        Material stickMat = XMaterial.matchXMaterial("STICK").map(XMaterial::parseMaterial).orElse(Material.STICK);
                        if (event.getCurrentItem().getType() == stickMat) {
                            String displayName = ColorUtil.color("&5Enderman Teleport");
                            if (displayName.equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}