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
    fun onPlayerLogin(event: PlayerLoginEvent?) {
        if (event == null) return
        plugin.cpnConfig.updateWithPlayer(event.player)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent?) {
        if (event == null) return

        val player = event.player
        plugin.playerColors[player] = plugin.cpnConfig.getStaticColor(player)
                ?: plugin.pickColor()

        event.joinMessage = "${player.displayName}$RESET$YELLOW joined the game."
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent?) {
        if (event == null) return

        event.format = "$GRAY<$RESET%s$RESET$GRAY>$RESET %s"
        for ((name, displayName) in plugin.playerColors.replacements)
            event.message = event.message.replace(name, displayName)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent?) {
        if (event == null) return
        val player = event.entity
        event.deathMessage = event.deathMessage?.replace(player.name, player.displayName)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent?) {
        if (event == null) return

        val player = event.player
        event.quitMessage = "${player.displayName} ${YELLOW}left the game."
        plugin.playerColors[player] = null
    }

}
