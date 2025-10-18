package me.mapacheee.gratituderoses;

import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.RuntimeLibPluginConfiguration;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.net.URLClassLoader;

@WinterBootPlugin
public final class GratitudeRosesPlugin extends PaperWinterPlugin {
    private static GratitudeRosesPlugin instance;

    public static <T> T getService(Class<T> type) {
        return instance.getInjector().getInstance(type);
    }

    @Override
    public void onPluginLoad() {
        super.onPluginLoad();
        instance = this;

        RuntimeLibPluginConfiguration config = RuntimeLibPluginConfiguration.parse();
        File libraries = new File(getDataFolder(), config.getLibsFolder());
        if (!libraries.exists()) {
            getLogger().info("[" + getName() + "] It appears you're running " + getName() + " for the first time.");
            getLogger().info("[" + getName() + "] Please give me a few seconds to install dependencies. This is a one-time process.");
        }
        DependencyManager dependencyManager = new DependencyManager(
                libraries,
                URLClassLoaderWrapper.wrap((URLClassLoader) getClass().getClassLoader())
        );
        config.getDependencies().forEach(dependencyManager::dependency);
        config.getRepositories().forEach(dependencyManager::repository);
        config.getRelocations().forEach(dependencyManager::relocate);
        dependencyManager.load();
    }

    @Override
    public void onPluginEnable() {
        super.onPluginEnable();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                if (papi != null) {
                    var provider = getService(me.mapacheee.gratituderoses.integration.PlaceholdersProvider.class);
                    provider.registerExpansion();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void onPluginDisable() {
        super.onPluginDisable();
        try {
            var provider = getService(me.mapacheee.gratituderoses.integration.PlaceholdersProvider.class);
            provider.unregisterExpansion();
        } catch (Throwable ignored) {
        }
        try {
            var storage = getService(me.mapacheee.gratituderoses.storage.StorageService.class);
            storage.close();
        } catch (Throwable ignored) {}
    }
}
