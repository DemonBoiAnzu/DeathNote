package com.github.steeldev.deathnote;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.commands.MainCommand;
import com.github.steeldev.deathnote.listeners.DeathNoteListener;
import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.*;
import com.github.steeldev.monstrorvm.api.items.ItemManager;
import com.github.steeldev.monstrorvm.api.items.MVItem;
import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.upperlevel.spigot.book.BookUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.github.steeldev.deathnote.util.Util.*;
import static org.bukkit.Bukkit.getPluginManager;

public class DeathNote extends JavaPlugin {
    private static DeathNote instance;
    public Config config = null;
    public UpdateChecker versionManager;
    public Plugin monstrorvmPlugin;
    public Logger logger;
    ItemStack deathNoteItem;
    ItemStack afflictionsBook;

    public static DeathNote getInstance() {
        return instance;
    }

    @Override
    public @NotNull Logger getLogger() {
        if (logger == null) logger = DNLogger.getLogger();
        return this.logger;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig().silentLogs(true));
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;

        CommandAPI.onEnable(this);
        CommandAPI.registerCommand(MainCommand.class);

        try {
            Database.getConnection();
            Database.create();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        MinecraftVersion.replaceLogger(getLogger());

        if (!Util.isRunningMinecraft(1, 16)) {
            Util.log("&c&l[&4&lERROR&c&l] Unsupported server version. Death Note only supports 1.16+");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadConfigurations();
        registerEvents();

        if (loadMonstrorvm() != null) {
            monstrorvmPlugin = loadMonstrorvm();
            if (monstrorvmPlugin.isEnabled()) {
                Message.MONSTRORVM_FOUND.log(monstrorvmPlugin.getDescription().getVersion());
            } else
                Message.MONSTRORVM_FOUND_DISABLED.log(monstrorvmPlugin.getDescription().getVersion());
        } else
            Message.MONSTRORVM_NOT_FOUND.log();

        createDeathNoteItem();
        PluginAfflictions.registerPluginAfflictions();

        //enableMetrics();

        Message.PLUGIN_ENABLED.log(getDescription().getVersion(), (float) (System.currentTimeMillis() - start) / 1000);

        //versionManager = new UpdateChecker(this, 0);
        //versionManager.checkForNewVersion();
    }

    public Plugin loadMonstrorvm() {
        return getPluginManager().getPlugin("Monstrorvm");
    }

    @Override
    public void onDisable() {
        Message.PLUGIN_DISABLED.log();
        try {
            Database.closeConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        instance = null;
    }

    public void loadNBTAPI() {
        Message.LOADING_NBT_API.log();
        NBTItem loadingItem = new NBTItem(new ItemStack(Material.STONE));
        loadingItem.addCompound("Glob");
        loadingItem.setString("Glob", "yes");
        Message.NBT_API_LOADED.log();
    }

    public void loadConfigurations() {
        config = new Config(this);
    }

    void createDeathNoteItem() {
        if (Util.monstrorvmEnabled()) {
            MVItem deathNote = new MVItem(deathNoteID, Material.WRITABLE_BOOK);
            deathNote.withDisplayName(deathNoteDisplayName);
            deathNote.lore = deathNoteLore;
            deathNote.withCustomModelData(1);
            ItemManager.registerNewItem(deathNote, getMain());
        } else {
            getMain().loadNBTAPI();
            ItemStack deathNote = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(deathNote.getType());
            List<String> lore = new ArrayList<>();
            for (String line : deathNoteLore) {
                lore.add(colorize(line));
            }
            meta.setLore(lore);
            meta.setDisplayName(colorize(deathNoteDisplayName));
            meta.setCustomModelData(1);
            deathNote.setItemMeta(meta);
            NBTItem deathNoteNBT = new NBTItem(deathNote);
            deathNoteNBT.setBoolean(deathNoteID, true);
            deathNoteItem = deathNoteNBT.getItem();
        }
    }

    public void createDeathNoteAfflictionsBook() {
        if (AfflictionManager.getDefaultAffliction() == null) return;
        List<Affliction> afflictions = AfflictionManager.getRegistered();
        int pageSize = 13;
        final List<List<Affliction>> pagesPartition = Lists.partition(afflictions, pageSize);
        List<BaseComponent[]> bookPages = new ArrayList<>();

        BookUtil.BookBuilder book = BookUtil.writtenBook().author(colorize("&7Ryuk")).title(colorize("&7Death Note Afflictions"));

        TextComponent firstPageComp = new TextComponent("");
        firstPageComp.addExtra(colorize("\n"));
        firstPageComp.addExtra(colorize("\n"));
        firstPageComp.addExtra(colorize("\n"));
        firstPageComp.addExtra(colorize("\n"));
        firstPageComp.addExtra(colorize("      &8&lDeath Note\n"));
        firstPageComp.addExtra(colorize("      &8&lAfflictions\n"));
        firstPageComp.addExtra(colorize("      &8----------\n"));
        bookPages.add(new BookUtil.PageBuilder().add(firstPageComp).build());
        int curPage = 0;
        for (List<Affliction> pageAfflictions : pagesPartition) {
            int number = (curPage < 1) ? 1 : (pageSize * curPage) + 1;
            TextComponent pageComp = new TextComponent("");
            for (Affliction affliction : pageAfflictions) {
                TextComponent afflictionComp = new TextComponent(colorize("&e" + number + " &8| &r" + affliction.getDisplay() + "\n"));

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

                pageComp.addExtra(afflictionComp);

                number++;
            }
            curPage++;
            bookPages.add(new BookUtil.PageBuilder().add(pageComp).build());
        }
        book.pages(bookPages);

        afflictionsBook = book.build();
    }

    public ItemStack getDeathNoteItem() {
        if (Util.monstrorvmEnabled()) return ItemManager.getItem(deathNoteID).getItemStack();
        else return deathNoteItem;
    }

    public ItemStack getAfflictionsBook() {
        if (afflictionsBook == null) createDeathNoteAfflictionsBook();
        return afflictionsBook;
    }

    public void enableMetrics() {
        Metrics metrics = new Metrics(this, 0);

        if (metrics.isEnabled()) {
            Message.STARTING_METRICS.log();
            metrics.addCustomChart(new Metrics.SimplePie("using_monstrorvm", () -> {
                if (monstrorvmPlugin != null) {
                    if (monstrorvmPlugin.isEnabled())
                        return monstrorvmPlugin.getDescription().getVersion();
                }
                return "No Monstrorvm";
            }));
        }
    }

    public void registerEvents() {
        Util.registerEvent(new DeathNoteListener());
    }
}
