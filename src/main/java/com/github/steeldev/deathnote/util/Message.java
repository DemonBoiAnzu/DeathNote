package com.github.steeldev.deathnote.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class Message {
    // Messages

    public static final Message MONSTRORVM_FOUND = get("&aFound &2Monstrorvm %s&a! Using custom item for the Death Note!");
    public static final Message MONSTRORVM_FOUND_DISABLED = get("&cFound &2Monstrorvm %s, but its disabled! Using basic custom item for the Death Note!");
    public static final Message MONSTRORVM_NOT_FOUND = get("&cCould not find &2Monstrorvm &con the server! Using basic custom item for the Death Note!");

    public static final Message PLUGIN_ENABLED = get("&aSuccessfully enabled &2%s &ain &e%s Seconds&a.");
    public static final Message PLUGIN_DISABLED = get("&cSuccessfully disabled!");

    public static final Message PLUGIN_CHECKING_FOR_UPDATE = get("&e&oChecking for a new version...");
    public static final Message PLUGIN_ON_LATEST = get("&2&oYou are on the latest version! &7&o(%s)");
    public static final Message PLUGIN_ON_IN_DEV_PREVIEW = get("&e&oYou are on an in-dev preview version! &7&o(%s)");
    public static final Message PLUGIN_NEW_VERSION_AVAILABLE_CONSOLE = get("&a&oA new version is available! &7&o(Current: %s, Latest: %s) &a&oYou can download the latest version here: &e&o%s");
    public static final Message PLUGIN_NEW_VERSION_AVAILABLE_CHAT = get("&a&oA new version is available! &7&o(Current: %s, Latest: %s)");
    public static final Message PLUGIN_NEW_VERSION_AVAILABLE_CHAT_CLICK = get("&6&lClick here to update");
    public static final Message PLUGIN_UPDATE_CHECK_FAILED = get("&4Failed to check for updates: &c%s");

    public static final Message PLUGIN_RELOADED = get("&aSuccessfully reloaded all configurations!");

    public static final Message LOADING_NBT_API = get("&aLoading NBT-API...");
    public static final Message NBT_API_LOADED = get("&aSuccessfully loaded NBT-API!");

    public static final Message STARTING_METRICS = get("&7Starting Metrics. Opt-out using the global bStats config.");

    public static final Message TARGET_INVALID = get("&rCould not find the specified target: &7%s&r.");
    public static final Message TARGET_WILL_BE_AFFLICTED = get("&rYour target, &7%s&r, will be afflicted.");
    public static final Message TARGET_WILL_BE_AFFLICTED_BY = get("&rYour target, &7%s&r, will be afflicted by &e%s&r.");
    public static final Message INPUTTED_AFFLICTION_INVALID = get("&rThe specified affliction, &7%s&r was invalid. &7%s&r will be afflicted with &e%s&r instead.");
    public static final Message TARGET_BEING_AFFLICTED = get("&7You feel a dark force taking hold of you..");


    // Message code
    private final String message;

    public Message(String message) {
        this.message = message;
    }

    private static Message get(String message) {
        return new Message(message);
    }

    public void sendActionBar(@Nullable CommandSender receiver, Object... params) {
        if (!(receiver instanceof Player)) return;
        Util.sendActionBar((Player) receiver, message, params);
    }

    public void sendTitle(String title, @Nullable CommandSender receiver, Object... params) {
        if (!(receiver instanceof Player)) return;
        Util.sendActionBar((Player) receiver, message, params);
    }

    public void broadcast(boolean withPrefix, Object... params) {
        Util.broadcast((withPrefix) ? Util.main.config.PREFIX + message : message, params);
    }

    public void send(@Nullable CommandSender receiver, boolean withPrefix, Object... params) {
        String finalMsg;
        if (withPrefix) finalMsg = Util.main.config.PREFIX + message;
        else finalMsg = message;
        Util.sendMessage(receiver, finalMsg, params);
    }

    public void log(Object... params) {
        Util.log(message, params);
    }

    public String toString() {
        return Util.colorize(this.message);
    }
}

