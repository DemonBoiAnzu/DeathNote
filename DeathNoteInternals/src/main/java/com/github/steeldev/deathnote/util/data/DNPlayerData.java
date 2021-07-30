package com.github.steeldev.deathnote.util.data;

import org.bukkit.entity.Player;

public class DNPlayerData {
    public Player player;
    public int kills;

    public DNPlayerData(Player player, int kills) {
        this.player = player;
        this.kills = kills;
    }
}
