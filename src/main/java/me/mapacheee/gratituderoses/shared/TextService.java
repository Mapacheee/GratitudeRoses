package me.mapacheee.gratituderoses.shared;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/* This class if to format colored messages, replace placeholders, and send chat/titles */

@Service
public class TextService {
    private final ConfigService config;
    private final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));

    @Inject
    public TextService(ConfigService config) {
        this.config = config;
    }

    public String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String format(String template, Map<String, String> placeholders) {
        String out = template == null ? "" : template;
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return color(config.prefix() + out);
    }

    public void send(Player player, String template, Map<String, String> placeholders) {
        player.sendMessage(format(template, placeholders));
    }

    public void title(Player player, String main, String sub, int stayTicks) {
        player.sendTitle(color(main), color(sub), 10, stayTicks, 10);
    }

    public String nowDate() {
        return sdf.get().format(new Date());
    }

    public String formatDate(long epochMillis) {
        return sdf.get().format(new Date(epochMillis));
    }
}
