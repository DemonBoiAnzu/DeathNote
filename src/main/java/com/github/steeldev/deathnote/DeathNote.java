package com.github.steeldev.deathnote;

import com.github.steeldev.deathnote.listeners.DeathNoteListener;
import com.github.steeldev.deathnote.managers.DeathNoteItemManager;
import com.github.steeldev.deathnote.managers.PluginAfflictionManager;
import com.github.steeldev.deathnote.util.DNLogger;
import com.github.steeldev.deathnote.util.Message;
import com.github.steeldev.deathnote.util.UpdateChecker;
import com.github.steeldev.deathnote.util.Util;
import com.github.steeldev.deathnote.util.config.Config;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import static org.bukkit.Bukkit.getPluginManager;

public class DeathNote extends JavaPlugin {
    private static DeathNote instance;
    public Config config = null;
    public UpdateChecker versionManager;

    public Plugin monstrorvmPlugin;

    public Logger logger;

    public DeathNoteItemManager deathNoteItemManager;
    public PluginAfflictionManager afflictionManager;

    public static DeathNote getInstance() {
        return instance;
    }

    @Override
    public @NotNull Logger getLogger() {
        if (logger == null) logger = DNLogger.getLogger();
        return this.logger;
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;

        MinecraftVersion.replaceLogger(getLogger());

        if (!Util.isRunningMinecraft(1, 16)) {
            Util.log("&c&l[&4&lERROR&c&l] Unsupported server version. Death Note only supports 1.16+");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadConfigurations();

        registerEvents();

        registerCommands();

        if (loadMonstrorvm() != null) {
            monstrorvmPlugin = loadMonstrorvm();
            if (monstrorvmPlugin.isEnabled()) {
                Message.MONSTRORVM_FOUND.log(monstrorvmPlugin.getDescription().getVersion());
            } else
                Message.MONSTRORVM_FOUND_DISABLED.log(monstrorvmPlugin.getDescription().getVersion());
        } else
            Message.MONSTRORVM_NOT_FOUND.log();

        deathNoteItemManager = new DeathNoteItemManager();
        deathNoteItemManager.createDeathNoteItem();

        afflictionManager = new PluginAfflictionManager();
        afflictionManager.registerDefaultAfflictions();

        enableMetrics();

        Message.PLUGIN_ENABLED.log(getDescription().getVersion(), (float) (System.currentTimeMillis() - start) / 1000);

        versionManager = new UpdateChecker(this, 0);
        versionManager.checkForNewVersion();
    }

    public Plugin loadMonstrorvm() {
        return getPluginManager().getPlugin("Monstrorvm");
    }

    @Override
    public void onDisable() {
        Message.PLUGIN_DISABLED.log();
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

    public void registerCommands() {

    }

    public void registerEvents() {
        Util.registerEvent(new DeathNoteListener());
    }
}
