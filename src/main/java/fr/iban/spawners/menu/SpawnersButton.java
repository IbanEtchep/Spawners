package fr.iban.spawners.menu;

import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.common.teleport.SLocation;
import fr.iban.spawners.Spawner;
import fr.iban.spawners.SpawnerManager;
import fr.iban.spawners.SpawnersPlugin;
import fr.iban.spawners.enums.PlacementFilter;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.engine.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SpawnersButton extends PaginateButton {

    private final SpawnersPlugin plugin;
    private final SpawnerManager spawnerManager;

    public SpawnersButton(Plugin plugin) {
        this.plugin = (SpawnersPlugin) plugin;
        this.spawnerManager = this.plugin.getSpawnerManager();
    }

    @Override
    public void onRender(Player player, InventoryEngine inventory) {
        SpawnerMenuData spawnerMenuData = plugin.getSpawnerMenuData(player);
        List<Spawner> spawners = spawnerMenuData.getSpawners();

        List<Spawner> filteredSpawners = new ArrayList<>(spawners);
        PlacementFilter filter = spawnerMenuData.getFilter();

        if (filter == PlacementFilter.PLACED) {
            filteredSpawners.removeIf(spawner -> spawner.getSLocation() == null);
        } else if (filter == PlacementFilter.UNPLACED) {
            filteredSpawners.removeIf(spawner -> spawner.getSLocation() != null);
        }

        Pagination<Spawner> pagination = new Pagination<>();
        List<Spawner> paginatedSpawners = pagination.paginate(filteredSpawners, this.slots.size(), inventory.getPage());

        for (int i = 0; i < paginatedSpawners.size(); i++) {
            int slot = this.slots.get(i);
            Spawner spawner = paginatedSpawners.get(i);

            inventory.addItem(slot, getSpawnerItem(spawner, player)).setLeftClick(event -> {
                if (spawner.getSLocation() != null && spawner.getSLocation().getServer().equalsIgnoreCase(SpawnersPlugin.getInstance().getServerName()) && !player.hasPermission("spawners.admin"))
                    return;

                if (player.getInventory().firstEmpty() != -1) {
                    ItemStack item = spawnerManager.getSpawnerItem(spawner.getType());
                    player.getInventory().addItem(item);
                    spawnerManager.removeSpawner(spawner);
                    spawners.remove(spawner);
                    plugin.openSpawnerMenu(player, spawnerMenuData);
                } else {
                    player.sendMessage("§cVotre inventaire est plein !");
                }
            });
        }
    }

    @Override
    public boolean updateOnClick() {
        return true;
    }

    @Override
    public int getPaginationSize(Player player) {
        return plugin.getSpawnerMenuData(player).getSpawners().size();
    }

    private ItemStack getSpawnerItem(Spawner spawner, Player player) {
        SLocation location = spawner.getSLocation();
        ItemBuilder itemBuilder = new ItemBuilder(plugin.getSpawnerManager().getSpawnerItem(spawner.getType()));

        if (location == null) {
            itemBuilder.addLore("§8Position : §7non placé")
                    .addLore("§7(clic pour le récupérer)");
        } else {
            itemBuilder.addLore("§fServeur : §a§l" + location.getServer())
                    .addLore("§fMonde : §a§l" + location.getWorld())
                    .addLore("§fCoordonées : X: §a§l" + location.getX() + " §fY: §a§l" + location.getY() + " §fZ: §a§l" + location.getZ());

            if (player.hasPermission("spawners.admin")) {
                itemBuilder.addLore("§cClic pour supprimer");
            }
        }

        return itemBuilder.build();
    }
}
