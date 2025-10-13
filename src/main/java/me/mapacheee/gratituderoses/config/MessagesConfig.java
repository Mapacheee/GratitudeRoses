package me.mapacheee.gratituderoses.config;

/* This class if to define the typed Configurate-backed messages configuration */
import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
@Configurate("messages")
public record MessagesConfig(
        String prefix,
        Player player,
        Admin admin
) {
    @ConfigSerializable
    public record Player(
            @Setting("thanked-chat") String thankedChat,
            @Setting("title-main") String titleMain,
            @Setting("title-sub") String titleSub,
            String cooldown,
            @Setting("wrong-world") String wrongWorld
    ) {}

    @ConfigSerializable
    public record Admin(
            String reloaded,
            @Setting("stats-global") String statsGlobal,
            @Setting("stats-player") String statsPlayer,
            @Setting("stats-player-none") String statsPlayerNone
    ) {}
}

