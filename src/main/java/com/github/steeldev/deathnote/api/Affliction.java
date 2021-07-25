package com.github.steeldev.deathnote.api;

import com.github.steeldev.deathnote.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Function;

import static com.github.steeldev.deathnote.util.Util.getMain;

public class Affliction {
    private final String id;
    private final List<String> triggers;
    private final AfflictionAction afflictionAction;
    private Plugin registeredBy;

    public Affliction(String id, List<String> triggers, AfflictionAction afflictionAction){
        this.id = id;
        this.triggers = triggers;
        this.afflictionAction = afflictionAction;
    }

    public String getId() {
        return id;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public AfflictionAction getAfflictionAction() {
        return afflictionAction;
    }

    public Plugin getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(Plugin registeredBy) {
        this.registeredBy = registeredBy;
    }

    public void execute(Player target){
        afflictionAction.execute(target);
    }
}
