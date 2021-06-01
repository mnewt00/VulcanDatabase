package com.mnewt00.vulcandatabase.commands;

import com.mnewt00.vulcandatabase.Log;
import com.mnewt00.vulcandatabase.VulcanDatabase;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class LogsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // logs <player> [amount|all] (all uploads to hastebin)
        if (!sender.hasPermission("vulcan.logs")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /logs <player> [amount|all]");
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Can't find the UUID of " + args[0] + ".");
            return true;
        }

        if (args.length == 2 && !args[1].equalsIgnoreCase("all")) {
            try {
                Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
                return true;
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(VulcanDatabase.getInstance(), () -> {
            if (args.length == 1 || (args.length == 2 && !args[1].equalsIgnoreCase("all"))) {
                List<Log> logs = VulcanDatabase.getInstance().getStorageProvider().getLogs(Integer.parseInt(args.length == 2 ? args[1] : "10"), player.getUniqueId());
                Bukkit.getScheduler().runTask(VulcanDatabase.getInstance(), () -> new FancyMessage("AntiCheat logs for ").color(ChatColor.GRAY).then(player.getName() + " ").color(ChatColor.WHITE).tooltip(ChatColor.GRAY + player.getUniqueId().toString())
                        .then("(" + Integer.parseInt(args.length == 2 ? args[1] : "10") + ")").color(ChatColor.GRAY).send(sender));

                for (Log log : logs) {
                    Bukkit.getScheduler().runTask(VulcanDatabase.getInstance(), () ->
                            new FancyMessage("AntiCheat logs for ").color(ChatColor.GRAY).then(player.getName() + " ").color(ChatColor.WHITE)
                                    .then("failed ").color(ChatColor.GRAY).then(log.getCheckName() + " " + log.getCheckType()).color(ChatColor.WHITE)
                                    .then(" [").color(ChatColor.GRAY).then(log.getVl() + "").color(ChatColor.WHITE).then("]").color(ChatColor.GRAY).send(sender));
                }
            }
        });
        return true;
    }
}
