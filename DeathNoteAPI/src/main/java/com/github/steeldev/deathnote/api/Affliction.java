package com.github.steeldev.deathnote.api;

import com.github.steeldev.deathnote.api.events.AfflictionTriggeredEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Consumer;

/**
 * An affliction object that stores information for an affliction
 */
public class Affliction {
    private final String key;
    private final String display;
    private final String description;
    private final List<String> triggers;
    private final Consumer<Player> afflictionAction;
    private final String deathMessage;
    private final Plugin registeredBy;
    private boolean enabled;

    /**
     * Create a new Affliction object
     *
     * @param key              The key
     * @param display          The display
     * @param triggers         The triggers
     * @param description      The description
     * @param deathMessage     The custom death message
     * @param registeredBy     The plugin creating this
     * @param afflictionAction The action/payload to trigger on the target
     */
    public Affliction(String key, String display, List<String> triggers, String description, String deathMessage, Plugin registeredBy, Consumer<Player> afflictionAction) {
        this.key = key;
        this.display = display;
        this.triggers = triggers;
        this.description = description;
        this.deathMessage = deathMessage;
        this.afflictionAction = afflictionAction;
        this.registeredBy = registeredBy;
    }

    /**
     * Get if this affliction is enabled or disabled
     *
     * @return Boolean
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Set if this Affliction is enabled or disabled
     *
     * @param in Input boolean
     */
    public void setEnabled(boolean in) {
        enabled = in;
    }

    /**
     * Get this afflictions triggers
     *
     * @return List of triggers
     */
    public List<String> getTriggers() {
        return triggers;
    }

    /**
     * Get the plugin this affliction was registered by
     *
     * @return Owning plugin
     */
    public Plugin getRegisteredBy() {
        return registeredBy;
    }

    /**
     * Get the afflictions display name
     *
     * @return Display name
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Get the afflictions custom death message
     *
     * @return Death message
     */
    public String getDeathMessage() {
        return deathMessage;
    }

    /**
     * Get the afflictions description
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the afflictions key/id
     *
     * @return Key/id
     */
    public String getKey() {
        return key;
    }

    /**
     * Execute this afflictions payload
     *
     * @param target The target player to execute on
     */
    public void execute(Player target) {
        AfflictionTriggeredEvent afflictionTriggeredEvent = new AfflictionTriggeredEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(afflictionTriggeredEvent);
        if (afflictionTriggeredEvent.isCancelled()) return;
        afflictionAction.accept(target);
    }
}
