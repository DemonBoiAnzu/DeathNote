package com.github.steeldev.deathnote.commands;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.Afflictions;
import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.steeldev.deathnote.util.Util.colorize;
import static com.github.steeldev.deathnote.util.Util.getMain;

public class DeathNoteCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!(sender instanceof Player) || sender.hasPermission("deathnote.admin")) {
                    getMain().loadConfigurations();
                    PluginAfflictions.registerPluginAfflictions();
                    Message.PLUGIN_RELOADED.send(sender, true);
                } else {
                    Message.NO_PERMISSION.send(sender, true);
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                Player target = null;
                if (sender.hasPermission("deathnote.admin")) {
                    if (!(sender instanceof Player) && args.length < 2) {
                        Message.MUST_PROVIDE_PLAYER.send(sender, true);
                        return true;
                    } else if (sender instanceof Player) target = (Player) sender;
                    if (args.length >= 2) target = getMain().getServer().getPlayer(args[1]);
                    if (target == null) {
                        Message.INVALID_PLAYER.send(sender, true);
                        return true;
                    }
                    ItemStack deathNote = getMain().getDeathNoteItem();
                    target.getInventory().addItem(deathNote);
                    Message.DEATH_NOTE_RECEIVED.send(target, true);
                    if (!target.equals(sender)) Message.GAVE_NOTE_TO_PLAYER.send(sender, true, target.getName());
                } else Message.NO_PERMISSION.send(sender, true);
            } else if (args[0].equalsIgnoreCase("afflictions")) {
                if (sender.hasPermission("deathnote.admin")) {
                    List<Affliction> afflictions = Afflictions.getRegistered();
                    Message.VALID_AFFLICTIONS.send(sender, true, afflictions.size());
                    for (Affliction affliction : afflictions) {
                        sender.sendMessage(colorize(affliction.getDisplay() + " &7- &r" + affliction.getTriggers()));
                    }
                    Message.AFFLICTION_EXAMPLE.send(sender, false);
                } else Message.NO_PERMISSION.send(sender, true);
            } else Message.INVALID_SUB_COMMAND.send(sender, true);
        }
        return false;
    }
}
