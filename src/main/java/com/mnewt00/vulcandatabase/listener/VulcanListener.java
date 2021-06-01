package com.mnewt00.vulcandatabase.listener;

import com.mnewt00.vulcandatabase.Log;
import com.mnewt00.vulcandatabase.VulcanDatabase;
import me.frep.vulcan.api.VulcanAPI;
import me.frep.vulcan.api.event.VulcanFlagEvent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VulcanListener implements Listener {
    @EventHandler
    public void onVulcanFlag(VulcanFlagEvent event) {
//        Bukkit.getScheduler().runTaskAsynchronously(VulcanDatabase.getInstance(), () ->
                VulcanDatabase.getInstance().getStorageProvider().addLog(new Log(
                        event.getPlayer().getUniqueId(), event.getPlayer().getName(),
                        System.currentTimeMillis(),
                        VulcanDatabase.getInstance().getConfig().getString("server-name"),
                        event.getInfo(),
                        WordUtils.capitalize(event.getCheck().getName()),
                        String.valueOf(event.getCheck().getType()).toUpperCase(),
                    event.getCheck().getVl() + 1, VulcanAPI.Factory.getApi().getClientVersion(event.getPlayer()),
                        VulcanAPI.Factory.getApi().getPing(event.getPlayer()),
                        VulcanAPI.Factory.getApi().getTps()),
                        event.getPlayer().getUniqueId());
//        );
    }
}
