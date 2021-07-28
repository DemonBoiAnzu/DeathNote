package com.github.steeldev.deathnote.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ResourceBundle;

public class Message {
    // Messages

    static final ResourceBundle messageBundle = ResourceBundle.getBundle("Messages");

    public static final Message MONSTRORVM_FOUND = get("found_monstrorvm");
    public static final Message MONSTRORVM_FOUND_DISABLED = get("found_monstrorvm_disabled");
    public static final Message MONSTRORVM_NOT_FOUND = get("cant_find_monstrorvm");

    public static final Message PLUGIN_ENABLED = get("plugin_enabled");
    public static final Message PLUGIN_DISABLED = get("plugin_disabled");

    public static final Message PLUGIN_CHECKING_FOR_UPDATE = get("version_checking");
    public static final Message PLUGIN_ON_LATEST = get("on_latest");
    public static final Message PLUGIN_ON_IN_DEV_PREVIEW = get("preview_version");
    public static final Message PLUGIN_NEW_VERSION_AVAILABLE_CONSOLE = get("version_available_console");
    public static final Message PLUGIN_NEW_VERSION_AVAILABLE_CHAT = get("version_available_chat");
    public static final Message PLUGIN_NEW_VERSION_AVAILABLE_CHAT_CLICK = get("version_chat_click");
    public static final Message PLUGIN_UPDATE_CHECK_FAILED = get("version_check_failed");

    public static final Message PLUGIN_RELOADED = get("plugin_reloaded");

    public static final Message LOADING_NBT_API = get("loading_nbt");
    public static final Message NBT_API_LOADED = get("loaded_nbt");

    public static final Message STARTING_METRICS = get("starting_metrics");

    public static final Message TARGET_INVALID = get("target_invalid");
    public static final Message TARGET_WILL_BE_AFFLICTED = get("target_afflicted");
    public static final Message TARGET_WILL_BE_AFFLICTED_IN = get("target_afflicted_in");
    public static final Message TARGET_WILL_BE_AFFLICTED_BY = get("target_afflicted_by");
    public static final Message TARGET_WILL_BE_AFFLICTED_BY_IN = get("target_afflicted_by_in");
    public static final Message INPUTTED_AFFLICTION_INVALID = get("affliction_invalid");
    public static final Message TARGET_BEING_AFFLICTED = get("being_afflicted");

    public static final Message MUST_PROVIDE_PLAYER = get("must_provide_player");
    public static final Message INVALID_PLAYER = get("invalid_player");

    public static final Message GAVE_NOTE_TO_PLAYER = get("death_note_given");
    public static final Message DEATH_NOTE_RECEIVED = get("death_note_received");

    public static final Message DEATH_NOTE_FIRST_TOUCH = get("first_time");
    public static final Message PLAYER_HASNT_TOUCHED = get("player_hasnt_touched");
    public static final Message PLAYER_HASNT_KILLED = get("player_hasnt_killed");
    public static final Message PLAYER_KILLS = get("player_kills");


    public static final Message NO_PERMISSION = get("dont_have_permissions");

    public static final Message INVALID_SUB_COMMAND = get("invalid_sub_command");

    public static final Message EXPECTED_NUMBER = get("expected_number");


    // Message code
    private final String message;

    public Message(String message) {
        this.message = message;
    }

    private static Message get(String id) {
        return new Message(messageBundle.getString(id));
    }

    public void sendActionBar(@Nullable CommandSender receiver, Object... params) {
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

