package com.mnewt00.vulcandatabase.listener;

import com.mnewt00.vulcandatabase.Log;
import com.mnewt00.vulcandatabase.VulcanDatabase;
import me.frep.vulcan.api.VulcanAPI;
import me.frep.vulcan.api.event.VulcanFlagEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VulcanListener implements Listener {
    @EventHandler
    public void onVulcanFlag(VulcanFlagEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(VulcanDatabase.getInstance(), () -> VulcanDatabase.getInstance().getStorageProvider().addLog(new Log(event.getPlayer().getUniqueId(), event.getPlayer().getName(), event.getTimestamp(), event.getCheck().getName(), String.valueOf(event.getCheck().getType()),
                event.getCheck().getVl() != 0 ? event.getCheck().getVl() : event.getCheck().getVl() + 1, VulcanAPI.Factory.getApi().getPing(event.getPlayer()), VulcanAPI.Factory.getApi().getTps()), event.getPlayer().getUniqueId()));
    }
}
