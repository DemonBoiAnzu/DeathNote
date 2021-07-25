package com.github.steeldev.deathnote.managers;

import com.github.steeldev.deathnote.afflictions.AffExplosion;
import com.github.steeldev.deathnote.afflictions.AffHeartAttack;
import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PluginAfflictionManager {
    public Map<String, Affliction> afflictionMap;

    public Affliction defaultAffliction;

    public void registerDefaultAfflictions(){
        if(afflictionMap == null) afflictionMap = new HashMap<>();

        Affliction heartAttack = new Affliction("heart_attack", Arrays.asList("heart attack", "sudden death"), new AffHeartAttack());
        defaultAffliction = heartAttack;
        AfflictionManager.registerAffliction(heartAttack);
        AfflictionManager.registerAffliction(new Affliction("explosion", Arrays.asList("explosion", "boom"), new AffExplosion()));
    }
}
