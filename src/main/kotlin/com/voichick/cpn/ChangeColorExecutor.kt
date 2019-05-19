package com.voichick.cpn

import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class ChangeColorExecutor(private val plugin: ColoredPlayerNames) : TabExecutor {

    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<String>
    ): Boolean {

        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to use this command")
            return true
        }

        val colors = plugin.playerColors
        val oldColor = colors[sender]
        val colorPicker = plugin.colorPicker

        val colorString = args.takeUnless { it.isEmpty() }?.joinToString(" ")
        val newColor = when {
            colorString == null -> colorPicker()
            sender.hasPermission("coloredplayernames.changecolor.specify") -> {
                val result = plugin.cpnConfig.colors[colorString]
                if (result == null) {
                    sender.sendMessage("${RED}Unrecognized color: \"$colorString\"")
                    return false
                } else {
                    result
                }
            }
            else -> {
                val message = "${RED}You do not have permission to specify a color"
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

    override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<String>
    ): List<String>? {
        if (sender !is Player)
            return emptyList()
        @Suppress("SpellCheckingInspection")
        if (!sender.hasPermission("coloredplayernames.changecolor.specify"))
            return null
        val config = plugin.cpnConfig
        val completionStrings = completionStrings(config.completion, args)
        return if (completionStrings.isEmpty())
            completionStrings(config.aliasCompletion, args)
        else
            completionStrings
    }

    private tailrec fun completionStrings(
            completion: Completion,
            args: Array<String>,
            fromIndex: Int = 0
    ): List<String> = if (fromIndex == args.lastIndex) {
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
