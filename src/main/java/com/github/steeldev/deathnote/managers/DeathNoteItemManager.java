package com.github.steeldev.deathnote.managers;

import com.github.steeldev.deathnote.util.Util;
import com.github.steeldev.monstrorvm.api.items.ItemManager;
import com.github.steeldev.monstrorvm.api.items.MVItem;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.github.steeldev.deathnote.util.Util.getMain;

public class DeathNoteItemManager {
    String deathNoteID = "death_note";

    // This will be null if Monstrorvm is being used
    //  but will be set if not
    // Getting it will return this, or the Monstrorvm item
    ItemStack deathNoteItem;

    String deathNoteDisplayName = "<##443c3c>Death Note";

    List<String> deathNoteLore = new ArrayList<String>() {
        {
            add("&7The humans whose name");
            add("&7is written in this note shall die.");
            add("");
            add("&7No cause given, they will simply die");
            add("&7of a heart attack.");
            add("");
            add("&7Give a cause by adding 'by' then");
            add("&7a cause of death.");
            add("");
            add("&7Additionally, you can provide a time");
            add("&7of death as well, by putting 'in'");
            add("&7followed by a timespan");
            add("&7&oe.g 10 minutes");
        }
    };


    public void createDeathNoteItem() {
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
            meta.setLore(deathNoteLore);
            meta.setDisplayName(deathNoteDisplayName);
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

    public boolean isDeathNote(ItemStack item) {
        if (Util.monstrorvmEnabled()) return ItemManager.isMVItem(item, deathNoteID);
        else {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.hasKey(deathNoteID);
        }
    }
}
