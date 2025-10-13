package me.mapacheee.gratituderoses.hotbar;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.gratituderoses.shared.ConfigService;
import me.mapacheee.gratituderoses.shared.TextService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/* This class if to build and give the configured hotbar item to players and manage its slot */

@Service
public class HotbarService {
    private final ConfigService config;
    private final TextService text;

    @Inject
    public HotbarService(ConfigService config, TextService text) {
        this.config = config;
        this.text = text;
    }

    public ItemStack buildItem() {
        Material mat = config.triggerMaterials().stream().findFirst().orElse(Material.ROSE_BUSH);
        ItemStack stack = new ItemStack(mat, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(text.raw(config.hotbarName()));
            List<Component> lore = new ArrayList<>();
            for (String line : config.hotbarLore()) lore.add(text.raw(line));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public void giveTo(Player player) {
        if (!config.hotbarEnabled()) return;
        int slot = config.hotbarSlot();
        ItemStack item = buildItem();
        player.getInventory().setItem(slot, item);
        player.updateInventory();
    }

    public boolean isEnabledWorld(String worldName) {
        return config.enabledWorlds().isEmpty() || config.enabledWorlds().contains(worldName);
    }
}
