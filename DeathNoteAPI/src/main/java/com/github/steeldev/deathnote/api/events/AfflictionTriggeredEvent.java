package com.github.steeldev.deathnote.api.events;

import com.github.steeldev.deathnote.api.Affliction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AfflictionTriggeredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Affliction triggeredAffliction;
    private boolean cancelled;

    public AfflictionTriggeredEvent(Affliction triggeredAffliction) {
        this.triggeredAffliction = triggeredAffliction;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Affliction getTriggeredAffliction() {
        return triggeredAffliction;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
