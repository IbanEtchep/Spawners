package fr.iban.spawners.listeners;

import fr.iban.spawners.SpawnerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinQuitListeners implements Listener {

    private final SpawnerManager manager;

    public JoinQuitListeners(SpawnerManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.cacheSpawnerCount(e.getPlayer().getUniqueId());
    }

}
