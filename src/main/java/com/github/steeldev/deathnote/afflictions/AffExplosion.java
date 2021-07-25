package com.github.steeldev.deathnote.afflictions;

import com.github.steeldev.deathnote.api.AfflictionAction;
import org.bukkit.entity.Player;

public class AffExplosion implements AfflictionAction {
    @Override
    public void execute(Player target) {
        target.getWorld().createExplosion(target.getLocation(), 10, true, true);
    }
}
