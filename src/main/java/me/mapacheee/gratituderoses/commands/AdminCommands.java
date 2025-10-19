package me.mapacheee.gratituderoses.commands;

//import com.google.inject.Inject;
import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.service.ReloadServiceManager;
import me.mapacheee.gratituderoses.shared.ConfigService;
import me.mapacheee.gratituderoses.shared.SchedulerService;
import me.mapacheee.gratituderoses.shared.TextService;
import me.mapacheee.gratituderoses.storage.StorageService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

/* This class if to implement admin commands: reload and stats using Winter Command module */

@CommandComponent
public class AdminCommands {
    private final ConfigService config;
    private final TextService text;
    private final StorageService storage;
    private final SchedulerService scheduler;
    private final ReloadServiceManager reloads;

    @Inject
    public AdminCommands(ConfigService config, TextService text, StorageService storage, SchedulerService scheduler, ReloadServiceManager reloads) {
        this.config = config;
        this.text = text;
        this.storage = storage;
        this.scheduler = scheduler;
        this.reloads = reloads;
    }

    @Command("gratituderoses reload")
    @Permission("gratituderoses.admin")
    public void reloadCmd(Source source) {
        reloads.reload();
        config.reload();
        var sender = source.source();
        if (sender instanceof org.bukkit.command.CommandSender cs) {
            cs.sendMessage(text.color(config.prefix() + config.msgReloaded()));
        }
    }

    @Command("gratituderoses stats")
    @Permission("gratituderoses.admin")
    public void statsGlobal(Source source) {
        var sender = (org.bukkit.command.CommandSender) source.source();
        scheduler.runAsync(() -> {
            try {
                long total = storage.totalLaunches();
                sender.sendMessage(text.color(config.prefix() + config.msgStatsGlobal().replace("{total}", String.valueOf(total))));
            } catch (Exception e) {
                sender.sendMessage(text.color(config.prefix() + "&cError al consultar estadísticas."));
            }
        });
    }

    @Command("gratituderoses stats <player>")
    @Permission("gratituderoses.admin")
    public void statsPlayer(Source source, @Argument("player") String playerName) {
        var sender = (org.bukkit.command.CommandSender) source.source();
        scheduler.runAsync(() -> {
            try {
                OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
                var uuid = off.getUniqueId();
                long playerTotal = storage.playerLaunches(uuid);
                if (playerTotal <= 0) {
                    sender.sendMessage(text.color(config.prefix() + config.msgStatsPlayerNone().replace("{player}", playerName)));
                    return;
                }
                Long first = storage.firstLaunch(uuid);
                String firstDate = first == null ? "-" : text.formatDate(first);
                String msg = config.msgStatsPlayer();
                msg = msg.replace("{player}", off.getName() == null ? playerName : off.getName());
                msg = msg.replace("{player_total}", String.valueOf(playerTotal));
                msg = msg.replace("{first_date}", firstDate);
                sender.sendMessage(text.color(config.prefix() + msg));
            } catch (Exception e) {
                sender.sendMessage(text.color(config.prefix() + "&cError al consultar estadísticas del jugador."));
            }
        });
    }
}
