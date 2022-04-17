package com.mnewt00.vulcandatabase.storage;

import com.google.common.collect.Lists;
import com.mnewt00.vulcandatabase.Log;
import com.mnewt00.vulcandatabase.VulcanDatabase;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.UUID;

public class SQLiteStorageProvider implements StorageProvider {

    @Getter
    private final Connection connection;

    public SQLiteStorageProvider() {
        Connection connection1;
        File database = new File(VulcanDatabase.getInstance().getDataFolder(), "database.db");
        try {
            Class.forName("org.sqlite.JDBC");
            connection1 = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
        } catch (SQLException | ClassNotFoundException e) {
            connection1 = null;
            e.printStackTrace();
        }
        this.connection = connection1;
        createTables();
    }

    @Override
    public void createTables() {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS vulcandb_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "uuid VARCHAR(36) NOT NULL," +
                        "name VARCHAR(255) NOT NULL," +
                        "timestamp VARCHAR(255) NOT NULL," +
                        "server VARCHAR(255)," +
                        "information VARCHAR(255)," +
                        "`check` VARCHAR(255) NOT NULL," +
                        "check_type VARCHAR(255) NOT NULL," +
                        "violations INTEGER NOT NULL," +
                        "version VARCHAR(255) NOT NULL," +
                        "ping INTEGER," +
                        "tps DOUBLE" +
                        ");"
        )) {
            preparedStatement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void addLog(Log log, UUID uuid) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO vulcandb_logs" +
                " (uuid, name, timestamp, `server`, information, `check`, check_type, violations, version, ping, tps)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            preparedStatement.setString(1, log.getUuid().toString());
            preparedStatement.setString(2, log.getPlayerName());
            preparedStatement.setString(3, String.valueOf(log.getTimestamp()));
            preparedStatement.setString(4, log.getServer());
            preparedStatement.setString(5, log.getInfo());
            preparedStatement.setString(6, log.getCheckName());
            preparedStatement.setString(7, log.getCheckType());
            preparedStatement.setInt(8, log.getVl());
            preparedStatement.setString(9, log.getVersion());
            preparedStatement.setInt(10, log.getPing());
            preparedStatement.setDouble(11, log.getTps());

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<Log> getLogs(int amount, int offset, UUID uuid) {
        List<Log> logs = Lists.newArrayList();

        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT uuid,name,timestamp,`server`,information,`check`,check_type,violations,version,ping,tps FROM vulcandb_logs WHERE `uuid` = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?;")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, amount);
            preparedStatement.setInt(3, offset);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                logs.add(new Log(
                        UUID.fromString(resultSet.getString(1)),
                        resultSet.getString(2),
                        Long.parseLong(resultSet.getString(3)),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6),
                        resultSet.getString(7),
                        resultSet.getInt(8),
                        resultSet.getString(9),
                        resultSet.getInt(10),
                        resultSet.getDouble(11)
                ));
            }
            resultSet.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return logs;
    }

    @Override
    public int count(UUID uuid) {
        int finalCount;
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT COUNT(*) FROM vulcandb_logs WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet set = preparedStatement.executeQuery();
            set.next();
            finalCount = set.getInt(1);
            set.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            finalCount = 0;
        }
        return finalCount;
    }
}
