package fr.iban.spawners.listeners;

import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.spawners.Spawner;
import fr.iban.spawners.SpawnerManager;
import fr.iban.spawners.SpawnersPlugin;
import fr.iban.spawners.utils.PermissionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

public class PlaceBreakListeners implements Listener {

    private static final String QUICKSHOP_FAKE_BREAK_EVENT =
            "quickshop.util.PermissionChecker$FakeBlockBreakEvent";

    private final SpawnersPlugin plugin;
    private final SpawnerManager manager;


    public PlaceBreakListeners(SpawnersPlugin spawners) {
        this.plugin = spawners;
        this.manager = spawners.getSpawnerManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        if (e.getClass().getName().endsWith(QUICKSHOP_FAKE_BREAK_EVENT)) return;

        if (!e.isCancelled()) {
            Block block = e.getBlock();
            Player player = e.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (block.getState() instanceof CreatureSpawner creatureSpawner) {
                UUID spawnerOwner = manager.getSpawnerOwner(creatureSpawner);
                e.setExpToDrop(0);

                if (spawnerOwner != null) {
                    if (Objects.equals(spawnerOwner, player.getUniqueId())) {
                        player.sendMessage("§aVous venez de récupérer votre spawner. (/spawners)");
                    } else {
                        player.sendMessage("§cCe spawner appartient à un autre joueur. Il lui a été renvoyé.");
                    }

                    int stackSize = manager.getStackSize(creatureSpawner);

                    if (stackSize > 1) {
                        manager.setStackSize(creatureSpawner, stackSize - 1);
                        e.setCancelled(true);
                    }

                    //Retirer un spawner
                    manager.getSpawnersAtAsync(block.getLocation()).thenAccept(spawners -> {
                        Spawner spawner = spawners.get(0);
                        spawner.setSLocation(null);
                        manager.saveSpawnerLocation(spawner);
                    });
                } else {
                    if (manager.isSpawnerPickaxe(itemInHand)) {
                        int dura = manager.getDurability(itemInHand);
                        manager.saveSpawnerAsync(player.getUniqueId(), creatureSpawner.getSpawnedType());
                        player.sendMessage("§aVous venez de récupérer un nouveau spawner. (/spawners)");
                        if (dura <= 1) {
                            player.getInventory().removeItem(itemInHand);
                        } else {
                            player.getInventory().setItemInMainHand(plugin.getSpawnerManager().getPioche(dura - 1));
                        }
                    }
                }
            } else {
                if (manager.isSpawnerPickaxe(itemInHand)) {
                    e.setCancelled(true);
                    player.sendMessage("§cLa pioche à spawner ne peut récupérer que des spawners.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        ItemStack itemInHand = e.getItemInHand().clone();

        if (block.getState() instanceof CreatureSpawner spawner) {
            int spawnerCount = manager.getSpawnerCount(player.getUniqueId());
            int maxSpawnerCount = PermissionUtils.getSpawnerLimit(player);

            if (spawnerCount >= maxSpawnerCount) {
                e.setCancelled(true);
                player.sendMessage("§cVous avez atteint la limite de spawners que vous pouvez placer.");
                return;
            }

            new BukkitRunnable() {

                @Override
                public void run() {
                    if (!e.isCancelled()) {
                        EntityType type = manager.getTypeFromPersistentData(itemInHand);
                        spawner.setSpawnedType(type);
                        spawner.getPersistentDataContainer().set(SpawnerManager.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
                        spawner.update();
                        manager.saveSpawnerAsync(player.getUniqueId(), spawner.getSpawnedType(), SLocationUtils.getSLocation(spawner.getLocation(), plugin.getServerName()));
                    }
                }
            }.runTaskLater(SpawnersPlugin.getInstance(), 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        if (player.isSneaking() || !manager.areStackableSpawnersEnabled()) return;

        boolean isPlacingSpawner = itemInHand != null && itemInHand.getType() == Material.SPAWNER;

        if (action == Action.RIGHT_CLICK_BLOCK && block != null && isPlacingSpawner && block.getState() instanceof CreatureSpawner spawner) {
            UUID spawnerOwner = manager.getSpawnerOwner(spawner);
            EntityType placedSpawnerType = manager.getTypeFromPersistentData(itemInHand);
            EntityType clickedSpawnerType = spawner.getSpawnedType();

            if (placedSpawnerType != clickedSpawnerType) {
                return;
            }

            if (spawnerOwner != null && spawnerOwner.equals(player.getUniqueId())) {
                int stackSize = manager.getStackSize(spawner);
                int maxStackSize = plugin.getConfig().getInt("max-stack-size", 10);

                if (stackSize >= maxStackSize) {
                    player.sendMessage("§cVous ne pouvez pas empiler plus de " + maxStackSize + " spawners.");
                    return;
                }

                int spawnerCount = manager.getSpawnerCount(player.getUniqueId());
                int maxSpawnerCount = PermissionUtils.getSpawnerLimit(player);

                if (spawnerCount >= maxSpawnerCount) {
                    player.sendMessage("§cVous avez atteint la limite de spawners que vous pouvez placer.");
                    return;
                }

                event.setCancelled(true);
                ItemStack itemToBeRemoved = itemInHand.clone();
                itemToBeRemoved.setAmount(1);
                player.getInventory().removeItem(itemToBeRemoved);
                spawner.getPersistentDataContainer().set(SpawnerManager.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
                manager.setStackSize(spawner, stackSize + 1);
                spawner.update();
                manager.saveSpawnerAsync(player.getUniqueId(), clickedSpawnerType, SLocationUtils.getSLocation(spawner.getLocation()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> block.getType() == Material.SPAWNER);
    }
}
