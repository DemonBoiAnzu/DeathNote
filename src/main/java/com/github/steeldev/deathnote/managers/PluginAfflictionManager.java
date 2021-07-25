package com.github.steeldev.deathnote.managers;

import com.github.steeldev.deathnote.afflictions.AffExplosion;
import com.github.steeldev.deathnote.afflictions.AffHeartAttack;
import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import org.bukkit.entity.Player;

import java.util.*;

public class PluginAfflictionManager {
    public Map<String, Affliction> afflictionMap;

    public List<Player> afflicted;

    public Affliction defaultAffliction;

    public void registerDefaultAfflictions(){
        if(afflictionMap == null) afflictionMap = new HashMap<>();
        if(afflicted == null) afflicted = new ArrayList<>();

        Affliction heartAttack = new Affliction("heart_attack", "&cHeart Attack", Arrays.asList("heart attack", "sudden death"), new AffHeartAttack());
        heartAttack.setDeathMessage("%s had a heart attack");
        defaultAffliction = heartAttack;
        AfflictionManager.registerAffliction(heartAttack);
        AfflictionManager.registerAffliction(new Affliction("explosion", "&cExplosion", Arrays.asList("explosion", "boom"), new AffExplosion()));
    }
}
