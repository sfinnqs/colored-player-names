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

		playerColors = new HashMap<UUID, ChatColor>(16);

		random = new Random();

		getServer().getPluginManager().registerEvents(this, this);

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		ChatColor color = getPermColor(player);
		if (color == null) {
			color = pickColor(player);
		}
		colorPlayer(player, color);

		event.setJoinMessage(player.getDisplayName() + ChatColor.YELLOW + " joined the game.");

	}

	private ChatColor pickColor(Player player) {
		List<ChatColor> availableColors = new ArrayList<ChatColor>(16);
		Map<ChatColor, Integer> colorsInUse = new EnumMap<ChatColor, Integer>(ChatColor.class);
		for (ChatColor color : colors) {
			colorsInUse.put(color, 0);
		}
		for (ChatColor color : playerColors.values()) {
			colorsInUse.put(color, colorsInUse.get(color) + 1);
		}
		int lowestNumber = Integer.MAX_VALUE;
		for (ChatColor color : colors) {
			int occurences = colorsInUse.get(color);
			if (occurences <= lowestNumber) {
				if (occurences < lowestNumber) {
					lowestNumber = occurences;
					availableColors.clear();
				}
				availableColors.add(color);
			}
		}

		return availableColors.get(random.nextInt(availableColors.size()));
	}

	private void colorPlayer(Player player, ChatColor color) {
		playerColors.put(player.getUniqueId(), color);

		player.setDisplayName(color + player.getPlayerListName() + ChatColor.RESET);

		Team team = scoreboard.registerNewTeam(player.getName());
		team.setDisplayName(player.getName());
		team.setPrefix(color.toString());
		team.setSuffix(ChatColor.RESET.toString());
		team.addEntry(player.getName());
		player.setScoreboard(scoreboard);
	}

	private ChatColor getPermColor(Player player) {
		for (int i = 0; i < colors.length; i++) {
			ChatColor color = colors[i];
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
		scoreboard.getTeam(event.getPlayer().getName()).unregister();

	}

	private static final int id = 80947;

	private static final ChatColor[] colors = { ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
			ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
			ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE,
			ChatColor.YELLOW, ChatColor.WHITE };

}
