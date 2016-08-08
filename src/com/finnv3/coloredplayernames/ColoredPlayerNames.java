package com.finnv3.coloredplayernames;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	private Map<UUID, ChatColor> playerColors;

	private static Random random;

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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			ChatColor oldColor = playerColors.get(player.getUniqueId());
			ChatColor color = null;

			if (args.length > 0) {
				ConfigurationSection colorSection = getConfig().getConfigurationSection("colors");
				for (String colorName : colorSection.getKeys(false)) {
					if (args[0].equalsIgnoreCase(colorName)) {
						color = ChatColor.getByChar(colorSection.getString(colorName + ".code"));
					}
				}
				if (color == null) {
					player.sendMessage(ChatColor.RED + "Unrecognized color");
				}
			}
			if (color == null) {
				colorPlayer(player);
			} else {
				colorPlayer(player, color);
			}
			if (oldColor.equals(playerColors.get(player.getUniqueId()))) {
				player.sendMessage("Your name is still " + player.getDisplayName());
			} else {
				player.sendMessage("Your name is now " + player.getDisplayName());
			}
		} else {
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command");
		}
		return true;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		colorPlayer(player);

		event.setJoinMessage(player.getDisplayName() + ChatColor.YELLOW + " joined the game.");

	}

	private ChatColor pickColor(Player player) {
		Set<ChatColor> availableColors = EnumSet.noneOf(ChatColor.class);
		Map<ChatColor, Integer> colorsInUse = new EnumMap<ChatColor, Integer>(ChatColor.class);
		Set<ChatColor> possibleColors = possibleColors();
		for (ChatColor color : possibleColors) {
			colorsInUse.put(color, 0);
		}
		for (ChatColor color : playerColors.values()) {
			colorsInUse.put(color, colorsInUse.get(color) + 1);
		}
		int lowestNumber = Integer.MAX_VALUE;
		for (ChatColor color : possibleColors) {
			int occurences = colorsInUse.get(color);
			if (occurences <= lowestNumber) {
				if (occurences < lowestNumber) {
					lowestNumber = occurences;
					availableColors.clear();
				}
				availableColors.add(color);
			}
		}
		if (availableColors.isEmpty()) {
			return ChatColor.RESET;
		}

		double weightTotal = 0.0;
		for (ChatColor color : availableColors) {
			weightTotal += weight(color);
		}
		if (weightTotal <= 0.0) {
			return elementFrom(availableColors);
		}
		double randomNumber = random.nextDouble();
		double probability = 0.0;
		for (ChatColor color : availableColors) {
			probability += weight(color) / weightTotal;
			if (randomNumber < probability) {
				return color;
			}
		}
		return elementFrom(availableColors);
	}

	private void colorPlayer(Player player) {
		ChatColor color = getPermColor(player);
		if (color == null) {
			color = pickColor(player);
		}
		colorPlayer(player, color);
	}

	private void colorPlayer(Player player, ChatColor color) {
		uncolorPlayer(player);

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
		Team team = scoreboard.getTeam(player.getName());
		if (team != null) {
			team.unregister();
		}
		player.setDisplayName(player.getName());
	}

	private ChatColor getPermColor(Player player) {
		ConfigurationSection colorSection = getConfig().getConfigurationSection("colors");
		for (String colorName : colorSection.getKeys(false)) {
			String permission = "coloredplayernames." + colorName;
			if (player.isPermissionSet(permission) && player.hasPermission(permission)) {
				return ChatColor.getByChar(colorSection.getString(colorName + ".code"));
			}
		}
		return null;
	}

	private Set<ChatColor> possibleColors() {
		Set<ChatColor> result = EnumSet.noneOf(ChatColor.class);
		ConfigurationSection colorSection = getConfig().getConfigurationSection("colors");
		for (String colorName : colorSection.getKeys(false)) {
			ConfigurationSection singleColor = colorSection.getConfigurationSection(colorName);
			result.add(ChatColor.getByChar(singleColor.getString("code")));
		}
		return result;
	}

	private double weight(ChatColor color) {
		ConfigurationSection colorSection = getConfig().getConfigurationSection("colors");
		for (String colorName : colorSection.getKeys(false)) {
			ConfigurationSection singleColor = colorSection.getConfigurationSection(colorName);
			if (singleColor.getString("code").equals(String.valueOf(color.getChar()))) {
				return singleColor.getDouble("weight");
			}
		}
		return 0.0;
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();

		event.setFormat(ChatColor.GRAY + "<" + ChatColor.RESET + player.getDisplayName() + ChatColor.GRAY + "> "
				+ ChatColor.RESET + event.getMessage());

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		event.setQuitMessage(event.getPlayer().getDisplayName() + ChatColor.YELLOW + " left the game.");
		uncolorPlayer(event.getPlayer());

	}

	private static final <E> E elementFrom(Collection<E> collection) {
		int index = random.nextInt(collection.size());

		int i = 0;
		for (E element : collection) {
			if (index == i) {
				return element;
			}
			i++;
		}

		throw new AssertionError();
	}

	private static final int id = 80947;

}
