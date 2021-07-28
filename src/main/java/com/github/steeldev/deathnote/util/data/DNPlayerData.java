package com.github.steeldev.deathnote.util.data;

import org.bukkit.entity.Player;

public class DNPlayerData {
    public Player player;
    public int kills;
    public boolean hasTouchedNote;

    public DNPlayerData(Player player, boolean hasTouchedNote, int kills){
        this.player = player;
        this.hasTouchedNote = hasTouchedNote;
        this.kills = kills;
    }
}
