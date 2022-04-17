/*
 * VulcanDatabase - VulcanDatabase.java
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

package com.mnewt00.vulcandatabase;

import com.mnewt00.vulcandatabase.commands.LogsCommand;
import com.mnewt00.vulcandatabase.listener.VulcanListener;
import com.mnewt00.vulcandatabase.storage.MySQLStorageProvider;
import com.mnewt00.vulcandatabase.storage.SQLiteStorageProvider;
import com.mnewt00.vulcandatabase.storage.StorageProvider;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class VulcanDatabase extends JavaPlugin {
    @Getter private static VulcanDatabase instance;
    @Getter private StorageProvider storageProvider;
    @Getter private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BukkitAudiences.create(this);

        saveDefaultConfig();



        if (getConfig().getString("connection-type").toUpperCase(Locale.ROOT).equals("MYSQL")) {
            ConfigurationSection data = getConfig().getConfigurationSection("connection-information");

            String host = data.getString("host").split(":")[0];
            String port = data.getString("host").split(":").length > 1 ? data.getString("host").split(":")[1] : "";
            String username = data.getString("username");
            String password = data.getString("password");
            String databaseName = data.getString("database-name");
            String tablePrefix = data.getString("table-prefix");
            boolean useSSL = data.getBoolean("useSSL");
            storageProvider = new MySQLStorageProvider(host, port, username, password, databaseName, tablePrefix, useSSL);
        } else {
            storageProvider = new SQLiteStorageProvider();
        }

        Bukkit.getPluginManager().registerEvents(new VulcanListener(), this);

        // We delay this to overwrite Vulcan's default /log command
        getServer().getScheduler().runTaskLater(this, () -> {
            Bukkit.getPluginCommand("logs").setExecutor(new LogsCommand());
        }, 20L); // 20 ticks = 1 second
    }

    @Override
    public void onDisable() {
    }
}
