package com.voichick.cpn

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
            val officialName = result.officialName
            if (sender.hasPermission("coloredplayernames.changecolor.$officialName")) {
                result
            } else {
                val message = "${RED}You do not have permission to set set yourself to $colorString"
                val messages = arrayOf(message, "/$label")
                sender.sendMessage(messages)
                return true
            }
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
            sender.hasPermission("coloredplayernames.changecolor.${it.officialName}")
        }
        val permColorNames = permColors.mapNotNull { config.colorNames[it] }
        val completion = Completion(permColorNames)
        val completionStrings = completionStrings(completion, args)
        if (completionStrings.isNotEmpty())
            return completionStrings

        val permColorAliases = permColors.mapNotNull { config.aliases[it] }.flatten()
        val aliasCompletion = Completion(permColorAliases)
        return completionStrings(aliasCompletion, args)
    }

    private tailrec fun completionStrings(completion: Completion, args: Array<String>, fromIndex: Int = 0): List<String> = if (fromIndex == args.lastIndex) {
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
