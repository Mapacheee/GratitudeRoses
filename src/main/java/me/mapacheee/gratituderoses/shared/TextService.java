package me.mapacheee.gratituderoses.shared;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/* This class if to format colored messages, replace placeholders, and send chat/titles */

@Service
public class TextService {
    private final ConfigService config;
    private final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Inject
    public TextService(ConfigService config) {
        this.config = config;
    }

    public String color(String s) {
        if (s == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    private String applyPlaceholders(String template, Map<String, String> placeholders) {
        String out = template == null ? "" : template;
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return out;
    }

    public Component component(String template, Map<String, String> placeholders) {
        String withPh = applyPlaceholders(template, placeholders);
        if (withPh == null) withPh = "";
        Component base = parseToComponent(withPh);
        String prefix = config.prefix();
        if (prefix != null && !prefix.isEmpty()) {
            base = parseToComponent(prefix).append(base);
        }
        return base;
    }

    private Component parseToComponent(String s) {
        if (s == null || s.isEmpty()) return Component.empty();
        if (s.indexOf('<') >= 0 && s.indexOf('>') > s.indexOf('<')) {
            try { return mm.deserialize(s); } catch (Throwable ignored) {}
        }
        return legacy.deserialize(s);
    }

    public Component raw(String s) { return parseToComponent(s); }

    public void send(Player player, String template, Map<String, String> placeholders) {
        player.sendMessage(component(template, placeholders));
    }

    public void title(Player player, String main, String sub, int stayTicks) {
        Component title = parseToComponent(main);
        Component subtitle = parseToComponent(sub);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(10L * 50L),
                Duration.ofMillis((long) stayTicks * 50L),
                Duration.ofMillis(10L * 50L)
        );
        player.showTitle(Title.title(title, subtitle, times));
    }

    public String nowDate() { return sdf.get().format(new Date()); }
    public String formatDate(long epochMillis) { return sdf.get().format(new Date(epochMillis)); }
}
