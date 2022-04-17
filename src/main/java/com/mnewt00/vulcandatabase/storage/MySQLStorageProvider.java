/*
 * VulcanDatabase - MySQLStorageProvider.java
 *
 * Copyright (c) 2021 mnewt00
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mnewt00.vulcandatabase.storage;

import com.google.common.collect.Lists;
import com.mnewt00.vulcandatabase.Log;
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

public class MySQLStorageProvider implements StorageProvider{

    @Getter private final Connection connection;

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

        HikariDataSource dataSource = new HikariDataSource(config);
        this.connection = dataSource.getConnection();

        createTables();
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
    public void createTables() {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS vulcandb_logs (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
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
}
