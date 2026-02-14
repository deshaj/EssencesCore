package com.essencecore.essence.effects;

public enum EffectTrigger {
    ALWAYS,
    IN_WATER,
    IN_LAVA,
    IN_AIR,
    ON_GROUND;
    
    public static EffectTrigger fromString(String str) {
        return switch (str.toLowerCase()) {
            case "always" -> ALWAYS;
            case "in-water", "in_water" -> IN_WATER;
            case "in-lava", "in_lava" -> IN_LAVA;
            case "in-air", "in_air" -> IN_AIR;
            case "on-ground", "on_ground" -> ON_GROUND;
            default -> ALWAYS;
        };
    }
}