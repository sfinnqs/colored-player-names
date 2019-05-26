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

        val colors = plugin.playerColors
        val oldColor = colors[sender]
        val pickColor = plugin.pickColor

        val colorString = args.takeUnless { it.isEmpty() }?.joinToString(" ")
        val newColor = if (colorString == null) {
            pickColor()
        } else {
            val result = plugin.cpnConfig.colors[colorString]
            if (result == null) {
                sender.sendMessage("${RED}Unrecognized color: \"$colorString\"")
                return false
            }
            if (result != oldColor && !isPermittedChange(sender, result)) {
                return true
            }
            result
        }

        colors[sender] = newColor
        val displayName = sender.displayName
        if (oldColor == newColor)
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
            permColors.intersect(plugin.pickColor.availableColors())

        val colorNames = forceFiltered.mapNotNull { config.colorNames[it] }
        val completion = Completion(colorNames)
        val completionStrings = completionStrings(completion, args)
        if (completionStrings.isNotEmpty())
            return completionStrings

        val aliases = permColors.mapNotNull { config.aliases[it] }.flatten()
        val aliasCompletion = Completion(aliases)
        return completionStrings(aliasCompletion, args)
    }

    private fun isPermittedChange(player: Player, color: ChatColor): Boolean {
        val colorName = plugin.cpnConfig.colorNames[color]
        val officialName = color.officialName
        if (!player.hasPermission("coloredplayernames.changecolor.specify.$officialName")) {
            val message = "${RED}You do not have permission to set yourself to $colorName"
            player.sendMessage(message)
            return false
        }

        if (player.hasPermission("coloredplayernames.changecolor.force"))
            return true
        if (plugin.pickColor.availableColors().contains(color))
            return true
        val count = plugin.playerColors.count(color)
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
    }

}
