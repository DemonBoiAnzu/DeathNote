package com.github.steeldev.deathnote.commands;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.Database;
import com.github.steeldev.deathnote.util.Message;
import com.github.steeldev.deathnote.util.Util;
import com.github.steeldev.deathnote.util.data.DNPlayerData;
import com.google.common.collect.Lists;
import dev.jorel.commandapi.annotations.*;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.colorize;
import static com.github.steeldev.deathnote.util.Util.getMain;

@Command("deathnote")
@Alias("dn")
public class MainCommand {
    @Default
    @Subcommand("help")
    @Permission("deathnote.admin")
    public static void deathNote(CommandSender sender) {
        Util.sendMessage(sender, "&rValid &7Death Note &rsub-commands:");
        Util.sendMessage(sender, "&7- &rhelp &8- &7shows this");
        Util.sendMessage(sender, "&7- &rreload &8- &7reload plugins configurations");
        Util.sendMessage(sender, "&7- &rafflictions &8- &7list all registered afflictions");
        Util.sendMessage(sender, "&7- &rgive [<player>] &8- &7give the death note to yourself or another player");
        Util.sendMessage(sender, "&7- &rkills [<player>] &8- &7view how many kills you or another player has with the death note");
    }

    @Subcommand("reload")
    @Permission("deathnote.admin")
    public static void reload(CommandSender sender) {
        getMain().loadConfigurations();
        PluginAfflictions.registerPluginAfflictions();
        Message.PLUGIN_RELOADED.send(sender, true);
    }

    @Subcommand("give")
    @Permission("deathnote.admin")
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
    @Permission("deathnote.admin")
    public static void give(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Message.MUST_PROVIDE_PLAYER.send(sender, true);
            return;
        }
        Player player = (Player) sender;
        Message.DEATH_NOTE_RECEIVED.send(player, false);
        player.getInventory().addItem(getMain().getDeathNoteItem());
    }

    @Subcommand("kills")
    @Permission("deathnote.admin")
    public static void kills(CommandSender sender){
        if(!(sender instanceof Player)){
            Message.MUST_PROVIDE_PLAYER.send(sender, true);
            return;
        }
        kills(sender,(Player)sender);
    }

    @Subcommand("kills")
    @Permission("deathnote.admin")
    public static void kills(CommandSender sender, @APlayerArgument Player player){
        if (player == null) {
            Message.INVALID_PLAYER.send(sender, true);
            return;
        }
        DNPlayerData data = null;
        try {
            data = Database.getPlayerData(player);
        }catch(SQLException ex){
            ex.printStackTrace();
        }

        if(data == null){
            Message.PLAYER_HASNT_TOUCHED.send(sender,true,player.getName());
        }else{
            if(data.kills < 1)
                Message.PLAYER_HASNT_KILLED.send(sender,true,player.getName());
            else
                Message.PLAYER_KILLS.send(sender,true,player.getName(),data.kills);
        }
    }

    @Subcommand("afflictions")
    @Permission("deathnote.admin")
    public static void afflictions(CommandSender sender) {
        afflictins(sender, 1);
    }

    @Subcommand("afflictions")
    @Permission("deathnote.admin")
    public static void afflictins(CommandSender sender, @AIntegerArgument int page) {
        page = page - 1;
        List<Affliction> afflictions = AfflictionManager.getRegistered();
        int pageSize = 10;
        final List<List<Affliction>> pages = Lists.partition(afflictions, pageSize);
        if (page < 0) page = 0;
        if (page >= pages.size()) page = pages.size() - 1;
        List<Affliction> pageContent = pages.get(page);

        sender.sendMessage(colorize("&7-------&8[&rAfflictions&8]&7-------"));
        int number = (page < 1) ? 1 : (pageSize * page) + 1;
        for (Affliction affliction : pageContent) {
            TextComponent afflictionComp = new TextComponent(colorize("&7" + number + " &8| &r" + affliction.getDisplay()));

            StringBuilder hoverT = new StringBuilder(affliction.getDisplay() + "\n&7Triggers &8| &r" + affliction.getTriggers());
            if (!affliction.getDescription().isEmpty())
                hoverT.append("\n&7Description &8| &r" + affliction.getDescription());

            hoverT.append("\n\n&7Usage Example &8| &rHerobrine by " + affliction.getTriggers().get(0));

            if (!affliction.getRegisteredBy().equals(getMain()))
                hoverT.append("\n\n&7From &8| &r" + affliction.getRegisteredBy().getName());

            if (AfflictionManager.getDefaultAffliction().equals(affliction))
                hoverT.append("\n\n&7&oDefault Affliction");


            Text hoverText = new Text(colorize(hoverT.toString()));
            afflictionComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

            sender.spigot().sendMessage(afflictionComp);
            number++;
        }
        sender.sendMessage(colorize("&7-------&8[&rPage " + (page + 1) + "&7/&r" + pages.size() + "&8]&7-------"));
    }
}
