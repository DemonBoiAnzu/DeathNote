package com.github.steeldev.deathnote.example;

import com.github.steeldev.deathnote.api.Affliction;
import com.github.steeldev.deathnote.api.AfflictionManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Random;

public class ExampleDeathNoteAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        AfflictionManager.register(new Affliction("globber",
                "&6Globber",
                Arrays.asList("globber", "glob"),
                "The target will be globbered.",
                "was globbered to death",
                this,
                player -> {
                    Random random = new Random();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int velY = random.nextInt(4);
                            int r = random.nextInt(2);
                            if (r == 1) velY = -velY;
                            player.setVelocity(new Vector(0, velY, 0));
                            player.damage(1);

                            if (player.isDead())
                                this.cancel();
                        }
                    }.runTaskTimer(this, 0, 10);
                }));
    }
}
