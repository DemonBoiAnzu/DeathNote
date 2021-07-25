package com.github.steeldev.deathnote.afflictions;

import com.github.steeldev.deathnote.api.AfflictionAction;
import org.bukkit.entity.Player;

public class AffHeartAttack implements AfflictionAction {
    @Override
    public void execute(Player target) {
        target.damage(target.getHealth() + 100);
    }
}
