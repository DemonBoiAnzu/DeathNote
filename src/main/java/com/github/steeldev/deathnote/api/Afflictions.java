package com.github.steeldev.deathnote.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Afflictions {
    private static final Map<String, Affliction> registry = new HashMap<>();

    static Affliction defaultAffliction;

    public static Affliction register(String key, Affliction value) {
        if (registry.containsKey(key)) {
            Affliction tempAff = get(key);
            if (value.getRegisteredBy().equals(tempAff.getRegisteredBy()))
                registry.replace(key, value);
            else return null;
        } else registry.put(key, value);
        return value;
    }

    public static void unregister(String key) {
        registry.remove(key);
    }

    public static Affliction get(String key) {
        return registry.get(key);
    }

    public static boolean isAfflictedRegistered(String key) {
        return get(key) != null;
    }

    public static Affliction getAfflictionByTriggerWord(String trigger) {
        for (Affliction affliction : registry.values()) {
            if (affliction.getTriggers().contains(trigger))
                return affliction;
        }
        return null;
    }

    public static List<Affliction> getRegistered() {
        return new ArrayList<>(registry.values());
    }

    public static Affliction getDefaultAffliction() {
        return defaultAffliction;
    }

    /**
     * The default affliction is what will trigger if
     * the inputted afflicted trigger is invalid
     *
     * @param defaultAffliction The Affliction to set as the default
     */
    public static void setDefaultAffliction(Affliction defaultAffliction) {
        Afflictions.defaultAffliction = defaultAffliction;
    }
}
