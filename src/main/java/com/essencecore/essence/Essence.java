package com.essencecore.essence;

import com.essencecore.essence.abilities.Ability;
import com.essencecore.essence.effects.EffectTrigger;
import com.essencecore.essence.effects.PassiveEffect;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Essence {
    
    private final String id;
    private final String name;
    private final List<String> description;
    private final String icon;
    private final double scale;
    private final Map<EffectTrigger, List<PassiveEffect>> passiveEffects;
    private final List<Ability> abilities;
    
    public List<PassiveEffect> getPassiveEffects(EffectTrigger trigger) {
        return passiveEffects.getOrDefault(trigger, List.of());
    }
}