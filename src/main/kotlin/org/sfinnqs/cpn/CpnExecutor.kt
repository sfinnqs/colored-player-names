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

import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class CpnExecutor(private val plugin: ColoredPlayerNames) : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String?>): Boolean {
        val firstArg = args.getOrNull(0)
        if (firstArg == null) {
            val error = "${RED}At least one argument is required"
            val usage = "Usage: /$label <reload | set | unset>"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        if (firstArg.equals("reload", true)) {
            plugin.reload()
            sender.sendMessage("Configuration reloaded")
            return true
        }
        val set = when {
            firstArg.equals("set", true) -> true
            firstArg.equals("unset", true) -> false
            else -> {
                val error = "${RED}Unrecognized argument: $firstArg"
                val usage = "Usage: /$label <reload | set | unset>"
                sender.sendMessage(arrayOf(error, usage))
                return true
            }
        }
        val usage = if (set)
            "Usage: /$label $firstArg <player> <color>"
        else
            "Usage: /$label $firstArg <player>"
        val playerArg = args.getOrNull(1)
        if (playerArg == null) {
            val error = "${RED}A player must be specified"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val uuid = PlayerSection.getUuid(playerArg)
        if (uuid == null) {
            val error = "${RED}Unrecognized player name: $playerArg"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val config = plugin.cpnConfig
        if (set) {
            if (args.size < 3) {
                val error = "${RED}A color must be specified"
                sender.sendMessage(arrayOf(error, usage))
                return true
            }
            val colorArg = args.asList().subList(2, args.size).joinToString(" ")
            val color = config.colors[colorArg]
            if (color == null) {
                val error = "${RED}Unrecognized color: $colorArg"
                sender.sendMessage(arrayOf(error, usage))
                return true
            }
            val player = plugin.server.getPlayer(uuid)
            if (player != null) {
                plugin.playerColors[player] = color
            }
            val playerSection = PlayerSection(playerArg, uuid, color)
            val updated = config.updateWithPlayerSection(playerSection)
            if (updated) {
                plugin.logger.info {
                    "Updating config because of a \"/$label $firstArg\" command run by ${sender.name}"
                }
                config.writeToFile()
                sender.sendMessage("$playerArg set permanently to $colorArg")
            } else {
                sender.sendMessage("$playerArg is already set to $colorArg")
            }
        } else {
            val updated = config.unsetStaticColor(uuid)
            if (updated) {
                plugin.logger.info {
                    "Updating config because of a \"/$label $firstArg\" command run by ${sender.name}"
                }
                config.writeToFile()
                sender.sendMessage("$playerArg is no longer set to a single color")
            } else {
                sender.sendMessage("$playerArg is already unset")
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        assert(args.isNotEmpty())
        return when (args.size) {
            1 -> {
                val arg = args[0]
                if (arg.isEmpty()) {
                    return listOf("reload", "set", "unset")
                }
                when {
                    matches("reload", arg) -> listOf("reload")
                    matches("set", arg) -> listOf("set")
                    matches("unset", arg) -> listOf("unset")
                    else -> emptyList()
                }
            }
            2 -> {
                if (!args[0].equals("set", true) && !args[0].equals("unset", true))
                    return emptyList()
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
