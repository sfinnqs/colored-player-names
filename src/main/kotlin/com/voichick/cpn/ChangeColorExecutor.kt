package com.voichick.cpn

import org.bukkit.ChatColor
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class ChangeColorExecutor(private val plugin: ColoredPlayerNames) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {

        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to use this command")
            return true
        }

        val playerColors = plugin.playerColors
        val oldColor = playerColors[sender]

        val colorString = args.takeUnless { it.isEmpty() }?.joinToString(" ")
        if (colorString == null) {
            playerColors.changeColor(sender)
        } else {

            val colors = plugin.cpnConfig.colors
            val canSpecify = colors.any {
                sender.hasPermission("coloredplayernames.changecolor.specify.${it.officialName}")
            }

            val result = colors[colorString]
            if (result == null) {
                val error = "${RED}Unrecognized color: \"$colorString\""
                sender.sendMessage(arrayOf(error, usage(canSpecify, label)))
                return true
            }
            if (result != oldColor && !isPermittedChange(sender, result, canSpecify, label))
                return true
            else
                playerColors[sender] = result
        }

        val displayName = sender.displayName
        if (playerColors[sender] == oldColor)
            sender.sendMessage("Your name is still $displayName")
        else
            sender.sendMessage("Your name is now $displayName")
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (sender !is Player)
            return emptyList()
        val config = plugin.cpnConfig
        val permColors = config.colors.filter {
            sender.hasPermission("coloredplayernames.changecolor.specify.${it.officialName}")
        } - plugin.playerColors[sender]
        val forceFiltered = if (sender.hasPermission("coloredplayernames.changecolor.force"))
            permColors
        else
            permColors.intersect(plugin.playerColors.availableColors(sender))

        val colorNames = forceFiltered.mapNotNull { config.colorNames[it] }
        val completion = Completion(colorNames)
        val completionStrings = completionStrings(completion, args)
        if (completionStrings.isNotEmpty())
            return completionStrings

        val aliases = permColors.mapNotNull { config.aliases[it] }.flatten()
        val aliasCompletion = Completion(aliases)
        return completionStrings(aliasCompletion, args)
    }

    private fun isPermittedChange(player: Player, color: ChatColor, canSpecify: Boolean, label: String): Boolean {
        val config = plugin.cpnConfig
        val playerColors = plugin.playerColors
        if (playerColors[player] == color)
            return true
        val colorName = config.colorNames[color]
        val officialName = color.officialName
        if (!player.hasPermission("coloredplayernames.changecolor.specify.$officialName")) {
            if (canSpecify) {
                val error = "${RED}You do not have permission to set yourself to $colorName"
                val usage = "Usage: /$label [color]"
                player.sendMessage(arrayOf(error, usage))
            } else {
                val error = "${RED}You do not have permission to specify your color"
                val usage = "Usage: /$label"
                player.sendMessage(arrayOf(error, usage))
            }
            return false
        }

        if (player.hasPermission("coloredplayernames.changecolor.force"))
            return true
        if (playerColors.availableColors(player).contains(color))
            return true
        val count = playerColors.count(color)
        assert(count > 0)
        val message = if (count == 1)
            "$colorName is unavailable because it is currently in use by another player"
        else
            "$colorName is unavailable because it is currently in use by $count other players"
        player.sendMessage(message)
        return false
    }

    companion object {
        tailrec fun completionStrings(completion: Completion, args: Array<String>, fromIndex: Int = 0): List<String> = if (fromIndex == args.lastIndex) {
            val prefix = args.last()
            completion.strings.filter { matches(it, prefix) }
        } else {
            val firstArg = args[fromIndex].toLowerCase(Locale.ROOT)
            val subcompletion = completion.subcompletions[firstArg]
            if (subcompletion == null)
                emptyList()
            else
                completionStrings(subcompletion, args, fromIndex + 1)
        }

        private fun matches(completion: String, typed: String): Boolean {
            val condensedCompletion = condenseString(completion)
            val condensedTyped = condenseString(typed)
            return condensedCompletion.startsWith(condensedTyped) && condensedCompletion != condensedTyped
        }

        fun usage(canSpecify: Boolean, label: String) = if (canSpecify)
            "Usage: /$label [color]"
        else
            "Usage: /$label"
    }

}
