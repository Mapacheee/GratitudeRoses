package me.mapacheee.gratituderoses.shared;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.gratituderoses.config.AppConfig;
import me.mapacheee.gratituderoses.config.MessagesConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.*;

/* This class if to load and expose plugin configuration (Configurate typed) and messages (Yaml) */

@Service
public class ConfigService {
    private final Container<AppConfig> configC;
    private final Container<MessagesConfig> messagesC;

    @Inject
    public ConfigService(Container<AppConfig> configC, Container<MessagesConfig> messagesC) {
        this.configC = configC;
        this.messagesC = messagesC;
    }

    private AppConfig cfg() {
        return configC.get();
    }

    private MessagesConfig msgCfg() {
        return messagesC.get();
    }

    public List<String> enabledWorlds() {
        var c = cfg();
        var list = c == null ? null : c.enabledWorlds();
        return list == null ? Collections.emptyList() : list;
    }

    public Set<Material> triggerMaterials() {
        var c = cfg();
        List<String> list = c == null || c.item() == null ? List.of("ROSE_BUSH") : c.item().triggerMaterials();
        if (list == null || list.isEmpty()) list = List.of("ROSE_BUSH");
        Set<Material> out = new HashSet<>();
        for (String s : list) {
            if (s == null || s.isBlank()) continue;
            Material m = Material.matchMaterial(s);
            if (m == null) m = Material.matchMaterial(s.toLowerCase(Locale.ROOT));
            if (m == null) m = Material.matchMaterial(s.toUpperCase(Locale.ROOT));
            if (m != null) out.add(m);
        }
        if (out.isEmpty()) out.add(Material.ROSE_BUSH);
        return out;
    }

    public boolean hotbarEnabled() {
        var c = cfg();
        return c == null || c.item() == null || c.item().hotbar() == null || c.item().hotbar().enabled();
    }

    public int hotbarSlot() {
        var c = cfg();
        int slot = c == null || c.item() == null || c.item().hotbar() == null ? 4 : c.item().hotbar().slot();
        return Math.max(0, Math.min(8, slot));
    }

    public String hotbarName() {
        var c = cfg();
        String name = c == null || c.item() == null || c.item().hotbar() == null ? null : c.item().hotbar().name();
        return name == null ? "&c&lRosa de Gratitud" : name;
    }

    public List<String> hotbarLore() {
        var c = cfg();
        List<String> lore = c == null || c.item() == null || c.item().hotbar() == null ? null : c.item().hotbar().lore();
        return lore == null ? Collections.emptyList() : lore;
    }

    public int detectionWindowSeconds() {
        var c = cfg();
        int v = c == null || c.detection() == null ? 10 : c.detection().waterDetectionWindowSeconds();
        return Math.max(1, v);
    }

    public int cooldownSeconds() {
        var c = cfg();
        int v = c == null || c.detection() == null ? 3 : c.detection().cooldownSeconds();
        return Math.max(0, v);
    }

    public int returnItemAfterSeconds() {
        var c = cfg();
        int v = c == null || c.detection() == null ? 3 : c.detection().returnItemAfterSeconds();
        return Math.max(0, v);
    }

    public int effectsDurationSeconds() {
        var c = cfg();
        int v = c == null || c.effects() == null ? 3 : c.effects().durationSeconds();
        return Math.max(1, v);
    }

    public int particleCount() {
        var c = cfg();
        int v = c == null || c.effects() == null ? 40 : c.effects().particleCount();
        return Math.max(1, v);
    }

    public boolean showTitle() {
        var c = cfg();
        return c == null || c.effects() == null || c.effects().showTitle();
    }

    public List<SoundSpec> soundSpecs() {
        List<SoundSpec> sounds = new ArrayList<>();
        var c = cfg();
        List<AppConfig.SoundSpec> list = c == null || c.effects() == null ? null : c.effects().sounds();
        if (list != null) {
            for (AppConfig.SoundSpec s : list) {
                String name = s.name();
                double volume = s.volume();
                double pitch = s.pitch();
                Sound sound = resolveSound(name);
                sounds.add(new SoundSpec(sound, (float) volume, (float) pitch));
            }
        }
        if (sounds.isEmpty()) sounds.add(new SoundSpec(defaultSound(), 1.0f, 1.0f));
        return sounds;
    }

    private Sound defaultSound() {
        Sound s = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.player.levelup"));
        return s != null ? s : Sound.ENTITY_PLAYER_LEVELUP;
    }

    private Sound resolveSound(String name) {
        if (name == null || name.isBlank()) return defaultSound();
        String cand = name.trim();
        List<String> candidates = new ArrayList<>();
        if (cand.contains(":")) {
            candidates.add(cand.toLowerCase(Locale.ROOT));
        } else {
            candidates.add("minecraft:" + cand.toLowerCase(Locale.ROOT));
            candidates.add("minecraft:" + cand.replace('_', '.').toLowerCase(Locale.ROOT));
        }
        for (String keyStr : candidates) {
            try {
                NamespacedKey key = NamespacedKey.fromString(keyStr);
                if (key != null) {
                    Sound byKey = Registry.SOUNDS.get(key);
                    if (byKey != null) return byKey;
                }
            } catch (Throwable ignored) {}
        }
        return defaultSound();
    }

    public String prefix() {
        var m = msgCfg();
        return m == null || m.prefix() == null ? "" : m.prefix();
    }

    public String msgThankedChat() { var m = msgCfg(); return m == null || m.player() == null || m.player().thankedChat() == null ? "" : m.player().thankedChat(); }
    public String msgTitleMain() { var m = msgCfg(); return m == null || m.player() == null || m.player().titleMain() == null ? "" : m.player().titleMain(); }
    public String msgTitleSub() { var m = msgCfg(); return m == null || m.player() == null || m.player().titleSub() == null ? "" : m.player().titleSub(); }
    public String msgCooldown() { var m = msgCfg(); return m == null || m.player() == null || m.player().cooldown() == null ? "" : m.player().cooldown(); }
    public String msgWrongWorld() { var m = msgCfg(); return m == null || m.player() == null || m.player().wrongWorld() == null ? "" : m.player().wrongWorld(); }
    public String msgReloaded() { var m = msgCfg(); return m == null || m.admin() == null || m.admin().reloaded() == null ? "" : m.admin().reloaded(); }
    public String msgStatsGlobal() { var m = msgCfg(); return m == null || m.admin() == null || m.admin().statsGlobal() == null ? "" : m.admin().statsGlobal(); }
    public String msgStatsPlayer() { var m = msgCfg(); return m == null || m.admin() == null || m.admin().statsPlayer() == null ? "" : m.admin().statsPlayer(); }
    public String msgStatsPlayerNone() { var m = msgCfg(); return m == null || m.admin() == null || m.admin().statsPlayerNone() == null ? "" : m.admin().statsPlayerNone(); }

    public String dbFile() {
        var c = cfg();
        return c == null || c.database() == null || c.database().file() == null ? "gratitude.db" : c.database().file();
    }

    public void reload() {
        try { configC.reload(); } catch (Throwable ignored) {}
        try { messagesC.reload(); } catch (Throwable ignored) {}
    }

    public record SoundSpec(Sound sound, float volume, float pitch) {}
}
