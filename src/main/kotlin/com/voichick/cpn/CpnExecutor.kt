package com.voichick.cpn

import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class CpnExecutor(private val plugin: ColoredPlayerNames) : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String?>): Boolean {
        val firstArg = args.getOrNull(0)
        if (firstArg == null) {
            val error = "${RED}The /$label command requires at least one argument"
            val usage = "Usage: /$label <reload|set>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        if (firstArg.equals("reload", true)) {
            plugin.reload()
            sender.sendMessage("Configuration reloaded")
            return true
        }
        if (!firstArg.equals("set", true)) {
            val error = "${RED}Unrecognized argument: $firstArg"
            val usage = "Usage: /$label <reload|set>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val playerArg = args.getOrNull(1)
        if (playerArg == null) {
            val error = "${RED}A player must be specified"
            val usage = "Usage: /$label set <player> <color>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        if (args.size < 3) {
            val error = "${RED}A color must be specified"
            val usage = "Usage: /$label set <player> <color>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val colorArg = args.asList().subList(2, args.size).joinToString(" ")
        val uuid = PlayerSection.getUuid(playerArg)
        if (uuid == null) {
            val error = "${RED}Unrecognized player name: $playerArg"
            val usage = "Usage: /$label set <player> <color>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val config = plugin.cpnConfig
        val color = config.colors[colorArg]
        if (color == null) {
            val error = "${RED}Unrecognized color: $colorArg"
            val usage = "Usage: /$label set <player> <color>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val playerSection = PlayerSection(playerArg, uuid, color)
        val updated = config.updateWithPlayerSection(playerSection)
        if (updated) {
            plugin.logger.info(
                    "Updating config because of a \"/$label set\" command run by ${sender.name}"
            )
            config.writeToFile()
            val player = plugin.server.getPlayer(uuid)
            if (player != null) {
                plugin.playerColors[player] = color
            }
            sender.sendMessage("$playerArg set permanently to $colorArg")
        } else {
            sender.sendMessage("$playerArg is already set to $colorArg")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        assert (args.isNotEmpty())
        return when (args.size) {
            1 -> {
                val arg = args[0]
                if (arg.isEmpty()) {
                    return listOf("reload", "set")
                }
                when {
                    matches("reload", arg) -> listOf("reload")
                    matches("set", arg) -> listOf("set")
                    else -> emptyList()
                }
            }
            2 -> {
                if (!args[0].equals("set", true)) return emptyList()
                val names = plugin.server.offlinePlayers.mapNotNull { it.name }
                val arg = args[1]
                names.filter { matches(it, arg) }
            }
            else -> {
                if (!args[0].equals("set", true)) return emptyList()
                val config = plugin.cpnConfig
                val colorNames = config.colorNames.values.toList()
                val completion = Completion(colorNames)
                val strings = ChangeColorExecutor.completionStrings(completion, args, 2)
                if (strings.isNotEmpty())
                    return strings

                val aliases = config.aliases.values.flatten()
                val aliasCompletion = Completion(aliases)
                ChangeColorExecutor.completionStrings(aliasCompletion, args, 2)
            }
        }
    }

    private fun matches(completion: String, typed: String) = completion.startsWith(typed, true) && !completion.equals(typed, true)
}
