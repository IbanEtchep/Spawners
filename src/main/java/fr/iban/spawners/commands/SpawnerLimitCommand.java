package fr.iban.spawners.commands;

import fr.iban.spawners.utils.PermissionUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("spawnerlimit")
public class SpawnerLimitCommand {

    @Subcommand("show")
    @CommandPermission("spawners.admin")
    public void showLimit(BukkitCommandActor sender, Player target) {
        int limit = PermissionUtils.getSpawnerLimit(target);
        sender.reply("§aLa limite de spawners de " + target.getName() + " est de " + limit + ".");
    }

    @Subcommand("increase")
    @CommandPermission("spawners.admin")
    public void increaseLimit(BukkitCommandActor sender, Player target, int amount) {
        PermissionUtils.increaseSpawnerLimit(target, amount);
        sender.reply("§aLa limite de spawners de " + target.getName() + " a été augmentée de " + amount + ".");
    }

    @Subcommand("decrease")
    @CommandPermission("spawners.admin")
    public void decreaseLimit(BukkitCommandActor sender, Player target, int amount) {
        PermissionUtils.decreaseSpawnerLimit(target, amount);
        sender.reply("§aLa limite de spawners de " + target.getName() + " a été diminuée de " + amount + ".");
    }

}
