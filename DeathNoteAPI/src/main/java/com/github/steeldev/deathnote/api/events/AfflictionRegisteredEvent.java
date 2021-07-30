package com.github.steeldev.deathnote.api.events;

import com.github.steeldev.deathnote.api.Affliction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AfflictionRegisteredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Affliction registeredAffliction;

    public AfflictionRegisteredEvent(Affliction registeredAffliction) {
        this.registeredAffliction = registeredAffliction;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Affliction getRegisteredAffliction() {
        return registeredAffliction;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
