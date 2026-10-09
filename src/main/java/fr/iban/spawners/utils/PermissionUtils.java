package fr.iban.spawners.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PermissionUtils {

    private static LuckPerms luckapi = LuckPermsProvider.get();

    public static void increaseSpawnerLimit(Player player, int amount) {
        CompletableFuture<User> userFuture = luckapi.getUserManager().loadUser(player.getUniqueId());
        userFuture.thenAcceptAsync(user -> {
            int spawners = 1;
            for (PermissionNode permNode : user.getNodes(NodeType.PERMISSION)) {
                if (permNode.getKey().startsWith("spawners.amount.")) {
                    spawners = Integer.parseInt(StringUtils.removeStart(permNode.getKey(), "spawners.amount."));
                    user.data().remove(permNode);
                }
            }
            Bukkit.getLogger().log(Level.INFO, "Limite de spawners de " + player.getName() + " " + spawners + " -> " + (spawners + amount));
            user.data().add(Node.builder("spawners.amount." + (spawners + amount)).build());
            luckapi.getUserManager().saveUser(user);
        });
    }

    public static void decreaseSpawnerLimit(Player player, int amount) {
        CompletableFuture<User> userFuture = luckapi.getUserManager().loadUser(player.getUniqueId());
        userFuture.thenAcceptAsync(user -> {
            int spawners = 1;
            for (PermissionNode permNode : user.getNodes(NodeType.PERMISSION)) {
                if (permNode.getKey().startsWith("spawners.amount.")) {
                    spawners = Integer.parseInt(StringUtils.removeStart(permNode.getKey(), "spawners.amount."));
                    user.data().remove(permNode);
                }
            }
            Bukkit.getLogger().log(Level.INFO, "Limite de spawners de " + player.getName() + " " + spawners + " -> " + (spawners - amount));
            user.data().add(Node.builder("spawners.amount." + (spawners - amount)).build());
            luckapi.getUserManager().saveUser(user);
        });
    }

    public static int getSpawnerLimit(Player player) {
        String permissionPrefix = "spawners.amount.";
        int maxSpawners = 10;

        for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions()) {
            String permission = attachmentInfo.getPermission();
            if (permission.startsWith(permissionPrefix)) {
                int permMaxSpawners = Integer.parseInt(permission.substring(permission.lastIndexOf(".") + 1));
                if (permMaxSpawners > maxSpawners) {
                    maxSpawners = permMaxSpawners;
                }
            }
        }
        return maxSpawners;
    }

    public static int getSpawnerLimitFromLP(UUID uuid) {
        CompletableFuture<User> userFuture = luckapi.getUserManager().loadUser(uuid);
        return userFuture.thenApplyAsync(user -> {
            int spawners = 1;
            for (PermissionNode permNode : user.getNodes(NodeType.PERMISSION)) {
                if (permNode.getKey().startsWith("spawners.amount.")) {
                    spawners = Integer.parseInt(StringUtils.removeStart(permNode.getKey(), "spawners.amount."));
                }
            }
            return spawners;
        }).join();
    }
}
