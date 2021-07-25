package com.github.steeldev.deathnote.api;

import com.github.steeldev.deathnote.util.Util;
import com.github.steeldev.monstrorvm.util.pluginutils.Message;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.getMain;

public class AfflictionManager {
    public static void registerAffliction(Affliction affliction){
        registerAffliction(affliction,getMain());
    }

    public static void registerAffliction(Affliction affliction, Plugin source){
        if(getMain().afflictionManager.afflictionMap == null) getMain().afflictionManager.afflictionMap = new HashMap<>();
        affliction.setRegisteredBy(source);

        if(getMain().afflictionManager.afflictionMap.containsKey(affliction.getId())){
            if(!updateAffliction(affliction)) return;
        }else{
            getMain().afflictionManager.afflictionMap.put(affliction.getId(),affliction);
        }
        // Message
    }

    public static boolean updateAffliction(Affliction affliction) {
        Affliction tempAffliction = getAffliction(affliction.getId());
        if (!getMain().afflictionManager.afflictionMap.containsKey(affliction.getId()) || tempAffliction == null)
            return false;
        if (tempAffliction.getRegisteredBy() != affliction.getRegisteredBy())
            return false;

        getMain().afflictionManager.afflictionMap.replace(affliction.getId(), affliction);
        return true;
    }

    public static Affliction getAffliction(String key){
        if (!getMain().afflictionManager.afflictionMap.containsKey(key)) return null;

        return getMain().afflictionManager.afflictionMap.get(key);
    }

    public static Affliction getAfflictedByTriggerWord(String trigger){
        for(Affliction affliction : getMain().afflictionManager.afflictionMap.values()){
            if(affliction.getTriggers().contains(trigger))
                return affliction;
        }
        return null;
    }

    public static List<String> getAfflictionList() {
        if (getMain().afflictionManager.afflictionMap == null) return new ArrayList<>();
        return new ArrayList<>(getMain().afflictionManager.afflictionMap.keySet());
    }
}
