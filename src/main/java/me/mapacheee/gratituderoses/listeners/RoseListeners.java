package me.mapacheee.gratituderoses.listeners;

import com.google.inject.Inject;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.gratituderoses.gratitude.GratitudeService;
import me.mapacheee.gratituderoses.hotbar.HotbarService;
import me.mapacheee.gratituderoses.shared.ConfigService;
import me.mapacheee.gratituderoses.shared.TextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/* This class if to listen player events to manage hotbar item, detect drops and route to GratitudeService */

@ListenerComponent
public class RoseListeners implements Listener {
    private final GratitudeService gratitudeService;
    private final HotbarService hotbarService;
    private final ConfigService config;
    private final TextService text;

    @Inject
    public RoseListeners(GratitudeService gratitudeService, HotbarService hotbarService, ConfigService config, TextService text) {
        this.gratitudeService = gratitudeService;
        this.hotbarService = hotbarService;
        this.config = config;
        this.text = text;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (hotbarService.isEnabledWorld(p.getWorld().getName())) {
            hotbarService.giveTo(p);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (hotbarService.isEnabledWorld(p.getWorld().getName())) {
            hotbarService.giveTo(p);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        if (hotbarService.isEnabledWorld(p.getWorld().getName())) {
            hotbarService.giveTo(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!gratitudeService.isWorldEnabled(p.getWorld().getName())) {
            if (gratitudeService.isTriggerMaterial(e.getItemDrop().getItemStack().getType())) {
                e.setCancelled(true);
                text.send(p, config.msgWrongWorld(), null);
            }
            return;
        }
        var stack = e.getItemDrop().getItemStack();
        if (!gratitudeService.isTriggerMaterial(stack.getType())) return;
        if (!gratitudeService.canUse(p)) {
            int rem = gratitudeService.remainingCooldown(p);
            text.send(p, config.msgCooldown(), java.util.Map.of("seconds", String.valueOf(rem)));
            e.setCancelled(true);
            return;
        }
        gratitudeService.onDroppedTracked(e.getItemDrop(), p);
    }
}

