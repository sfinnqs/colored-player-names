package com.voichick.cpn

import org.bukkit.ChatColor.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class CpnListener(private val plugin: ColoredPlayerNames) : Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        plugin.cpnConfig.updateWithPlayer(event.player)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (player.hasPermission("coloredplayernames.color")) {
            val playerColors = plugin.playerColors
            playerColors.changeColor(player)
            val replacement = playerColors.getDisplayName(player) + YELLOW
            event.joinMessage = event.joinMessage.replace(player.name, replacement)
        } else {
            plugin.playerColors[player] = null
        }
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        println(event.format)
        event.format = "$GRAY<$RESET%1\$s$RESET$GRAY>$RESET %2\$s"
        var message = event.message
        for ((name, displayName) in plugin.playerColors.replacements)
            message = message.replace(name, displayName)
        event.message = message
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (player.hasPermission("coloredplayernames.color")) {
            val replacement = plugin.playerColors.getDisplayName(player)
            event.deathMessage = event.deathMessage?.replace(player.name, replacement)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val playerColors = plugin.playerColors
        if (player.hasPermission("coloredplayernames.color")) {
            val replacement = playerColors.getDisplayName(player) + YELLOW
            event.quitMessage = event.quitMessage.replace(player.name, replacement)
        }
        playerColors[player] = null
    }

}
