package me.blha303;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;

public class RegionShow extends JavaPlugin implements Listener {
	private Logger log = this.getLogger();
	Essentials essentials;
	Warps warps;

	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
        Plugin p = pm.getPlugin("Essentials");
        if(p == null) {
            log.severe("Cannot find Essentials!");
            return;
        }
        essentials = (Essentials)p;
        if (!essentials.isEnabled()) pm.disablePlugin(this);
		pm.registerEvents(this, this);
	}

	public void reportError(Exception e, String message) {
		reportError(e, message, true);
	}

	public void reportError(Exception e, String message, boolean dumpStackTrace) {
		log.severe("[RegionShow] " + message + " - " + e.toString());
		if (dumpStackTrace)
			e.printStackTrace();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String name = "";
		
		if (!this.isEnabled())
			return;

		if (event.isCancelled())
			return;

		name = getWarpName(event.getPlayer().getLocation());		

		event.setMessage(ChatColor.GRAY + "[" + name + "] " + ChatColor.WHITE + event.getMessage());
		return;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = null;
		if (args.length == 1) {
			p = this.getServer().getPlayer(args[0]);
		}
		
		if (sender instanceof Player) {
			if (!((Player)sender).hasPermission("command.loc")) {
				sender.sendMessage("You can't use this command.");
				return true;
			}
			if (p == null) p = (Player)sender;
		}
		String name = getWarpName(p.getLocation());
		sender.sendMessage(p.getDisplayName() + " is near " + ChatColor.GRAY + name);
		
		return true;
	}
	
	private String getWarpName(Location location) {
		String name = "";
		warps = essentials.getWarps();
		if (warps != null) {
			HashMap<String,Double> map = new HashMap<String,Double>();
			Collection<String> warplist = warps.getWarpNames();
			for (String n : warplist) {
				Location loc;
				try {
					loc = warps.getWarp(n);
					if (loc != null) {
						double dist = location.distanceSquared(loc);
						map.put(n, dist);
					}
				} catch (Exception e) {
					reportError(e, "Error", true);
					return null;
				}
			}
			Entry<String,Double> min = null;
			for (Entry<String,Double> entry : map.entrySet()) {
				if (min == null || min.getValue() > entry.getValue()) {
			        min = entry;
			    }
			}
			if (min != null) {
				name = min.getKey();
			} else {
				name = location.getWorld().getName();
			}
		} else {
			name = location.getWorld().getName();
		}
		return name;
	}
}
