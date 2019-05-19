package com.voichick.cpn

import org.bukkit.ChatColor.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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
                ?: plugin.colorPicker()

        event.joinMessage = "${player.displayName}$RESET$YELLOW joined the game."
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent?) {
        if (event == null) return

        event.format = "$GRAY<$RESET%s$RESET$GRAY>$RESET %s"
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent?) {
        if (event == null) return

        val player = event.player
        event.quitMessage = "${player.displayName} ${YELLOW}left the game."
        plugin.playerColors[player] = null
    }

}
