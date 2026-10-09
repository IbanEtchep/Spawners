package fr.iban.spawners.menu;

import fr.iban.spawners.SpawnerManager;
import fr.iban.spawners.SpawnersPlugin;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.utils.Placeholders;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SpawnerLimitInfoButton extends Button {

    private final SpawnersPlugin plugin;
    private final SpawnerManager spawnerManager;

    public SpawnerLimitInfoButton(Plugin plugin) {
        this.plugin = (SpawnersPlugin) plugin;
        this.spawnerManager = this.plugin.getSpawnerManager();
    }

    @Override
    public @Nullable ItemStack getCustomItemStack(@NotNull Player player, boolean useCache, @NotNull Placeholders placeholders) {
        SpawnerMenuData spawnerMenuData = plugin.getSpawnerMenuData(player);
        placeholders.register("placed_count", spawnerManager.getSpawnerCount(spawnerMenuData.getTarget().getUniqueId()) + "");
        placeholders.register("placed_limit", spawnerMenuData.getSpawnerLimit() + "");
        return this.getItemStack().build(player, false, placeholders);
    }
}
