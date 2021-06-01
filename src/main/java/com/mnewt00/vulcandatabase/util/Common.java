package com.mnewt00.vulcandatabase.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class Common {
    public String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
