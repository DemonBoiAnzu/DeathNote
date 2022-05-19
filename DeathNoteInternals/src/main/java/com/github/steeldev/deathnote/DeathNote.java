package com.github.steeldev.deathnote;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.commands.MainCommand;
import com.github.steeldev.deathnote.listeners.DeathNoteListener;
import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.*;
import com.google.common.collect.Lists;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.upperlevel.spigot.book.BookUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.github.steeldev.deathnote.util.Util.colorize;
import static com.github.steeldev.deathnote.util.Util.getMain;

public class DeathNote extends JavaPlugin {
    private static DeathNote instance;
    public Config config = null;
    public UpdateChecker versionManager;
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

        /*if (!Util.isRunningMinecraft(1, 16)) {
            Util.log("&c&l[&4&lERROR&c&l] Unsupported server version. Death Note only supports 1.16+");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }*/

        try {
            if (!Files.exists(getDataFolder().toPath()))
                Files.createDirectory(getDataFolder().toPath());
            Database.getConnection();
            Database.create();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        loadConfigurations();
        registerEvents();

        createDeathNoteItem();
        PluginAfflictions.registerPluginAfflictions();

        if (config.BOOK_CRAFTING_RECIPE_ENABLED) {
            ShapedRecipe bookRecipe = new ShapedRecipe(new NamespacedKey(this, "death_note"), deathNoteItem);
            bookRecipe.shape(" R ", "RWR", " R ");
            bookRecipe.setIngredient('R', Material.REDSTONE);
            bookRecipe.setIngredient('W', Material.WRITABLE_BOOK);
            Bukkit.addRecipe(bookRecipe);
            Util.log("&aRegistered recipe for the Death Note.");
        }

        enableMetrics();

        Message.PLUGIN_ENABLED.log(getDescription().getVersion(), (float) (System.currentTimeMillis() - start) / 1000);

        versionManager = new UpdateChecker(this, 94803);
        versionManager.checkForNewVersion();
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

    public void loadConfigurations() {
        config = new Config(this);
    }

    void createDeathNoteItem() {
        Util.deathNoteKey = new NamespacedKey(getMain(), "item_type");
        Util.bookUsesKey = new NamespacedKey(getMain(), "book_uses");
        String deathNoteDisplayName = "<#443c3c>Death Note";
        List<String> deathNoteLore = new ArrayList<String>() {
            {
                add("&7A strange note.");
                add("");
                add("&7RMB &c: &7Use Note");
                add("&7Crouch-RMB &c: &7View Afflictions & How to");
                if (config.BOOK_MAX_USES > 0) {
                    add("");
                    add(String.format("&e0&7/&e%d &7Uses", config.BOOK_MAX_USES));
                }
            }
        };
        ItemStack deathNote = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(deathNote.getType());
        List<String> lore = new ArrayList<>();
        for (String line : deathNoteLore) {
            lore.add(colorize(line));
        }
        meta.setLore(lore);
        meta.setDisplayName(colorize(deathNoteDisplayName));
        meta.setCustomModelData(1);
        meta.getPersistentDataContainer().set(Util.deathNoteKey, PersistentDataType.STRING, "death_note");
        meta.getPersistentDataContainer().set(Util.bookUsesKey, PersistentDataType.INTEGER, 0);
        deathNote.setItemMeta(meta);
        deathNoteItem = deathNote;
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
        firstPageComp.addExtra(colorize("      &0&lDeath Note\n"));
        firstPageComp.addExtra(colorize("&c-&0&lTable of Contents&c-\n"));
        firstPageComp.addExtra(colorize("      &8----------\n"));
        firstPageComp.addExtra(colorize("\n"));
        firstPageComp.addExtra(colorize("          &0Pages\n"));
        firstPageComp.addExtra(colorize("    &0&l2 &8&l| How to Use\n"));
        firstPageComp.addExtra(colorize("   &0&l3+ &8&l| Afflictions\n"));

        TextComponent howToPageComp = new TextComponent("");
        howToPageComp.addExtra(colorize("       &0&lHow to Use\n"));
        howToPageComp.addExtra(colorize("       &8----------\n"));
        howToPageComp.addExtra(colorize("\n"));
        howToPageComp.addExtra(colorize("&0-&8The humans whose name is written in this note shall die.\n"));
        howToPageComp.addExtra(colorize("&0-&8No cause given, they will simply die of a heart attack.\n"));
        howToPageComp.addExtra(colorize("&0-&8A cause is defined with 'by' and a trigger.\n"));
        howToPageComp.addExtra(colorize("&0-&8Time is defined with 'in' and a timespan &o(eg 10 minutes)\n"));
        bookPages.add(new BookUtil.PageBuilder().add(firstPageComp).build());
        bookPages.add(new BookUtil.PageBuilder().add(howToPageComp).build());
        int curPage = 0;
        for (List<Affliction> pageAfflictions : pagesPartition) {
            int number = (curPage < 1) ? 1 : (pageSize * curPage) + 1;
            TextComponent pageComp = new TextComponent("");
            for (Affliction affliction : pageAfflictions) {
                TextComponent afflictionComp = new TextComponent(colorize("&0" + number + " &8| &r" + ChatColor.stripColor(colorize(affliction.getDisplay())) + "\n"));

                StringBuilder hoverT = new StringBuilder(affliction.getDisplay() + "\n&7Triggers &8| &r" + affliction.getTriggers());
                if (!affliction.getDescription().isEmpty())
                    hoverT.append("\n&7Description &8| &r").append(affliction.getDescription());

                hoverT.append("\n\n&7Usage Example &8| &rHerobrine by ").append(affliction.getTriggers().get(0));

                if (!affliction.getRegisteredBy().equals(getMain()))
                    hoverT.append("\n\n&7From &8| &r").append(affliction.getRegisteredBy().getName());

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
        return deathNoteItem;
    }

    public ItemStack getAfflictionsBook() {
        if (afflictionsBook == null) createDeathNoteAfflictionsBook();
        return afflictionsBook;
    }

    public void enableMetrics() {
        Metrics metrics = new Metrics(this, 12275);

        if (metrics.isEnabled()) {
            Message.STARTING_METRICS.log();
        }
    }

    public void registerEvents() {
        Util.registerEvent(new DeathNoteListener());
    }
}
