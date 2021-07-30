package com.github.steeldev.deathnote.api.events;

import com.github.steeldev.deathnote.api.Affliction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AfflictionUnregisteredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Affliction unregisteredAffliction;

    public AfflictionUnregisteredEvent(Affliction unregisteredAffliction) {
        this.unregisteredAffliction = unregisteredAffliction;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Affliction getUnregisteredAffliction() {
        return unregisteredAffliction;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
