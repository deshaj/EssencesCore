package com.essencecore.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerData {
    
    private final UUID uuid;
    private String activeEssence;
    private Map<String, Boolean> abilityToggles;
    private long lastShiftToggle;
    
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.activeEssence = null;
        this.abilityToggles = new HashMap<>();
        this.lastShiftToggle = 0;
    }
    
    public boolean hasEssence() {
        return activeEssence != null;
    }
    
    public boolean isAbilityEnabled(String abilityName) {
        return abilityToggles.getOrDefault(abilityName, true);
    }
    
    public void toggleAbility(String abilityName) {
        abilityToggles.put(abilityName, !isAbilityEnabled(abilityName));
    }
}