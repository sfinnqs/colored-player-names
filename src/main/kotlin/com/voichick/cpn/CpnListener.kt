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
        plugin.playerColors.changeColor(player)

        event.joinMessage = event.joinMessage.replace(player.name, player.displayName)
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.format = "$GRAY<$RESET%s$GRAY>$RESET %s"
        for ((name, displayName) in plugin.playerColors.replacements) {
            event.message = event.message.replace(name, displayName)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        println(event.deathMessage)
        event.deathMessage = event.deathMessage?.replace(player.name, player.displayName)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        println(event.quitMessage)
        event.quitMessage = event.quitMessage.replace(player.name, player.displayName + YELLOW)
        plugin.playerColors[player] = null
    }

}
