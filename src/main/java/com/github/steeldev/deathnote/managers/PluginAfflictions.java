package com.github.steeldev.deathnote.managers;

import com.github.steeldev.deathnote.api.Affliction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.steeldev.deathnote.api.AfflictionManager.*;
import static com.github.steeldev.deathnote.util.Util.getMain;

public class PluginAfflictions {
    public static void registerPluginAfflictions() {
        // Cannot be disabled
        Affliction heartAttack = new Affliction("&cHeart Attack",
                Arrays.asList("heart attack", "sudden death"),
                "The target will have a heart attack and die instantly.",
                "%s had a heart attack",
                getMain(),
                player -> player.damage(player.getHealth()));
        register("heart attack", heartAttack);
        setDefaultAffliction(heartAttack);


        if (getMain().config.EXPLOSION_AFFLICTION_ENABLED) {
            register("explosion", new Affliction("&cExplosion",
                    Arrays.asList("explosion", "boom", "big boom"),
                    "The target will have a powerful explosion manifested at their feet.",
                    "%s blew up",
                    getMain(),
                    player -> player.getWorld().createExplosion(player.getLocation(), 10, true)));
        } else unregister("explosion");

        if (getMain().config.LIGHTNING_AFFLICTION_ENABLED) {
            register("lightning", new Affliction("&eLightning",
                    Arrays.asList("lightning", "smite", "strike down"),
                    "The target will get struck by lightning until they die.",
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
        } else unregister("lightning");

        if (getMain().config.FIRE_AFFLICTION_ENABLED) {
            register("fire", new Affliction("&4Fire",
                    Arrays.asList("fire", "burning"),
                    "The target will burn to death, with no way of stopping it.",
                    "%s burned to death",
                    getMain(),
                    player -> {
                        World world = player.getWorld();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.setFireTicks(99999);
                                Block block = world.getBlockAt(player.getLocation());
                                if (block.getType().equals(Material.WATER)) {
                                    player.damage(1);
                                    block.setType(Material.AIR);
                                }

                                if (player.isDead())
                                    this.cancel();
                            }
                        }.runTaskTimer(getMain(), 20, 20);
                    }));
        } else unregister("fire");

        if (getMain().config.POISON_AFFLICTION_ENABLED) {
            register("poison", new Affliction("&2Poison",
                    Arrays.asList("poison"),
                    "The target will get poisoned until they die.",
                    "%s was poisoned to death",
                    getMain(),
                    player -> {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.hasPotionEffect(PotionEffectType.POISON))
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 9999, 255));

                                if (player.getHealth() <= 2)
                                    player.damage(1);

                                if (player.isDead())
                                    this.cancel();
                            }
                        }.runTaskTimer(getMain(), 20, 20);
                    }));
        } else unregister("poison");

        if (getMain().config.VOID_AFFLICTION_ENABLED) {
            register("void", new Affliction("&8Void",
                    Arrays.asList("void"),
                    "The target will be teleported into the void.",
                    "%s fell out of the world",
                    getMain(),
                    player -> {
                        Location loc = player.getLocation();
                        loc.setY(-60);
                        player.teleport(loc);
                    }));
        } else unregister("void");

        if (getMain().config.CREEPER_AFFLICTION_ENABLED) {
            register("creeper", new Affliction("&2Creeper",
                    Arrays.asList("creeper", "creepers"),
                    "The target will have creepers spawned on them until they die.",
                    "%s was blown up by creepers",
                    getMain(),
                    player -> {
                        new BukkitRunnable() {
                            List<LivingEntity> creeperList = new ArrayList<>();

                            @Override
                            public void run() {
                                Creeper creeper = (Creeper) player.getWorld().spawnEntity(player.getLocation(), EntityType.CREEPER);
                                creeper.setFuseTicks(3);
                                creeper.setInvulnerable(true);
                                creeper.ignite();
                                creeperList.add(creeper);
                                if (player.isDead()) {
                                    for (LivingEntity creep : creeperList) {
                                        creep.remove();
                                    }
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(getMain(), 20, 40);
                    }));
        } else unregister("creeper");

        if (getMain().config.FALLING_AFFLICTION_ENABLED) {
            register("fall", new Affliction("&rFalling",
                    Arrays.asList("fall", "falling"),
                    "The target will be launched into the air and fall to their death.",
                    "%s fell to their death",
                    getMain(),
                    player -> {
                        player.setVelocity(new Vector(0, 999, 0));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 4));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.isDead())
                                    player.damage(player.getHealth());
                            }
                        }.runTaskLater(getMain(), 100);
                    }));
        } else unregister("fall");

        if (getMain().config.ARROWS_AFFLICTION_ENABLED) {
            register("arrows", new Affliction("&7Arrows",
                    Arrays.asList("arrows", "arrow storm"),
                    "The target will have arrows fall onto them until they die.",
                    "%s died to a rain of arrows",
                    getMain(),
                    player -> {
                        new BukkitRunnable() {
                            List<Entity> arrowList = new ArrayList<>();

                            @Override
                            public void run() {
                                Location arrowSpawn = player.getLocation().add(0, 10, 0);
                                Arrow arrow = (Arrow) player.getWorld().spawnEntity(arrowSpawn, EntityType.ARROW);
                                arrow.setFallDistance(20);
                                arrowList.add(arrow);
                                if (player.isDead()) {
                                    for (Entity arr : arrowList) {
                                        arr.remove();
                                    }
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(getMain(), 10, 10);
                    }));
        } else unregister("arrows");

        if (getMain().config.ANVIL_AFFLICTION_ENABLED) {
            register("anvil", new Affliction("&7Anvil",
                    Arrays.asList("anvil", "anvils"),
                    "The target will have anvils fall onto them until they die.",
                    "%s was squashed by an anvil",
                    getMain(),
                    player -> {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location anvilSpawn = player.getLocation().add(0, 10, 0);
                                player.getWorld().getBlockAt(anvilSpawn).setType(Material.ANVIL);
                                if (player.isDead())
                                    this.cancel();
                            }
                        }.runTaskTimer(getMain(), 20, 20);
                    }));
        } else unregister("anvil");

        if (getMain().config.SUFFOCATION_AFFLICTION_ENABLED) {
            register("suffocation", new Affliction("&7Suffocation",
                    Arrays.asList("suffocation", "wall breathing"),
                    "The target will be suffocated.",
                    "%s suffocated",
                    getMain(),
                    player -> {
                        Location location = player.getLocation();
                        location.setY(7);
                        player.teleport(location.add(0, 1, 0));
                        player.getLocation().getBlock().setType(Material.OBSIDIAN);
                        player.getLocation().getBlock().getRelative(0, 1, 0).setType(Material.OBSIDIAN);
                    }));
        } else unregister("suffocation");

        if (getMain().config.DROWNING_AFFLICTION_ENABLED) {
            register("drowning", new Affliction("&bDrowning",
                    Arrays.asList("drowning", "drown"),
                    "The target will drown.",
                    "%s drowned",
                    getMain(),
                    player -> {
                        Location location = player.getLocation();
                        location.setY(7);
                        player.teleport(location.add(0, 1, 0));
                        player.getLocation().getBlock().setType(Material.WATER);
                        player.getLocation().getBlock().getRelative(0, 1, 0).setType(Material.WATER);
                    }));
        } else unregister("drowning");

        if (getMain().config.COW_AFFLICTION_ENABLED) {
            register("cow", new Affliction("&6Cow",
                    Arrays.asList("cow", "cow crush"),
                    "The target will be crushed by cows.",
                    "%s was squashed by cows",
                    getMain(),
                    player -> {
                        new BukkitRunnable() {
                            List<Entity> cowList = new ArrayList<>();

                            @Override
                            public void run() {
                                Location cowSpawn = player.getLocation().add(0, 10, 0);
                                Cow cow = (Cow) player.getWorld().spawnEntity(cowSpawn, EntityType.COW);
                                cowList.add(cow);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (!player.isDead()) {
                                            cow.setInvulnerable(true);
                                            player.damage(6);
                                        } else this.cancel();
                                    }
                                }.runTaskLater(getMain(), 20);
                                if (player.isDead()) {
                                    for (Entity c : cowList) {
                                        c.remove();
                                    }
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(getMain(), 20, 20);
                    }));
        } else unregister("cow");

        if (getMain().config.LAVA_AFFLICTION_ENABLED) {
            register("lava", new Affliction("&4Lava",
                    Arrays.asList("lava"),
                    "The target will burn in lava.",
                    "%s burned to death in lava",
                    getMain(),
                    player -> {
                        Location location = player.getLocation();
                        location.setY(7);
                        player.teleport(location.add(0, 1, 0));
                        player.getLocation().getBlock().setType(Material.LAVA);
                        player.getLocation().getBlock().getRelative(0, 1, 0).setType(Material.LAVA);
                    }));
        } else unregister("lava");

        if (getMain().config.PIG_AFFLICTION_ENABLED) {
            register("pig", new Affliction("&5Pig",
                    Arrays.asList("pig", "pig bomb"),
                    "The target will be blown up by a pig.",
                    "%s was blown up by a pig",
                    getMain(),
                    player -> {
                        Pig pig = (Pig) player.getWorld().spawnEntity(player.getLocation(), EntityType.PIG);
                        pig.setInvulnerable(true);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                pig.setVelocity(new Vector(0, 1, 0));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        pig.getWorld().createExplosion(pig.getLocation(), 10);
                                        pig.remove();
                                    }
                                }.runTaskLater(getMain(), 25);
                            }
                        }.runTaskLater(getMain(), 20);
                    }));
        } else unregister("pig");
    }
}
