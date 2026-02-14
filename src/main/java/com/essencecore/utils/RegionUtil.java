package com.essencecore.utils;

import com.essencecore.EssenceCore;
import org.bukkit.Location;

public class RegionUtil {
    
    public static boolean isInRegion(EssenceCore plugin, Location location) {
        if (!plugin.getConfig().getBoolean("settings.region-restrictions.enabled", false)) {
            return true;
        }
        
        return true;
    }
}