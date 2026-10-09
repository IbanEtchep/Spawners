package fr.iban.spawners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.CoreCommandHandlerVisitor;
import fr.iban.spawners.commands.SpawnerLimitCommand;
import fr.iban.spawners.commands.SpawnersCommand;
import fr.iban.spawners.listeners.JoinQuitListeners;
import fr.iban.spawners.listeners.PlaceBreakListeners;
import fr.iban.spawners.menu.SpawnerFilterButton;
import fr.iban.spawners.menu.SpawnerLimitInfoButton;
import fr.iban.spawners.menu.SpawnerMenuData;
import fr.iban.spawners.menu.SpawnersButton;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.exceptions.InventoryException;
import fr.maxlego08.menu.api.loader.NoneLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class SpawnersPlugin extends JavaPlugin {

    private static SpawnersPlugin instance;
    private SpawnerManager spawnerManager;
    private Map<UUID, SpawnerMenuData> spawnerMenuData;
    private InventoryManager inventoryManager;
    private Inventory spawnerMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        spawnerManager = new SpawnerManager(this);

        spawnerMenuData = new HashMap<>();

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlaceBreakListeners(this), this);
        pm.registerEvents(new JoinQuitListeners(spawnerManager), this);

        registerCommands();
        loadZMenu();
    }

    private void registerCommands() {
        Lamp<BukkitCommandActor> lamp =  BukkitLamp.builder(this)
                .accept(new CoreCommandHandlerVisitor(CoreBukkitPlugin.getInstance()).visitor())
                .build();

        lamp.register(new SpawnersCommand(this));
        lamp.register(new SpawnerLimitCommand());
    }

    public static SpawnersPlugin getInstance() {
        return instance;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public boolean isMultipaperSupportEnabled() {
        return getConfig().getBoolean("multipaper-support.enabled", false);
    }

    public @Nullable String getMultipaperServerName() {
        return isMultipaperSupportEnabled() ? getConfig().getString("multipaper-support.server-name") : null;
    }

    public String getServerName() {
        String multipaperServer = getMultipaperServerName();
        return multipaperServer != null ? multipaperServer : CoreBukkitPlugin.getInstance().getServerName();
    }

    public SpawnerMenuData getSpawnerMenuData(Player player) {
        return spawnerMenuData.get(player.getUniqueId());
    }

    private void loadZMenu() {
        inventoryManager = getProvider(InventoryManager.class);
        ButtonManager buttonManager = getProvider(ButtonManager.class);

        if (inventoryManager == null || buttonManager == null) {
            getLogger().warning("ZMenu is not installed !");
            return;
        }

        buttonManager.register(new NoneLoader(this, SpawnersButton.class, "spawners_pagination"));
        buttonManager.register(new NoneLoader(this, SpawnerLimitInfoButton.class, "spawners_limit_info"));
        buttonManager.register(new NoneLoader(this, SpawnerFilterButton.class, "spawners_filter"));

        try {
            spawnerMenu = inventoryManager.loadInventoryOrSaveResource(this, "inventories/menu.yml");
        } catch (InventoryException exception) {
            getLogger().log(Level.SEVERE, "Failed to load inventory", exception);
        }
    }

    private <T> @Nullable T getProvider(Class<T> classProvider) {
        RegisteredServiceProvider<T> provider = Bukkit.getServer().getServicesManager().getRegistration(classProvider);
        return provider == null ? null : provider.getProvider();
    }

    public void openSpawnerMenu(@NotNull Player player, SpawnerMenuData data) {
        spawnerMenuData.put(player.getUniqueId(), data);
        this.inventoryManager.openInventory(player, spawnerMenu);
    }
}
