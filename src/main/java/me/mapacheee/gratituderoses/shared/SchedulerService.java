package me.mapacheee.gratituderoses.shared;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/* This class if to abstract scheduling across Paper and Folia, offering sync/async and region-bound tasks */

@Service
public class SchedulerService {
    private final Plugin plugin;
    private final boolean folia;

    @Inject
    public SchedulerService(Plugin plugin) {
        this.plugin = plugin;
        this.folia = isFoliaPresent();
    }

    private boolean isFoliaPresent() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void runSync(Runnable task) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runAsync(Runnable task) {
        if (folia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void runLaterSync(Runnable task, long delayTicks) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void runRegion(Entity entity, Runnable task) {
        if (folia) {
            entity.getScheduler().execute(plugin, task, null, 0L);
        } else {
            runSync(task);
        }
    }

    public void runRegionLater(Entity entity, Runnable task, long delayTicks) {
        if (folia) {
            entity.getScheduler().runDelayed(plugin, (Consumer<ScheduledTask>) task, null, delayTicks);
        } else {
            runLaterSync(task, delayTicks);
        }
    }
}

