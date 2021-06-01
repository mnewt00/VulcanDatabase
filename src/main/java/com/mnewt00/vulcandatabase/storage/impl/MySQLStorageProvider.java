package com.mnewt00.vulcandatabase.storage.impl;

import com.google.common.collect.Lists;
import com.mnewt00.vulcandatabase.Log;
import com.mnewt00.vulcandatabase.storage.AbstractStorageProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MySQLStorageProvider implements AbstractStorageProvider {
    private final HikariDataSource dataSource;
    private final String tablePrefix;
    @Getter private Connection connection;

    @SneakyThrows
    public MySQLStorageProvider(String host, String port, String username, String password, String databaseName, String tablePrefix, boolean useSSL) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + (port.isEmpty() ? "3306" : port) + "/" + databaseName);
        config.setUsername(username);
        config.setPassword(password);


        // skidded from luckperms
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("alwaysSendSetIsolation", "false");
        config.addDataSourceProperty("cacheCallableStmts", "true");
        config.addDataSourceProperty("serverTimezone", "UTC");

        this.dataSource = new HikariDataSource(config);
        this.connection = dataSource.getConnection();

        this.tablePrefix = tablePrefix;
        initiateTables(tablePrefix);
    }

    @SuppressWarnings("SqlResolve")
    @Override
    public List<Log> getLogs(int amount, UUID uuid) {
        List<Log> logs = Lists.newArrayList();

        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT uuid,name,timestamp,`check`,check_type,violations,ping,tps FROM " + (this.tablePrefix + "logs") + " WHERE `uuid` = ? LIMIT ?;")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, amount);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("add log");
                logs.add(new Log(
                            UUID.fromString(resultSet.getString(1)),
                                    resultSet.getString(2),
                                    Long.parseLong(resultSet.getString(3)),
                                    resultSet.getString(4),
                                    resultSet.getString(5),
                                    resultSet.getInt(6),
                                    resultSet.getInt(7),
                                    resultSet.getDouble(8)
                        ));
            }
            resultSet.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        System.out.println(logs.size());
        return logs;
    }

    @Override @SuppressWarnings("SqlResolve")
    public void addLog(Log log, UUID uuid) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO " + (this.tablePrefix + "logs") +
                " (uuid, name, timestamp, `check`, check_type, violations, ping, tps)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?);")) {
            preparedStatement.setString(1, log.getUuid().toString());
            preparedStatement.setString(2, log.getPlayerName());
            preparedStatement.setString(3, String.valueOf(log.getTimestamp()));
            preparedStatement.setString(4, log.getCheckName());
            preparedStatement.setString(5, log.getCheckType());
            preparedStatement.setInt(6, log.getVl());
            preparedStatement.setInt(7, log.getPing());
            preparedStatement.setDouble(8, log.getTps());

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void initiateTables(String prefix) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + (prefix + "logs") + " (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "uuid VARCHAR(36) NOT NULL," +
                        "name VARCHAR(255) NOT NULL," +
                        "timestamp VARCHAR(255) NOT NULL," +
                        "`check` VARCHAR(255) NOT NULL," +
                        "check_type VARCHAR(255) NOT NULL," +
                        "violations INTEGER NOT NULL," +
                        "ping INTEGER," +
                        "tps DOUBLE" +
                        ");"
        )) {
            preparedStatement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
