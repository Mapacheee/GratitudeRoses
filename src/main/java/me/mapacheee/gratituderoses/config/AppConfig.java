package me.mapacheee.gratituderoses.config;

/* This class if to define the typed Configurate-backed configuration for the plugin */
import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
@Configurate("config")
public record AppConfig(
        @Setting("enabled-worlds") List<String> enabledWorlds,
        Item item,
        Detection detection,
        Effects effects,
        Database database
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
    public record Database(String file) {}
}
