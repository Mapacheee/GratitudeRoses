package me.mapacheee.gratituderoses.shared;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.gratituderoses.config.AppConfig;
import me.mapacheee.gratituderoses.config.MessagesConfig;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
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

    private AppConfig cfg() { return configC.get(); }
    private MessagesConfig msgCfg() { return messagesC.get(); }

    public List<String> enabledWorlds() {
        var list = cfg().enabledWorlds();
        return list == null ? Collections.emptyList() : list;
    }

    public boolean wgEnabled() {
        var wg = cfg().worldguard();
        return wg != null && wg.enabled();
    }

    public Set<String> wgAllowedRegionsLower() {
        var wg = cfg().worldguard();
        List<String> list = wg == null ? null : wg.allowedRegions();
        if (list == null) return Collections.emptySet();
        Set<String> out = new HashSet<>();
        for (String s : list) if (s != null && !s.isBlank()) out.add(s.toLowerCase(Locale.ROOT));
        return out;
    }

    public Set<Material> triggerMaterials() {
        List<String> list = cfg().item() == null ? List.of("ROSE_BUSH") : cfg().item().triggerMaterials();
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
        var item = cfg().item();
        var hotbar = item == null ? null : item.hotbar();
        return hotbar == null || hotbar.enabled();
    }

    public int hotbarSlot() {
        var item = cfg().item();
        var hotbar = item == null ? null : item.hotbar();
        int slot = hotbar == null ? 4 : hotbar.slot();
        return Math.max(0, Math.min(8, slot));
    }

    public String hotbarName() {
        var item = cfg().item();
        var hotbar = item == null ? null : item.hotbar();
        String name = hotbar == null ? null : hotbar.name();
        return name == null ? "&c&lRosa de Gratitud" : name;
    }

    public List<String> hotbarLore() {
        var item = cfg().item();
        var hotbar = item == null ? null : item.hotbar();
        List<String> lore = hotbar == null ? null : hotbar.lore();
        return lore == null ? Collections.emptyList() : lore;
    }

    public int detectionWindowSeconds() {
        var det = cfg().detection();
        int v = det == null ? 10 : det.waterDetectionWindowSeconds();
        return Math.max(1, v);
    }

    public int cooldownSeconds() {
        var det = cfg().detection();
        int v = det == null ? 3 : det.cooldownSeconds();
        return Math.max(0, v);
    }

    public int returnItemAfterSeconds() {
        var det = cfg().detection();
        int v = det == null ? 3 : det.returnItemAfterSeconds();
        return Math.max(0, v);
    }

    public int effectsDurationSeconds() {
        var eff = cfg().effects();
        int v = eff == null ? 3 : eff.durationSeconds();
        return Math.max(1, v);
    }

    public int particleCount() {
        var eff = cfg().effects();
        int v = eff == null ? 40 : eff.particleCount();
        return Math.max(1, v);
    }

    public boolean showTitle() {
        var eff = cfg().effects();
        return eff == null || eff.showTitle();
    }

    public List<SoundSpec> soundSpecs() {
        List<SoundSpec> sounds = new ArrayList<>();
        var eff = cfg().effects();
        List<AppConfig.SoundSpec> list = eff == null ? null : eff.sounds();
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

    public Particle dustParticle() {
        var eff = cfg().effects();
        String type = eff == null || eff.particleDustType() == null ? "REDSTONE" : eff.particleDustType();
        for (String cand : List.of(type, "REDSTONE", "DUST", "DUST_COLOR_TRANSITION")) {
            Particle p = tryParticle(cand);
            if (p != null && isDustLike(p)) return p;
        }
        Particle[] all = Particle.values();
        return all.length > 0 ? all[0] : null;
    }

    public Particle splashParticle() {
        var eff = cfg().effects();
        String type = eff == null || eff.particleSplashType() == null ? "WATER_SPLASH" : eff.particleSplashType();
        for (String cand : List.of(type, "WATER_SPLASH", "SPLASH", "BUBBLE", "BUBBLE_POP", "FALLING_WATER")) {
            Particle p = tryParticle(cand);
            if (p != null) return p;
        }
        Particle[] all = Particle.values();
        return all.length > 0 ? all[0] : null;
    }

    public Particle.DustOptions dustOptions() {
        Color color = parseColor();
        float size = (float) dustSize();
        return new Particle.DustOptions(color, Math.max(0.1f, size));
    }

    public double dustSize() {
        var eff = cfg().effects();
        double size = eff == null ? 1.5 : eff.particleDustSize();
        if (size <= 0) size = 1.5;
        return size;
    }

    public Color parseColor() {
        var eff = cfg().effects();
        String raw = eff == null ? null : eff.particleDustColor();
        if (raw == null || raw.isBlank()) return Color.fromRGB(255, 0, 0);
        String s = raw.trim();
        try {
            if (s.startsWith("#")) {
                int rgb = Integer.parseInt(s.substring(1), 16);
                return Color.fromRGB(rgb);
            }
            if (s.contains(",")) {
                String[] parts = s.split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return Color.fromRGB(clamp(r), clamp(g), clamp(b));
            }
        } catch (Throwable ignored) {}
        return Color.fromRGB(255, 0, 0);
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private Particle tryParticle(String name) {
        if (name == null) return null;
        String n = name.trim();
        try { return Particle.valueOf(n.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ignored) {}
        return null;
    }

    private boolean isDustLike(Particle p) {
        String n = p.name();
        return n.equalsIgnoreCase("REDSTONE") || n.equalsIgnoreCase("DUST") || n.equalsIgnoreCase("DUST_COLOR_TRANSITION");
    }

    public String prefix() {
        var m = msgCfg();
        return m.prefix() == null ? "" : m.prefix();
    }

    public String msgThankedChat() { var m = msgCfg(); return m.player() == null || m.player().thankedChat() == null ? "" : m.player().thankedChat(); }
    public String msgTitleMain() { var m = msgCfg(); return m.player() == null || m.player().titleMain() == null ? "" : m.player().titleMain(); }
    public String msgTitleSub() { var m = msgCfg(); return m.player() == null || m.player().titleSub() == null ? "" : m.player().titleSub(); }
    public String msgCooldown() { var m = msgCfg(); return m.player() == null || m.player().cooldown() == null ? "" : m.player().cooldown(); }
    public String msgWrongWorld() { var m = msgCfg(); return m.player() == null || m.player().wrongWorld() == null ? "" : m.player().wrongWorld(); }
    public String msgReloaded() { var m = msgCfg(); return m.admin() == null || m.admin().reloaded() == null ? "" : m.admin().reloaded(); }
    public String msgStatsGlobal() { var m = msgCfg(); return m.admin() == null || m.admin().statsGlobal() == null ? "" : m.admin().statsGlobal(); }
    public String msgStatsPlayer() { var m = msgCfg(); return m.admin() == null || m.admin().statsPlayer() == null ? "" : m.admin().statsPlayer(); }
    public String msgStatsPlayerNone() { var m = msgCfg(); return m.admin() == null || m.admin().statsPlayerNone() == null ? "" : m.admin().statsPlayerNone(); }

    public String dbFile() {
        var d = cfg().database();
        return d == null || d.file() == null ? "gratitude.db" : d.file();
    }

    public String dbType() {
        var d = cfg().database();
        String t = d == null || d.type() == null ? "SQLITE" : d.type();
        return t.trim().equalsIgnoreCase("MYSQL") ? "MYSQL" : "SQLITE";
    }

    public String dbHost() { var d = cfg().database(); return d == null || d.host() == null ? "localhost" : d.host(); }
    public int dbPort() { var d = cfg().database(); return d == null || d.port() == 0 ? 3306 : d.port(); }
    public String dbName() { var d = cfg().database(); return d == null || d.name() == null ? "gratitude" : d.name(); }
    public String dbUser() { var d = cfg().database(); return d == null || d.user() == null ? "root" : d.user(); }
    public String dbPassword() { var d = cfg().database(); return d == null || d.password() == null ? "" : d.password(); }
    public boolean dbUseSsl() { var d = cfg().database(); return d != null && d.useSsl(); }
    public String dbParams() { var d = cfg().database(); return d == null || d.params() == null ? "" : d.params(); }

    public int poolMaxSize() { var d = cfg().database(); int v = d == null ? 10 : d.poolMaxSize(); return v <= 0 ? 10 : v; }
    public int poolMinIdle() { var d = cfg().database(); int v = d == null ? 2 : d.poolMinIdle(); return Math.max(0, v); }
    public long connTimeoutMs() { var d = cfg().database(); long v = d == null ? 10000L : d.connectionTimeoutMs(); return v <= 0 ? 10000L : v; }
    public long leakDetectMs() { var d = cfg().database(); long v = d == null ? 0L : d.leakDetectionThresholdMs(); return Math.max(0L, v); }

    public void reload() {
        try { configC.reload(); } catch (Throwable ignored) {}
        try { messagesC.reload(); } catch (Throwable ignored) {}
    }

    public record SoundSpec(Sound sound, float volume, float pitch) {}
}
