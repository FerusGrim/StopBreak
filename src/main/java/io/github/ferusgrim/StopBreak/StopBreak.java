
package io.github.ferusgrim.StopBreak;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StopBreak extends JavaPlugin {
    public String mStart = ChatColor.translateAlternateColorCodes('&', "&a&l[GrimList]&r&e ");

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockListener(), this);
        getCommand("stopbreak").setExecutor(new Executor());
    }

    private class BlockListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        private void onBlockBreakEvent(BlockBreakEvent event) {
            if (!getConfig().getBoolean("Enabled") ||
                    (!getConfig().getBoolean("StopOPs") && event.getPlayer().isOp())
                    || !getConfig().getStringList("Protected-Worlds").contains(event.getPlayer().getLocation().getWorld().getName())) return;
            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        private void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
            if (!(event.getRemover() instanceof Player) ||
                    !getConfig().getBoolean("Enabled") ||
                    (!getConfig().getBoolean("StopOPs") && ((Player)event.getRemover()).isOp())
                    || !getConfig().getStringList("Protected-Worlds").contains(((Player) event.getRemover()).getLocation().getWorld().getName())) return;
            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        private void onEntityDamageEvent(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player) ||
                    !getConfig().getBoolean("Enabled") ||
                    (!getConfig().getBoolean("StopOPs") && ((Player)event.getDamager()).isOp()) ||
                    !(event.getEntity() instanceof ItemFrame) ||
                    !getConfig().getStringList("Protected-Worlds").contains(((Player) event.getDamager()).getLocation().getWorld().getName())) return;
            event.setCancelled(true);
        }
    }

    private class Executor implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length < 1 || args[0].equals("help")) {
                if (!sender.hasPermission("stopbreak.help")) {
                    sender.sendMessage((sender instanceof Player ? mStart : "") + "No permissions!");
                    return true;
                }
                return false;
            }
            String world = "";
            if (args.length > 1) {
                world = args[1];
            }
            switch (args[0]) {
                case "add":
                    return addWorld(sender, world);
                case "remove":
                    return removeWorld(sender, world);
                default:
                    sender.sendMessage((sender instanceof Player ? mStart : "") + "Invalid command!");
                    return true;
            }
        }

        private boolean addWorld(CommandSender sender, String world) {
            if (!sender.hasPermission("stopbreak.add")) {
                sender.sendMessage((sender instanceof Player ? mStart : "") + "No permissions!");
                return true;
            }
            List<String> worlds = new ArrayList<>(getConfig().getStringList("Protected-Worlds"));
            if (world.isEmpty()) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("If you're not a player, you must specify a world name!");
                    return true;
                }
                world = ((Player) sender).getLocation().getWorld().getName();
            }
            if (worlds.contains(world)) {
                sender.sendMessage((sender instanceof Player ? mStart : "") + "This world is already protected!");
                return true;
            } else {
                worlds.add(world);
                getConfig().set("Protected-Worlds", worlds);
                saveConfig();
                sender.sendMessage((sender instanceof Player ? mStart : "") + "Added " + world + " to the protected worlds list!");
                return true;
            }
        }

        private boolean removeWorld(CommandSender sender, String world) {
            if (!sender.hasPermission("stopbreak.remove")) {
                sender.sendMessage((sender instanceof Player ? mStart : "") + "No permissions!");
                return true;
            }
            List<String> worlds = new ArrayList<>(getConfig().getStringList("Protected-Worlds"));
            if (world.isEmpty()) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("If you're not a player, you must specify a world name!");
                    return true;
                }
                world = ((Player) sender).getLocation().getWorld().getName();
            }
            if (worlds.contains(world)) {
                worlds.remove(world);
                getConfig().set("Protected-Worlds", worlds);
                saveConfig();
                sender.sendMessage((sender instanceof Player ? mStart : "") + "Removed " + world + " from the protected worlds list!");
                return true;
            } else {
                sender.sendMessage((sender instanceof Player ? mStart : "") + "This world isn't protected!");
                return true;
            }
        }
    }
}
