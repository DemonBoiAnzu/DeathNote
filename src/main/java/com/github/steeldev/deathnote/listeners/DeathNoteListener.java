package com.github.steeldev.deathnote.listeners;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import com.github.steeldev.deathnote.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.getMain;

public class DeathNoteListener implements Listener {
    @EventHandler
    public void bookEdit(PlayerEditBookEvent event){
        if(event.isCancelled()) return;
        if(!getMain().deathNoteItemManager.isDeathNote(event.getPlayer().getItemInUse())) return;
        // Signed
        if(event.isSigning()) {
            event.setSigning(false);
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        BookMeta meta = event.getNewBookMeta();

        String page = meta.getPage(meta.getPageCount());

        List<String> entrySplit = Arrays.asList(page.split(" by "));
        Player target = Bukkit.getServer().getPlayer(entrySplit.get(0));
        Affliction inputtedCause = (entrySplit.size() > 1) ? AfflictionManager.getAfflictedByTriggerWord(entrySplit.get(1)) : null;
        if(target == null){
            Message.TARGET_INVALID.send(player,true,entrySplit.get(0));
            return;
        }

        if(entrySplit.size() > 1)
            Message.TARGET_WILL_BE_AFFLICTED_BY.send(player,true,target.getName(),entrySplit.get(1));
        else
            Message.TARGET_WILL_BE_AFFLICTED.send(player,true,target.getName());
        new BukkitRunnable() {
            @Override
            public void run() {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,200,1));
                target.playSound(target.getLocation(),Sound.AMBIENT_CAVE,SoundCategory.MASTER,1,1);
                Message.TARGET_BEING_AFFLICTED.send(target,false);

                if(inputtedCause == null){
                    Affliction defaultAffliction = getMain().afflictionManager.defaultAffliction;
                    if(entrySplit.size() > 1)
                        Message.INPUTTED_AFFLICTION_INVALID.send(player,true,entrySplit.get(1),target.getName(),defaultAffliction.getTriggers().get(0));
                    defaultAffliction.execute(target);
                }else {
                    inputtedCause.execute(target);
                }
            }
        }.runTaskLater(getMain(),300);
    }

    @EventHandler
    public void openBook(PlayerInteractEvent event){
        if(event.getHand() == null) return;
        if(!event.getHand().equals(EquipmentSlot.HAND)) return;
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                !event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if(item == null) return;
        if(!item.getType().equals(Material.WRITABLE_BOOK)) return;
        if(!getMain().deathNoteItemManager.isDeathNote(item)) return;
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER,1,1);
    }
}
