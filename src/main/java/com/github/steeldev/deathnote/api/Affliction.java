package com.github.steeldev.deathnote.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class Affliction {
    private final String id;
    private final String display;
    private final List<String> triggers;
    private final AfflictionAction afflictionAction;
    private String deathMessage;
    private Plugin registeredBy;

    public Affliction(String id, String display, List<String> triggers, AfflictionAction afflictionAction) {
        this.id = id;
        this.display = display;
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

    public String getDisplay() {
        return display;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public void execute(Player target) {
        afflictionAction.execute(target);
    }
}
