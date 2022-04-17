/*
 * VulcanDatabase - LogsCommand.java
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

package com.mnewt00.vulcandatabase.commands;

import com.mnewt00.vulcandatabase.Log;
import com.mnewt00.vulcandatabase.VulcanDatabase;
import com.mnewt00.vulcandatabase.util.Common;
import me.frep.vulcan.api.VulcanAPI;
import me.frep.vulcan.api.check.Check;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // logs <player> [pages]
        if (!sender.hasPermission("vulcan.logs")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /logs <player> [pages]");
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Can't find the UUID of " + args[0] + ".");
            return true;
        }

        if (args.length == 2) {
            try {
                Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
                return true;
            }
        }

        int pages = Integer.parseInt(args.length == 2 ? args[1] : "1") - 1;

        Bukkit.getScheduler().runTaskAsynchronously(VulcanDatabase.getInstance(), () -> {
            List<Log> logs = VulcanDatabase.getInstance().getStorageProvider().getLogs(VulcanDatabase.getInstance().getConfig().getInt("items-per-page", 10), pages * 10, player.getUniqueId());
            int pageCount = VulcanDatabase.getInstance().getStorageProvider().count(player.getUniqueId());

            if (logs.isEmpty() && pageCount != 0) {
                sender.sendMessage(ChatColor.RED + "There is no page " + (pages + 1) + " for that player!");
                return;
            } else if (logs.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "That player has no AntiCheat logs!");
                return;
            }

            TextComponent textComponent = Component.text(
                    Common.colorize(Common.colorize(VulcanDatabase.getInstance().getConfig().getString("messages.log-header.message")
                    .replace("%player%", player.getName())
                    .replace("%uuid%", player.getUniqueId().toString())
                    .replace("%page%", String.valueOf(pages + 1))
                    .replace("%maxpage%", String.valueOf(pageCount / 10 + 1))))
            );

            if (!VulcanDatabase.getInstance().getConfig().getString("messages.log-header.tooltip").isEmpty()) {
                HoverEvent<Component> hoverEvent = HoverEvent.showText(Component.text(Common.colorize(VulcanDatabase.getInstance().getConfig().getString("messages.log-header.tooltip")
                        .replace("%player%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .replace("%page%", String.valueOf(pages + 1))
                        .replace("%maxpage%", String.valueOf(pageCount / 10 + 1)))));
                (VulcanDatabase.getInstance().getAdventure()).sender(sender).sendMessage(textComponent.hoverEvent(hoverEvent));
            } else {
                (VulcanDatabase.getInstance().getAdventure()).sender(sender).sendMessage(textComponent);
            }


            Map<String, Check> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            VulcanAPI.Factory.getApi().getChecks(Bukkit.getOnlinePlayers().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("No players are online to get checks descriptions."))).forEach(c -> treeMap.put(c.getName() + c.getType(), c));

            for (Log log : logs) {
                Bukkit.getScheduler().runTask(VulcanDatabase.getInstance(), () -> {
                    long diff = System.currentTimeMillis() - log.getTimestamp();

                    // https://stackoverflow.com/a/13018647/11357644
                    int SECOND_MILLIS = 1000;
                    int MINUTE_MILLIS = 60 * SECOND_MILLIS;
                    int HOUR_MILLIS = 60 * MINUTE_MILLIS;
                    int DAY_MILLIS = 24 * HOUR_MILLIS;

                    String friendlyTime;
                    if (diff < MINUTE_MILLIS) {
                        friendlyTime = "Just now";
                    } else if (diff < 2 * MINUTE_MILLIS) {
                        friendlyTime = "A minute ago";
                    } else if (diff < 50 * MINUTE_MILLIS) {
                        friendlyTime = diff / MINUTE_MILLIS + " minutes ago";
                    } else if (diff < 90 * MINUTE_MILLIS) {
                        friendlyTime = "An hour ago";
                    } else if (diff < 24 * HOUR_MILLIS) {
                        friendlyTime = diff / HOUR_MILLIS + " hours ago";
                    } else if (diff < 48 * HOUR_MILLIS) {
                        friendlyTime = "Yesterday";
                    } else {
                        friendlyTime = diff / DAY_MILLIS + " days ago";
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat(VulcanDatabase.getInstance().getConfig().getString("messages.log.time.format"));
                    TimeZone timeZone = TimeZone.getTimeZone(VulcanDatabase.getInstance().getConfig().getString("log-date-timezone", "Australia/Melbourne"));
                    sdf.setTimeZone(timeZone);

                    HoverEvent<Component> logEventHoverFirst = HoverEvent.showText(Component.text(Common.colorize(VulcanDatabase.getInstance().getConfig().getString("messages.log.time.hover").replace("%niceformatted%", friendlyTime).replace("%longdateformat%", sdf.format(new Date(log.getTimestamp()))))));

                    TextComponent.Builder builder = Component.text(Common.colorize(VulcanDatabase.getInstance().getConfig().getString("messages.log.time.message").replace("%niceformatted%", friendlyTime).replace("%longdateformat%", sdf.format(new Date(log.getTimestamp())))))
                            .hoverEvent(logEventHoverFirst).toBuilder();

                    String description = treeMap.get((log.getCheckName() + log.getCheckType()).replace(" ", "").toLowerCase(Locale.ROOT)).getDescription();

                    TextComponent mainLog = (Component.text(" " + Common.colorize(VulcanDatabase.getInstance().getConfig().getString("messages.log.main-message.message")
                            .replace("%player%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString())
                            .replace("%info%", log.getInfo())
                            .replace("%description%", description)
                            .replace("%version%", log.getVersion())
                            .replace("%check%", log.getCheckName())
                            .replace("%type%", log.getCheckType())
                            .replace("%vl%", log.getVl() + "")
                            .replace("%ping%", log.getPing() + "")
                            .replace("%tps%", new DecimalFormat("#.##").format(log.getTps()) + "")
                            .replace("%server%", log.getServer())
                    )));

                    if (!VulcanDatabase.getInstance().getConfig().getStringList("messages.log.main-message.hover").isEmpty()) {
                        List<String> tooltip = VulcanDatabase.getInstance().getConfig().getStringList("messages.log.main-message.hover");
                        tooltip.replaceAll(string -> Common.colorize(string.replace("%player%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString())
                                .replace("%info%", log.getInfo())
                                .replace("%description%", description)
                                .replace("%version%", log.getVersion())
                                .replace("%check%", log.getCheckName())
                                .replace("%type%", log.getCheckType())
                                .replace("%vl%", log.getVl() + "")
                                .replace("%ping%", log.getPing() + "")
                                .replace("%tps%", new DecimalFormat("#.##").format(log.getTps()) + "")
                                .replace("%server%", log.getServer())
                        ));

                        HoverEvent<Component> mainTooltip = HoverEvent.showText(Component.text(String.join("\n", tooltip)));
                        builder.append(mainLog.hoverEvent(mainTooltip));
                    } else {
                        builder.append(mainLog);
                    }
                    (VulcanDatabase.getInstance().getAdventure()).sender(sender).sendMessage(builder.build());

                });
            }
        });
        return true;
    }
}
