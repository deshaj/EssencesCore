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
    private boolean hasUsedTrial;
    private String trialEssence;
    private long trialEndTime;
    
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.activeEssence = null;
        this.abilityToggles = new HashMap<>();
        this.lastShiftToggle = 0;
        this.hasUsedTrial = false;
        this.trialEssence = null;
        this.trialEndTime = 0;
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
    
    public boolean isOnTrial() {
        return trialEssence != null && trialEndTime > System.currentTimeMillis();
    }
    
    public boolean hasTrialExpired() {
        return trialEssence != null && trialEndTime > 0 && trialEndTime <= System.currentTimeMillis();
    }
    
    public boolean isUsedTrial() {
        return hasUsedTrial;
    }
    
    public void setUsedTrial(boolean used) {
        this.hasUsedTrial = used;
    }
}