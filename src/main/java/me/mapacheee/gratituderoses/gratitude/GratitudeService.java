package me.mapacheee.gratituderoses.gratitude;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.gratituderoses.hotbar.HotbarService;
import me.mapacheee.gratituderoses.shared.ConfigService;
import me.mapacheee.gratituderoses.shared.SchedulerService;
import me.mapacheee.gratituderoses.shared.TextService;
import me.mapacheee.gratituderoses.storage.StorageService;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/* This class if to handle the gratitude flow: detect water contact, record DB, fire effects, messages and return hotbar rose */

@Service
public class GratitudeService {
    private final ConfigService config;
    private final TextService text;
    private final SchedulerService scheduler;
    private final StorageService storage;
    private final HotbarService hotbar;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @Inject
    public GratitudeService(ConfigService config, TextService text, SchedulerService scheduler, StorageService storage, HotbarService hotbar) {
        this.config = config;
        this.text = text;
        this.scheduler = scheduler;
        this.storage = storage;
        this.hotbar = hotbar;
    }

    public boolean isTriggerMaterial(Material material) {
        Set<Material> set = config.triggerMaterials();
        return set.contains(material);
    }

    public boolean isWorldEnabled(String worldName) {
        return config.enabledWorlds().isEmpty() || config.enabledWorlds().contains(worldName);
    }

    public boolean canUse(Player player) {
        int cd = config.cooldownSeconds();
        if (cd <= 0) return true;
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        return last == null || now - last >= cd * 1000L;
    }

    public int remainingCooldown(Player player) {
        int cd = config.cooldownSeconds();
        if (cd <= 0) return 0;
        Long last = cooldowns.get(player.getUniqueId());
        if (last == null) return 0;
        long rem = cd * 1000L - (System.currentTimeMillis() - last);
        return (int) Math.ceil(Math.max(0, rem) / 1000.0);
    }

    public void onDroppedTracked(Item item, Player player) {
        final long timeoutTicks = config.detectionWindowSeconds() * 20L;
        final long[] elapsed = {0L};
        final boolean[] done = {false};
        scheduler.runRegion(item, () -> {
            if (!item.isValid() || item.isDead()) return;
        });
        scheduler.runRegionLater(item, () -> {}, 1L);
        scheduler.runRegion(item, new Runnable() {
            @Override
            public void run() {
                if (done[0]) return;
                if (!item.isValid() || item.isDead()) return;
                Location loc = item.getLocation();
                if (isWaterAt(loc)) {
                    done[0] = true;
                    item.remove();
                    handleGratitude(player, loc);
                    return;
                }
                elapsed[0] += 2L;
                if (elapsed[0] >= timeoutTicks) {
                    done[0] = true;
                    return;
                }
                scheduler.runRegionLater(item, this, 2L);
            }
        });
    }

    private boolean isWaterAt(Location loc) {
        Material type = loc.getBlock().getType();
        if (type == Material.WATER) return true;
        BlockData data = loc.getBlock().getBlockData();
        if (data instanceof Waterlogged wl) {
            return wl.isWaterlogged();
        }
        return false;
    }

    private void handleGratitude(Player player, Location at) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        scheduler.runAsync(() -> {
            long number;
            long total;
            try {
                storage.recordLaunch(player.getUniqueId(), player.getName());
                total = storage.totalLaunches();
                number = total;
            } catch (SQLException e) {
                number = -1L;
                total = -1L;
            }
            long finalNumber = number;
            long finalTotal = total;
            scheduler.runRegion(player, () -> {
                fireEffects(at);
                Map<String, String> ph = Map.of(
                        "player", player.getName(),
                        "number", String.valueOf(finalNumber),
                        "total", String.valueOf(finalTotal),
                        "date", text.nowDate()
                );
                text.send(player, config.msgThankedChat(), ph);
                if (config.showTitle()) {
                    text.title(player, config.msgTitleMain(), config.msgTitleSub().replace("{number}", String.valueOf(finalNumber)), config.effectsDurationSeconds() * 20);
                }
                int ret = config.returnItemAfterSeconds();
                if (ret > 0) {
                    scheduler.runRegionLater(player, () -> hotbar.giveTo(player), ret * 20L);
                }
            });
        });
    }

    private void fireEffects(Location at) {
        var world = at.getWorld();
        if (world == null) return;
        int count = config.particleCount();
        Particle dustParticle = resolveParticle(new String[]{"REDSTONE", "DUST"});
        Particle splashParticle = resolveParticle(new String[]{"WATER_SPLASH", "SPLASH", "BUBBLE"});
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.5f);
        int durationTicks = config.effectsDurationSeconds() * 20;
        for (int i = 0; i < durationTicks; i += 5) {
            final int delay = i;
            scheduler.runLaterSync(() -> {
                if (dustParticle != null && isDustLike(dustParticle)) {
                    world.spawnParticle(dustParticle, at, count, 0.3, 0.2, 0.3, dust);
                }
                if (splashParticle != null) {
                    world.spawnParticle(splashParticle, at, Math.max(5, count / 3), 0.3, 0.2, 0.3, 0.01);
                }
            }, delay);
        }
        for (me.mapacheee.gratituderoses.shared.ConfigService.SoundSpec s : config.soundSpecs()) {
            world.playSound(at, s.sound(), s.volume(), s.pitch());
        }
    }

    private Particle resolveParticle(String[] names) {
        for (String n : names) {
            try { return Particle.valueOf(n); } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    private boolean isDustLike(Particle p) {
        String n = p.toString();
        return n.equalsIgnoreCase("REDSTONE") || n.equalsIgnoreCase("DUST");
    }
}
