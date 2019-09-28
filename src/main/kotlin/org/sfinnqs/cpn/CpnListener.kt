/**
 * ColoredPlayerNames - A Bukkit plugin for changing name colors
 * Copyright (C) 2019 sfinnqs
 *
 * This file is part of ColoredPlayerNames.
 *
 * ColoredPlayerNames is free software; you can redistribute it and/or modify it
 * under the terms of version 3 of the GNU General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <https://www.gnu.org/licenses>.
 */
package org.sfinnqs.cpn

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
