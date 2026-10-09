package fr.iban.spawners;

import fr.iban.common.teleport.SLocation;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class Spawner {

    private final int id;
    private final UUID owner;
    private final EntityType type;
    private SLocation slocation;

    public Spawner(int id, UUID owner, EntityType type, SLocation slocation) {
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.slocation = slocation;
    }

    public int getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public EntityType getType() {
        return type;
    }

    public SLocation getSLocation() {
        return slocation;
    }

    public void setSLocation(SLocation slocation) {
        this.slocation = slocation;
    }

}
