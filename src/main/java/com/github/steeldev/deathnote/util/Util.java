package com.github.steeldev.deathnote.util;

import com.github.steeldev.deathnote.DeathNote;
import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.monstrorvm.api.items.ItemManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.chat.TextComponent.fromLegacyText;

public class Util {
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");
    private static final String PREFIX = "&7[&8DeathNote&7]&r ";
    private static final String NBTAPI_PREFIX = "&7[&8NBTAPI&7]&r ";
    public static Random rand = new Random();
    public static String[] version;
    public static String deathNoteID = "death_note";
    public static String deathNoteDisplayName = "<#443c3c>Death Note";
    public static List<String> deathNoteLore = new ArrayList<String>() {
        {
            add("&7The humans whose name");
            add("&7is written in this note shall die.");
            add("");
            add("&7No cause given, they will simply die");
            add("&7of a heart attack.");
            add("");
            add("&7Give a cause by adding 'by' then");
            add("&7a cause of death.");
            add("");
            add("&7Additionally, you can provide a time");
            add("&7of death as well, by putting 'in'");
            add("&7followed by a timespan");
            add("&7&oe.g 10 minutes");
        }
    };
    static DeathNote main = DeathNote.getInstance();
    static Map<Player, Affliction> afflicted = new HashMap<>();

    public static DeathNote getMain() {
        if (main == null) main = DeathNote.getInstance();
        return main;
    }

    public static String colorize(String string) {
        Matcher matcher = HEX_PATTERN.matcher(string);
        while (matcher.find()) {
            final net.md_5.bungee.api.ChatColor hexColor = net.md_5.bungee.api.ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = string.substring(0, matcher.start());
            final String after = string.substring(matcher.end());
            string = before + hexColor + after;
            matcher = HEX_PATTERN.matcher(string);
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static boolean chanceOf(int chance) {
        return rand.nextInt(100) < chance;
    }

    public static boolean chanceOf(float chance) {
        return rand.nextFloat() < chance;
    }

    public static String getNbtapiPrefix() {
        return NBTAPI_PREFIX;
    }

    public static void log(String log) {
        Bukkit.getConsoleSender().sendMessage(colorize(PREFIX + log));
    }

    private static void send(CommandSender receiver, String format, Object... objects) {
        receiver.sendMessage(colorize(String.format(format, objects)));
    }

    public static void log(String format, Object... objects) {
        Bukkit.getConsoleSender().sendMessage(colorize(PREFIX + String.format(format, objects)));
    }

    public static void sendMessage(CommandSender receiver, String format, Object... objects) {
        if (receiver == null || receiver instanceof ConsoleCommandSender) {
            log(format, objects);
        } else {
            send(receiver, format, objects);
        }
    }

    public static void sendActionBar(Player receiver, String format, Object... objects) {
        if (receiver == null) return;
        receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR, fromLegacyText(colorize(String.format(format, objects))));
    }

    public static void sendTitle(Player receiver, String title, String format, Object... objects) {
        if (receiver == null) return;
        receiver.sendTitle(colorize(title), colorize(String.format(format, objects)));
    }

    public static void broadcast(String format, Object... params) {
        getMain().getServer().broadcastMessage(colorize(String.format(format, params)));
    }

    public static void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public static void registerEvent(Listener listener) {
        getMain().getServer().getPluginManager().registerEvents(listener, main);
    }

    public static void registerCommand(String command, CommandExecutor commandExecutor) {
        getMain().getCommand(command).setExecutor(commandExecutor);
    }

    public static boolean monstrorvmEnabled() {
        return getMain().monstrorvmPlugin != null && getMain().monstrorvmPlugin.isEnabled();
    }

    public static boolean isAfflicted(Player player) {
        return afflicted.containsKey(player);
    }

    public static void setAfflicted(Player player, Affliction affliction) {
        if (affliction != null) afflicted.put(player, affliction);
        else afflicted.remove(player);
    }

    public static Affliction getPlayerAffliction(Player player) {
        return afflicted.get(player);
    }

    public static String trimEndingWhiteSpace(String input){
        return input.replaceAll("\\s+$","");
    }

    public static boolean isDeathNote(ItemStack item) {
        if (!item.getType().equals(Material.WRITABLE_BOOK)) return false;
        if (Util.monstrorvmEnabled()) return ItemManager.isMVItem(item, deathNoteID);
        else {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.hasKey(deathNoteID);
        }
    }

    // Thanks to ShaneBee for providing these functions

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major Major version to check (Most likely just going to be 1)
     * @param minor Minor version to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor) {
        return isRunningMinecraft(major, minor, 0);
    }

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major    Major version to check (Most likely just going to be 1)
     * @param minor    Minor version to check
     * @param revision Revision to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor, int revision) {
        if (version == null) version = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        int maj = Integer.parseInt(version[0]);
        int min = Integer.parseInt(version[1]);
        int rev;
        try {
            rev = Integer.parseInt(version[2]);
        } catch (Exception ignore) {
            rev = 0;
        }
        return maj > major || min > minor || (min == minor && rev >= revision);
    }

    public static String getVersionString() {
        return version[0] + "." + version[1] + "." + version[2];
    }
}
