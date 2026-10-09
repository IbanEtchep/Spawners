package fr.iban.spawners;

import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.teleport.SLocation;
import fr.iban.spawners.storage.Storage;
import fr.iban.spawners.utils.TextDisplayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SpawnerManager {

    private final SpawnersPlugin plugin;
    private final Storage storage;

    private final Map<UUID, Integer> spawnerCounts = new HashMap<>();

    public static final NamespacedKey piocheKey = new NamespacedKey(SpawnersPlugin.getInstance(), "pioche_spawners");
    public static final NamespacedKey spawnerTypeKey = new NamespacedKey(SpawnersPlugin.getInstance(), "spawner_type");
    public static final NamespacedKey stackSizeKey = new NamespacedKey(SpawnersPlugin.getInstance(), "spawner_stack_size");
    public static NamespacedKey ownerKey = null;
    private final boolean areStackableSpawnersEnabled;


    public SpawnerManager(SpawnersPlugin plugin) {
        this.plugin = plugin;
        this.storage = new Storage();
        ownerKey = new NamespacedKey(plugin, plugin.getConfig().getString("spawner-owner-key", "owner"));
        areStackableSpawnersEnabled = plugin.getConfig().getBoolean("stackable-spawners.enabled", false);
    }

    public List<Spawner> getSpawners(UUID uuid) {
        return storage.getSpawners(uuid);
    }

    public CompletableFuture<List<Spawner>> getSpawnersAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> getSpawners(uuid));
    }

    public void saveSpawnerAsync(UUID uuid, EntityType type) {
        CompletableFuture.runAsync(() -> storage.addSpawner(uuid, type));
    }

    public void saveSpawnerAsync(UUID uuid, EntityType type, SLocation sloc) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storage.addSpawner(uuid, type, sloc);
            spawnerCounts.put(uuid, spawnerCounts.getOrDefault(uuid, 0) + 1);
        });
    }

    public void saveSpawnerLocation(Spawner spawner) {
        CompletableFuture.runAsync(() -> {
            storage.setSpawnerLocation(spawner.getId(), spawner.getSLocation());

            if (spawner.getSLocation() == null) {
                decreaseSpawnerCount(spawner.getOwner());
            }
        });
    }

    public void removeSpawner(Spawner spawner) {
        SLocation location = spawner.getSLocation();

        if (location != null) {
            Block block = SLocationUtils.getLocation(location).getBlock();
            int stackSize = getStackSize(block);

            if (stackSize <= 1) {
                block.setType(Material.AIR);
            } else {
                setStackSize((CreatureSpawner) block.getState(), stackSize - 1);
            }
        }

        storage.deleteSpawner(spawner.getId());
        decreaseSpawnerCount(spawner.getOwner());
    }

    public CompletableFuture<List<Spawner>> getSpawnersAtAsync(Location location) {
        return CompletableFuture.supplyAsync(() -> storage.getSpawnersAt(location));
    }

    public String getNameFromDisplayName(String displayname) {
        String[] splited = displayname.split("§5");
        if (splited.length == 2) {
            return splited[1].toUpperCase();
        }
        return null;
    }

    public EntityType getTypeByName(String name) {
        if (name != null) {
            for (EntityType et : EntityType.values()) {
                if (et.toString().equalsIgnoreCase(name))
                    return et;
            }
        }
        return EntityType.PIG;
    }

    public EntityType getTypeFromPersistentData(ItemStack it) {
        ItemMeta itm = it.getItemMeta();
        String type = itm.getPersistentDataContainer().get(spawnerTypeKey, PersistentDataType.STRING);
        for (EntityType et : EntityType.values()) {
            if (et.toString().equalsIgnoreCase(type))
                return et;
        }
        return getTypeByName(getNameFromDisplayName(itm.getDisplayName()));
    }

    public ItemStack getSpawnerItem(EntityType type) {
        ItemStack item = new ItemBuilder(Material.SPAWNER)
                .setName("§dSpawner à §5" + type.toString().toLowerCase())
                .build();
        ItemMeta itm = item.getItemMeta();
        itm.getPersistentDataContainer().set(SpawnerManager.spawnerTypeKey, PersistentDataType.STRING, type.toString());
        item.setItemMeta(itm);

        return item;
    }


    public ItemStack getPioche(int durability) {
        ItemStack item = new ItemBuilder(Material.GOLDEN_PICKAXE)
                .setName("§5§lPioche à spawner")
                .addLore("§dPermet de récupérer un spawner naturel.")
                .addLore("§dUtilisations restantes : §e§l" + durability)
                .build();
        ItemMeta itm = item.getItemMeta();
        itm.setUnbreakable(true);
        item.setItemMeta(itm);
        return item;
    }

    public boolean isSpawnerPickaxe(ItemStack item) {
        return item.getType() == Material.GOLDEN_PICKAXE && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains("§dPermet de récupérer un spawner naturel.");
    }

    public int getDurability(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return 0;

        for (String string : item.getItemMeta().getLore()) {
            if (string.contains("§dUtilisations restantes : §e§l")) {
                return Integer.parseInt(string.split("§dUtilisations restantes : §e§l")[1]);
            }
        }

        return 0;
    }

    public boolean areStackableSpawnersEnabled() {
        return areStackableSpawnersEnabled;
    }

    public UUID getSpawnerOwner(CreatureSpawner creatureSpawner) {
        if (creatureSpawner.getPersistentDataContainer().has(SpawnerManager.ownerKey, PersistentDataType.STRING)) {
            return UUID.fromString(Objects.requireNonNull(creatureSpawner.getPersistentDataContainer().get(SpawnerManager.ownerKey, PersistentDataType.STRING)));
        }

        return null;
    }

    public int getStackSize(CreatureSpawner creatureSpawner) {
        if (creatureSpawner.getPersistentDataContainer().has(SpawnerManager.stackSizeKey, PersistentDataType.INTEGER)) {
            return creatureSpawner.getPersistentDataContainer().get(SpawnerManager.stackSizeKey, PersistentDataType.INTEGER);
        }

        return 1;
    }

    public int getStackSize(Block block) {
        if (block.getState() instanceof CreatureSpawner creatureSpawner) {
            return getStackSize(creatureSpawner);
        }

        return 1;
    }

    public void setStackSize(CreatureSpawner spawner, int stackSize) {
        spawner.getPersistentDataContainer().set(SpawnerManager.stackSizeKey, PersistentDataType.INTEGER, stackSize);

        int defaultMinDelay = 200;
        int defaultMaxDelay = 800;

        spawner.setMinSpawnDelay(defaultMinDelay / stackSize);
        spawner.setMaxSpawnDelay(defaultMaxDelay / stackSize);

        spawner.update();
        TextDisplayUtils.updateStackSizeDisplay(spawner.getLocation(), stackSize);
    }

    public int getSpawnerCount(UUID uuid) {
        return spawnerCounts.getOrDefault(uuid, 0);
    }

    private void decreaseSpawnerCount(UUID uuid) {
        int count = spawnerCounts.getOrDefault(uuid, 0);

        if (count > 0) {
            spawnerCounts.put(uuid, count - 1);
        }
    }

    public CompletableFuture<Void> cacheSpawnerCount(UUID uuid) {
        return CompletableFuture.runAsync(() -> spawnerCounts.put(uuid, storage.getSpawnerCount(uuid)));
    }
}
