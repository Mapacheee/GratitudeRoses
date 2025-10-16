package me.mapacheee.gratituderoses.integration;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.gratituderoses.shared.ConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import java.util.Locale;
import java.util.Set;

/* This class if to check if a location is inside allowed WorldGuard regions based on config */

@Service
public class WorldGuardRegionService {
    private final ConfigService config;

    @Inject
    public WorldGuardRegionService(ConfigService config) {
        this.config = config;
    }

    public boolean isAllowed(Location loc) {
        if (!config.wgEnabled()) return true;
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg == null || !wg.isEnabled()) return false;
        Set<String> allowed = config.wgAllowedRegionsLower();
        if (allowed.isEmpty()) return false;
        try {
            World weWorld = BukkitAdapter.adapt(loc.getWorld());
            BlockVector3 pt = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(weWorld);
            if (manager == null) return false;
            ApplicableRegionSet set = manager.getApplicableRegions(pt);
            for (ProtectedRegion r : set) {
                if (r != null && r.getId() != null && allowed.contains(r.getId().toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            return false;
        } catch (Throwable ex) {
            return false;
        }
    }
}
