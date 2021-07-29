package com.github.steeldev.deathnote.util;

import com.github.steeldev.deathnote.DeathNote;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {
    private final DeathNote plugin;
    // Config stuff
    public String PREFIX;
    public boolean DEBUG;
    public boolean NEW_UPDATE_MESSAGE_ON_JOIN;
    public boolean NEW_UPDATE_MESSAGE_ON_RELOAD;

    public boolean RANDOM_AFFLICTION_ENABLED;
    public boolean EXPLOSION_AFFLICTION_ENABLED;
    public boolean LIGHTNING_AFFLICTION_ENABLED;
    public boolean FIRE_AFFLICTION_ENABLED;
    public boolean POISON_AFFLICTION_ENABLED;
    public boolean VOID_AFFLICTION_ENABLED;
    public boolean CREEPER_AFFLICTION_ENABLED;
    public boolean FALLING_AFFLICTION_ENABLED;
    public boolean ARROWS_AFFLICTION_ENABLED;
    public boolean ANVIL_AFFLICTION_ENABLED;
    public boolean SUFFOCATION_AFFLICTION_ENABLED;
    public boolean DROWNING_AFFLICTION_ENABLED;
    public boolean COW_AFFLICTION_ENABLED;
    public boolean LAVA_AFFLICTION_ENABLED;
    public boolean PIG_AFFLICTION_ENABLED;
    public boolean ARCHANGELS_FURY_AFFLICTIONS_ENABLED;


    private FileConfiguration config;
    private File configFile;

    public Config(DeathNote plugin) {
        this.plugin = plugin;
        loadConfigFile();
    }

    public void setString(String path, String value) throws IOException {
        config.set(path, value);

        config.save(configFile);
    }

    public void setBool(String path, boolean value) throws IOException {
        config.set(path, value);

        config.save(configFile);
    }

    private void loadConfigFile() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "Config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("Config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        matchConfig();
        loadConfigs();
    }

    // Used to update config
    @SuppressWarnings("ConstantConditions")
    private void matchConfig() {
        try {
            boolean hasUpdated = false;
            InputStream stream = plugin.getResource(configFile.getName());
            assert stream != null;
            InputStreamReader is = new InputStreamReader(stream);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(is);
            for (String key : defConfig.getConfigurationSection("").getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defConfig.get(key));
                    hasUpdated = true;
                }
            }
            for (String key : config.getConfigurationSection("").getKeys(true)) {
                if (!defConfig.contains(key)) {
                    config.set(key, null);
                    hasUpdated = true;
                }
            }
            if (hasUpdated)
                config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfigs() {
        PREFIX = config.getString("Prefix");
        DEBUG = config.getBoolean("Debug");
        NEW_UPDATE_MESSAGE_ON_JOIN = config.getBoolean("UpdateCheck.MessageOnJoin");
        NEW_UPDATE_MESSAGE_ON_RELOAD = config.getBoolean("UpdateCheck.MessageOnReload");

        RANDOM_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Random");
        EXPLOSION_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Explosion");
        LIGHTNING_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Lightning");
        FIRE_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Fire");
        POISON_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Poison");
        VOID_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Void");
        CREEPER_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Creeper");
        FALLING_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Falling");
        ARROWS_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Arrows");
        ANVIL_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Anvil");
        SUFFOCATION_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Suffocation");
        DROWNING_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Drowning");
        COW_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Cow");
        LAVA_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Lava");
        PIG_AFFLICTION_ENABLED = config.getBoolean("Afflictions.Pig");
        ARCHANGELS_FURY_AFFLICTIONS_ENABLED = config.getBoolean("Afflictions.ArchangelsFury");
    }
}
