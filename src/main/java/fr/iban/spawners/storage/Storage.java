package fr.iban.spawners.storage;

import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.teleport.SLocation;
import fr.iban.spawners.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Storage {

    private DataSource ds = DbAccess.getDataSource();

    public Storage() {
        SQLTables.createTables();
    }


    public List<Spawner> getSpawners(UUID uuid) {
        List<Spawner> spawners = new ArrayList<>();
        String sql = "SELECT * FROM sc_spawners WHERE uuid LIKE ?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        EntityType type = EntityType.valueOf(rs.getString("type"));
                        String server = rs.getString("server");
                        SLocation sLocation = null;
                        if (server != null) {
                            String world = rs.getString("world");
                            double x = rs.getDouble("x");
                            double y = rs.getDouble("y");
                            double z = rs.getDouble("z");
                            sLocation = new SLocation(server, world, x, y, z, 0, 0);
                        }
                        spawners.add(new Spawner(id, uuid, type, sLocation));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spawners;
    }

    public List<Spawner> getSpawnersAt(Location location) {
        SLocation sloc = SLocationUtils.getSLocation(location);

        List<Spawner> spawners = new ArrayList<>();
        String sql = "SELECT * FROM sc_spawners WHERE server LIKE ? AND world = ? AND x = ? AND y = ? AND z = ?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, sloc.getServer());
                ps.setString(2, sloc.getWorld());
                ps.setDouble(3, location.getX());
                ps.setDouble(4, location.getY());
                ps.setDouble(5, location.getZ());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        EntityType type = EntityType.valueOf(rs.getString("type"));
                        String server = rs.getString("server");
                        spawners.add(new Spawner(id, uuid, type, sloc));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spawners;
    }

    public void addSpawner(UUID uuid, EntityType type, SLocation sloc) {
        String sql = "INSERT INTO sc_spawners (uuid, type, server, world, x, y, z) VALUES(?, ?, ?, ? ,? ,?, ?);";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, type.toString());
                ps.setString(3, sloc.getServer());
                ps.setString(4, sloc.getWorld());
                ps.setDouble(5, sloc.getX());
                ps.setDouble(6, sloc.getY());
                ps.setDouble(7, sloc.getZ());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSpawner(UUID uuid, EntityType type) {
        String sql = "INSERT INTO sc_spawners (uuid, type) VALUES(?, ?);";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, type.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setSpawnerLocation(int id, SLocation sloc) {
        //server, world, x, y, z, pitch, yaw
        String sql = "UPDATE sc_spawners SET server=?, world=?, x=?, y=?, z=? WHERE id=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                if (sloc == null) {
                    ps.setNull(1, Types.VARCHAR);
                    ps.setNull(2, Types.VARCHAR);
                    ps.setNull(3, Types.FLOAT);
                    ps.setNull(4, Types.FLOAT);
                    ps.setNull(5, Types.FLOAT);
                } else {
                    ps.setString(1, sloc.getServer());
                    ps.setString(2, sloc.getWorld());
                    ps.setDouble(3, sloc.getX());
                    ps.setDouble(4, sloc.getY());
                    ps.setDouble(5, sloc.getZ());
                }
                ps.setInt(6, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSpawner(int id) {
        String sql = "DELETE FROM sc_spawners WHERE id LIKE ?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getSpawnerCount(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM sc_spawners WHERE uuid LIKE ?;";
        
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
