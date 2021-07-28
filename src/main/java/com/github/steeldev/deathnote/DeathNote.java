package com.github.steeldev.deathnote;

import com.github.steeldev.deathnote.commands.MainCommand;
import com.github.steeldev.deathnote.listeners.DeathNoteListener;
import com.github.steeldev.deathnote.managers.PluginAfflictions;
import com.github.steeldev.deathnote.util.*;
import com.github.steeldev.monstrorvm.api.items.ItemManager;
import com.github.steeldev.monstrorvm.api.items.MVItem;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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
        }catch(SQLException ex){
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

    public ItemStack getDeathNoteItem() {
        if (Util.monstrorvmEnabled()) return ItemManager.getItem(deathNoteID).getItemStack();
        else return deathNoteItem;
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
