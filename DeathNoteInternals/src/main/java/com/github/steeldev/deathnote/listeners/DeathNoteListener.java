package com.github.steeldev.deathnote.listeners;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.api.events.AfflictionRegisteredEvent;
import com.github.steeldev.deathnote.api.events.AfflictionTriggeredEvent;
import com.github.steeldev.deathnote.api.events.AfflictionUnregisteredEvent;
import com.github.steeldev.deathnote.util.Database;
import com.github.steeldev.deathnote.util.Message;
import com.github.steeldev.deathnote.util.Timespan;
import com.github.steeldev.deathnote.util.Util;
import com.github.steeldev.deathnote.util.data.DNPlayerData;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.book.BookUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.*;

public class DeathNoteListener implements Listener {
    @EventHandler
    public void afflictionRegistered(AfflictionRegisteredEvent event) {
        Affliction affliction = event.getRegisteredAffliction();
        if (!affliction.getRegisteredBy().equals(getMain()))
            Message.AFFLICTION_REGISTERED.log(affliction.getDisplay(), affliction.getRegisteredBy().getName());
        getMain().createDeathNoteAfflictionsBook();
    }

    @EventHandler
    public void afflictionUnregistered(AfflictionUnregisteredEvent event) {
        Affliction affliction = event.getUnregisteredAffliction();
        if (!affliction.getRegisteredBy().equals(getMain()))
            Message.AFFLICTION_UNREGISTERED.log(affliction.getDisplay());
        getMain().createDeathNoteAfflictionsBook();
    }

    @EventHandler
    public void bookEdit(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        BookMeta meta = event.getNewBookMeta();
        if (!isDeathNote(player.getInventory().getItemInMainHand())) return;
        if (event.isSigning()) {
            event.setSigning(false);
            event.setCancelled(true);
            return;
        }
        List<String> pages = meta.getPages();
        if (pages.size() == 0) return;
        List<String> pagesSplit = Arrays.asList(pages.get(pages.size() - 1).split("\n"));
        String page = pagesSplit.get(pagesSplit.size() - 1);
        if (page.trim().isEmpty()) return;
        List<String> entrySplit = Arrays.asList(page.split("( by | in)"));
        if (entrySplit.size() == 0) return;
        String playerEntry = entrySplit.get(0).trim();
        String afflictionEntry = (entrySplit.size() > 1) ? entrySplit.get(1).trim() : "";
        long time = 30; // in ticks
        String timeInput = "";

        Player target = Bukkit.getServer().getPlayer(playerEntry);
        Affliction defaultAffliction = AfflictionManager.getDefaultAffliction();
        Affliction inputtedAffliction;
        if (!afflictionEntry.equals("")) {
            if (afflictionEntry.equalsIgnoreCase("random") && getMain().config.RANDOM_AFFLICTION_ENABLED)
                inputtedAffliction = AfflictionManager.getRandomAffliction();
            else
                inputtedAffliction = AfflictionManager.getAfflictionByTriggerWord(afflictionEntry);
        } else inputtedAffliction = defaultAffliction;
        if (target == null) {
            Message.TARGET_INVALID.send(player, true, playerEntry);
            return;
        }
        if (target.isDead()) {
            Message.TARGET_CANT_BE_AFFLICTED.send(player, true);
            return;
        }
        if (entrySplit.size() > 2 && Timespan.parse(entrySplit.get(2).trim()) != -1) {
            time = Timespan.parse(entrySplit.get(2).trim());
            timeInput = entrySplit.get(2).trim();
        } else if (entrySplit.size() > 1 && Timespan.parse(entrySplit.get(1).trim()) != -1 && inputtedAffliction == null) {
            time = Timespan.parse(entrySplit.get(1).trim());
            timeInput = entrySplit.get(1).trim();
            inputtedAffliction = defaultAffliction;
        }
        if (inputtedAffliction == null) {
            Message.INPUTTED_AFFLICTION_INVALID.send(player, true, afflictionEntry, target.getName(), defaultAffliction.getDisplay());
            inputtedAffliction = defaultAffliction;
        } else {
            String afflictionDisplay = inputtedAffliction.getDisplay();
            if (inputtedAffliction != defaultAffliction) {
                if (time > 30)
                    Message.TARGET_WILL_BE_AFFLICTED_BY_IN.send(player, true, target.getName(), afflictionDisplay, timeInput);
                else
                    Message.TARGET_WILL_BE_AFFLICTED_BY.send(player, true, target.getName(), afflictionDisplay);
            } else {
                if (time > 30)
                    Message.TARGET_WILL_BE_AFFLICTED_IN.send(player, true, target.getName(), timeInput);
                else
                    Message.TARGET_WILL_BE_AFFLICTED.send(player, true, target.getName());
            }

            if (time < 31 && !getMain().config.INSTANT_TRIGGER) {
                if (getMain().config.DEBUG)
                    Util.log("[DEBUG] Instant trigger disabled & custom time not inputted by user, setting initial delay to 40 seconds.");
                time = Timespan.parse("40 seconds");
            }
        }

        Affliction finalInputtedAffliction = inputtedAffliction;
        AfflictionTriggeredEvent afflictionTriggeredEvent = new AfflictionTriggeredEvent(finalInputtedAffliction);
        Bukkit.getServer().getPluginManager().callEvent(afflictionTriggeredEvent);
        if (afflictionTriggeredEvent.isCancelled()) {
            Message.TARGET_CANT_BE_AFFLICTED.send(player, true);
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand().clone();
        ItemMeta itemMeta = item.getItemMeta();
        if (!item.getType().equals(Material.AIR) && itemMeta != null) {
            List<String> bookLore = itemMeta.getLore();
            if (getMain().config.BOOK_MAX_USES > 0) {
                int curUses = itemMeta.getPersistentDataContainer().getOrDefault(bookUsesKey, PersistentDataType.INTEGER, 0);
                curUses++;
                itemMeta.getPersistentDataContainer().set(bookUsesKey, PersistentDataType.INTEGER, curUses);
                if (bookLore.size() < 5) {
                    bookLore.add("");
                    bookLore.add(colorize(String.format("&e%d&7/&e%d &7Uses", curUses, getMain().config.BOOK_MAX_USES)));
                } else
                    bookLore.set(5, colorize(String.format("&e%d&7/&e%d &7Uses", curUses, getMain().config.BOOK_MAX_USES)));
                itemMeta.setLore(bookLore);
                item.setItemMeta(itemMeta);

                int finalCurUses = curUses;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (finalCurUses >= getMain().config.BOOK_MAX_USES) {
                            Message.DEATH_NOTE_USED_UP.send(player, true);
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.7f);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) Timespan.parse("5 seconds"), 1));
                        } else player.getInventory().setItemInMainHand(item);
                    }
                }.runTaskLater(getMain(), 4);
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (bookLore.size() > 3) {
                            bookLore.remove(5);
                            bookLore.remove(4);
                            itemMeta.setLore(bookLore);
                            item.setItemMeta(itemMeta);
                            player.getInventory().setItemInMainHand(item);
                        }
                    }
                }.runTaskLater(getMain(), 4);
            }
        }

        if (getMain().config.DEBUG)
            Util.log(String.format("&7[DEBUG] &e%s &rhas been set to be afflicted by &7%s &rin &e%d ticks &rby &e%s&r.", target.getName(), inputtedAffliction.getDisplay(), time, player.getName()));
        new BukkitRunnable() {
            @Override
            public void run() {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 1));
                target.playSound(target.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.MASTER, 1, 1);
                Message.TARGET_BEING_AFFLICTED.send(target, false);
                target.setGameMode(GameMode.SURVIVAL);
                target.setFlying(false);
                target.setInvulnerable(false);
                target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                setAfflicted(target, finalInputtedAffliction);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        finalInputtedAffliction.execute(target);
                    }
                }.runTaskLater(getMain(), 40);
            }
        }.runTaskLater(getMain(), time);
        setAfflicted(target, null);
        if (getMain().config.TRACK_KILLS) {
            try {
                DNPlayerData data = Database.getPlayerData(player);
                data.kills++;
                Database.updatePlayerData(data);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void openBook(PlayerInteractEvent event) {
        if (event.getHand() == null) return;
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                !event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (getMain().config.TRACK_KILLS) {
            DNPlayerData data = null;
            try {
                data = Database.getPlayerData(player);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            if (item == null) return;
            if (!item.getType().equals(Material.WRITABLE_BOOK)) return;
            if (!isDeathNote(item)) return;
            if (data == null) {
                try {
                    Database.addPlayer(player);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                Message.DEATH_NOTE_FIRST_TOUCH.send(player, true);
            }
        }
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1, 1);
        if (player.isSneaking()) {
            event.setCancelled(true);
            getMain().createDeathNoteAfflictionsBook();
            BookUtil.openPlayer(player, getMain().getAfflictionsBook());
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isAfflicted(player)) setAfflicted(player, null);
    }

    @EventHandler
    public void entityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().isInvulnerable()) event.getDrops().clear();
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (isAfflicted(player)) {
            Affliction affliction = getPlayerAffliction(player);
            if (affliction == null) return;
            if (affliction.getDeathMessage() != null && !affliction.getDeathMessage().isEmpty())
                event.setDeathMessage(player.getName() + " " + affliction.getDeathMessage());
            setAfflicted(player, null);
        }
    }

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isAfflicted(player)) event.setCancelled(true);
    }

    @EventHandler
    public void playerPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isAfflicted(player)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isAfflicted(player)) event.setCancelled(true);
    }

    @EventHandler
    public void playerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isAfflicted(player)) event.setCancelled(true);
    }

    @EventHandler
    public void playerChat(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (isAfflicted(player)) event.setCancelled(true);
    }
}
