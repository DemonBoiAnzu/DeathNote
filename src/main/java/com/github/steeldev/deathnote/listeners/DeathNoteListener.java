package com.github.steeldev.deathnote.listeners;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.util.Message;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.getMain;

public class DeathNoteListener implements Listener {
    @EventHandler
    public void bookEdit(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;
        if (!getMain().deathNoteItemManager.isDeathNote(event.getPlayer().getItemInUse())) return;
        if (event.isSigning()) {
            event.setSigning(false);
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        BookMeta meta = event.getNewBookMeta();

        String page = meta.getPage(meta.getPageCount());

        List<String> entrySplit = Arrays.asList(page.split(" by "));
        Player target = Bukkit.getServer().getPlayer(entrySplit.get(0));
        Affliction inputtedAffliction = (entrySplit.size() > 1) ? AfflictionManager.getAfflictionByTriggerWord(entrySplit.get(1)) : null;
        Affliction defaultAffliction = getMain().afflictionManager.defaultAffliction;
        if (target == null) {
            Message.TARGET_INVALID.send(player, true, entrySplit.get(0));
            return;
        }

        String afflictionDisplay = (inputtedAffliction == null) ? defaultAffliction.getDisplay() : inputtedAffliction.getDisplay();

        if (inputtedAffliction != null)
            Message.TARGET_WILL_BE_AFFLICTED_BY.send(player, true, target.getName(), afflictionDisplay);
        else
            Message.INPUTTED_AFFLICTION_INVALID.send(player, true, entrySplit.get(1), target.getName(), afflictionDisplay);
        //Message.TARGET_WILL_BE_AFFLICTED.send(player,true,target.getName());
        new BukkitRunnable() {
            @Override
            public void run() {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20, 1));
                target.playSound(target.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.MASTER, 1, 1);
                Message.TARGET_BEING_AFFLICTED.send(target, false);
                target.setGameMode(GameMode.SURVIVAL);
                target.setFlying(false);
                target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                AfflictionManager.setAfflicted(target, true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (inputtedAffliction == null) {
                            defaultAffliction.execute(target);
                            if (defaultAffliction.getDeathMessage() != null && !defaultAffliction.getDeathMessage().isEmpty())
                                target.setMetadata("afflictionDeathMessage", new FixedMetadataValue(getMain(), defaultAffliction.getDeathMessage()));
                        } else {
                            inputtedAffliction.execute(target);
                            if (inputtedAffliction.getDeathMessage() != null && !inputtedAffliction.getDeathMessage().isEmpty())
                                target.setMetadata("afflictionDeathMessage", new FixedMetadataValue(getMain(), inputtedAffliction.getDeathMessage()));
                        }
                    }
                }.runTaskLater(getMain(), 40);
            }
        }.runTaskLater(getMain(), 10);
        AfflictionManager.setAfflicted(target, false);
    }

    @EventHandler
    public void openBook(PlayerInteractEvent event) {
        if (event.getHand() == null) return;
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                !event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (item == null) return;
        if (!item.getType().equals(Material.WRITABLE_BOOK)) return;
        if (!getMain().deathNoteItemManager.isDeathNote(item)) return;
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1, 1);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (AfflictionManager.isAfflicted(player)) AfflictionManager.setAfflicted(player, false);
    }

    @EventHandler
    public void entityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().isInvulnerable()) event.getDrops().clear();
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (AfflictionManager.isAfflicted(player)) {
            if (player.hasMetadata("afflictionDeathMessage"))
                event.setDeathMessage(player.getMetadata("afflictionDeathMessage").toString());
        }
    }

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (AfflictionManager.isAfflicted(player)) event.setCancelled(true);
    }

    @EventHandler
    public void playerPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (AfflictionManager.isAfflicted(player)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (AfflictionManager.isAfflicted(player)) event.setCancelled(true);
    }

    @EventHandler
    public void playerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (AfflictionManager.isAfflicted(player)) event.setCancelled(true);
    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (AfflictionManager.isAfflicted(player)) event.setCancelled(true);
    }
}
