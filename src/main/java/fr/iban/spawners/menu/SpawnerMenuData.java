package fr.iban.spawners.menu;

import fr.iban.spawners.Spawner;
import fr.iban.spawners.enums.PlacementFilter;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class SpawnerMenuData {

    private final OfflinePlayer target;
    private final List<Spawner> spawners;
    private PlacementFilter filter;
    private final int spawnerLimit;

    public SpawnerMenuData(OfflinePlayer target, List<Spawner> spawners, int spawnerLimit) {
        this.target = target;
        this.spawners = spawners;
        this.filter = PlacementFilter.ALL;
        this.spawnerLimit = spawnerLimit;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public PlacementFilter getFilter() {
        return filter;
    }

    public void setFilter(PlacementFilter filter) {
        this.filter = filter;
    }

    public List<Spawner> getSpawners() {
        return spawners;
    }

    public int getSpawnerLimit() {
        return spawnerLimit;
    }
}
