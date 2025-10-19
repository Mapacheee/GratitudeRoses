package me.mapacheee.gratituderoses.config;

import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

/* This class if to define the typed Configurate-backed configuration for the plugin */

@ConfigSerializable
@Configurate("config")
public record AppConfig(
        @Setting("enabled-worlds") List<String> enabledWorlds,
        Item item,
        Detection detection,
        Effects effects,
        Database database,
        @Setting("worldguard") WorldGuardSection worldguard
) {
    @ConfigSerializable
    public record Item(@Setting("trigger-materials") List<String> triggerMaterials, Hotbar hotbar) {}

    @ConfigSerializable
    public record Hotbar(boolean enabled, int slot, String name, List<String> lore) {}

    @ConfigSerializable
    public record Detection(
            @Setting("water-detection-window-seconds") int waterDetectionWindowSeconds,
            @Setting("cooldown-seconds") int cooldownSeconds,
            @Setting("return-item-after-seconds") int returnItemAfterSeconds
    ) {}

    @ConfigSerializable
    public record Effects(
            @Setting("duration-seconds") int durationSeconds,
            @Setting("particle-count") int particleCount,
            @Setting("show-title") boolean showTitle,
            @Setting("particle-dust-type") String particleDustType,
            @Setting("particle-dust-color") String particleDustColor,
            @Setting("particle-dust-size") double particleDustSize,
            @Setting("particle-splash-type") String particleSplashType,
            List<SoundSpec> sounds
    ) {}

    @ConfigSerializable
    public record SoundSpec(String name, double volume, double pitch) {}

    @ConfigSerializable
    public record Database(
            @Setting("type") String type,
            @Setting("file") String file,
            @Setting("host") String host,
            @Setting("port") int port,
            @Setting("name") String name,
            @Setting("user") String user,
            @Setting("password") String password,
            @Setting("use-ssl") boolean useSsl,
            @Setting("params") String params,
            @Setting("pool-max-size") int poolMaxSize,
            @Setting("pool-min-idle") int poolMinIdle,
            @Setting("connection-timeout-ms") long connectionTimeoutMs,
            @Setting("leak-detection-threshold-ms") long leakDetectionThresholdMs
    ) {}

    @ConfigSerializable
    public record WorldGuardSection(
            boolean enabled,
            @Setting("allowed-regions") List<String> allowedRegions
    ) {}
}
