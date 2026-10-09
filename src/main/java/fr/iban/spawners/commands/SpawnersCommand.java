package fr.iban.spawners.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.model.MSPlayer;
import fr.iban.spawners.SpawnerManager;
import fr.iban.spawners.SpawnersPlugin;
import fr.iban.spawners.menu.SpawnerMenuData;
import fr.iban.spawners.menu.SpawnersMenu;
import fr.iban.spawners.utils.PermissionUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.UUID;

@Command("spawners")
public class SpawnersCommand {

    private final SpawnersPlugin plugin;
    private final SpawnerManager spawnerManager;

    public SpawnersCommand(SpawnersPlugin plugin) {
        this.plugin = plugin;
        this.spawnerManager = plugin.getSpawnerManager();
    }

    @CommandPlaceholder
    public void spawners(Player player) {
        spawnersShow(player, player);
    }

    @Subcommand("show")
    @CommandPermission("spawners.admin")
    public void spawnersShow(Player sender, OfflinePlayer target) {
        spawnerManager.getSpawnersAsync(target.getUniqueId()).thenAccept(spawners -> {
            Bukkit.getScheduler().runTask(SpawnersPlugin.getInstance(),
                    () -> new SpawnersMenu(sender, spawners, spawnerManager, target).open());
        });
    }

    @Subcommand("give")
    @CommandPermission("spawners.admin")
    public void give(BukkitCommandActor sender, MSPlayer target, EntityType type, @Range(min = 1) int amount) {
        for (int i = 0; i < amount; i++) {
            spawnerManager.saveSpawnerAsync(target.getUniqueId(), type);
        }
        sender.reply("§aVous avez donné " + amount + " spawner(s) à " + target.getName());

        CoreBukkitPlugin.getInstance().getPlayerManager().sendMessageIfOnline(
                target.getUniqueId(),
                MiniMessage.miniMessage().deserialize("<green>Vous avez reçu " + amount + " spawner(s) de " + type.toString())
        );
    }

    @Subcommand("givepickaxe")
    @CommandPermission("spawners.admin")
    public void givePickaxe(BukkitCommandActor sender, @Range(min = 1) int durability, @Default("me") Player target) {
        if (target.getInventory().firstEmpty() != -1) {
            target.getInventory().addItem(spawnerManager.getPioche(durability));
        } else {
            target.getWorld().dropItem(target.getLocation(), spawnerManager.getPioche(durability));
        }
    }

    @Subcommand("menu")
    @CommandPermission("spawners.admin")
    public void menu(Player player) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var spawners = spawnerManager.getSpawners(uuid);
            int spawnerLimit = PermissionUtils.getSpawnerLimitFromLP(uuid);
            spawnerManager.cacheSpawnerCount(uuid).join();

            SpawnerMenuData data = new SpawnerMenuData(player, spawners, spawnerLimit);
            Bukkit.getScheduler().runTask(plugin, () -> plugin.openSpawnerMenu(player, data));
        });
    }

}
