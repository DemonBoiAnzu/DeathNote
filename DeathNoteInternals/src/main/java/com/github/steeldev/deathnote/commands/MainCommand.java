package com.github.steeldev.deathnote.commands;

import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.Database;
import com.github.steeldev.deathnote.util.Message;
import com.github.steeldev.deathnote.util.Util;
import com.github.steeldev.deathnote.util.data.DNPlayerData;
import dev.jorel.commandapi.annotations.*;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.book.BookUtil;

import java.sql.SQLException;

import static com.github.steeldev.deathnote.util.Util.getMain;

@Command("deathnote")
@Alias("dn")
@Permission("deathnote.admin")
public class MainCommand {
    @Default
    @Subcommand("help")
    public static void deathNote(CommandSender sender) {
        Util.sendMessage(sender, "&rValid &7Death Note &rsub-commands:");
        Util.sendMessage(sender, "&7- &rhelp &8- &7shows this");
        Util.sendMessage(sender, "&7- &rreload &8- &7reload plugins configurations");
        Util.sendMessage(sender, "&7- &rafflictions &8- &7list all registered afflictions");
        Util.sendMessage(sender, "&7- &rgive [<player>] &8- &7give the death note to yourself or another player");
        Util.sendMessage(sender, "&7- &rkills [<player>] &8- &7view how many kills you or another player has with the death note");
    }

    @Subcommand("reload")
    public static void reload(CommandSender sender) {
        getMain().loadConfigurations();
        PluginAfflictions.registerPluginAfflictions();
        getMain().createDeathNoteAfflictionsBook();
        Message.PLUGIN_RELOADED.send(sender, true);
    }

    @Subcommand("give")
    public static void give(CommandSender sender, @APlayerArgument Player target) {
        if (target == null) {
            Message.INVALID_PLAYER.send(sender, true);
            return;
        }
        Message.GAVE_NOTE_TO_PLAYER.send(sender, true, target.getName());
        Message.DEATH_NOTE_RECEIVED.send(target, false);
        target.getInventory().addItem(getMain().getDeathNoteItem());
    }

    @Subcommand("give")
    public static void give(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Message.MUST_PROVIDE_PLAYER.send(sender, true);
            return;
        }
        Player player = (Player) sender;
        Message.DEATH_NOTE_RECEIVED.send(player, true);
        player.getInventory().addItem(getMain().getDeathNoteItem());
    }

    @Subcommand("kills")
    public static void kills(CommandSender sender) {
        if (!getMain().config.TRACK_KILLS) {
            Message.FEATURE_DISABLED.send(sender, true);
            return;
        }
        if (!(sender instanceof Player)) {
            Message.MUST_PROVIDE_PLAYER.send(sender, true);
            return;
        }
        kills(sender, (Player) sender);
    }

    @Subcommand("kills")
    public static void kills(CommandSender sender, @APlayerArgument Player player) {
        if (!getMain().config.TRACK_KILLS) {
            Message.FEATURE_DISABLED.send(sender, true);
            return;
        }
        if (player == null) {
            Message.INVALID_PLAYER.send(sender, true);
            return;
        }
        DNPlayerData data = null;
        try {
            data = Database.getPlayerData(player);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (data == null) {
            Message.PLAYER_HASNT_TOUCHED.send(sender, true, player.getName());
        } else {
            if (data.kills < 1)
                Message.PLAYER_HASNT_KILLED.send(sender, true, player.getName());
            else
                Message.PLAYER_KILLS.send(sender, true, player.getName(), data.kills);
        }
    }

    @Subcommand("afflictions")
    public static void afflictins(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Message.ONLY_PLAYERS_CAN_EXECUTE.send(sender, true);
            return;
        }
        Player player = (Player) sender;

        BookUtil.openPlayer(player, getMain().getAfflictionsBook());
    }
}
