package com.voichick.cpn

import org.bukkit.ChatColor
import org.bukkit.ChatColor.RESET
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class PlayerColors(private val board: Scoreboard? = null) {

    private val playerColors = mutableMapOf<Player, ChatColor>()
    private val counts = EnumMap<ChatColor, Int>(ChatColor::class.java)

    private val replacementsRef = AtomicReference<Map<String, String>>()
    val replacements: Map<String, String>
    get() = replacementsRef.get()

    operator fun get(player: Player) = playerColors[player]

    operator fun set(player: Player, color: ChatColor?) {

        val name = player.name

        // Update display name
        val displayName = if (color == null)
            name
        else
            color.toString() + name + RESET
        player.setDisplayName(displayName)
        player.setPlayerListName(displayName)

        // Update counts
        val oldColor = if (color == null)
            playerColors.remove(player)
        else
            playerColors.put(player, color)
        if (oldColor != null)
            counts.merge(oldColor, -1, Int::plus)
        if (color != null)
            counts.merge(color, 1, Int::plus)

        // Update replacements
        val newReplacements = TreeMap<String, String>()
        for (other in playerColors.keys)
            newReplacements[other.name] = other.displayName
        replacementsRef.set(newReplacements)

        if (board == null)
            return
        // Set player's scoreboard to board
        val playerBoard = player.scoreboard
        val teamName = TEAM_PREFIX + name
        if (playerBoard != board) {
            val team = playerBoard.getEntryTeam(name)
            if (team == null || team.name == teamName) {
                player.scoreboard = board
            } else {
                ColoredPlayerNames.logger.warning {
                    "Player \"$name\" is currently on team \"${team.name}\". For full CPN functionality, please remove them from this team."
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

    private companion object {
        private const val TEAM_PREFIX = "__CPN__"
    }

}
