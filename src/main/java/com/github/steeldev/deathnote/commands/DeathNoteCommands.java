package com.github.steeldev.deathnote.commands;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.Message;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.colorize;
import static com.github.steeldev.deathnote.util.Util.getMain;

public class DeathNoteCommands implements CommandExecutor, TabCompleter {
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
                    int page = 0;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]) - 1;
                        } catch (Exception ignored) {
                            Message.EXPECTED_NUMBER.send(sender, true);
                            return true;
                        }
                    }
                    List<Affliction> afflictions = AfflictionManager.getRegistered();
                    int pageSize = 10;
                    final List<List<Affliction>> pages = Lists.partition(afflictions, pageSize);
                    if (page < 0) page = 0;
                    if (page >= pages.size()) page = pages.size() - 1;
                    List<Affliction> pageContent = pages.get(page);

                    sender.sendMessage(colorize("&7-------&8[&rAfflictions&8]&7-------"));
                    int number = (page < 1) ? 1 : (pageSize * page)+1;
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
                } else Message.NO_PERMISSION.send(sender, true);
            } else Message.INVALID_SUB_COMMAND.send(sender, true);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length >= 3) return new ArrayList<>();
        if (args.length <= 1) {
            return new ArrayList<String>() {{
                    add("reload");
                    add("give");
                    add("afflictions");
                }};
        } else {
            if (args[0].equalsIgnoreCase("reload"))
                return new ArrayList<>();
            else if (args[0].equalsIgnoreCase("give"))
                return null;
            else if (args[0].equalsIgnoreCase("afflictions"))
                return new ArrayList<>();
            else return new ArrayList<>();
        }
    }
}
