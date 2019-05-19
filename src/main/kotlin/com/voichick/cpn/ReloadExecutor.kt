package com.voichick.cpn

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class ReloadExecutor(private val plugin: ColoredPlayerNames) : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>) = if (args.getOrNull(0) == "reload") {
        plugin.reload()
        sender.sendMessage("Configuration reloaded")
        true
    } else {
        false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>) = if (args.size == 1)
        listOf("reload")
    else
        emptyList()
}
