package fr.iban.spawners.menu;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.common.teleport.SLocation;
import fr.iban.spawners.Spawner;
import fr.iban.spawners.SpawnerManager;
import fr.iban.spawners.SpawnersPlugin;
import fr.iban.spawners.enums.PlacementFilter;
import fr.iban.spawners.utils.PermissionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnersMenu extends PaginatedMenu {

    private final List<Spawner> spawners;
    private final Map<Integer, Spawner> spawnerAtSlot = new HashMap<>();

    private final SpawnerManager spawnerManager;
    private final OfflinePlayer target;

    private PlacementFilter filter = PlacementFilter.ALL;

    public SpawnersMenu(Player player, List<Spawner> spawners, SpawnerManager spawnerManager, OfflinePlayer target) {
        super(player);
        this.spawners = spawners;
        this.spawnerManager = spawnerManager;
        this.target = target;
    }

    @Override
    public String getMenuName() {
        if (target == player) {
            int amount = spawnerManager.getSpawnerCount(player.getUniqueId());
            int limit = PermissionUtils.getSpawnerLimit(player);

            return "§5§lSpawners §f- §8(" + amount + " / " + limit + ")";
        }

        return "§5Spawners de " + target.getName();
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public int getElementAmount() {
        return spawners.size();
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;

        if (e.getClickedInventory() == e.getView().getTopInventory()) {
            if (e.getCurrentItem().getType().toString().toUpperCase().endsWith("GLASS_PANE")) {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "précédent")) {
                    if (page == 0) {
                        p.sendMessage("§cVous êtes déjà à la première page.");
                    } else {
                        page = page - 1;
                        super.open();
                    }
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "suivant")) {
                    if ((index + 1) <= spawners.size()) {
                        page = page + 1;
                        super.open();
                    } else {
                        p.sendMessage("§cVous êtes déjà à la dernière page.");
                    }
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.DARK_RED + "fermer")) {
                    p.closeInventory();
                }
            }

            if (e.getCurrentItem().getType() == Material.HOPPER) {
                changeFilter();
                super.open();
            }

            Spawner clickedSpawner = spawnerAtSlot.get(e.getSlot());
            if (clickedSpawner == null) return;

            boolean isInDifferentServer = clickedSpawner.getSLocation() != null && !clickedSpawner.getSLocation().getServer().equalsIgnoreCase(SpawnersPlugin.getInstance().getServerName());

            if (isInDifferentServer && !player.hasPermission("spawners.admin")) return;

            if (e.getClick() == ClickType.LEFT) {
                if (player.getInventory().firstEmpty() != -1) {
                    ItemStack item = spawnerManager.getSpawnerItem(clickedSpawner.getType());
                    player.getInventory().addItem(item);
                    spawnerManager.removeSpawner(clickedSpawner);
                    spawners.remove(clickedSpawner);
                    super.open();
                } else {
                    player.sendMessage("§cVotre inventaire est plein !");
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        ItemStack infoItem = getInfoItem();

        if (infoItem != null) {
            inventory.setItem(45, infoItem);
        }


        List<Spawner> filteredSpawners = new ArrayList<>(spawners);

        if (filter == PlacementFilter.PLACED) {
            filteredSpawners.removeIf(spawner -> spawner.getSLocation() == null);
            inventory.setItem(53, new ItemBuilder(Material.HOPPER).setName("§f§lFiltre")
                    .addLore("§7➜ Tous les spawners")
                    .addLore("§7➜ §aSpawners placés")
                    .addLore("§7➜ Spawners non placés")
                    .build());
        } else if (filter == PlacementFilter.UNPLACED) {
            filteredSpawners.removeIf(spawner -> spawner.getSLocation() != null);
            inventory.setItem(53, new ItemBuilder(Material.HOPPER).setName("§f§lFiltre")
                    .addLore("§7➜ Tous les spawners")
                    .addLore("§7➜ Spawners placés")
                    .addLore("§7➜ §aSpawners non placés")
                    .build());
        } else {
            inventory.setItem(53, new ItemBuilder(Material.HOPPER).setName("§f§lFiltre")
                    .addLore("§7➜ §aTous les spawners")
                    .addLore("§7➜ Spawners placés")
                    .addLore("§7➜ Spawners non placés")
                    .build());
        }

        if (!filteredSpawners.isEmpty()) {
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if (index >= filteredSpawners.size()) break;
                if (filteredSpawners.get(index) != null) {
                    final int slot = inventory.firstEmpty();
                    Spawner spawner = filteredSpawners.get(index);
                    spawnerAtSlot.put(slot, spawner);
                    inventory.setItem(slot, formatItem(spawner));
                }
            }
        }
    }

    private ItemStack formatItem(Spawner spawner) {
        SLocation location = spawner.getSLocation();
        ItemBuilder itemBuilder = new ItemBuilder(spawnerManager.getSpawnerItem(spawner.getType()));
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

    private ItemStack getInfoItem() {
        if (target == player) {
            int amount = spawnerManager.getSpawnerCount(player.getUniqueId());
            int limit = PermissionUtils.getSpawnerLimit(player);

            return new ItemBuilder(Material.PAPER)
                    .setName("§f§lInformations")
                    .addLore("§7Vous pouvez placer un nombre limité de spawners")
                    .addLore("§7mais en posséder autant que vous le souhaitez.")
                    .addLore("§fActuellement placé §a" + amount + "§7/§a" + limit + "§7.")
                    .build();
        }

        return null;
    }


    private void changeFilter() {
        if (filter == PlacementFilter.ALL) {
            filter = PlacementFilter.PLACED;
        } else if (filter == PlacementFilter.PLACED) {
            filter = PlacementFilter.UNPLACED;
        } else {
            filter = PlacementFilter.ALL;
        }
    }
}
