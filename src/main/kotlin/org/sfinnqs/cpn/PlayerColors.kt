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

import org.bukkit.ChatColor
import org.bukkit.ChatColor.RESET
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReference

class PlayerColors(private val config: CpnConfig, private val board: Scoreboard? = null) {

    private val playerColors = mutableMapOf<Player, ChatColor>()
    private val counts = EnumMap<ChatColor, Int>(ChatColor::class.java)
    private val lastColors = mutableMapOf<Player, ChatColor>()
    private val teamNames = mutableMapOf<String, UUID>()

    private val replacementsRef = AtomicReference<Map<String, String>>(emptyMap())
    val replacements: Map<String, String>
        get() = replacementsRef.get()

    operator fun get(player: Player) = playerColors[player]

    operator fun set(player: Player, color: ChatColor?) {

        // Update counts
        val oldColor = if (color == null)
            playerColors.remove(player)
        else
            playerColors.put(player, color)
        if (oldColor != null)
            counts.merge(oldColor, -1, Int::plus)
        if (color != null)
            counts.merge(color, 1, Int::plus)

        // Update display name
        val displayName = getDisplayName(player)
        player.setDisplayName(displayName)
        player.setPlayerListName(displayName)

        // Update replacements
        val newReplacements = playerColors.keys.associate { it.name to getDisplayName(it) }
        replacementsRef.set(newReplacements)

        // Update lastColors
        if (color != null)
            lastColors[player] = color

        val name = player.name

        if (board == null) return
        // Set player's scoreboard to board
        val playerBoard = player.scoreboard
        val teamName = getTeamName(player)
        if (playerBoard != board) {
            val team = playerBoard.getEntryTeam(name)
            if (team == null || team.name == teamName) {
                player.scoreboard = board
            } else {
                ColoredPlayerNames.logger.warning {
                    "Player \"$name\" is currently on team \"${team.name}\". For full CPN functionality, please remove them from this team or turn off scoreboard in the configuration."
                }
                return
            }
        }
        // Update scoreboard with color
        if (color == null) {
            board.getTeam(teamName)?.unregister()
        } else {
            val team = board.getTeam(teamName) ?: board.registerNewTeam(teamName)
            team.displayName = displayName
            team.color = color
            team.setCanSeeFriendlyInvisibles(false)
            team.addEntry(name)
        }

    }

    fun count(color: ChatColor) = counts[color] ?: 0

    fun getDisplayName(player: Player): String {
        val color = playerColors[player]
        val name = player.name
        return if (color == null)
            name
        else
            color.toString() + name + RESET
    }

    fun changeColor(player: Player) {
        val newColor = pickColor(player)
        set(player, newColor)
    }

    fun availableColors(player: Player): Set<ChatColor> {
        val result = EnumSet.noneOf(ChatColor::class.java)
        result.addAll(possibleColors(player, true).keys)
        result.addAll(possibleColors(player, false).keys)
        return result
    }

    private fun pickColor(player: Player): ChatColor? {
        val staticColor = config.getStaticColor(player)
        if (staticColor != null)
            return staticColor
        val colors = possibleColors(player)
        val lastColor = lastColors[player]
        if (colors.size >= 2) colors.remove(lastColor)
        val weightSum = colors.values.sum()
        if (weightSum <= 0.0)
            return null
        val randomVal = ThreadLocalRandom.current().nextDouble(weightSum)
        var accumulation = 0.0
        for (entry in colors) {
            accumulation += entry.value
            if (accumulation >= randomVal)
                return entry.key
        }
        return null
    }

    private fun possibleColors(player: Player, posWeightsOnly: Boolean = true): MutableMap<ChatColor, Double> {
        val weights = config.weights
        val posWeights = if (posWeightsOnly)
            weights.filterValues { it >= 0 }
        else
            weights
        val playerColor = playerColors[player]
        val updatedCounts = posWeights.keys.associateWith {
            if (it == playerColor)
                count(it) - 1
            else
                count(it)
        }
        val lowestCount = updatedCounts.values.min()
        val filtered = posWeights.filterKeys { updatedCounts[it] == lowestCount }
        val result = EnumMap<ChatColor, Double>(ChatColor::class.java)
        result.putAll(filtered)
        return result
    }

    private fun getTeamName(player: OfflinePlayer): String {
        val preferred = TEAM_PREFIX + player.name
        if (preferred.length <= MAX_TEAM_NAME_LENGTH)
            return preferred
        val clipped = preferred.substring(0, MAX_TEAM_NAME_LENGTH)
        val result = getAvailableName(player.uniqueId, clipped)
        teamNames[result] = player.uniqueId
        return result
    }

    private tailrec fun getAvailableName(uuid: UUID, possible: String): String {
        val uuidForName = teamNames[possible]
        return if (uuidForName == null || uuidForName == uuid)
            possible
        else
            getAvailableName(uuid, nextName(possible))
    }

    private companion object {
        private fun nextName(name: String): String {
            val firstPart = name.substring(0, name.lastIndex)
            return when (val lastChar = name.last()) {
                in '0'..'8' -> firstPart + (lastChar + 1)
                '9' -> {
                    nextName(firstPart) + '0'
                }
                else -> firstPart + '0'
            }
        }

        private const val TEAM_PREFIX = "__CPN__"
        private const val MAX_TEAM_NAME_LENGTH = 16
    }

}
