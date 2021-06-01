package com.mnewt00.vulcandatabase;

import com.mnewt00.vulcandatabase.commands.LogsCommand;
import com.mnewt00.vulcandatabase.listener.VulcanListener;
import com.mnewt00.vulcandatabase.storage.AbstractStorageProvider;
import com.mnewt00.vulcandatabase.storage.impl.MySQLStorageProvider;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class VulcanDatabase extends JavaPlugin {
    @Getter private static VulcanDatabase instance;
    @Getter private AbstractStorageProvider storageProvider;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        ConfigurationSection data = getConfig().getConfigurationSection("connection-information");

        String host = data.getString("host").split(":")[0];
        String port = data.getString("host").split(":").length > 1 ? data.getString("host").split(":")[0] : "";
        String username = data.getString("username");
        String password = data.getString("password");
        String databaseName = data.getString("database-name");
        String tablePrefix = data.getString("table-prefix");
        boolean useSSL = data.getBoolean("useSSL");

        switch (getConfig().getString("storage-type").toLowerCase()) {
            case "mysql":
                storageProvider = new MySQLStorageProvider(host, port, username, password, databaseName, tablePrefix, useSSL);
            case "mariadb":
//                storageType = StorageType.MARIADB;
            case "mongo":
//                storageType = StorageType.MONGODB;
                break;
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
