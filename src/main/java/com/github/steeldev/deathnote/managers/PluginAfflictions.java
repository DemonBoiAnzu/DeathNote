package com.github.steeldev.deathnote.managers;

import com.github.steeldev.deathnote.api.Affliction;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import static com.github.steeldev.deathnote.api.Afflictions.*;
import static com.github.steeldev.deathnote.util.Util.getMain;

public class PluginAfflictions {
    public static void registerPluginAfflictions() {
        // Cannot be disabled
        Affliction heartAttack = new Affliction("&cHeart Attack",
                Arrays.asList("heart attack", "sudden death"),
                "%s had a heart attack",
                getMain(),
                player -> player.damage(player.getHealth()));
        register("heart attack", heartAttack);
        setDefaultAffliction(heartAttack);


        if (getMain().config.EXPLOSION_AFFLICTION_ENABLED) {
            register("explosion", new Affliction("&cExplosion",
                    Arrays.asList("explosion", "boom", "big boom"),
                    "%s blew up",
                    getMain(),
                    player -> player.getWorld().createExplosion(player.getLocation(), 10, true)));
        } else if (isAfflictedRegistered("explosion")) unregister("explosion");

        if (getMain().config.LIGHTNING_AFFLICTION_ENABLED) {
            register("lightning", new Affliction("&eLightning",
                    Arrays.asList("lightning", "smite", "strike down"),
                    "%s was struck by lightning",
                    getMain(),
                    player -> {
                        World world = player.getWorld();
                        world.strikeLightning(player.getLocation());
                        new BukkitRunnable() {
                            int strikes = 0;

                            @Override
                            public void run() {
                                world.strikeLightning(player.getLocation());
                                strikes++;
                                if (strikes >= 6 || player.isDead())
                                    this.cancel();
                            }
                        }.runTaskTimer(getMain(), 20, 20);
                    }));
        } else if (isAfflictedRegistered("lightning")) unregister("lightning");

        if (getMain().config.FIRE_AFFLICTION_ENABLED) {
            register("fire", new Affliction("&4Fire",
                    Arrays.asList("fire", "burning"),
                    "%s burned to death",
                    getMain(),
                    player -> {
                        World world = player.getWorld();
                        while (!player.isDead()) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.setFireTicks(99999);
                                    Block block = world.getBlockAt(player.getLocation());
                                    if (block.getType().equals(Material.WATER))
                                        block.setType(Material.AIR);
                                }
                            }.runTaskLater(getMain(), 20);
                        }
                    }));
        } else if (isAfflictedRegistered("fire")) unregister("fire");
    }
}
