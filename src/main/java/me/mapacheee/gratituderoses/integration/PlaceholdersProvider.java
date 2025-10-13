package me.mapacheee.gratituderoses.integration;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mapacheee.gratituderoses.storage.StorageService;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/* This class if to register PlaceholderAPI expansions for total and player launches */

@Service
public class PlaceholdersProvider extends PlaceholderExpansion {
    private final Plugin plugin;
    private final StorageService storage;
    private boolean registered = false;

    @Inject
    public PlaceholdersProvider(Plugin plugin, StorageService storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gratitude";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        String v = plugin.getPluginMeta().getVersion();
        return v == null ? "" : v;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    public boolean registerExpansion() {
        if (!registered) {
            registered = super.register();
        }
        return registered;
    }

    public void unregisterExpansion() {
        if (registered) {
            registered = false;
            super.unregister();
        }
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        try {
            switch (params.toLowerCase()) {
                case "total":
                case "gratitude_total":
                    return String.valueOf(storage.totalLaunches());
                case "player_number":
                case "gratitude_player_number":
                    if (player == null) return "0";
                    return String.valueOf(storage.playerLaunches(player.getUniqueId()));
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
