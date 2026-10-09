package fr.iban.spawners.menu;

import fr.iban.spawners.SpawnersPlugin;
import fr.iban.spawners.enums.PlacementFilter;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerFilterButton extends Button {

    private final SpawnersPlugin plugin;

    public SpawnerFilterButton(Plugin plugin) {
        this.plugin = (SpawnersPlugin) plugin;
    }

    @Override
    public @Nullable ItemStack getCustomItemStack(@NotNull Player player, boolean useCache, @NotNull Placeholders placeholders) {
        SpawnerMenuData spawnerMenuData = plugin.getSpawnerMenuData(player);
        ItemStack item = this.getItemStack().build(player, false);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null) return item;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            for (PlacementFilter value : PlacementFilter.values()) {
                String tag = "[" + value + "]";
                if (line.contains(tag)) {
                    line = (spawnerMenuData.getFilter() == value) ? line.replace(tag, "§a") : line.replace(tag, "");
                    break;
                }
            }
            lore.set(i, line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryEngine inventory, int slot, Placeholders placeholders) {
        SpawnerMenuData spawnerMenuData = plugin.getSpawnerMenuData(player);
        changeFilter(spawnerMenuData);
        plugin.openSpawnerMenu(player, spawnerMenuData);
    }

    private void changeFilter(SpawnerMenuData spawnerMenuData) {
        PlacementFilter filter = spawnerMenuData.getFilter();

        if (filter == PlacementFilter.ALL) {
            spawnerMenuData.setFilter(PlacementFilter.PLACED);
        } else if (filter == PlacementFilter.PLACED) {
            spawnerMenuData.setFilter(PlacementFilter.UNPLACED);
        } else {
            spawnerMenuData.setFilter(PlacementFilter.ALL);
        }
    }
}
