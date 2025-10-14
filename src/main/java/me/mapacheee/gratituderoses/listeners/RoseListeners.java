package me.mapacheee.gratituderoses.listeners;

import com.google.inject.Inject;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.gratituderoses.gratitude.GratitudeService;
import me.mapacheee.gratituderoses.hotbar.HotbarService;
import me.mapacheee.gratituderoses.shared.ConfigService;
import me.mapacheee.gratituderoses.shared.SchedulerService;
import me.mapacheee.gratituderoses.shared.TextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/* This class if to listen player events to manage hotbar item, detect drops and route to GratitudeService */

@ListenerComponent
public class RoseListeners implements Listener {
    private final GratitudeService gratitudeService;
    private final HotbarService hotbarService;
    private final ConfigService config;
    private final TextService text;
    private final SchedulerService scheduler;

    @Inject
    public RoseListeners(GratitudeService gratitudeService, HotbarService hotbarService, ConfigService config, TextService text, SchedulerService scheduler) {
        this.gratitudeService = gratitudeService;
        this.hotbarService = hotbarService;
        this.config = config;
        this.text = text;
        this.scheduler = scheduler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (hotbarService.isEnabledWorld(p.getWorld().getName())) {
            hotbarService.giveTo(p);
            scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (hotbarService.isEnabledWorld(p.getWorld().getName())) {
            hotbarService.giveTo(p);
            scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        if (hotbarService.isEnabledWorld(p.getWorld().getName())) {
            hotbarService.giveTo(p);
            scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
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

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (gratitudeService.onPickupAttempt(player, e.getItem())) {
            e.setCancelled(true);
            scheduler.runLaterSync(() -> hotbarService.ensurePosition(player), 1L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!hotbarService.isEnabledWorld(p.getWorld().getName())) return;
        int roseSlot = hotbarService.roseSlot();
        var clickedInv = e.getClickedInventory();
        var current = e.getCurrentItem();
        var cursor = e.getCursor();
        boolean currentIsRose = hotbarService.isPluginRose(current);
        boolean cursorIsRose = hotbarService.isPluginRose(cursor);

        if (clickedInv == p.getInventory()) {
            if (currentIsRose && e.getSlot() != roseSlot) { e.setCancelled(true); return; }
            if (cursorIsRose && e.getSlot() != roseSlot) { e.setCancelled(true); return; }
            if (e.getSlot() == roseSlot && !currentIsRose && !cursorIsRose) { e.setCancelled(true); return; }
        }

        int hb = e.getHotbarButton();
        if (hb >= 0) {
            var hotbarItem = p.getInventory().getItem(hb);
            boolean hbIsRose = hotbarService.isPluginRose(hotbarItem);
            if (hb == roseSlot && !hotbarService.isPluginRose(e.getCurrentItem())) { e.setCancelled(true); }
            if (hbIsRose && e.getSlot() != roseSlot) { e.setCancelled(true); }
            if (hb != roseSlot && currentIsRose) { e.setCancelled(true); }
        }
        scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!hotbarService.isEnabledWorld(p.getWorld().getName())) return;
        int roseSlot = hotbarService.roseSlot();
        int base = p.getOpenInventory().getTopInventory().getSize();
        int roseRaw = base + roseSlot;
        var cursor = e.getOldCursor();
        boolean cursorIsRose = hotbarService.isPluginRose(cursor);
        if (cursorIsRose) {
            if (e.getRawSlots().stream().anyMatch(s -> s != roseRaw)) { e.setCancelled(true); }
            scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
            return;
        }
        if (e.getRawSlots().stream().anyMatch(s -> s == roseRaw)) { e.setCancelled(true); }
        scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!hotbarService.isEnabledWorld(p.getWorld().getName())) return;
        if (hotbarService.isPluginRose(e.getMainHandItem()) || hotbarService.isPluginRose(e.getOffHandItem())) {
            e.setCancelled(true);
        }
        scheduler.runLaterSync(() -> hotbarService.ensurePosition(p), 1L);
    }
}
