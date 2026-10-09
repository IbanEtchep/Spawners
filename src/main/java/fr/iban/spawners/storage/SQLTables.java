package fr.iban.spawners.storage;

import fr.iban.common.data.sql.DbAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLTables {

    public static void createTables() {
        createTable("CREATE TABLE IF NOT EXISTS sc_spawners(" +
                "    id INT AUTO_INCREMENT," +
                "    uuid VARCHAR(36) NOT NULL," +
                "    type VARCHAR(255) NOT NULL," +
                "    server VARCHAR(255)," +
                "    world VARCHAR(255)," +
                "    x DOUBLE," +
                "    y DOUBLE," +
                "    z DOUBLE," +
                "    PRIMARY KEY (id)\n" +
                ");");
    }

    private static void createTable(String statement) {
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(statement)){
                preparedStatement.executeUpdate();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
