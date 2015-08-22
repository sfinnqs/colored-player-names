package com.finnv3.coloredplayernames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.mcstats.MetricsLite;

import net.gravitydevelopment.updater.Updater;

/**
 * The main (and only) ColoredPlayerNames class
 *
 * @author Finn Voichick
 */
public final class ColoredPlayerNames extends JavaPlugin implements Listener {

	private Scoreboard scoreboard;
	private Map<ChatColor, Double> weights;
	private Map<UUID, ChatColor> playerColors;

	private Random random;

	@Override
	public void onEnable() {

		saveDefaultConfig();
		if (getConfig().getBoolean("auto-update")) {
			new Updater(this, id, getFile(), Updater.UpdateType.DEFAULT, false);
		}
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			getLogger().log(Level.WARNING, "Failed to submit stats to mcstats.org", e);
		}

		scoreboard = getServer().getScoreboardManager().getNewScoreboard();

		weights = new HashMap<ChatColor, Double>();
		ConfigurationSection colorSection = getConfig().getConfigurationSection("colors");
		for (String colorName : colorSection.getKeys(false)) {
			ConfigurationSection singleColor = colorSection.getConfigurationSection(colorName);
			weights.put(ChatColor.getByChar(singleColor.getString("code")), singleColor.getDouble("weight"));
		}
		
		playerColors = new HashMap<UUID, ChatColor>(16);

		random = new Random();

		getServer().getPluginManager().registerEvents(this, this);
		
		
		for (Player player : getServer().getOnlinePlayers()) {
			colorPlayer(player);
		}

	}
	
	@Override
	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			uncolorPlayer(player);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		colorPlayer(player);

		event.setJoinMessage(player.getDisplayName() + ChatColor.YELLOW + " joined the game.");

	}

	private ChatColor pickColor(Player player) {
		List<ChatColor> availableColors = new ArrayList<ChatColor>(16);
		Map<ChatColor, Integer> colorsInUse = new EnumMap<ChatColor, Integer>(ChatColor.class);
		for (ChatColor color : weights.keySet()) {
			colorsInUse.put(color, 0);
		}
		for (ChatColor color : playerColors.values()) {
			colorsInUse.put(color, colorsInUse.get(color) + 1);
		}
		int lowestNumber = Integer.MAX_VALUE;
		for (ChatColor color : weights.keySet()) {
			int occurences = colorsInUse.get(color);
			if (occurences <= lowestNumber) {
				if (occurences < lowestNumber) {
					lowestNumber = occurences;
					availableColors.clear();
				}
				availableColors.add(color);
			}
		}
		
		double weightTotal = 0.0;
		for (ChatColor color : availableColors) {
			weightTotal += weights.get(color);
		}
		double randomNumber = random.nextDouble();
		double probability = 0.0;
		for (ChatColor color : availableColors) {
			probability += weights.get(color) / weightTotal;
			if (randomNumber < probability) {
				return color;
			}
		}
		throw new AssertionError();
	}
	
	private void colorPlayer(Player player) {
		ChatColor color = getPermColor(player);
		if (color == null) {
			color = pickColor(player);
		}
		colorPlayer(player, color);
	}

	private void colorPlayer(Player player, ChatColor color) {
		playerColors.put(player.getUniqueId(), color);

		player.setDisplayName(color + player.getName() + ChatColor.RESET);

		Team team = scoreboard.registerNewTeam(player.getName());
		team.setDisplayName(player.getName());
		team.setPrefix(color.toString());
		team.setSuffix(ChatColor.RESET.toString());
		team.addEntry(player.getName());
		player.setScoreboard(scoreboard);
	}
	
	private void uncolorPlayer(Player player) {
		scoreboard.getTeam(player.getName()).unregister();
		player.setDisplayName(player.getName());
	}

	private ChatColor getPermColor(Player player) {
		for (ChatColor color : weights.keySet()) {
			if (player.hasPermission("coloredplayernames." + color.name())) {
				return color;
			}
		}
		return null;
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();

		event.setFormat(ChatColor.GRAY + "<" + player.getDisplayName() + ChatColor.GRAY + "> " + ChatColor.RESET
				+ event.getMessage());

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		event.setQuitMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " left the game.");
		uncolorPlayer(event.getPlayer());

	}

	private static final int id = 80947;

}
