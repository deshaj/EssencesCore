package com.essencecore.managers;

import com.essencecore.EssenceCore;
import com.essencecore.essence.Essence;
import com.essencecore.essence.abilities.*;
import com.essencecore.essence.effects.PassiveEffect;
import com.essencecore.essence.effects.EffectTrigger;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.*;

@Getter
public class EssenceManager {
    
    private final EssenceCore plugin;
    private final Map<String, Essence> essences;
    
    public EssenceManager(EssenceCore plugin) {
        this.plugin = plugin;
        this.essences = new HashMap<>();
    }
    
    public void loadEssences() {
        essences.clear();
        ConfigurationSection essencesSection = plugin.getConfigManager().getEssencesConfig().getConfigurationSection("essences");
        
        if (essencesSection == null) return;
        
        for (String key : essencesSection.getKeys(false)) {
            ConfigurationSection essenceSection = essencesSection.getConfigurationSection(key);
            if (essenceSection == null) continue;
            
            Essence essence = loadEssence(key, essenceSection);
            essences.put(key.toLowerCase(), essence);
            plugin.getLogger().info("Loaded essence: " + key + " with " + essence.getAbilities().size() + " abilities");
        }
        
        plugin.getLogger().info("Loaded " + essences.size() + " essences!");
    }
    
    private Essence loadEssence(String id, ConfigurationSection section) {
        String name = section.getString("name", id);
        List<String> description = section.getStringList("description");
        String icon = section.getString("icon", "PAPER");
        double scale = section.getDouble("scale", 1.0);
        int cost = section.getInt("cost", 500);
        
        Map<EffectTrigger, List<PassiveEffect>> passiveEffects = loadPassiveEffects(section.getConfigurationSection("passive-effects"));
        List<Ability> abilities = loadAbilities(section);
        
        return new Essence(id, name, description, icon, scale, cost, passiveEffects, abilities);
    }
    
    private Map<EffectTrigger, List<PassiveEffect>> loadPassiveEffects(ConfigurationSection section) {
        Map<EffectTrigger, List<PassiveEffect>> effects = new HashMap<>();
        
        if (section == null) return effects;
        
        for (String triggerKey : section.getKeys(false)) {
            EffectTrigger trigger = EffectTrigger.fromString(triggerKey);
            List<PassiveEffect> triggerEffects = new ArrayList<>();
            
            List<String> effectList = section.getStringList(triggerKey);
            for (int i = 0; i < effectList.size(); i += 3) {
                if (i + 2 < effectList.size()) {
                    String type = effectList.get(i).replace("type: ", "").trim();
                    int amplifier = Integer.parseInt(effectList.get(i + 1).replace("amplifier: ", "").trim());
                    int duration = Integer.parseInt(effectList.get(i + 2).replace("duration: ", "").trim());
                    
                    triggerEffects.add(new PassiveEffect(type, amplifier, duration));
                }
            }
            
            effects.put(trigger, triggerEffects);
        }
        
        return effects;
    }
    
    private List<Ability> loadAbilities(ConfigurationSection section) {
        List<Ability> abilities = new ArrayList<>();
        
        List<Map<?, ?>> abilityList = section.getMapList("abilities");
        
        for (Map<?, ?> abilityMap : abilityList) {
            ConfigurationSection abilitySection = new MemoryConfiguration();
            for (Map.Entry<?, ?> entry : abilityMap.entrySet()) {
                abilitySection.set(entry.getKey().toString(), entry.getValue());
            }
            
            String typeStr = abilitySection.getString("type", "NO_FALL_DAMAGE");
            AbilityType type = AbilityType.valueOf(typeStr);
            Ability ability = createAbility(type, abilitySection);
            
            if (ability != null) {
                abilities.add(ability);
            }
        }
        
        return abilities;
    }
    
    private Ability createAbility(AbilityType type, ConfigurationSection section) {
        return switch (type) {
            case NO_FALL_DAMAGE -> new NoFallDamageAbility(section);
            case SLAM -> new SlamAbility(section);
            case LEAP -> new LeapAbility(section);
            case WIND_CHARGE -> new WindChargeAbility(section);
            case PICK_UP_PLAYER -> new PickUpPlayerAbility(section);
            case FROG_LEAP -> new FrogLeapAbility(section);
            case HUNGER_AURA -> new HungerAuraAbility(section);
            case MOVE_ENTITIES -> new MoveEntitiesAbility(section);
            case TELEPORT_STICK -> new TeleportStickAbility(section);
            case WALL_CLIMB -> new WallClimbAbility(section);
            case FIREBALL -> new FireballAbility(section);
            case BLINK -> new BlinkAbility(section);
        };
    }
    
    public Optional<Essence> getEssence(String id) {
        return Optional.ofNullable(essences.get(id.toLowerCase()));
    }
    
    public Collection<Essence> getAllEssences() {
        return essences.values();
    }
}