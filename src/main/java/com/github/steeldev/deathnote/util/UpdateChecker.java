package com.github.steeldev.deathnote.util;

import com.github.steeldev.deathnote.DeathNote;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    static DeathNote main = DeathNote.getInstance();
    public final String resourceLink = "hhttps://www.spigotmc.org/resources/death-note.94803/";
    public boolean outdated;
    public String newVersion;
    JavaPlugin plugin;
    int resourceID;

    public UpdateChecker(JavaPlugin plugin, int resourceID) {
        this.plugin = plugin;
        this.resourceID = resourceID;
    }

    public void checkForNewVersion() {
        Message.PLUGIN_CHECKING_FOR_UPDATE.log();
        getVersion(version -> {
            int latestVersion = Integer.parseInt(version.replaceAll("\\.", ""));
            int currentVersion = Integer.parseInt(main.getDescription().getVersion().replaceAll("\\.", ""));

            if (currentVersion == latestVersion) {
                outdated = false;
                Message.PLUGIN_ON_LATEST.log(version);
            } else if (currentVersion > latestVersion) {
                outdated = false;
                Message.PLUGIN_ON_IN_DEV_PREVIEW.log(main.getDescription().getVersion());
            } else {
                outdated = true;
                newVersion = version;
                Message.PLUGIN_NEW_VERSION_AVAILABLE_CONSOLE.log(main.getDescription().getVersion(), version, resourceLink);
            }
        });
    }

    public void sendNewUpdateMessageToPlayer(Player player) {
        if (!player.isOp() && !player.hasPermission("deathnote.admin")) return;

        if (!outdated) return;

        Message.PLUGIN_NEW_VERSION_AVAILABLE_CHAT.send(player, true, main.getDescription().getVersion(), newVersion);
        TextComponent link = new TextComponent(Message.PLUGIN_NEW_VERSION_AVAILABLE_CHAT_CLICK.toString());
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, resourceLink));
        player.spigot().sendMessage(link);
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL(String.format("https://api.spigotmc.org/legacy/update.php?resource=%d", this.resourceID)).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                Message.PLUGIN_UPDATE_CHECK_FAILED.log(exception.getMessage());
            }
        });
    }
}
