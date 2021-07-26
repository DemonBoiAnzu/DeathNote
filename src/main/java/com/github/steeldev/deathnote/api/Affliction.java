package com.github.steeldev.deathnote.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Consumer;

public class Affliction {
    private final String display;
    private final List<String> triggers;
    private final Consumer<Player> afflictionAction;
    private final String deathMessage;
    private final Plugin registeredBy;

    public Affliction(String display, List<String> triggers, String deathMessage, Plugin registeredBy, Consumer<Player> afflictionAction) {
        this.display = display;
        this.triggers = triggers;
        this.deathMessage = deathMessage;
        this.afflictionAction = afflictionAction;
        this.registeredBy = registeredBy;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public Plugin getRegisteredBy() {
        return registeredBy;
    }

    public String getDisplay() {
        return display;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void execute(Player target) {
        afflictionAction.accept(target);
    }
}
