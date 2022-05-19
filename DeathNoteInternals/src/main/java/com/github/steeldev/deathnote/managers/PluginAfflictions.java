package com.github.steeldev.deathnote.managers;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.util.Message;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.steeldev.deathnote.api.AfflictionManager.*;
import static com.github.steeldev.deathnote.util.Util.*;

public class PluginAfflictions {
    public static void registerPluginAfflictions() {
        // Cannot be disabled
        Affliction heartAttack = new Affliction("heart_attack",
                "&cHeart Attack",
                Arrays.asList("heart attack", "sudden death"),
                "The target will have a heart attack and die instantly.",
                "had a heart attack",
                getMain(),
                player -> player.damage(player.getHealth()));
        setDefaultAffliction(heartAttack);
        register(heartAttack);


        if (getMain().config.EXPLOSION_AFFLICTION_ENABLED) {
            register(new Affliction("explosion", "&cExplosion",
                    Arrays.asList("explosion", "boom", "big boom"),
                    "The target will have a powerful explosion manifested at their feet.",
                    "blew up",
                    getMain(),
                    player -> player.getWorld().createExplosion(player.getLocation(), 10, true)));
        } else unregister("explosion");

        if (getMain().config.LIGHTNING_AFFLICTION_ENABLED) {
            register(new Affliction("lightning",
                    "&eLightning",
                    Arrays.asList("lightning", "smite", "strike down"),
                    "The target will get struck by lightning until they die.",
                    "was struck by lightning",
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
            register(new Affliction("fire",
                    "&4Fire",
                    Arrays.asList("fire", "burning"),
                    "The target will burn to death, with no way of stopping it.",
                    "burned to death",
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
            register(new Affliction("poison",
                    "&2Poison",
                    Collections.singletonList("poison"),
                    "The target will get poisoned until they die.",
                    "was poisoned to death",
                    getMain(),
                    player -> new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.hasPotionEffect(PotionEffectType.POISON))
                                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 9999, 255));

                            if (player.getHealth() <= 2)
                                player.damage(1);

                            if (player.isDead())
                                this.cancel();
                        }
                    }.runTaskTimer(getMain(), 20, 20)));
        } else unregister("poison");

        if (getMain().config.VOID_AFFLICTION_ENABLED) {
            register(new Affliction("void", "&8Void",
                    Collections.singletonList("void"),
                    "The target will be teleported into the void.",
                    "fell out of the world",
                    getMain(),
                    player -> {
                        Location loc = player.getLocation();
                        loc.setY(-80);
                        player.teleport(loc);
                    }));
        } else unregister("void");

        if (getMain().config.CREEPER_AFFLICTION_ENABLED) {
            register(new Affliction("creeper",
                    "&2Creeper",
                    Arrays.asList("creeper", "creepers"),
                    "The target will have creepers spawned on them until they die.",
                    "was blown up by creepers",
                    getMain(),
                    player -> new BukkitRunnable() {
                        final List<LivingEntity> creeperList = new ArrayList<>();

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
                    }.runTaskTimer(getMain(), 20, 40)));
        } else unregister("creeper");

        if (getMain().config.FALLING_AFFLICTION_ENABLED) {
            register(new Affliction("falling",
                    "&rFalling",
                    Arrays.asList("fall", "falling"),
                    "The target will be launched into the air and fall to their death.",
                    "fell to their death",
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
            register(new Affliction("arrows",
                    "&8Arrows",
                    Arrays.asList("arrows", "arrow storm"),
                    "The target will have arrows fall onto them until they die.",
                    "died to a rain of arrows",
                    getMain(),
                    player -> new BukkitRunnable() {
                        final List<Entity> arrowList = new ArrayList<>();

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
                    }.runTaskTimer(getMain(), 10, 10)));
        } else unregister("arrows");

        if (getMain().config.ANVIL_AFFLICTION_ENABLED) {
            register(new Affliction("anvil",
                    "&8Anvil",
                    Arrays.asList("anvil", "anvils"),
                    "The target will have anvils fall onto them until they die.",
                    "was squashed by an anvil",
                    getMain(),
                    player -> new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location anvilSpawn = player.getLocation().add(0, 10, 0);
                            player.getWorld().getBlockAt(anvilSpawn).setType(Material.ANVIL);
                            if (player.isDead())
                                this.cancel();
                        }
                    }.runTaskTimer(getMain(), 20, 20)));
        } else unregister("anvil");

        if (getMain().config.SUFFOCATION_AFFLICTION_ENABLED) {
            register(new Affliction("suffocation",
                    "&8Suffocation",
                    Arrays.asList("suffocation", "wall breathing"),
                    "The target will be suffocated.",
                    "suffocated",
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
            register(new Affliction("drowning",
                    "&bDrowning",
                    Arrays.asList("drowning", "drown"),
                    "The target will drown.",
                    "drowned",
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
            register(new Affliction("cow",
                    "&6Cow",
                    Arrays.asList("cow", "cow crush"),
                    "The target will be crushed by cows.",
                    "was squashed by cows",
                    getMain(),
                    player -> new BukkitRunnable() {
                        final List<Entity> cowList = new ArrayList<>();

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
                    }.runTaskTimer(getMain(), 20, 20)));
        } else unregister("cow");

        if (getMain().config.LAVA_AFFLICTION_ENABLED) {
            register(new Affliction("lava", "&4Lava",
                    Collections.singletonList("lava"),
                    "The target will burn in lava.",
                    "burned to death in lava",
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
            register(new Affliction("pig",
                    "&5Pig",
                    Arrays.asList("pig", "pig bomb"),
                    "The target will be blown up by a pig.",
                    "was blown up by a pig",
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

        if (getMain().config.ARCHANGELS_FURY_AFFLICTION_ENABLED) {
            register(new Affliction("archangels_fury",
                    "&bArchangels Fury",
                    Arrays.asList("archangels fury", "archangels smite", "archangels wrath"),
                    "The target will be smitten by the Archangel.",
                    "was smitten by the Archangel.",
                    getMain(),
                    player -> {
                        World world = player.getWorld();
                        world.setThundering(true);
                        world.setStorm(true);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new BukkitRunnable() {
                                    int bellChimes = 0;
                                    Location playerLoc = player.getLocation();

                                    @Override
                                    public void run() {
                                        playerLoc = player.getLocation();
                                        Location bellLoc = playerLoc;
                                        bellLoc.add(0, 5, 0);
                                        world.playSound(bellLoc, Sound.BLOCK_BELL_USE, SoundCategory.MASTER, 1, 0.4f);
                                        world.playSound(bellLoc, Sound.BLOCK_BELL_RESONATE, SoundCategory.MASTER, 1, 0.4f);
                                        bellChimes++;

                                        if (bellChimes >= 5) {
                                            new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    playerLoc = player.getLocation();
                                                    world.setThundering(false);
                                                    world.setStorm(false);
                                                    world.strikeLightning(playerLoc);
                                                    world.createExplosion(playerLoc, 15, true);
                                                    new BukkitRunnable() {
                                                        @Override
                                                        public void run() {
                                                            List<Material> randBlocks = Arrays.asList(Material.OBSIDIAN, Material.BLACK_TERRACOTTA, Material.BLACK_CONCRETE);
                                                            List<Material> soulSands = Arrays.asList(Material.SOUL_SAND, Material.SOUL_SOIL);
                                                            for (int x = playerLoc.getBlockX() - 10; x <= playerLoc.getBlockX() + 10; x++) {
                                                                for (int y = playerLoc.getBlockY() - 10; y <= playerLoc.getBlockY() + 10; y++) {
                                                                    for (int z = playerLoc.getBlockZ() - 10; z <= playerLoc.getBlockZ() + 10; z++) {
                                                                        Block block = world.getBlockAt(x, y, z);
                                                                        if (!block.getType().equals(Material.AIR)) {
                                                                            if (chanceOf(50))
                                                                                block.setType(randBlocks.get(rand.nextInt(randBlocks.size())));
                                                                        }
                                                                        if (block.getType().equals(Material.FIRE)) {
                                                                            block.getRelative(0, -1, 0).setType(soulSands.get(rand.nextInt(soulSands.size())));
                                                                            block.setType(Material.SOUL_FIRE);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }.runTaskLater(getMain(), 5);
                                                }
                                            }.runTaskLater(getMain(), 60);
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(getMain(), 0, 20);
                            }
                        }.runTaskLater(getMain(), 150);
                    }));
        } else unregister("archangels_fury");

        if (getMain().config.NAIR_AFFLICTION_ENABLED) {
            register(new Affliction("nair",
                    "&fNair",
                    Arrays.asList("nair", "nair burn", "nair on nuts"),
                    "The target will burn slowly by nair.",
                    "was burned alive by nair",
                    getMain(),
                    player -> {
                        World world = player.getWorld();
                        player.setFireTicks(99999);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9999, 255, true, false, false));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.setFireTicks(99999);
                                player.damage(1);
                                Block block = world.getBlockAt(player.getLocation());
                                if (block.getType().equals(Material.WATER))
                                    block.setType(Material.AIR);
                                if (player.isDead())
                                    this.cancel();
                            }
                        }.runTaskTimer(getMain(), 10, 30);
                    }));
        } else unregister("nair");

        if (getMain().config.BEES_AFFLICTION_ENABLED) {
            register(new Affliction("bees",
                    "&6Bees",
                    Arrays.asList("bees", "bee", "bee attack", "bee swarm"),
                    "The target will get attacked by bees. #SaveTheBees!",
                    "was killed by a swarm of bees. #SaveTheBees!",
                    getMain(),
                    player -> {
                        World world = player.getWorld();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                var bee = (Bee) world.spawnEntity(player.getLocation().add(0, 2, 0), EntityType.BEE);
                                bee.setInvulnerable(true);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        bee.setAnger(9999);
                                        bee.setTarget(player);
                                        bee.setHasStung(false);

                                        bee.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(0.5);
                                        bee.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);

                                        if (player.isDead()) {
                                            bee.remove();
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(getMain(), 10, 5);
                                if (player.isDead()) {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(getMain(), 10, 20);
                    }));
        } else unregister("bees");

        if (getMain().config.FANGS_AFFLICTION_ENABLED) {
            register(new Affliction("fangs",
                    "&5Fangs",
                    Arrays.asList("fangs", "evoker fangs", "fang"),
                    "The target will get eaten alive by evoker fangs.",
                    "was eaten alive by evoker fangs.",
                    getMain(),
                    player -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 2));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 255));
                        player.setFoodLevel(3);
                        new BukkitRunnable() {
                            int tick = 0;
                            float radius = 5f;
                            float radPerSec = 3f;
                            float radPerTick = radPerSec / 20f;

                            @Override
                            public void run() {
                                if (!player.isDead()) {
                                    tick++;
                                    if (radius > 1 && tick < 300) {
                                        Location center = player.getLocation();
                                        radPerSec += 0.05f;
                                        radius -= 0.03f;
                                        if (radPerSec >= 8)
                                            radPerSec = 8f;
                                        radPerTick = radPerSec / 20f;
                                        Location loc = getLocationAroundCircle(center, radius, radPerTick * tick);
                                        player.getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
                                    }
                                    if (radius <= 1 && tick < 300) {
                                        radius = 5f;
                                        player.damage(4);
                                        player.setNoDamageTicks(0);
                                        player.getWorld().spawnEntity(player.getLocation(), EntityType.EVOKER_FANGS);
                                    }
                                    if (tick >= 300) {
                                        tick = 0;
                                        radPerSec = 3f;
                                        radPerTick = radPerSec / 20f;
                                    }
                                } else this.cancel();
                            }
                        }.runTaskTimer(getMain(), 0, 1);
                    }));
        } else unregister("fangs");

        Message.DEFAULTS_REGISTERED.log();
    }
}
