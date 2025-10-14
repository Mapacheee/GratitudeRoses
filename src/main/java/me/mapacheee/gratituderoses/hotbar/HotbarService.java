package me.mapacheee.gratituderoses.hotbar;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.gratituderoses.shared.ConfigService;
import me.mapacheee.gratituderoses.shared.TextService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/* This class if to build and give the configured hotbar item to players and manage its slot */

@Service
public class HotbarService {
    private final ConfigService config;
    private final TextService text;
    private final Plugin plugin;
    private final NamespacedKey roseKey;

    @Inject
    public HotbarService(ConfigService config, TextService text, Plugin plugin) {
        this.config = config;
        this.text = text;
        this.plugin = plugin;
        this.roseKey = new NamespacedKey(plugin, "rose");
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
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(roseKey, PersistentDataType.BYTE, (byte) 1);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public boolean isPluginRose(ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        Byte v = meta.getPersistentDataContainer().get(roseKey, PersistentDataType.BYTE);
        return v != null && v == (byte) 1;
    }

    public int roseSlot() { return config.hotbarSlot(); }

    public void giveTo(Player player) {
        if (!config.hotbarEnabled()) return;
        int slot = config.hotbarSlot();
        ItemStack item = buildItem();
        player.getInventory().setItem(slot, item);
        player.updateInventory();
    }

    public void ensurePosition(Player player) {
        if (!config.hotbarEnabled()) return;
        PlayerInventory inv = player.getInventory();
        int slot = roseSlot();
        ItemStack at = inv.getItem(slot);
        if (!isPluginRose(at)) {
            if (at != null && at.getType() != Material.AIR) {
                int empty = inv.firstEmpty();
                if (empty >= 0 && empty != slot) {
                    inv.setItem(empty, at);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), at);
                }
            }
            inv.setItem(slot, buildItem());
        }
        for (int i = 0; i < 9; i++) {
            if (i == slot) continue;
            ItemStack other = inv.getItem(i);
            if (isPluginRose(other)) {
                inv.setItem(i, null);
            }
        }
        player.updateInventory();
    }

    public boolean isEnabledWorld(String worldName) {
        return config.enabledWorlds().isEmpty() || config.enabledWorlds().contains(worldName);
    }
}
